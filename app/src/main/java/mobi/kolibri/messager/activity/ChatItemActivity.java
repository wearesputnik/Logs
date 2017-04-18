package mobi.kolibri.messager.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.devadvance.circularseekbar.CircularSeekBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.Utils;
import mobi.kolibri.messager.adapters.MessagerAdapter;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.MessagInfo;
import mobi.kolibri.messager.object.SQLMessager;

public class ChatItemActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    Integer user_id_from;
    Integer chat_id;
    SQLMessager sqlMessager;
    ListView listMeseges;
    EditText textMessages;
    ImageButton sendMessages;
    SQLiteDatabase db;
    MessagerAdapter adapter;
    Integer photo_witch;
    String duration = "0";
    String type_chat;
    LinearLayout laySelectPhoto;
    ImageView btnTakePhoto, btnChooseExisting;
    Updater u;
    private Uri mUri;
    private String filepath = "";
    Bitmap selected_bitmap = null;
    private int REQUEST_TAKE_PHOTO = 1;
    private int REQUEST_CHOOSE_EXISTING = 2;
    private int REQUEST_CROP_IMAGE = 3;
    private static final int GALLERY_KITKAT_INTENT_CALLED = 4;
    private TextView txtTitleActionBar;
    ImageView imageView;
    String formattedDate;

    String[] PermisionLocation = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    Integer RequestLocationId = 1;
    private View mLayout;
    private Integer photoButtom = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_item);
        mLayout = (View) findViewById(R.id.main_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        txtTitleActionBar = (TextView) findViewById(R.id.txtTitleActionBar);
        laySelectPhoto = (LinearLayout) findViewById(R.id.laySelectPhoto);
        btnTakePhoto = (ImageView) findViewById(R.id.btnTakePhoto);
        btnChooseExisting = (ImageView) findViewById(R.id.btnChooseExisting);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            user_id_from = b.getInt("user_id_from");
            chat_id = b.getInt("chat_id");
            type_chat = b.getString("type");
        }

        photo_witch = 0;

        new getMessegTask().execute();

        listMeseges = (ListView) findViewById(R.id.listChatMessages);
        listMeseges.setDividerHeight(0);
        adapter = new MessagerAdapter(ChatItemActivity.this, HttpConnectRecive.getUserId(ChatItemActivity.this));
        listMeseges.setAdapter(adapter);
        textMessages = (EditText) findViewById(R.id.edtChatMessages);
        sendMessages = (ImageButton) findViewById(R.id.btnChatSend);
        imageView = (ImageView) findViewById(R.id.imageView4);

        sqlMessager = new SQLMessager(ChatItemActivity.this);

        db = sqlMessager.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CHAT + " WHERE id=" + chat_id, null);
        if (c.moveToFirst()) {
            int nameCollumn = c.getColumnIndex(SQLMessager.CHAT_NAME);
            txtTitleActionBar.setText(c.getString(nameCollumn));
        } else {
            Cursor c_us = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE " + SQLMessager.CONTACTS_USER_ID + "=" + user_id_from, null);
            if (c_us.moveToFirst()) {
                int nameCollumn = c_us.getColumnIndex(SQLMessager.CONTACTS_NAME);

                txtTitleActionBar.setText(c_us.getString(nameCollumn));
            }
        }

        Cursor c_ch = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_MESSAGER + " WHERE " + SQLMessager.MESSAGER_CHAT_ID + "='" + chat_id + "'", null);
        if (c_ch.moveToFirst()) {
            int idCollumn = c_ch.getColumnIndex("id");
            int idFromCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_FROM_ID);
            int idToCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_TO_ID);
            int idMessageCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_MESSAG);
            int attacmentCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_ATTACHMENT);
            int durationCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_DURATION);
            int createdCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_CREATED);
            do {
                MessagInfo result_sql = new MessagInfo();
                result_sql.id_messege = c_ch.getInt(idCollumn);
                result_sql.id_from = c_ch.getString(idFromCollumn);
                result_sql.id_to = c_ch.getString(idToCollumn);
                result_sql.message = c_ch.getString(idMessageCollumn);
                result_sql.attachment = c_ch.getString(attacmentCollumn);
                result_sql.duration = c_ch.getString(durationCollumn);
                result_sql.created = c_ch.getString(createdCollumn);
                adapter.add(result_sql);
            } while (c_ch.moveToNext());

        }
        adapter.notifyDataSetChanged();
        scrollDown();

        sendMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!textMessages.getText().toString().trim().equals("") || selected_bitmap != null) {
                    SendMessage();
                }
            }
        });

        u = new Updater();

        u.start();

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                laySelectPhoto.setVisibility(View.GONE);
                photoButtom = 1;
                mUri = generateFileUri();
                if (mUri == null) {
                    Toast.makeText(ChatItemActivity.this, "SD card not available", Toast.LENGTH_LONG).show();
                    return;
                }
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ActivityCompat.checkSelfPermission(ChatItemActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(ChatItemActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(ChatItemActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        requestCameraPermission();

                    }
                    else {
                        PhotoTakePhoto();
                    }
                }
                else {
                    PhotoTakePhoto();
                }
            }
        });

        btnChooseExisting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                laySelectPhoto.setVisibility(View.GONE);
                photoButtom = 2;
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ActivityCompat.checkSelfPermission(ChatItemActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(ChatItemActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(ChatItemActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        requestCameraPermission();

                    }
                    else {
                        PhotoChooseExisting();
                    }
                }
                else {
                    PhotoChooseExisting();
                }
            }
        });
    }

    private void PhotoTakePhoto() {
        if (Build.VERSION.SDK_INT >= 19) {
            Intent intent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        } else {
            Intent intent = new Intent(
                    "android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        }
    }

    private void PhotoChooseExisting() {
        if (Build.VERSION.SDK_INT >= 19) {
            Intent intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent1.addCategory(Intent.CATEGORY_OPENABLE);
            intent1.setType("image/jpeg");
            startActivityForResult(intent1,
                    GALLERY_KITKAT_INTENT_CALLED);
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(intent,
                    "Complete action using"), REQUEST_CHOOSE_EXISTING);
        }
    }

    private void requestCameraPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            Snackbar.make(mLayout, R.string.permission_camera_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(ChatItemActivity.this, PermisionLocation, RequestLocationId);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, PermisionLocation, RequestLocationId);
        }
    }

    private void SendMessage() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formattedDate = df.format(cal.getTime());
        long id = 0;
        if (!formattedDate.trim().toString().equals("")) {
            if (chat_id != 0) {
                ContentValues cv_ms = new ContentValues();
                cv_ms.put(SQLMessager.MESSAGER_CHAT_ID, chat_id.toString());
                cv_ms.put(SQLMessager.MESSAGER_FROM_ID, user_id_from.toString());
                cv_ms.put(SQLMessager.MESSAGER_TO_ID, HttpConnectRecive.getUserId(ChatItemActivity.this));
                cv_ms.put(SQLMessager.MESSAGER_MESSAG, textMessages.getText().toString());
                cv_ms.put(SQLMessager.MESSAGER_CREATED, formattedDate);
                if (selected_bitmap != null) {
                    cv_ms.put(SQLMessager.MESSAGER_ATTACHMENT, filepath);
                    cv_ms.put(SQLMessager.MESSAGER_DURATION, duration);
                }
                cv_ms.put(SQLMessager.MESSAGER_SERVER, "1");
                id = db.insert(SQLMessager.TABLE_MESSAGER, null, cv_ms);
            } else {
                String title = null;
                Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE " + SQLMessager.CONTACTS_USER_ID + "='" + user_id_from + "'", null);
                if (c.moveToFirst()) {
                    int nameCollumn = c.getColumnIndex(SQLMessager.CONTACTS_NAME);
                    title = c.getString(nameCollumn);
                }
                long chat_id_db = 0;
                JSONArray arrayUser = new JSONArray();
                JSONObject itemJs = new JSONObject();
                try {
                    itemJs.put("user_id", user_id_from.toString());
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                arrayUser.put(itemJs);
                ContentValues cv_ch = new ContentValues();
                cv_ch.put(SQLMessager.CHAT_JSON_INTERLOCUTOR, arrayUser.toString());
                cv_ch.put(SQLMessager.CHAT_TYPE, type_chat.toString());
                cv_ch.put(SQLMessager.CHAT_READ, "1");
                cv_ch.put(SQLMessager.CHAT_NAME, title);
                chat_id_db = db.insert(SQLMessager.TABLE_CHAT, null, cv_ch);
                if (chat_id_db > 0) {
                    chat_id = (int) chat_id_db;
                    ContentValues cv_ms = new ContentValues();
                    cv_ms.put(SQLMessager.MESSAGER_CHAT_ID, chat_id_db);
                    cv_ms.put(SQLMessager.MESSAGER_FROM_ID, user_id_from.toString());
                    cv_ms.put(SQLMessager.MESSAGER_TO_ID, HttpConnectRecive.getUserId(ChatItemActivity.this));
                    cv_ms.put(SQLMessager.MESSAGER_MESSAG, textMessages.getText().toString());
                    cv_ms.put(SQLMessager.MESSAGER_SERVER, "1");
                    cv_ms.put(SQLMessager.MESSAGER_CREATED, formattedDate);
                    if (selected_bitmap != null) {
                        cv_ms.put(SQLMessager.MESSAGER_ATTACHMENT, filepath);
                        cv_ms.put(SQLMessager.MESSAGER_DURATION, duration);
                    }
                    id = db.insert(SQLMessager.TABLE_MESSAGER, null, cv_ms);
                }
            }
            MessagInfo result_sql = new MessagInfo();
            result_sql.id_messege = (int)id;
            result_sql.id_from = user_id_from.toString();
            result_sql.id_to = HttpConnectRecive.getUserId(ChatItemActivity.this);
            result_sql.message = textMessages.getText().toString();
            result_sql.type_chat = type_chat;
            result_sql.created = formattedDate;
            if (selected_bitmap != null) {
                result_sql.attachment = filepath;
                result_sql.duration = duration;
            }
            new setMessagerTask().execute(result_sql);
            adapter.add(result_sql);
            textMessages.setText("");
            adapter.notifyDataSetChanged();
            scrollDown();
            photo_witch = 0;
            selected_bitmap = null;
            photoButtom = 0;
            imageView.setVisibility(View.GONE);
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
                                    new getMessegTask().execute();
                                }
                            }
                    );
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                u.stopped = true;
                if (type_chat.trim().equals("secret")) {
                    db = sqlMessager.getWritableDatabase();
                    db.delete(SQLMessager.TABLE_CHAT, "id=?", new String[]{chat_id.toString()});
                    db.delete(SQLMessager.TABLE_MESSAGER, SQLMessager.MESSAGER_CHAT_ID + "=?", new String[]{chat_id.toString()});
                }
                onBackPressed();
                finish();
                break;
            case R.id.action_send_photo:
                if (laySelectPhoto.getVisibility() == View.GONE) {
                    laySelectPhoto.setVisibility(View.VISIBLE);
                    Animation anim = AnimationUtils.loadAnimation(ChatItemActivity.this, R.anim.menuplus);
                    anim.reset();
                    laySelectPhoto.clearAnimation();
                    laySelectPhoto.startAnimation(anim);
                } else {
                    laySelectPhoto.setVisibility(View.GONE);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class setMessagerTask extends AsyncTask<MessagInfo, String, MessagInfo> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected MessagInfo doInBackground(MessagInfo... params) {
            MessagInfo result = null;
            if (params != null) {
                result = HttpConnectRecive.getInstance().setMessage(ChatItemActivity.this, params[0], photo_witch);
            }
            return result;
        }

        protected void onPostExecute(MessagInfo result) {
            super.onPostExecute(result);

        }
    }

    class getMessegTask extends AsyncTask<String, String, List<MessagInfo>> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected List<MessagInfo> doInBackground(String... params) {
            List<MessagInfo> result = HttpConnectRecive.getInstance().getMessage(ChatItemActivity.this, user_id_from.toString());
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
                    cv_ms.put(SQLMessager.MESSAGER_ATTACHMENT, item.attachment);
                    cv_ms.put(SQLMessager.MESSAGER_DURATION, item.duration);
                    cv_ms.put(SQLMessager.MESSAGER_CREATED, item.created);
                    long id = db.insert(SQLMessager.TABLE_MESSAGER, null, cv_ms);
                    MessagInfo result_sql = new MessagInfo();
                    result_sql.id_messege = (int)id;
                    result_sql.id_from = item.id_from;
                    result_sql.id_to = item.id_to;
                    result_sql.message = item.message;
                    result_sql.attachment = item.attachment;
                    result_sql.duration = item.duration;
                    result_sql.created = item.created;
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

    private Uri generateFileUri() {

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return null;

        File path = new File(Environment.getExternalStorageDirectory(), "LogsMesager");
        if (!path.exists()) {
            if (!path.mkdirs()) {
                return null;
            }
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());
        File newFile = new File(path.getPath() + File.separator + timeStamp + ".jpg");
        return Uri.fromFile(newFile);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestLocationId) {
            if (Utils.verifyPermissions(grantResults)) {
                Snackbar.make(mLayout, R.string.permision_available_camera, Snackbar.LENGTH_SHORT).show();
                if (photoButtom == 1) {
                    PhotoTakePhoto();
                }
                else if (photoButtom == 2) {
                    PhotoChooseExisting();
                }
            } else {
                Snackbar.make(mLayout, R.string.permissions_not_granted, Snackbar.LENGTH_SHORT).show();
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressWarnings("ResourceType")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        imageView.setVisibility(View.GONE);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CHOOSE_EXISTING) {
                if (data != null) {
                    Uri selectedImage = data.getData();
                    filepath = Utils.getPath(getApplicationContext(), selectedImage);

                    //performCrop(selectedImage);
                    selected_bitmap = null;
                    try {
                        selected_bitmap = Utils.decodeUri(
                                ChatItemActivity.this, selectedImage, 300);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (selected_bitmap != null) {
                        Log.e("PHOTO PATCH: ", filepath + " " + selected_bitmap.toString());
                        imageView.setImageBitmap(selected_bitmap);
                        duration = "0";
                        photo_witch = 1;
                        SendMessage();

                    }
                }

            }
            if (requestCode == GALLERY_KITKAT_INTENT_CALLED) {
                ParcelFileDescriptor parcelFileDescriptor;
                Uri mImageCaptureUri = data.getData();
                try {
                    final int takeFlags = data.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(
                            mImageCaptureUri, takeFlags);
                    /// performCrop(mImageCaptureUri);
                    parcelFileDescriptor = getContentResolver()
                            .openFileDescriptor(mImageCaptureUri, "r");
                    FileDescriptor fileDescriptor = parcelFileDescriptor
                            .getFileDescriptor();
                    selected_bitmap = BitmapFactory
                            .decodeFileDescriptor(fileDescriptor);
                    parcelFileDescriptor.close();
                    filepath = Utils.getPath(getApplicationContext(), mImageCaptureUri);
                    if (selected_bitmap != null) {
                        Log.e("PHOTO PATCH: ", filepath + " " + selected_bitmap.toString());
                        imageView.setImageBitmap(selected_bitmap);
                        duration = "0";
                        photo_witch = 1;
                        SendMessage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if (requestCode == REQUEST_TAKE_PHOTO) {
                filepath = mUri.getPath();

                selected_bitmap = BitmapFactory.decodeFile(filepath);
                if (selected_bitmap != null) {
                    Log.e("PHOTO PATCH: ", filepath + " " + selected_bitmap.toString());
                    imageView.setImageBitmap(selected_bitmap);
                    duration = "0";
                    photo_witch = 1;
                    SendMessage();
                }
            }

            if (requestCode == REQUEST_CROP_IMAGE) {
                Bundle extras = data.getExtras();
                Bitmap selectedBitmap = extras.getParcelable("data");
                filepath = Environment.getExternalStorageDirectory() + filename;
                Bitmap thumbnail = BitmapFactory.decodeFile(filepath);
                selectedBitmap = thumbnail;
                if (selectedBitmap != null) {
                    duration = "0";
                    photo_witch = 1;
                    SendMessage();
                }
            }

        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(),
                inImage, "temp", null);
        return Uri.parse(path);
    }

    private String filename = "";

    private void performCrop(Uri picUri) {
        try {

            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(picUri, "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 800);
            intent.putExtra("outputY", 800);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
            filename = "/temporary_holder"
                    + Calendar.getInstance().getTimeInMillis() + ".jpg";
            File f = new File(Environment.getExternalStorageDirectory(),
                    filename);
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Log.e("io", ex.getMessage());
            }

            Uri uri = Uri.fromFile(f);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            startActivityForResult(intent, REQUEST_CROP_IMAGE);
        } catch (ActivityNotFoundException anfe) {
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast
                    .makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /*private void DialogDuration() {
        final Dialog dialog = new Dialog(ChatItemActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_duration);

        Button btnSaveDuration = (Button) dialog.findViewById(R.id.btnSaveDuration);
        final TextView txtSecondV = (TextView) dialog.findViewById(R.id.textView11);

        final CircularSeekBar seekbar = (CircularSeekBar) dialog.findViewById(R.id.circularSeekBar1);
        seekbar.setProgress(5);
        txtSecondV.setText("5");
        seekbar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                txtSecondV.setText(circularSeekBar.getProgress() + "");
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
                txtSecondV.setText(seekBar.getProgress() + "");

            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {
                txtSecondV.setText(seekBar.getProgress() + "");

            }
        });

        btnSaveDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                duration = txtSecondV.getText().toString();
                photo_witch = 1;
                SendMessage();
                dialog.dismiss();
            }
        });

        dialog.show();
    }*/

}
