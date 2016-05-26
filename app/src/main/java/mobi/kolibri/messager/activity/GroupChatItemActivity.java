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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.devadvance.circularseekbar.CircularSeekBar;

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
    LinearLayout laySelectPhoto;
    ImageView btnTakePhoto, btnChooseExisting;
    Updater u;
    Integer photo_witch;
    String duration;
    private String filepath = "";
    Bitmap selected_bitmap = null;
    private int REQUEST_TAKE_PHOTO = 1;
    private int REQUEST_CHOOSE_EXISTING = 2;
    private int REQUEST_CROP_IMAGE = 3;
    private static final int GALLERY_KITKAT_INTENT_CALLED = 4;
    private TextView txtTitleActionBar;
    ImageView imageView;
    String formattedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_item);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        txtTitleActionBar = (TextView) findViewById(R.id.txtTitleActionBar);
        laySelectPhoto = (LinearLayout) findViewById(R.id.laySelectPhoto);
        btnTakePhoto = (ImageView) findViewById(R.id.btnTakePhoto);
        btnChooseExisting = (ImageView) findViewById(R.id.btnChooseExisting);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            id_chat = b.getInt("chat_id");
            type_chat = b.getString("type");
        }

        photo_witch = 0;

        type_chat = "group";

        Log.e("CHAT_ID", id_chat.toString());

        textMessages = (EditText) findViewById(R.id.edtChatMessages);
        sendMessages = (ImageButton) findViewById(R.id.btnChatSend);
        listMeseges = (ListView) findViewById(R.id.listChatMessages);
        listMeseges.setDividerHeight(0);
        adapter = new GroupMessagerAdapter(GroupChatItemActivity.this, HttpConnectRecive.getUserId(GroupChatItemActivity.this));
        listMeseges.setAdapter(adapter);
        imageView = (ImageView) findViewById(R.id.imageView4);


        sqlMessager = new SQLMessager(GroupChatItemActivity.this);

        db = sqlMessager.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CHAT + " WHERE id=" + id_chat, null);
        if (c.moveToFirst()) {
            int nameCollumn = c.getColumnIndex(SQLMessager.CHAT_NAME);
            int usersCollumn = c.getColumnIndex(SQLMessager.CHAT_JSON_INTERLOCUTOR);
            chat_name = c.getString(nameCollumn);
            txtTitleActionBar.setText(chat_name);
            getSupportActionBar().setTitle(chat_name);
            json_users = c.getString(usersCollumn);
        }

        Cursor c_ch = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_MESSAGER + " WHERE " + SQLMessager.MESSAGER_CHAT_ID + "='" + id_chat + "'", null);
        if (c_ch.moveToFirst()) {
            int idFromCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_FROM_ID);
            int idToCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_TO_ID);
            int idMessageCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_MESSAG);
            int attacmentCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_ATTACHMENT);
            int durationCollumn = c_ch.getColumnIndex(SQLMessager.MESSAGER_DURATION);
            do {
                GroupMessagerInfo result_sql = new GroupMessagerInfo();
                result_sql.id_from = c_ch.getString(idFromCollumn);
                result_sql.id_to = c_ch.getString(idToCollumn);
                result_sql.message = c_ch.getString(idMessageCollumn);
                result_sql.attachment = c_ch.getString(attacmentCollumn);
                result_sql.duration = c_ch.getString(durationCollumn);
                adapter.add(result_sql);
            } while (c_ch.moveToNext());

        }
        adapter.notifyDataSetChanged();
        scrollDown();

        sendMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!textMessages.getText().toString().trim().equals("") || selected_bitmap != null) {
                    ContentValues cv_ms = new ContentValues();
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    formattedDate = df.format(cal.getTime());
                    cv_ms.put(SQLMessager.MESSAGER_CHAT_ID, id_chat.toString());
                    cv_ms.put(SQLMessager.MESSAGER_FROM_ID, json_users);
                    cv_ms.put(SQLMessager.MESSAGER_TO_ID, HttpConnectRecive.getUserId(GroupChatItemActivity.this));
                    cv_ms.put(SQLMessager.MESSAGER_MESSAG, textMessages.getText().toString());
                    cv_ms.put(SQLMessager.MESSAGER_CREATED, formattedDate);
                    cv_ms.put(SQLMessager.MESSAGER_SERVER, "1");
                    if (selected_bitmap != null) {
                        cv_ms.put(SQLMessager.MESSAGER_ATTACHMENT, filepath);
                        cv_ms.put(SQLMessager.MESSAGER_DURATION, duration);
                    }
                    db.insert(SQLMessager.TABLE_MESSAGER, null, cv_ms);
                    new setGroupMessagerTask().execute();
                    GroupMessagerInfo result_sql = new GroupMessagerInfo();
                    result_sql.id_from = json_users;
                    result_sql.id_to = HttpConnectRecive.getUserId(GroupChatItemActivity.this);
                    result_sql.message = textMessages.getText().toString();
                    result_sql.created = formattedDate;
                    if (selected_bitmap != null) {
                        result_sql.attachment = filepath;
                        result_sql.duration = duration;
                    }
                    adapter.add(result_sql);
                    textMessages.setText("");
                    adapter.notifyDataSetChanged();
                    scrollDown();
                }
            }
        });

        u = new Updater();

        u.start();

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                laySelectPhoto.setVisibility(View.GONE);
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
                laySelectPhoto.setVisibility(View.GONE);
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
                onBackPressed();
                finish();
                break;
            case R.id.action_send_photo:
                if (laySelectPhoto.getVisibility() == View.GONE) {
                    laySelectPhoto.setVisibility(View.VISIBLE);
                    Animation anim = AnimationUtils.loadAnimation(GroupChatItemActivity.this, R.anim.menuplus);
                    anim.reset();
                    laySelectPhoto.clearAnimation();
                    laySelectPhoto.startAnimation(anim);
                }
                else {
                    laySelectPhoto.setVisibility(View.GONE);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class setGroupMessagerTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected String doInBackground(String... params) {
            GroupMessagerInfo item_msg = new GroupMessagerInfo();
            item_msg.json_users = json_users;
            item_msg.message = textMessages.getText().toString();
            item_msg.type_chat = type_chat;
            item_msg.chat_name = chat_name;
            item_msg.created = formattedDate;
            if (selected_bitmap != null) {
                item_msg.attachment = filepath;
                item_msg.duration = duration;
            }
            String result = HttpConnectRecive.setGroupMessage(GroupChatItemActivity.this, item_msg, photo_witch);
            return result;
        }

        protected void onPostExecute(String result) {


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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressWarnings("ResourceType")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        imageView.setVisibility(View.GONE);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == REQUEST_CHOOSE_EXISTING){
                if (data != null) {
                    Uri selectedImage = data.getData();
                    filepath = Utils.getPath(getApplicationContext(), selectedImage);

                    performCrop(selectedImage);
                    selected_bitmap = null;
                    try {
                        selected_bitmap = Utils.decodeUri(
                                GroupChatItemActivity.this, selectedImage, 300);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (selected_bitmap != null) {
                        Log.e("PHOTO PATCH: ", filepath + " " + selected_bitmap.toString());
                        imageView.setImageBitmap(selected_bitmap);
                        DialogDuration();

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
                        DialogDuration();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if(requestCode == REQUEST_TAKE_PHOTO){
                Bitmap imageData = (Bitmap) data.getExtras().get("data");
                Uri selectedImage = getImageUri(GroupChatItemActivity.this,
                        imageData);
                filepath = Utils.getPath(getApplicationContext(),
                        selectedImage);
                if (Build.VERSION.SDK_INT < 19)
                    performCrop(selectedImage);
                selected_bitmap = (Bitmap) data.getExtras().get(
                        "data");
                if (selected_bitmap != null) {
                    Log.e("PHOTO PATCH: ", filepath + " " + selected_bitmap.toString());
                    imageView.setImageBitmap(selected_bitmap);
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
                   DialogDuration();
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
        final Dialog dialog = new Dialog(GroupChatItemActivity.this);
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
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
