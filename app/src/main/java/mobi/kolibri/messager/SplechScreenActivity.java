package mobi.kolibri.messager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import mobi.kolibri.messager.helper.ParseUtils;
import mobi.kolibri.messager.helper.PrefManager;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.SQLMessager;

public class SplechScreenActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 3000;
    private SQLMessager sqlMessager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splech_screen);
        sqlMessager = new SQLMessager(SplechScreenActivity.this);
        SQLiteDatabase db = sqlMessager.getWritableDatabase();


        final Cursor ca = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_APP_ID, null);
        if (ca.moveToFirst()) {
            int appidColIndex = ca.getColumnIndex(sqlMessager.APP_ID);
            final int useridColIndex = ca.getColumnIndex(sqlMessager.USER_ID);

            HttpConnectRecive.api_key = ca.getString(appidColIndex);

            ParseUtils.verifyParseConfiguration(this);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    Intent i = new Intent(SplechScreenActivity.this, DashboardActivity.class);
                    i.putExtra("user_id", Integer.parseInt(ca.getString(useridColIndex)));
                    startActivity(i);

                }
            }, SPLASH_TIME_OUT);

        } else {

            new Handler().postDelayed(new Runnable() {


                @Override
                public void run() {
                    Intent i = new Intent(SplechScreenActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();

                }
            }, SPLASH_TIME_OUT);
        }

    }

}
