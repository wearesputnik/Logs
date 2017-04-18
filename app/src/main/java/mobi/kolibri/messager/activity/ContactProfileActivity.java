package mobi.kolibri.messager.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.ByteArrayOutputStream;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.ProfileInfo;
import mobi.kolibri.messager.object.SQLMessager;

public class ContactProfileActivity extends AppCompatActivity {
    Integer user_id;
    String id_user;
    TextView txtName, txtPhone, txtSummary;
    private DisplayImageOptions options;
    ImageView imgProfile, btnNewChat;
    private TextView txtTitleActionBar;
    SQLMessager sqlMessager;
    SQLiteDatabase db;
    Button btnNewSecretChat;
    String server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_profile);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            user_id = b.getInt("user_id");
            server = b.getString("server");
        }

        Log.e("USER_ID", "" + user_id);

        txtName = (TextView) findViewById(R.id.txtName);
        txtPhone = (TextView) findViewById(R.id.txtPhone);
        txtSummary = (TextView) findViewById(R.id.txtSummary);
        imgProfile = (ImageView) findViewById(R.id.imgProfile);
        btnNewChat = (ImageView) findViewById(R.id.btnNewChat);
        btnNewSecretChat = (Button) findViewById(R.id.btnNewSecretChat);

        sqlMessager = new SQLMessager(ContactProfileActivity.this);
        db = sqlMessager.getWritableDatabase();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        txtTitleActionBar = (TextView) findViewById(R.id.txtTitleActionBar);

        options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.mipmap.profile_max)
            .showImageForEmptyUri(R.mipmap.profile_max)
            .showImageOnFail(R.mipmap.profile_max)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();
        if (server.equals("2")) {
            Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE user_id=" + user_id, null);
            if (c.moveToFirst()) {
                int idCollumn = c.getColumnIndex("id");
                int useridCollumn = c.getColumnIndex(SQLMessager.CONTACTS_USER_ID);
                int nameCollumn = c.getColumnIndex(SQLMessager.CONTACTS_NAME);
                int phoneCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHONE);
                int summaryCollumn = c.getColumnIndex(SQLMessager.CONTACTS_SUMMARY);
                int photoCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHOTO);
                int photoSaveCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHOTO_SAVE);

                user_id = c.getInt(idCollumn);
                id_user = c.getString(useridCollumn);
                txtTitleActionBar.setText(c.getString(nameCollumn));
                txtName.setText(c.getString(nameCollumn));
                txtPhone.setText(c.getString(phoneCollumn));
                txtSummary.setText(c.getString(summaryCollumn));
                if (c.getString(photoSaveCollumn) != null) {
                    imgProfile.setImageURI(Uri.parse(c.getString(photoSaveCollumn)));
                }
                else if (c.getString(photoCollumn) != null) {
                    String url_img = HttpConnectRecive.URLP + c.getString(photoCollumn);
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
                                    Uri patchSave = getImageUri(ContactProfileActivity.this, loadedImage);
                                    ContentValues cv_ms = new ContentValues();
                                    cv_ms.put(SQLMessager.CONTACTS_PHOTO_SAVE, patchSave.toString());
                                    db.update(SQLMessager.TABLE_CONTACTS, cv_ms, "id=?", new String[]{"" + user_id});
                                }
                            }, new ImageLoadingProgressListener() {
                                @Override
                                public void onProgressUpdate(String imageUri, View view, int current, int total) {

                                }
                            });
                } else {
                    imgProfile.setImageResource(R.mipmap.profile_max);
                }
            }
            else {
                new getUserServerProfile().execute();
            }
        }
        else {
            Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE id=" + user_id, null);
            if (c.moveToFirst()) {
                int useridCollumn = c.getColumnIndex(SQLMessager.CONTACTS_USER_ID);
                int nameCollumn = c.getColumnIndex(SQLMessager.CONTACTS_NAME);
                int phoneCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHONE);
                int summaryCollumn = c.getColumnIndex(SQLMessager.CONTACTS_SUMMARY);
                int photoCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHOTO);
                int photoSaveCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHOTO_SAVE);

                id_user = c.getString(useridCollumn);
                txtTitleActionBar.setText(c.getString(nameCollumn));
                txtName.setText(c.getString(nameCollumn));
                txtPhone.setText(c.getString(phoneCollumn));
                txtSummary.setText(c.getString(summaryCollumn));

                if (c.getString(photoSaveCollumn) != null) {
                    imgProfile.setImageURI(Uri.parse(c.getString(photoSaveCollumn)));
                }
                else if (c.getString(photoCollumn) != null) {
                    String url_img = HttpConnectRecive.URLP + c.getString(photoCollumn);
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
                                    Uri patchSave = getImageUri(ContactProfileActivity.this, loadedImage);
                                    ContentValues cv_ms = new ContentValues();
                                    cv_ms.put(SQLMessager.CONTACTS_PHOTO_SAVE, patchSave.toString());
                                    db.update(SQLMessager.TABLE_CONTACTS, cv_ms, "id=?", new String[]{"" + user_id});
                                }
                            }, new ImageLoadingProgressListener() {
                                @Override
                                public void onProgressUpdate(String imageUri, View view, int current, int total) {

                                }
                            });
                } else {
                    imgProfile.setImageResource(R.mipmap.profile_max);
                }
            }
        }

        btnNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor c_chat = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CHAT + " WHERE " + SQLMessager.CHAT_JSON_INTERLOCUTOR + "='[{\"user_id\":\"" + id_user + "\"}]' and " + SQLMessager.CHAT_TYPE + "='regular'", null);
                if (c_chat.moveToFirst()) {
                    int chatidCollumn = c_chat.getColumnIndex("id");
                    int typeCollumn = c_chat.getColumnIndex(SQLMessager.CHAT_TYPE);
                    Intent i = new Intent(ContactProfileActivity.this, ChatItemActivity.class);
                    i.putExtra("user_id_from", Integer.parseInt(id_user));
                    i.putExtra("chat_id", c_chat.getInt(chatidCollumn));
                    i.putExtra("type", c_chat.getString(typeCollumn));
                    startActivity(i);
                }
                else {
                    Intent i = new Intent(ContactProfileActivity.this, ChatItemActivity.class);
                    i.putExtra("user_id_from", Integer.parseInt(id_user));
                    i.putExtra("chat_id", 0);
                    i.putExtra("type", "regular");
                    startActivity(i);
                }
            }
        });

        btnNewSecretChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor c_chat = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CHAT + " WHERE " + SQLMessager.CHAT_JSON_INTERLOCUTOR + "='[{\"user_id\":\"" + id_user + "\"}]' and " + SQLMessager.CHAT_TYPE + "='secret'", null);
                if (c_chat.moveToFirst()) {
                    int chatidCollumn = c_chat.getColumnIndex("id");
                    int typeCollumn = c_chat.getColumnIndex(SQLMessager.CHAT_TYPE);
                    Intent i = new Intent(ContactProfileActivity.this, ChatItemActivity.class);
                    i.putExtra("user_id_from", Integer.parseInt(id_user));
                    i.putExtra("chat_id", c_chat.getInt(chatidCollumn));
                    i.putExtra("type", c_chat.getString(typeCollumn));
                    startActivity(i);
                }
                else {
                    Intent i = new Intent(ContactProfileActivity.this, ChatItemActivity.class);
                    i.putExtra("user_id_from", Integer.parseInt(id_user));
                    i.putExtra("chat_id", 0);
                    i.putExtra("type", "secret");
                    startActivity(i);
                }
            }
        });
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(),
                inImage, "LogsMesager", null);
        return Uri.parse(path);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class getUserServerProfile extends AsyncTask<String, String, ProfileInfo> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected ProfileInfo doInBackground(String... params) {
            ProfileInfo result = HttpConnectRecive.getInstance().getUserServerProfile(user_id.toString(), ContactProfileActivity.this);
            return result;
        }

        protected void onPostExecute(ProfileInfo result) {
            if (result != null) {
                id_user = result.user_id;
                txtTitleActionBar.setText(result.firstname + " " + result.lastname);
                txtName.setText(result.firstname + " " + result.lastname);
                txtPhone.setText(result.phone);
                txtSummary.setText(result.summary);
                if (result.photo != null) {
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
                } else {
                    imgProfile.setImageResource(R.mipmap.profile_max);
                }
            }
            super.onPostExecute(result);
        }
    }

}
