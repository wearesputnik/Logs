package mobi.kolibri.messager.activity;

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
import android.widget.TextView;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.MessagInfo;
import mobi.kolibri.messager.object.SQLMessager;

public class GroupChatItemActivity extends AppCompatActivity {
    Integer id_chat;
    SQLMessager sqlMessager;
    SQLiteDatabase db;
    ListView listMeseges;
    String type_chat;
    EditText textMessages;
    Button sendMessages;
    String json_users;
    String chat_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_item);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            id_chat = b.getInt("chat_id");
            type_chat = b.getString("type");
        }

        Log.e("CHAT_ID", id_chat.toString());

        textMessages = (EditText) findViewById(R.id.edtChatMessages);
        sendMessages = (Button) findViewById(R.id.btnChatSend);

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

        sendMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!textMessages.getText().toString().trim().equals("")) {
                    new setGroupMessagerTask().execute();
                }
            }
        });
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
            MessagInfo result_sql = new MessagInfo();
            /*result_sql.id_from = user_id_from.toString();
            result_sql.id_to = user_id_from.toString();
            result_sql.message = textMessages.getText().toString();
            adapter.add(result_sql);*/
            textMessages.setText("");
            //adapter.notifyDataSetChanged();
            //scrollDown();

            super.onPostExecute(result);

        }
    }

    private void scrollDown() {
        listMeseges.setSelection(listMeseges.getCount() - 1);
    }
}
