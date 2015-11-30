package mobi.kolibri.messager;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.SQLMessager;


public class ActivationActivity extends AppCompatActivity {
    Integer user_id;
    EditText edtKeyActivete;
    Button btnKeyActivete;
    private SQLMessager sqlMessager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);

        sqlMessager = new SQLMessager(ActivationActivity.this);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            user_id = b.getInt("user_id");
        }

        edtKeyActivete = (EditText) findViewById(R.id.edtKeyActivete);
        edtKeyActivete.setFocusableInTouchMode(true);
        edtKeyActivete.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.setBackgroundResource(R.drawable.custom_edittext_pasiv);
                } else {
                    v.setBackgroundResource(R.drawable.custom_edittext_activ);
                }
            }
        });
        btnKeyActivete = (Button) findViewById(R.id.btnKeyActivete);

        btnKeyActivete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtKeyActivete.getText().equals("")) {
                    new ActivationTask().execute();
                }
            }
        });
    }

    class ActivationTask extends AsyncTask<String, String, String> {
        Dialog dialog;
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(ActivationActivity.this, R.style.TransparentProgressDialog);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_progressdialog);
            dialog.show();


        }
        @SuppressWarnings("static-access")
        protected String doInBackground(String... params) {
            String result = HttpConnectRecive.Activation(user_id, edtKeyActivete.getText().toString());
            return result;
        }

        protected void onPostExecute(String result) {
            dialog.dismiss();
            ContentValues cv = new ContentValues();
            SQLiteDatabase db = sqlMessager.getWritableDatabase();
            cv.put(SQLMessager.APP_ID, result);
            cv.put(SQLMessager.USER_ID, user_id);
            db.insert(SQLMessager.TABLE_APP_ID, null, cv);
            Intent i = new Intent(ActivationActivity.this, DashboardActivity.class);
            i.putExtra("user_id", user_id);
            startActivity(i);
            finish();

            super.onPostExecute(result);

        }

    }
}
