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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
    ProfileInfo item_result;
    private Integer photo_witch;
    private DisplayImageOptions options;
    FloatingActionButton fab;

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

        fab = (FloatingActionButton) findViewById(R.id.fab);
        edtEditFirstname = (EditText) findViewById(R.id.edtEditFirstname);
        edtEditLastname = (EditText) findViewById(R.id.edtEditLastname);
        edtEditPhone = (EditText) findViewById(R.id.edtEditPhone);
        edtEditSummary = (EditText) findViewById(R.id.edtEditSummary);


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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item_result.lastname = edtEditLastname.getText().toString();
                item_result.firstname = edtEditFirstname.getText().toString();
                item_result.phone = edtEditPhone.getText().toString();
                item_result.summary = edtEditSummary.getText().toString();

                new EditProfileTask().execute();
            }
        });
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
            ProfileInfo result = HttpConnectRecive.getProfile(EditProfileActivity.this);
            return result;
        }

        protected void onPostExecute(ProfileInfo result) {
            dialog.dismiss();
            if (result != null) {
                edtEditFirstname.setText(result.firstname);
                edtEditLastname.setText(result.lastname);
                edtEditPhone.setText(result.phone);
                edtEditSummary.setText(result.summary);
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
            ProfileInfo result = HttpConnectRecive.getInstance().setProfile(EditProfileActivity.this, user_id, item_result, photo_witch);
            return result;
        }

        protected void onPostExecute(ProfileInfo result) {
            dialog.dismiss();
            super.onPostExecute(result);
        }
    }
}
