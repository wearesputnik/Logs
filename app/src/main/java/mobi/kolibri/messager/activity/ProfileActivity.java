package mobi.kolibri.messager.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import mobi.kolibri.messager.DashboardActivity;
import mobi.kolibri.messager.R;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.ProfileInfo;

public class ProfileActivity extends AppCompatActivity {
    Integer user_id;
    TextView txtName, txtPhone, txtSummary;
    private DisplayImageOptions options;
    ImageView imgProfile;
    TextView txtTitleActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            user_id = b.getInt("user_id");
        }

        txtName = (TextView) findViewById(R.id.txtName);
        txtPhone = (TextView) findViewById(R.id.txtPhone);
        txtSummary = (TextView) findViewById(R.id.txtSummary);
        imgProfile = (ImageView) findViewById(R.id.imgProfile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        txtTitleActionBar = (TextView) findViewById(R.id.txtTitleActionBar);
        txtTitleActionBar.setText("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


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
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new ProfileTask().execute();
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
            dialog = new Dialog(ProfileActivity.this, R.style.TransparentProgressDialog);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_progressdialog);
            dialog.show();


        }

        @SuppressWarnings("static-access")
        protected ProfileInfo doInBackground(String... params) {
            ProfileInfo result = HttpConnectRecive.getProfile(ProfileActivity.this);
            return result;
        }

        protected void onPostExecute(ProfileInfo result) {
            dialog.dismiss();
            if (result != null) {
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
}
