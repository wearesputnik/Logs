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

import net.yanzm.mth.MaterialTabHost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import mobi.kolibri.messager.object.MessagInfo;
import mobi.kolibri.messager.object.SQLMessager;


public class DashboardActivity extends AppCompatActivity {
    Integer user_id;
    SQLMessager sqlMessager;
    List<ContactInfo> listContacts;
    SQLiteDatabase db;
    private PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);


        listContacts = new ArrayList<>();
        sqlMessager = new SQLMessager(DashboardActivity.this);
        db = sqlMessager.getWritableDatabase();


        Bundle b = getIntent().getExtras();
        if (b != null) {
            user_id = b.getInt("user_id");
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }

        pref = new PrefManager(getApplicationContext());

        Intent intent = getIntent();

        String email = intent.getStringExtra("email");

        if (email != null) {
            ParseUtils.subscribeWithEmail(pref.getEmail());
        }

        MaterialTabHost tabHost = (MaterialTabHost) findViewById(android.R.id.tabhost);
        tabHost.setType(MaterialTabHost.Type.FullScreenWidth);

        SectionsPagerAdapter pagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            tabHost.addTab(pagerAdapter.getPageTitle(i));
        }

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(tabHost);

        tabHost.setOnTabChangeListener(new MaterialTabHost.OnTabChangeListener() {
            @Override
            public void onTabSelected(int position) {
                viewPager.setCurrentItem(position);
            }
        });

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

    class NewAppKeyTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected String doInBackground(String... params) {
            String result = HttpConnectRecive.newAppKey(DashboardActivity.this, user_id);
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
            new ContactServerTask().execute();
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
                                    new getMessegAllTask().execute();
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
                        db.insert(SQLMessager.TABLE_MESSAGER, null, cv_ms);
                    }
                    else {
                        long chat_id_db = 0;
                        ContentValues cv_ch = new ContentValues();
                        cv_ch.put(SQLMessager.CHAT_JSON_INTERLOCUTOR, arrayUser.toString());
                        cv_ch.put(SQLMessager.CHAT_TYPE, item.type_chat);
                        chat_id_db = db.insert(SQLMessager.TABLE_CHAT, null, cv_ch);
                        if (chat_id_db > 0) {
                            ContentValues cv_ms = new ContentValues();
                            cv_ms.put(SQLMessager.MESSAGER_CHAT_ID, chat_id_db);
                            cv_ms.put(SQLMessager.MESSAGER_FROM_ID, user_id);
                            cv_ms.put(SQLMessager.MESSAGER_TO_ID, item.id_to);
                            cv_ms.put(SQLMessager.MESSAGER_MESSAG, item.message);
                            cv_ms.put(SQLMessager.MESSAGER_SERVER, "0");
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

        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 1) {
                return new ChatFragment();
            }
            return new CirclesFragment();
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }
}
