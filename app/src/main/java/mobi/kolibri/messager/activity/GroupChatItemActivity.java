package mobi.kolibri.messager.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.object.SQLMessager;

public class GroupChatItemActivity extends AppCompatActivity {
    Integer id_chat;
    SQLMessager sqlMessager;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_item);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            id_chat = b.getInt("id_chat");
        }

        Log.e("CHAT_ID", id_chat.toString());

        sqlMessager = new SQLMessager(GroupChatItemActivity.this);

        db = sqlMessager.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CHAT + " WHERE id=" + id_chat, null);
        if (c.moveToFirst()) {
            int nameCollumn = c.getColumnIndex(SQLMessager.CHAT_NAME);
            getSupportActionBar().setTitle(c.getString(nameCollumn));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        finish();

        return super.onOptionsItemSelected(item);
    }
}
