package mobi.kolibri.messager;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import mobi.kolibri.messager.http.HttpConnectRecive;


public class SingupActivity extends AppCompatActivity {
    Button btnSingup;
    EditText edtEmail, edtPassword, edtConfPassword, edtPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup);

        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtConfPassword = (EditText) findViewById(R.id.edtConfPassword);
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        btnSingup = (Button) findViewById(R.id.btnSingup);

        btnSingup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validation()) {
                    if (edtPassword.getText().toString().trim().equals(edtConfPassword.getText().toString().trim())) {
                        if (HttpConnectRecive.isOnline(SingupActivity.this)) {
                            new SingupTask().execute();
                        }
                        else {
                            Toast.makeText(SingupActivity.this, "No Internet", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(SingupActivity.this, "Password mismatch", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private Boolean validation() {
        if (!isValidEmail(edtEmail.getText().toString())) {
            return false;
        }
        if (edtPassword.getText().toString().trim().equals("")) {
            return false;
        }
        if (edtConfPassword.getText().toString().trim().equals("")) {
            return false;
        }
        if (edtPhone.getText().toString().trim().equals("")) {
            return false;
        }
        return true;
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    class SingupTask extends AsyncTask<String, String, Integer> {
        Dialog dialog;
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(SingupActivity.this, R.style.TransparentProgressDialog);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_progressdialog);
            dialog.show();


        }
        @SuppressWarnings("static-access")
        protected Integer doInBackground(String... params) {
            Integer result = HttpConnectRecive.CreateAccaunt(edtEmail.getText().toString(), edtPassword.getText().toString(), edtPhone.getText().toString());
            return result;
        }

        protected void onPostExecute(Integer result) {
            dialog.dismiss();
            if (result != 0) {
                Intent i = new Intent(SingupActivity.this, ActivationActivity.class);
                i.putExtra("user_id", result);
                startActivity(i);
                finish();
            }
            super.onPostExecute(result);

        }

    }
}
