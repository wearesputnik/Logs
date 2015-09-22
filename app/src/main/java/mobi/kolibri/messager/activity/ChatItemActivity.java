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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.adapters.MessagerAdapter;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.MessagInfo;
import mobi.kolibri.messager.object.SQLMessager;

public class ChatItemActivity extends AppCompatActivity {
    Integer user_id_from;
    Integer chat_id;
    SQLMessager sqlMessager;
    ListView listMeseges;
    EditText textMessages;
    Button sendMessages;
    SQLiteDatabase db;
    MessagerAdapter adapter;
    String type_chat;
    Updater u;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_item);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            user_id_from = b.getInt("user_id_from");
            chat_id = b.getInt("chat_id");
            type_chat = b.getString("type");
        }



        new getMessegTask().execute();

        listMeseges = (ListView) findViewById(R.id.listChatMessages);
        listMeseges.setDividerHeight(0);
        adapter = new MessagerAdapter(ChatItemActivity.this, HttpConnectRecive.getUserId(ChatItemActivity.this));
        listMeseges.setAdapter(adapter);
        textMessages = (EditText) findViewById(R.id.edtChatMessages);
        sendMessages = (Button) findViewById(R.id.btnChatSend);

        sqlMessager = new SQLMessager(ChatItemActivity.this);

        db = sqlMessager.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE " + SQLMessager.CONTACTS_USER_ID + "='" + user_id_from + "'", null);
        if (c.moveToFirst()) {
            int nameCollumn = c.getColumnIndex(SQLMessager.CONTACTS_NAME);
            getSupportActionBar().setTitle(c.getString(nameCollumn));
        }

        Cursor c_ch = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_MESSAGER + " WHERE " + SQLMessager.MESSAGER_CHAT_ID + "='" + chat_id + "'", null);
        if (c_ch.moveToFirst()) {
            int idFromCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_FROM_ID);
            int idToCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_TO_ID);
            int idMessageCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_MESSAG);
            do {
                MessagInfo result_sql = new MessagInfo();
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
                    if (chat_id != 0) {
                        ContentValues cv_ms = new ContentValues();
                        cv_ms.put(SQLMessager.MESSAGER_CHAT_ID, chat_id.toString());
                        cv_ms.put(SQLMessager.MESSAGER_FROM_ID, user_id_from.toString());
                        cv_ms.put(SQLMessager.MESSAGER_TO_ID, HttpConnectRecive.getUserId(ChatItemActivity.this));
                        cv_ms.put(SQLMessager.MESSAGER_MESSAG, textMessages.getText().toString());
                        cv_ms.put(SQLMessager.MESSAGER_SERVER, "1");
                        db.insert(SQLMessager.TABLE_MESSAGER, null, cv_ms);
                    } else {
                        long chat_id_db = 0;
                        JSONArray arrayUser = new JSONArray();
                        JSONObject itemJs = new JSONObject();
                        try {
                            itemJs.put("user_id", user_id_from.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        arrayUser.put(itemJs);
                        ContentValues cv_ch = new ContentValues();
                        cv_ch.put(SQLMessager.CHAT_JSON_INTERLOCUTOR, arrayUser.toString());
                        cv_ch.put(SQLMessager.CHAT_TYPE, type_chat.toString());
                        cv_ch.put(SQLMessager.CHAT_READ, "1");
                        chat_id_db = db.insert(SQLMessager.TABLE_CHAT, null, cv_ch);
                        if (chat_id_db > 0) {
                            chat_id = (int) chat_id_db;
                            ContentValues cv_ms = new ContentValues();
                            cv_ms.put(SQLMessager.MESSAGER_CHAT_ID, chat_id_db);
                            cv_ms.put(SQLMessager.MESSAGER_FROM_ID, user_id_from.toString());
                            cv_ms.put(SQLMessager.MESSAGER_TO_ID, HttpConnectRecive.getUserId(ChatItemActivity.this));
                            cv_ms.put(SQLMessager.MESSAGER_MESSAG, textMessages.getText().toString());
                            cv_ms.put(SQLMessager.MESSAGER_SERVER, "1");
                            db.insert(SQLMessager.TABLE_MESSAGER, null, cv_ms);
                        }
                    }
                    new setMessagerTask().execute();
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
                                    new getMessegTask().execute();
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
        u.stopped = true;
        if (type_chat.trim().equals("secret")) {
            db = sqlMessager.getWritableDatabase();
            db.delete(SQLMessager.TABLE_CHAT, "id=?", new String[]{chat_id.toString()});
            db.delete(SQLMessager.TABLE_MESSAGER, SQLMessager.MESSAGER_CHAT_ID + "=?", new String[]{chat_id.toString()});
            Log.e("TYPE CHAT ITEM", "delete");
        }
        onBackPressed();
        finish();
        return super.onOptionsItemSelected(item);
    }

    class setMessagerTask extends AsyncTask<String, String, MessagInfo> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected MessagInfo doInBackground(String... params) {
            MessagInfo result = HttpConnectRecive.setMessage(ChatItemActivity.this, user_id_from, textMessages.getText().toString(), type_chat);
            return result;
        }

        protected void onPostExecute(MessagInfo result) {
            MessagInfo result_sql = new MessagInfo();
            result_sql.id_from = user_id_from.toString();
            result_sql.id_to = HttpConnectRecive.getUserId(ChatItemActivity.this);
            result_sql.message = textMessages.getText().toString();
            adapter.add(result_sql);
            textMessages.setText("");
            adapter.notifyDataSetChanged();
            scrollDown();

            super.onPostExecute(result);

        }
    }

    class getMessegTask extends AsyncTask<String, String, List<MessagInfo>> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected List<MessagInfo> doInBackground(String... params) {
            List<MessagInfo> result = HttpConnectRecive.getMessage(ChatItemActivity.this, user_id_from.toString());
            return result;
        }

        protected void onPostExecute(List<MessagInfo> result) {
            if (result != null) {
                for (MessagInfo item : result) {
                    ContentValues cv_ms = new ContentValues();
                    cv_ms.put(SQLMessager.MESSAGER_CHAT_ID, chat_id);
                    cv_ms.put(SQLMessager.MESSAGER_FROM_ID, HttpConnectRecive.getUserId(ChatItemActivity.this));
                    cv_ms.put(SQLMessager.MESSAGER_TO_ID, item.id_to);
                    cv_ms.put(SQLMessager.MESSAGER_MESSAG, item.message);
                    cv_ms.put(SQLMessager.MESSAGER_SERVER, "0");
                    db.insert(SQLMessager.TABLE_MESSAGER, null, cv_ms);
                    MessagInfo result_sql = new MessagInfo();
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
