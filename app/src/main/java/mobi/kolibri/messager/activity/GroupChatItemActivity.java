package mobi.kolibri.messager.activity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.adapters.GroupMessagerAdapter;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.GroupMessagerInfo;
import mobi.kolibri.messager.object.SQLMessager;

public class GroupChatItemActivity extends AppCompatActivity {
    Integer id_chat;
    SQLMessager sqlMessager;
    SQLiteDatabase db;
    ListView listMeseges;
    String type_chat;
    EditText textMessages;
    ImageButton sendMessages;
    String json_users;
    String chat_name;
    GroupMessagerAdapter adapter;
    Updater u;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_item);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.back_from_chats);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            id_chat = b.getInt("chat_id");
            type_chat = b.getString("type");
        }

        Log.e("CHAT_ID", id_chat.toString());

        textMessages = (EditText) findViewById(R.id.edtChatMessages);
        sendMessages = (ImageButton) findViewById(R.id.btnChatSend);
        listMeseges = (ListView) findViewById(R.id.listChatMessages);
        adapter = new GroupMessagerAdapter(GroupChatItemActivity.this, HttpConnectRecive.getUserId(GroupChatItemActivity.this));
        listMeseges.setAdapter(adapter);

        sqlMessager = new SQLMessager(GroupChatItemActivity.this);

        db = sqlMessager.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CHAT + " WHERE id=" + id_chat, null);
        if (c.moveToFirst()) {
            int nameCollumn = c.getColumnIndex(SQLMessager.CHAT_NAME);
            int usersCollumn = c.getColumnIndex(SQLMessager.CHAT_JSON_INTERLOCUTOR);
            chat_name = c.getString(nameCollumn);
            getSupportActionBar().setTitle(chat_name);
            json_users = c.getString(usersCollumn);
        }

        Cursor c_ch = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_MESSAGER + " WHERE " + SQLMessager.MESSAGER_CHAT_ID + "='" + id_chat + "'", null);
        if (c_ch.moveToFirst()) {
            int idFromCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_FROM_ID);
            int idToCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_TO_ID);
            int idMessageCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_MESSAG);
            do {
                GroupMessagerInfo result_sql = new GroupMessagerInfo();
                result_sql.id_from = c_ch.getString(idFromCollumn);
                result_sql.id_to = c_ch.getString(idToCollumn);
                result_sql.message = c_ch.getString(idMessageCollumn);
                adapter.add(result_sql);
            } while (c_ch.moveToNext());

        }
        adapter.notifyDataSetChanged();
        scrollDown();

        sendMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!textMessages.getText().toString().trim().equals("")) {
                    ContentValues cv_ms = new ContentValues();
                    cv_ms.put(SQLMessager.MESSAGER_CHAT_ID, id_chat.toString());
                    cv_ms.put(SQLMessager.MESSAGER_FROM_ID, json_users);
                    cv_ms.put(SQLMessager.MESSAGER_TO_ID, HttpConnectRecive.getUserId(GroupChatItemActivity.this));
                    cv_ms.put(SQLMessager.MESSAGER_MESSAG, textMessages.getText().toString());
                    cv_ms.put(SQLMessager.MESSAGER_SERVER, "1");
                    db.insert(SQLMessager.TABLE_MESSAGER, null, cv_ms);
                    new setGroupMessagerTask().execute();
                }
            }
        });

        u = new Updater();

        u.start();
    }

    private class Updater extends Thread {
        public boolean stopped = false;

        public void run() {
            try {
                while (!stopped) {
                    // Активность списка
                    runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    new getGroupMessegTask().execute();
                                }
                            }
                    );
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        finish();

        return super.onOptionsItemSelected(item);
    }

    class setGroupMessagerTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected String doInBackground(String... params) {
            String result = HttpConnectRecive.setGroupMessage(GroupChatItemActivity.this, json_users, textMessages.getText().toString(), type_chat, chat_name);
            return result;
        }

        protected void onPostExecute(String result) {
            GroupMessagerInfo result_sql = new GroupMessagerInfo();
            result_sql.id_from = json_users;
            result_sql.id_to = HttpConnectRecive.getUserId(GroupChatItemActivity.this);
            result_sql.message = textMessages.getText().toString();
            adapter.add(result_sql);
            textMessages.setText("");
            adapter.notifyDataSetChanged();
            scrollDown();

            super.onPostExecute(result);

        }
    }

    class getGroupMessegTask extends AsyncTask<String, String, List<GroupMessagerInfo>> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected List<GroupMessagerInfo> doInBackground(String... params) {
            List<GroupMessagerInfo> result = HttpConnectRecive.postGroupMessager(GroupChatItemActivity.this, json_users);
            return result;
        }

        protected void onPostExecute(List<GroupMessagerInfo> result) {
            if (result != null) {
                for (GroupMessagerInfo item : result) {
                    ContentValues cv_ms = new ContentValues();
                    cv_ms.put(SQLMessager.MESSAGER_CHAT_ID, id_chat);
                    cv_ms.put(SQLMessager.MESSAGER_FROM_ID, HttpConnectRecive.getUserId(GroupChatItemActivity.this));
                    cv_ms.put(SQLMessager.MESSAGER_TO_ID, item.id_to);
                    cv_ms.put(SQLMessager.MESSAGER_MESSAG, item.message);
                    cv_ms.put(SQLMessager.MESSAGER_SERVER, "0");
                    db.insert(SQLMessager.TABLE_MESSAGER, null, cv_ms);
                    GroupMessagerInfo result_sql = new GroupMessagerInfo();
                    result_sql.id_from = item.id_from;
                    result_sql.id_to = item.id_to;
                    result_sql.message = item.message;
                    adapter.add(result_sql);

                }
                adapter.notifyDataSetChanged();
                scrollDown();

            }
            super.onPostExecute(result);

        }
    }

    private void scrollDown() {
        listMeseges.setSelection(listMeseges.getCount() - 1);
    }
}
