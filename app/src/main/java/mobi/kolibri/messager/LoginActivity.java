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
import android.widget.TextView;
import android.widget.Toast;

import mobi.kolibri.messager.helper.ParseUtils;
import mobi.kolibri.messager.helper.PrefManager;
import mobi.kolibri.messager.http.HttpConnectRecive;


public class LoginActivity extends AppCompatActivity {
    TextView singup;
    Button btnLogin;
    EditText edtLoginEmail, edtLoginPass;
    private PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        singup = (TextView) findViewById(R.id.singup);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        edtLoginEmail = (EditText) findViewById(R.id.edtLoginEmail);
        edtLoginPass = (EditText) findViewById(R.id.edtLoginPass);

        ParseUtils.verifyParseConfiguration(this);

        pref = new PrefManager(getApplicationContext());


        singup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, SingupActivity.class);
                startActivity(i);
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validation()) {
                    if (HttpConnectRecive.isOnline(LoginActivity.this)) {
                        new LoginTask().execute();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "No Internet", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private Boolean validation() {
        if (!isValidEmail(edtLoginEmail.getText())) {
            return false;
        }
        if (edtLoginPass.getText().equals("")) {
            return false;
        }
        return true;
    }

    class LoginTask extends AsyncTask<String, String, String> {
        Dialog dialog;
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(LoginActivity.this, R.style.TransparentProgressDialog);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_progressdialog);
            dialog.show();


        }
        @SuppressWarnings("static-access")
        protected String doInBackground(String... params) {
            String result = HttpConnectRecive.Login(edtLoginEmail.getText().toString(), edtLoginPass.getText().toString(), LoginActivity.this);
            return result;
        }

        protected void onPostExecute(String result) {
            dialog.dismiss();

            pref.createLoginSession(edtLoginEmail.getText().toString());

            Intent i = new Intent(LoginActivity.this, DashboardActivity.class);
            i.putExtra("user_id", Integer.parseInt(result));
            startActivity(i);
            finish();

            super.onPostExecute(result);

        }

    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
