package mobi.kolibri.messager.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.Utils;
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
    ImageButton sendMessages;
    SQLiteDatabase db;
    MessagerAdapter adapter;
    String type_chat;
    Updater u;
    private Uri mUri;
    private String filepath = "";
    Bitmap selected_bitmap = null;
    private int REQUEST_TAKE_PHOTO = 1;
    private int REQUEST_CHOOSE_EXISTING = 2;
    private int REQUEST_CROP_IMAGE = 3;
    private static final int GALLERY_KITKAT_INTENT_CALLED = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_item);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.back_from_chats);

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
        sendMessages = (ImageButton) findViewById(R.id.btnChatSend);

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
                DialogPhoto();
                break;
        }
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

    private void DialogPhoto() {

        final Dialog dialog = new Dialog(ChatItemActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_photo);
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        Button btnTakePhoto = (Button) dialog
                .findViewById(R.id.btnRegChat);
        Button btnChooseExisting = (Button) dialog
                .findViewById(R.id.btnSecChat);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                /*mUri = generateFileUri();
                if (mUri == null) {
                    Toast.makeText(ChatItemActivity.this, "SD card not available", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);*/
                if (Build.VERSION.SDK_INT >= 19) {
                    Intent intent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_TAKE_PHOTO);
                } else {
                    Intent intent = new Intent(
                            "android.media.action.IMAGE_CAPTURE");
                    startActivityForResult(intent, REQUEST_TAKE_PHOTO);
                }
            }
        });

        btnChooseExisting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CHOOSE_EXISTING);*/
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
        });
        dialog.show();
    }

    private Uri generateFileUri() {

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return null;

        File path = new File (Environment.getExternalStorageDirectory(), "LogsMesager");
        if (! path.exists()){
            if (! path.mkdirs()){
                return null;
            }
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());
        File newFile = new File(path.getPath() + File.separator + timeStamp + ".jpg");
        return Uri.fromFile(newFile);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressWarnings("ResourceType")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == REQUEST_CHOOSE_EXISTING){
                if (data != null) {
                    Uri selectedImage = data.getData();
                    filepath = Utils.getPath(getApplicationContext(),
                            selectedImage);

                    performCrop(selectedImage);
                    selected_bitmap = null;
                    try {
                        selected_bitmap = Utils.decodeUri(
                                ChatItemActivity.this, selectedImage, 300);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (selected_bitmap != null) {
                        //imgPhoto.setImageBitmap(selected_bitmap);
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
                       // imgPhoto.setImageBitmap(selected_bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if(requestCode == REQUEST_TAKE_PHOTO){
                Log.i("Photo", "Photo taken");
                Bitmap imageData = (Bitmap) data.getExtras().get("data");
                Uri selectedImage = getImageUri(ChatItemActivity.this,
                        imageData);
                filepath = Utils.getPath(getApplicationContext(),
                        selectedImage);
                if (Build.VERSION.SDK_INT < 19)
                    performCrop(selectedImage);
                selected_bitmap = (Bitmap) data.getExtras().get(
                        "data");
                if (selected_bitmap != null) {
                    DialogDuration();
                }
            }

            if(requestCode == REQUEST_CROP_IMAGE){
                Bundle extras = data.getExtras();
                Bitmap selectedBitmap = extras.getParcelable("data");
                filepath = Environment.getExternalStorageDirectory() + filename;
                Bitmap thumbnail = BitmapFactory.decodeFile(filepath);
                selectedBitmap = thumbnail;
                if (selectedBitmap != null) {
               //     imgPhoto.setImageBitmap(selectedBitmap);
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

    private void DialogDuration () {
        final Dialog dialog = new Dialog(ChatItemActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_duration);

        NumberPicker np = (NumberPicker) dialog.findViewById(R.id.numberPicker);
        np.setMinValue(0);
        np.setMaxValue(60);
        np.setWrapSelectorWheel(false);

        dialog.show();
    }
}
