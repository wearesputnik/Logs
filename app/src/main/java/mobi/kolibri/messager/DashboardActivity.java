package mobi.kolibri.messager;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.yanzm.mth.MaterialTabHost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mobi.kolibri.messager.activity.ProfileActivity;
import mobi.kolibri.messager.fragment.ChatFragment;
import mobi.kolibri.messager.fragment.CirclesFragment;
import mobi.kolibri.messager.helper.ParseUtils;
import mobi.kolibri.messager.helper.PrefManager;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.ContactInfo;
import mobi.kolibri.messager.object.GroupMessagerInfo;
import mobi.kolibri.messager.object.MessagInfo;
import mobi.kolibri.messager.object.SQLMessager;


public class DashboardActivity extends AppCompatActivity {
    Integer user_id;
    SQLMessager sqlMessager;
    List<ContactInfo> listContacts;
    SQLiteDatabase db;
    private PrefManager pref;
    private CirclesFragment mCirclesFragment = null;
    private ChatFragment mChatFragment = null;

    private TextView btnCircles;
    private TextView btnChats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        initButtons();

        if(mCirclesFragment == null){
            mCirclesFragment = new CirclesFragment();
            btnCircles.setBackgroundResource(R.drawable.activ_bag_tab);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, mCirclesFragment)
                .commit();


        listContacts = new ArrayList<>();
        sqlMessager = new SQLMessager(DashboardActivity.this);
        db = sqlMessager.getWritableDatabase();


        Bundle b = getIntent().getExtras();
        if (b != null) {
            user_id = b.getInt("user_id");
        }

       /// ParseUtils.registerParse(DashboardActivity.this, "users_" + user_id);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }

        pref = new PrefManager(getApplicationContext());

        Intent intent = getIntent();

        String email = intent.getStringExtra("email");

