package mobi.kolibri.messager;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.onesignal.OneSignal;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

import mobi.kolibri.messager.fragment.ChatFragment;
import mobi.kolibri.messager.fragment.CirclesFragment;
import mobi.kolibri.messager.fragment.PeopleFragment;
import mobi.kolibri.messager.fragment.SettingFragment;
import mobi.kolibri.messager.helper.PrefManager;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.ContactInfo;
import mobi.kolibri.messager.object.GroupMessagerInfo;
import mobi.kolibri.messager.object.MessagInfo;
import mobi.kolibri.messager.object.ProfileInfo;
import mobi.kolibri.messager.object.SQLMessager;
import mobi.kolibri.messager.helper.ExampleNotificationOpenedHandler;


public class DashboardActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{
    Integer user_id;
    SQLMessager sqlMessager;
    List<ContactInfo> listContacts;
    SQLiteDatabase db;
    ImageView imgProfile;
    Button btnProfile;
    TextView txtTitleActionBar;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    public PrefManager pref;
    private CirclesFragment mCirclesFragment = null;
    private DisplayImageOptions options;

    String[] PermisionLocation = {
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    };
    String[] PermisionStorage = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    Integer RequestContactId = 1;
    Integer RequestStorageId = 2;
    private View mLayout;

    private FragmentManager.OnBackStackChangedListener
            mOnBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            syncActionBarArrowState();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        mLayout = (View) findViewById(R.id.main_layout);

