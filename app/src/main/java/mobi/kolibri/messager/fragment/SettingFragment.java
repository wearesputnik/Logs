package mobi.kolibri.messager.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import mobi.kolibri.messager.LoginActivity;
import mobi.kolibri.messager.R;
import mobi.kolibri.messager.activity.EditProfileActivity;
import mobi.kolibri.messager.helper.PrefManager;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.ProfileInfo;
import mobi.kolibri.messager.object.SQLMessager;

public class SettingFragment extends Fragment {
    SQLMessager sqlMessager;
    SQLiteDatabase db;
    public PrefManager pref;
    private Button btnProfileEdit;
    private Button btnLogOut;
    TextView txtName, txtPhone, txtSummary;
    private DisplayImageOptions options;
    ImageView imgProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);

        sqlMessager = new SQLMessager(getActivity());
        db = sqlMessager.getWritableDatabase();

        pref = new PrefManager(getActivity().getApplicationContext());

        btnProfileEdit = (Button) rootView.findViewById(R.id.btnProfileEdit);
        btnLogOut = (Button) rootView.findViewById(R.id.btnLogOut);
        txtName = (TextView) rootView.findViewById(R.id.txtName);
        txtPhone = (TextView) rootView.findViewById(R.id.txtPhone);
        txtSummary = (TextView) rootView.findViewById(R.id.txtSummary);
        imgProfile = (ImageView) rootView.findViewById(R.id.imgProfile);

        btnProfileEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), EditProfileActivity.class);
                i.putExtra("user_id", HttpConnectRecive.getUserId(getActivity()));
                startActivity(i);
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

        new ProfileTask().execute();

        return rootView;
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
            ProfileInfo result = HttpConnectRecive.getProfile(getActivity());
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
