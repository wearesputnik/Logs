package mobi.kolibri.messager.activity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.ContactInfo;
import mobi.kolibri.messager.object.SQLMessager;

public class CircleItemActivity extends AppCompatActivity {
    Integer circle_id;
    SQLMessager sqlMessager;
    SQLiteDatabase db;
    ContactAdapter adapter;
    ListView listContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_item);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            circle_id = b.getInt("circle_id");
        }

        sqlMessager = new SQLMessager(CircleItemActivity.this);

        db = sqlMessager.getWritableDatabase();

        listContact = (ListView) findViewById(R.id.listContact);
        adapter = new ContactAdapter(CircleItemActivity.this);
        listContact.setAdapter(adapter);
        listContact.setDividerHeight(0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.back_from_chats);

        Cursor c_cir = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CIRCLES + " WHERE id=" + circle_id, null);
        if (c_cir.moveToFirst()) {
            int ideCollumn = c_cir.getColumnIndex("id");
            int nameCollumn = c_cir.getColumnIndex(SQLMessager.CIRCLES_NAME);

            getSupportActionBar().setTitle(c_cir.getString(nameCollumn));

        }

        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CIRCLES_CONTACT + " WHERE " + SQLMessager.CIRCLES_CONTACT_ID_CIR + "=" + circle_id, null);
        if (c.moveToFirst()) {
            int idcontCollumn = c.getColumnIndex(SQLMessager.CIRCLES_CONTACT_ID_CONT);
            do {
                Cursor c_con = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE id=" + c.getInt(idcontCollumn), null);
                if (c_con.moveToFirst()) {
                    int ideCollumn = c_con.getColumnIndex("id");
                    int nameCollumn = c_con.getColumnIndex(SQLMessager.CONTACTS_NAME);
                    int phoneCollumn = c_con.getColumnIndex(SQLMessager.CONTACTS_PHONE);
                    int photoCollumn = c_con.getColumnIndex(SQLMessager.CONTACTS_PHOTO);

                    ContactInfo result_sql = new ContactInfo();
                    result_sql.id_db = c_con.getInt(ideCollumn);
                    result_sql.name = c_con.getString(nameCollumn);
                    result_sql.phone = c_con.getString(phoneCollumn);
                    result_sql.photo = c_con.getString(photoCollumn);
                    adapter.add(result_sql);

                }
            } while (c.moveToNext());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_circle_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                break;
            case R.id.action_clear_all:
                CircleClearAll();
                break;
            case R.id.action_remowe:
                RemoweCircle();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void RemoweCircle() {
        db.delete(SQLMessager.TABLE_CIRCLES, "id=?", new String[]{circle_id.toString()});
        db.delete(SQLMessager.TABLE_CIRCLES_CONTACT, SQLMessager.CIRCLES_CONTACT_ID_CIR + "=?", new String[]{circle_id.toString()});
        finish();
    }

    private void CircleClearAll() {
        db.delete(SQLMessager.TABLE_CIRCLES_CONTACT, SQLMessager.CIRCLES_CONTACT_ID_CIR + "=?", new String[]{circle_id.toString()});
        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    private class ContactAdapter extends ArrayAdapter<ContactInfo> {
        List<ContactInfo> listItem;
        private DisplayImageOptions options;


        public ContactAdapter (Context context) {
            super(context, 0);
            listItem = new ArrayList<ContactInfo>();
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.mipmap.profile_min)
                    .showImageForEmptyUri(R.mipmap.profile_min)
                    .showImageOnFail(R.mipmap.profile_min)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ContactInfo item = getItem(position);


            View v = convertView;
            if (v == null) {
                LayoutInflater vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.contact_item, null);
                ViewHolder holder = new ViewHolder();
                holder.name = (TextView) v.findViewById(R.id.textView3);
                holder.phone = (TextView) v.findViewById(R.id.textView4);
                holder.image = (ImageView) v.findViewById(R.id.imageView5);
                v.setTag(holder);
            }

            final ViewHolder holder = (ViewHolder) v.getTag();

            holder.name.setText(item.name);
            holder.phone.setText(item.phone);
            if (item.photo != null) {
                String url_img = HttpConnectRecive.URLP + item.photo;
                ImageLoader.getInstance()
                        .displayImage(url_img, holder.image, options, new SimpleImageLoadingListener() {
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
            else {
                holder.image.setImageResource(R.mipmap.profile_min);
            }


            return v;
        }

        class ViewHolder {
            TextView name;
            TextView phone;
            ImageView image;
        }



    }
}