        if (email != null) {
            ParseUtils.subscribeWithEmail(pref.getEmail());
        }

        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS, null);
        if (!c.moveToFirst()) {
            Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
            startManagingCursor(cursor);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    ContentValues cv = new ContentValues();
                    cv.put(SQLMessager.CONTACTS_NAME, cursor.getString(0));
                    cv.put(SQLMessager.CONTACTS_PHONE, cursor.getString(1));
                    cv.put(SQLMessager.CONTACTS_SERV, "0");
                    db.insert(SQLMessager.TABLE_CONTACTS, null, cv);

                }
            }
        }

        new NewAppKeyTask().execute();

    }

    private void initButtons() {
        btnCircles = (TextView) findViewById(R.id.btnCircles);
        btnChats = (TextView) findViewById(R.id.btnChats);

        btnCircles.setOnClickListener(btnCirclesListener);
        btnChats.setOnClickListener(btnChatsListener);
    }

    private final View.OnClickListener btnCirclesListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            btnCircles.setBackgroundResource(R.drawable.activ_bag_tab);
            btnChats.setBackgroundResource(R.drawable.pasiv_bac_tab);
            FragmentManager fManager = getSupportFragmentManager();
            if(!(fManager.findFragmentById(R.id.content_frame) instanceof CirclesFragment)){
                if(mCirclesFragment == null){
                    mCirclesFragment = new CirclesFragment();
                }
                fManager.beginTransaction()
                        .replace(R.id.content_frame, mCirclesFragment)
                        .commit();
            }
        }
    };

    private final View.OnClickListener btnChatsListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            btnCircles.setBackgroundResource(R.drawable.pasiv_bac_tab);
            btnChats.setBackgroundResource(R.drawable.activ_bag_tab);
            FragmentManager fManager = getSupportFragmentManager();
            if(!(fManager.findFragmentById(R.id.content_frame) instanceof ChatFragment)){
                if(mChatFragment == null){
                    mChatFragment = new ChatFragment();
                }
                fManager.beginTransaction()
                        .replace(R.id.content_frame, mChatFragment)
                        .commit();
            }
        }
    };

    class NewAppKeyTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected String doInBackground(String... params) {
            String result = null;
            if (HttpConnectRecive.isOnline(DashboardActivity.this)) {
                result = HttpConnectRecive.newAppKey(DashboardActivity.this, user_id);
            }
            return result;
        }

        protected void onPostExecute(String result) {
            SQLiteDatabase db = sqlMessager.getWritableDatabase();
            Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS, null);
            if (c.moveToFirst()) {
                int idCollumn = c.getColumnIndex("id");
                int phoneCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHONE);
                do {
                    ContactInfo result_sql = new ContactInfo();
                    result_sql.id_db = c.getInt(idCollumn);
                    result_sql.phone = c.getString(phoneCollumn);
                    listContacts.add(result_sql);
                } while (c.moveToNext());
            }
            if (HttpConnectRecive.isOnline(DashboardActivity.this)) {
                new ContactServerTask().execute();
            }
            Updater u = new Updater();
            u.start();
            super.onPostExecute(result);

        }
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
                                    if (HttpConnectRecive.isOnline(DashboardActivity.this)) {
                                        new getMessegAllTask().execute();
                                        new getGroupMessegAllTask().execute();
                                    }
                                }
                            }
                    );
                    try {
                        Thread.sleep(10000);
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    class ContactServerTask extends AsyncTask<String, String, ContactInfo> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected ContactInfo doInBackground(String... params) {
            ContactInfo result = HttpConnectRecive.ContactServer(DashboardActivity.this, ContactInfo.stringJson(listContacts));
            return result;
        }

        protected void onPostExecute(ContactInfo result) {


            super.onPostExecute(result);

        }
    }

    class getMessegAllTask extends AsyncTask<String, String, List<MessagInfo>> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected List<MessagInfo> doInBackground(String... params) {
            List<MessagInfo> result = HttpConnectRecive.getMessage(DashboardActivity.this, "0");
            return result;
        }

        protected void onPostExecute(List<MessagInfo> result) {
            if (result != null) {
                for (MessagInfo item : result) {
                    JSONArray arrayUser = new JSONArray();
                    JSONObject itemJs = new JSONObject();
                    try {
                        itemJs.put("user_id", item.id_to.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    arrayUser.put(itemJs);
                    Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CHAT + " WHERE " + SQLMessager.CHAT_JSON_INTERLOCUTOR + "='" + arrayUser.toString() + "' and " + SQLMessager.CHAT_TYPE + "='" + item.type_chat + "'", null);
                    if (c.moveToFirst()) {
                        int idCollumn = c.getColumnIndex("id");
                        ContentValues cv_ms = new ContentValues();
                        cv_ms.put(SQLMessager.MESSAGER_CHAT_ID, c.getString(idCollumn));
                        cv_ms.put(SQLMessager.MESSAGER_FROM_ID, user_id);
                        cv_ms.put(SQLMessager.MESSAGER_TO_ID, item.id_to);
                        cv_ms.put(SQLMessager.MESSAGER_MESSAG, item.message);
                        cv_ms.put(SQLMessager.MESSAGER_SERVER, "0");
                        cv_ms.put(SQLMessager.MESSAGER_ATTACHMENT, item.attachment);
                        cv_ms.put(SQLMessager.MESSAGER_DURATION, item.duration);
                        cv_ms.put(SQLMessager.MESSAGER_CREATED, item.created);
                        db.insert(SQLMessager.TABLE_MESSAGER, null, cv_ms);
                    }
                    else {
                        long chat_id_db = 0;
                        ContentValues cv_ch = new ContentValues();
                        cv_ch.put(SQLMessager.CHAT_JSON_INTERLOCUTOR, arrayUser.toString());
                        cv_ch.put(SQLMessager.CHAT_TYPE, item.type_chat);
                        cv_ch.put(SQLMessager.CHAT_NAME, item.name_to);
                        chat_id_db = db.insert(SQLMessager.TABLE_CHAT, null, cv_ch);
                        if (chat_id_db > 0) {
                            ContentValues cv_ms = new ContentValues();
                            cv_ms.put(SQLMessager.MESSAGER_CHAT_ID, chat_id_db);
                            cv_ms.put(SQLMessager.MESSAGER_FROM_ID, user_id);
                            cv_ms.put(SQLMessager.MESSAGER_TO_ID, item.id_to);
                            cv_ms.put(SQLMessager.MESSAGER_MESSAG, item.message);
                            cv_ms.put(SQLMessager.MESSAGER_SERVER, "0");
                            cv_ms.put(SQLMessager.MESSAGER_ATTACHMENT, item.attachment);
                            cv_ms.put(SQLMessager.MESSAGER_DURATION, item.duration);
                            cv_ms.put(SQLMessager.MESSAGER_CREATED, item.created);
                            db.insert(SQLMessager.TABLE_MESSAGER, null, cv_ms);
                        }
                    }
                }
            }
            super.onPostExecute(result);

        }
    }

    class getGroupMessegAllTask extends AsyncTask<String, String, List<GroupMessagerInfo>> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected List<GroupMessagerInfo> doInBackground(String... params) {
            List<GroupMessagerInfo> result = HttpConnectRecive.getGroupMessager(DashboardActivity.this);
            return result;
        }

        protected void onPostExecute(List<GroupMessagerInfo> result) {
            if (result != null) {
                for (GroupMessagerInfo item : result) {

                    Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CHAT + " WHERE " + SQLMessager.CHAT_JSON_INTERLOCUTOR + "='" + item.json_users + "' and " + SQLMessager.CHAT_TYPE + "='" + item.type_chat + "'", null);
                    if (c.moveToFirst()) {
                        int idCollumn = c.getColumnIndex("id");
                        ContentValues cv_ms = new ContentValues();
                        cv_ms.put(SQLMessager.MESSAGER_CHAT_ID, c.getString(idCollumn));
                        cv_ms.put(SQLMessager.MESSAGER_FROM_ID, user_id);
                        cv_ms.put(SQLMessager.MESSAGER_TO_ID, item.id_to);
                        cv_ms.put(SQLMessager.MESSAGER_MESSAG, item.message);
                        cv_ms.put(SQLMessager.MESSAGER_SERVER, "0");
                        cv_ms.put(SQLMessager.MESSAGER_ATTACHMENT, item.attachment);
                        cv_ms.put(SQLMessager.MESSAGER_DURATION, item.duration);
                        cv_ms.put(SQLMessager.MESSAGER_CREATED, item.created);
                        db.insert(SQLMessager.TABLE_MESSAGER, null, cv_ms);
                    }
                    else {
                        long chat_id_db = 0;
                        ContentValues cv_ch = new ContentValues();
                        cv_ch.put(SQLMessager.CHAT_JSON_INTERLOCUTOR, item.json_users);
                        cv_ch.put(SQLMessager.CHAT_NAME, item.chat_name);
                        cv_ch.put(SQLMessager.CHAT_TYPE, item.type_chat);
                        chat_id_db = db.insert(SQLMessager.TABLE_CHAT, null, cv_ch);
                        if (chat_id_db > 0) {
                            ContentValues cv_ms = new ContentValues();
                            cv_ms.put(SQLMessager.MESSAGER_CHAT_ID, chat_id_db);
                            cv_ms.put(SQLMessager.MESSAGER_FROM_ID, user_id);
                            cv_ms.put(SQLMessager.MESSAGER_TO_ID, item.id_to);
                            cv_ms.put(SQLMessager.MESSAGER_MESSAG, item.message);
                            cv_ms.put(SQLMessager.MESSAGER_SERVER, "0");
                            cv_ms.put(SQLMessager.MESSAGER_ATTACHMENT, item.attachment);
                            cv_ms.put(SQLMessager.MESSAGER_DURATION, item.duration);
                            cv_ms.put(SQLMessager.MESSAGER_CREATED, item.created);
                            db.insert(SQLMessager.TABLE_MESSAGER, null, cv_ms);
                        }
                    }
                }
            }
            super.onPostExecute(result);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                Intent i = new Intent(DashboardActivity.this, ProfileActivity.class);
                i.putExtra("user_id", user_id);
                startActivity(i);
                return true;
            case R.id.action_out:
                LogOut();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void LogOut() {
        db.delete(SQLMessager.TABLE_CHAT, null, null);
        db.delete(SQLMessager.TABLE_MESSAGER, null, null);
        db.delete(SQLMessager.TABLE_APP_ID, null, null);
        db.delete(SQLMessager.TABLE_CIRCLES_CONTACT, null, null);
        db.delete(SQLMessager.TABLE_CIRCLES, null, null);
        db.delete(SQLMessager.TABLE_CONTACTS, null, null);
        pref.logout();

        Intent i = new Intent(DashboardActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}