        sqlMessager = new SQLMessager(DashboardActivity.this);
        db = sqlMessager.getWritableDatabase();

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)// to hide dialog
                //.setNotificationOpenedHandler(new ExampleNotificationOpenedHandler())
                .init();

        options = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer(1000))
                .showImageOnLoading(R.mipmap.profile_max)
                .showImageForEmptyUri(R.mipmap.profile_max)
                .showImageOnFail(R.mipmap.profile_max)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        txtTitleActionBar = (TextView) findViewById(R.id.txtTitleActionBar);
        txtTitleActionBar.setText("Logs");

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_menu_white);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.root_screen_drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.app_name,
                R.string.app_name) {

            public void onDrawerClosed(View view) {
                syncActionBarArrowState();
            }

            public void onDrawerOpened(View drawerView) {
                mDrawerToggle.setDrawerIndicatorEnabled(true);
            }
        };

        if(mCirclesFragment == null){
            replaceAnimatedFragment(new ChatFragment());

        }

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);


        initButtonsPartHome();

        listContacts = new ArrayList<>();

        Bundle b = getIntent().getExtras();
        if (b != null) {
            user_id = b.getInt("user_id");
        }

        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                ContentValues cv_ms = new ContentValues();
                cv_ms.put(SQLMessager.PLAYER_ID, userId);
                db.update(SQLMessager.TABLE_APP_ID, cv_ms, "id=?", new String[] {"1"});
                Log.e("debug", "User:" + userId + " " + user_id);
                if (registrationId != null)
                    Log.e("debug", "registrationId:" + registrationId);
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(DashboardActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(DashboardActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

                requestContactsPermission();

            }
            else {
                loadContacts();
            }
            if (ActivityCompat.checkSelfPermission(DashboardActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(DashboardActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermission();
            }
        }
        else {
            Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS, null);
            loadContacts();
        }

    }

    private void loadContacts() {
        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS, null);
        if (!c.moveToFirst()) {
            Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
            startManagingCursor(cursor);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    if (isValidPhone(cursor.getString(1))) {
                        ContentValues cv = new ContentValues();
                        cv.put(SQLMessager.CONTACTS_NAME, cursor.getString(0));
                        cv.put(SQLMessager.CONTACTS_PHONE, cursor.getString(1));
                        cv.put(SQLMessager.CONTACTS_SERV, "0");
                        db.insert(SQLMessager.TABLE_CONTACTS, null, cv);
                    }
                }
            }
        }
        new NewAppKeyTask().execute();
    }

    private void requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            Snackbar.make(mLayout, R.string.permission_storage_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(DashboardActivity.this, PermisionStorage, RequestStorageId);
                        }
                    })
                    .show();

        } else {
            ActivityCompat.requestPermissions(this, PermisionStorage, RequestStorageId);
        }
    }

    private void requestContactsPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {

            Snackbar.make(mLayout, R.string.permission_contacts_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(DashboardActivity.this, PermisionLocation, RequestContactId);
                        }
                    })
                    .show();
            if (ActivityCompat.checkSelfPermission(DashboardActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(DashboardActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermission();
            }
        } else {
            ActivityCompat.requestPermissions(DashboardActivity.this, PermisionLocation, RequestContactId);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestContactId) {
            if (Utils.verifyPermissions(grantResults)) {
                Snackbar.make(mLayout, R.string.permision_available_contacts,
                        Snackbar.LENGTH_SHORT)
                        .show();
                loadContacts();
            }
            else {
                Snackbar.make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
        else if (requestCode == RequestStorageId) {
            if (Utils.verifyPermissions(grantResults)) {
                Snackbar.make(mLayout, R.string.permision_available_storage, Snackbar.LENGTH_SHORT).show();
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(mOnBackStackChangedListener);
        super.onDestroy();
    }

    private void replaceAnimatedFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    private void closeAllFragments() {
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }

    private void syncActionBarArrowState() {
        int backStackEntryCount =
                getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount == 0) {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        } else {
            mDrawerToggle.setDrawerIndicatorEnabled(false);
        }
    }

    private void initButtonsPartHome() {
        imgProfile = (ImageView) findViewById(R.id.imgProfile);
        btnProfile = (Button) findViewById(R.id.btnProfile);
        LinearLayout layProfile = (LinearLayout) findViewById(R.id.layProfile);
        final Button btnPeople = (Button) findViewById(R.id.btnPeople);
        final Button btnCircle = (Button) findViewById(R.id.btnCircle);
        final Button btnChat = (Button) findViewById(R.id.btnChat);
        final Button btnSetting = (Button) findViewById(R.id.btnSetting);


        layProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*mDrawerLayout.closeDrawers();
                Intent i = new Intent(DashboardActivity.this, ProfileActivity.class);
                i.putExtra("user_id", user_id);
                startActivity(i);*/
                mDrawerLayout.closeDrawers();
                closeAllFragments();
                replaceAnimatedFragment(new SettingFragment());
                btnSetting.setBackgroundResource(R.drawable.custom_active_part);
                btnPeople.setBackgroundResource(R.drawable.custom_buttom_part);
                btnChat.setBackgroundResource(R.drawable.custom_buttom_part);
                btnCircle.setBackgroundResource(R.drawable.custom_buttom_part);
            }
        });
        btnPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
                closeAllFragments();
                replaceAnimatedFragment(new PeopleFragment());
                v.setBackgroundResource(R.drawable.custom_active_part);
                btnSetting.setBackgroundResource(R.drawable.custom_buttom_part);
                btnCircle.setBackgroundResource(R.drawable.custom_buttom_part);
                btnChat.setBackgroundResource(R.drawable.custom_buttom_part);
            }
        });
        btnCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
                closeAllFragments();
                replaceAnimatedFragment(new CirclesFragment());
                v.setBackgroundResource(R.drawable.custom_active_part);
                btnSetting.setBackgroundResource(R.drawable.custom_buttom_part);
                btnPeople.setBackgroundResource(R.drawable.custom_buttom_part);
                btnChat.setBackgroundResource(R.drawable.custom_buttom_part);
            }
        });
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
                closeAllFragments();
                replaceAnimatedFragment(new ChatFragment());
                v.setBackgroundResource(R.drawable.custom_active_part);
                btnPeople.setBackgroundResource(R.drawable.custom_buttom_part);
                btnSetting.setBackgroundResource(R.drawable.custom_buttom_part);
                btnCircle.setBackgroundResource(R.drawable.custom_buttom_part);
            }
        });
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
                closeAllFragments();
                replaceAnimatedFragment(new SettingFragment());
                v.setBackgroundResource(R.drawable.custom_active_part);
                btnPeople.setBackgroundResource(R.drawable.custom_buttom_part);
                btnChat.setBackgroundResource(R.drawable.custom_buttom_part);
                btnCircle.setBackgroundResource(R.drawable.custom_buttom_part);
            }
        });
        btnChat.setBackgroundResource(R.drawable.custom_active_part);


    }

    class NewAppKeyTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected String doInBackground(String... params) {
            String result = null;
            if (HttpConnectRecive.isOnline(DashboardActivity.this)) {
                result = HttpConnectRecive.getInstance().newAppKey(DashboardActivity.this, user_id);
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
                new statusContact().execute();
                new ProfileTask().execute();
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
                                        new statusContact().execute();
                                        new ProfileTask().execute();
                                        new ContactServerTask().execute();
                                    }
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

    class ContactServerTask extends AsyncTask<String, String, ContactInfo> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected ContactInfo doInBackground(String... params) {
            ContactInfo result = HttpConnectRecive.getInstance().ContactServer(DashboardActivity.this, ContactInfo.stringJson(listContacts));
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
            List<MessagInfo> result = HttpConnectRecive.getInstance().getMessage(DashboardActivity.this, "0");
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
            List<GroupMessagerInfo> result = HttpConnectRecive.getInstance().getGroupMessager(DashboardActivity.this);
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

    class statusContact extends AsyncTask<String, ContactInfo, ContactInfo> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected ContactInfo doInBackground(String... params) {
            ContactInfo result = null;
            if (HttpConnectRecive.isOnline(DashboardActivity.this)) {
                result = HttpConnectRecive.getInstance().statusContact(DashboardActivity.this, ServerContact());
            }
            return result;
        }

        protected void onPostExecute(ContactInfo result) {

            super.onPostExecute(result);

        }
    }

    private String ServerContact() {
        List<ContactInfo> contactInfoList = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE server='1'", null);
        if (c.moveToFirst()) {
            int ideCollumn = c.getColumnIndex("id");
            int useridCollumn = c.getColumnIndex(SQLMessager.CONTACTS_USER_ID);
            int statusCollumn = c.getColumnIndex(SQLMessager.CONTACTS_STATUS);
            do {
                ContactInfo result_sql = new ContactInfo();
                result_sql.id_db = c.getInt(ideCollumn);
                result_sql.user_id = c.getInt(useridCollumn);
                result_sql.status = c.getString(statusCollumn);
                contactInfoList.add(result_sql);
            } while (c.moveToNext());
        }
        return ContactInfo.stringJsonStatus(contactInfoList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.isDrawerIndicatorEnabled() &&
                mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class ProfileTask extends AsyncTask<String, String, ProfileInfo> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected ProfileInfo doInBackground(String... params) {
            ProfileInfo result = HttpConnectRecive.getInstance().getProfile(DashboardActivity.this);
            return result;
        }

        protected void onPostExecute(ProfileInfo result) {
            if (result != null) {
                btnProfile.setText(result.firstname + " " + result.lastname);
                String url_img = HttpConnectRecive.URLP + result.photo;
                ImageLoader.getInstance()
                        .displayImage(url_img, imgProfile, options, new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {

                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                            }
                        }, new ImageLoadingProgressListener() {
                            @Override
                            public void onProgressUpdate(String imageUri, View view, int current, int total) {

                            }
                        });
            }

            super.onPostExecute(result);

        }
    }

    public final static boolean isValidPhone(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.PHONE.matcher(target).matches();
    }

    public class Message {
        public String message;
        public String name;
        public long time;
    }
}
