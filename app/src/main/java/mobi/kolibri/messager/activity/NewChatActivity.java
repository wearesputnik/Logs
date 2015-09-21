package mobi.kolibri.messager.activity;

import android.content.Context;
import android.content.Intent;
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
import android.widget.LinearLayout;
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

public class NewChatActivity extends AppCompatActivity {
    ListView listContact;
    ContactAdapter adapter;
    SQLMessager sqlMessager;
    String type_chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            type_chat = b.getString("type");
        }

        listContact = (ListView) findViewById(R.id.listView2);
        adapter = new ContactAdapter(NewChatActivity.this);
        listContact.setAdapter(adapter);

        sqlMessager = new SQLMessager(NewChatActivity.this);

        SQLiteDatabase db = sqlMessager.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE " + SQLMessager.CONTACTS_SERV + "='1'", null);
        if (c.moveToFirst()) {
            int nameCollumn = c.getColumnIndex(SQLMessager.CONTACTS_NAME);
            int phoneCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHONE);
            int photoCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHOTO);
            int useridCollumn = c.getColumnIndex(SQLMessager.CONTACTS_USER_ID);
            do {
                ContactInfo result_sql = new ContactInfo();
                result_sql.name = c.getString(nameCollumn);
                result_sql.phone = c.getString(phoneCollumn);
                result_sql.photo = c.getString(photoCollumn);
                result_sql.user_id = c.getInt(useridCollumn);
                adapter.add(result_sql);
            } while (c.moveToNext());
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        finish();
        return super.onOptionsItemSelected(item);
    }

    private class ContactAdapter extends ArrayAdapter<ContactInfo> {
        List<ContactInfo> listItem;
        private DisplayImageOptions options;
        Context contV;


        public ContactAdapter (Context context) {
            super(context, 0);
            contV = context;
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
                holder.clickLayout = (LinearLayout) v.findViewById(R.id.clickLayout);
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
            holder.clickLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(contV, ChatItemActivity.class);
                    i.putExtra("user_id_from", item.user_id);
                    i.putExtra("chat_id", 0);
                    i.putExtra("type", type_chat);
                    startActivity(i);
                    finish();
                }
            });

            return v;
        }

        class ViewHolder {
            TextView name;
            TextView phone;
            ImageView image;
            LinearLayout clickLayout;
        }

    }
}
