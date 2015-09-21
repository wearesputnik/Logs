package mobi.kolibri.messager.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.ProfileInfo;

public class EditProfileActivity extends AppCompatActivity {
    Integer user_id;
    EditText edtEditFirstname, edtEditLastname, edtEditPhone, edtEditSummary;
    Button btnEditSave;
    ProfileInfo item_result;
    ImageView imgEditProfile;
    private Uri mUri;
    private String appAddPhoto = "";
    private Integer photo_witch;
    private DisplayImageOptions options;

    private int REQUEST_TAKE_PHOTO = 1;
    private int REQUEST_CHOOSE_EXISTING = 2;
    private int REQUEST_CROP_IMAGE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            user_id = b.getInt("user_id");
        }

        photo_witch = 0;

        imgEditProfile = (ImageView) findViewById(R.id.imgEditProfile);
        edtEditFirstname = (EditText) findViewById(R.id.edtEditFirstname);
        edtEditLastname = (EditText) findViewById(R.id.edtEditLastname);
        edtEditPhone = (EditText) findViewById(R.id.edtEditPhone);
        edtEditSummary = (EditText) findViewById(R.id.edtEditSummary);
        btnEditSave = (Button) findViewById(R.id.btnEditSave);

        item_result = new ProfileInfo();

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.mipmap.profile_max)
                .showImageForEmptyUri(R.mipmap.profile_max)
                .showImageOnFail(R.mipmap.profile_max)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        new ProfileTask().execute();

        imgEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogPhoto();
            }
        });

        btnEditSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item_result.lastname = edtEditLastname.getText().toString();
                item_result.firstname = edtEditFirstname.getText().toString();
                item_result.phone = edtEditPhone.getText().toString();
                item_result.summary = edtEditSummary.getText().toString();

                if (!appAddPhoto.equals("")) {
                    item_result.photo = appAddPhoto;
                    photo_witch = 1;
                }

                new EditProfileTask().execute();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == REQUEST_CHOOSE_EXISTING){
                //Take avatar from gallery
                mUri = data.getData();
                appAddPhoto = getPath(mUri);
                System.out.println("Image Path : " + appAddPhoto);

                imgEditProfile.setImageBitmap(BitmapFactory.decodeFile(appAddPhoto));
               /* String selectedImagePath = Utils.getPath(getActivity(), selectedImageUri);
                File inputFile;
                File outputDir = getActivity().getCacheDir();
                File outputFile;
                try {
                    inputFile = new File(selectedImagePath);

                    outputFile = File.createTempFile("newAvatar", "png", outputDir);

                    FileInputStream ios = new FileInputStream(inputFile);
                    FileOutputStream fos = new FileOutputStream(outputFile.getPath());
                    Utils.copyStream(ios, fos);


                    fos.close();
                    ios.close();
                    appAddPhoto = outputFile.getAbsolutePath();
                    appointAddPhotoLoad();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Can't save photo", Toast.LENGTH_SHORT).show();
                }*/
                return;

            }

            if(requestCode == REQUEST_TAKE_PHOTO){
                Log.i("Photo", "Photo taken");
                appAddPhoto = mUri.getPath();
                Log.e("Photo_uri", appAddPhoto);
                imgEditProfile.setImageBitmap(BitmapFactory.decodeFile(appAddPhoto));
            }

            /*if(requestCode == REQUEST_CROP_IMAGE){
                //Crop avatar to square
                String path = data.getStringExtra(CropImage.IMAGE_PATH);
                if(path != null){
                    imgEditProfile.setImageBitmap(BitmapFactory.decodeFile(path));
                    appAddPhoto = path;
                    return;
                }
            }*/

        }
    }

    @SuppressWarnings("deprecation")
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        finish();
        return super.onOptionsItemSelected(item);
    }

    class ProfileTask extends AsyncTask<String, String, ProfileInfo> {
        Dialog dialog;

        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(EditProfileActivity.this, R.style.TransparentProgressDialog);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_progressdialog);
            dialog.show();


        }

        @SuppressWarnings("static-access")
        protected ProfileInfo doInBackground(String... params) {
            ProfileInfo result = HttpConnectRecive.getProfile(user_id, EditProfileActivity.this);
            return result;
        }

        protected void onPostExecute(ProfileInfo result) {
            dialog.dismiss();
            if (result != null) {
                edtEditFirstname.setText(result.firstname);
                edtEditLastname.setText(result.lastname);
                edtEditPhone.setText(result.phone);
                edtEditSummary.setText(result.summary);
                String url_img = HttpConnectRecive.URLP + result.photo;
                ImageLoader.getInstance()
                        .displayImage(url_img, imgEditProfile, options, new SimpleImageLoadingListener() {
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

    class EditProfileTask extends AsyncTask<String, String, ProfileInfo> {
        Dialog dialog;

        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(EditProfileActivity.this, R.style.TransparentProgressDialog);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_progressdialog);
            dialog.show();


        }

        @SuppressWarnings("static-access")
        protected ProfileInfo doInBackground(String... params) {
            ProfileInfo result = HttpConnectRecive.setProfile(EditProfileActivity.this, user_id, item_result, photo_witch);
            return result;
        }

        protected void onPostExecute(ProfileInfo result) {
            dialog.dismiss();
            if (result != null) {
                onBackPressed();
            }

            super.onPostExecute(result);

        }
    }

    private void DialogPhoto() {

        final Dialog dialog = new Dialog(EditProfileActivity.this);
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
                mUri = generateFileUri();
                if (mUri == null) {
                    Toast.makeText(EditProfileActivity.this, "SD card not available", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            }
        });

        btnChooseExisting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CHOOSE_EXISTING);
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
}
