package mobi.kolibri.messager.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;

import mobi.kolibri.messager.LoginActivity;
import mobi.kolibri.messager.R;
import mobi.kolibri.messager.UILApplication;
import mobi.kolibri.messager.Utils;
import mobi.kolibri.messager.activity.ChatItemActivity;
import mobi.kolibri.messager.activity.EditProfileActivity;
import mobi.kolibri.messager.helper.PrefManager;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.ProfileInfo;
import mobi.kolibri.messager.object.SQLMessager;

public class SettingFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {
    SQLMessager sqlMessager;
    SQLiteDatabase db;
    public PrefManager pref;
    private Button btnProfileEdit;
    private Button btnLogOut;
    TextView txtName, txtPhone, txtSummary;
    private DisplayImageOptions options;
    ImageView imgProfile, imageViewGallery, imageViewPhoto, imageViewDelete;
    RelativeLayout relativeLayoutPhoto, relativeLayoutView;
    ImageButton imageButtonPhoto;
    ProfileInfo profileInfo;
    Integer photo_witch;
    private Uri mUri;
    private String filepath = "";
    Bitmap selected_bitmap = null;
    private int REQUEST_TAKE_PHOTO = 1;
    private int REQUEST_CHOOSE_EXISTING = 2;
    private int REQUEST_CROP_IMAGE = 3;
    private static final int GALLERY_KITKAT_INTENT_CALLED = 4;

    String[] PermisionLocation = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    Integer RequestLocationId = 1;
    private View mLayout;
    private Integer photoButtom = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        mLayout = (View) rootView.findViewById(R.id.relativeLayoutView);
        sqlMessager = new SQLMessager(getActivity());
        db = sqlMessager.getWritableDatabase();

        pref = new PrefManager(getActivity().getApplicationContext());

        btnProfileEdit = (Button) rootView.findViewById(R.id.btnProfileEdit);
        btnLogOut = (Button) rootView.findViewById(R.id.btnLogOut);
        txtName = (TextView) rootView.findViewById(R.id.txtName);
        txtPhone = (TextView) rootView.findViewById(R.id.txtPhone);
        txtSummary = (TextView) rootView.findViewById(R.id.txtSummary);
        imgProfile = (ImageView) rootView.findViewById(R.id.imgProfile);
        imageViewGallery = (ImageView) rootView.findViewById(R.id.imageViewGallery);
        imageViewPhoto = (ImageView) rootView.findViewById(R.id.imageViewPhoto);
        imageViewDelete = (ImageView) rootView.findViewById(R.id.imageViewDelete);
        relativeLayoutPhoto = (RelativeLayout) rootView.findViewById(R.id.relativeLayoutPhoto);
        imageButtonPhoto = (ImageButton) rootView.findViewById(R.id.imageButtonPhoto);

        btnProfileEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), EditProfileActivity.class);
                i.putExtra("user_id", UILApplication.UserID);
                startActivity(i);
            }
        });

        imageButtonPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(relativeLayoutPhoto.getVisibility() == View.GONE) {
                    relativeLayoutPhoto.setVisibility(View.VISIBLE);
                }
                else {
                    relativeLayoutPhoto.setVisibility(View.GONE);
                }
            }
        });

        relativeLayoutPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayoutPhoto.setVisibility(View.GONE);
            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogOut();
            }
        });

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.mipmap.profile_max)
                .showImageForEmptyUri(R.mipmap.profile_max)
                .showImageOnFail(R.mipmap.profile_max)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        imageViewPhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                relativeLayoutPhoto.setVisibility(View.GONE);
                photoButtom = 1;
                mUri = generateFileUri();
                if (mUri == null) {
                    Toast.makeText(getActivity(), "SD card not available", Toast.LENGTH_LONG).show();
                    return;
                }
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

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

        imageViewGallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                relativeLayoutPhoto.setVisibility(View.GONE);
                photoButtom = 2;
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

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

        new ProfileTask().execute();

        return rootView;
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

        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            Snackbar.make(mLayout, R.string.permission_camera_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(getActivity(), PermisionLocation, RequestLocationId);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(getActivity(), PermisionLocation, RequestLocationId);
        }
    }

    private void LogOut() {
        db.delete(SQLMessager.TABLE_CHAT, null, null);
        db.delete(SQLMessager.TABLE_MESSAGER, null, null);
        db.delete(SQLMessager.TABLE_APP_ID, null, null);
        db.delete(SQLMessager.TABLE_CIRCLES_CONTACT, null, null);
        db.delete(SQLMessager.TABLE_CIRCLES, null, null);
        db.delete(SQLMessager.TABLE_CONTACTS, null, null);
        pref.logout();

        Intent i = new Intent(getActivity(), LoginActivity.class);
        startActivity(i);
        getActivity().finish();
    }

    class ProfileTask extends AsyncTask<String, String, ProfileInfo> {
        Dialog dialog;

        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(getActivity(), R.style.TransparentProgressDialog);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_progressdialog);
            dialog.show();


        }

        @SuppressWarnings("static-access")
        protected ProfileInfo doInBackground(String... params) {
            ProfileInfo result = HttpConnectRecive.getInstance().getProfile(getActivity());
            return result;
        }

        protected void onPostExecute(ProfileInfo result) {
            dialog.dismiss();
            if (result != null) {
                profileInfo = result;
                txtName.setText(result.firstname + " " + result.lastname);
                txtPhone.setText(result.phone);
                txtSummary.setText(result.summary);
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
        if (resultCode == Activity.RESULT_OK) {
            relativeLayoutPhoto.setVisibility(View.GONE);
            if (requestCode == REQUEST_CHOOSE_EXISTING) {
                if (data != null) {
                    Uri selectedImage = data.getData();
                    filepath = Utils.getPath(getActivity().getApplicationContext(), selectedImage);

                    //performCrop(selectedImage);
                    selected_bitmap = null;
                    try {
                        selected_bitmap = Utils.decodeUri(getActivity(), selectedImage, 300);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (selected_bitmap != null) {
                        Log.e("PHOTO PATCH: ", filepath + " " + selected_bitmap.toString());
                        imgProfile.setImageBitmap(selected_bitmap);
                        photo_witch = 1;
                        new EditProfileTask().execute();
                    }
                }

            }
            if (requestCode == GALLERY_KITKAT_INTENT_CALLED) {
                ParcelFileDescriptor parcelFileDescriptor;
                Uri mImageCaptureUri = data.getData();
                try {
                    final int takeFlags = data.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getActivity().getContentResolver().takePersistableUriPermission(
                            mImageCaptureUri, takeFlags);
                    /// performCrop(mImageCaptureUri);
                    parcelFileDescriptor = getActivity().getContentResolver()
                            .openFileDescriptor(mImageCaptureUri, "r");
                    FileDescriptor fileDescriptor = parcelFileDescriptor
                            .getFileDescriptor();
                    selected_bitmap = BitmapFactory
                            .decodeFileDescriptor(fileDescriptor);
                    parcelFileDescriptor.close();
                    filepath = Utils.getPath(getActivity().getApplicationContext(), mImageCaptureUri);
                    if (selected_bitmap != null) {
                        Log.e("PHOTO PATCH: ", filepath + " " + selected_bitmap.toString());
                        imgProfile.setImageBitmap(selected_bitmap);
                        photo_witch = 1;
                        new EditProfileTask().execute();
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
                    imgProfile.setImageBitmap(selected_bitmap);
                    photo_witch = 1;
                    new EditProfileTask().execute();
                }
            }


        }
    }

    class EditProfileTask extends AsyncTask<String, String, ProfileInfo> {
        Dialog dialog;

        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(getActivity(), R.style.TransparentProgressDialog);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_progressdialog);
            dialog.show();


        }

        @SuppressWarnings("static-access")
        protected ProfileInfo doInBackground(String... params) {
            profileInfo.photo = filepath;
            ProfileInfo result = HttpConnectRecive.setProfile(profileInfo, photo_witch);
            return result;
        }

        protected void onPostExecute(ProfileInfo result) {
            dialog.dismiss();

            super.onPostExecute(result);

        }
    }
}
