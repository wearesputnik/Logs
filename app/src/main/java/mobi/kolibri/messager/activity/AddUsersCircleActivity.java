package mobi.kolibri.messager.activity;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.ContactInfo;
import mobi.kolibri.messager.object.SQLMessager;


public class AddUsersCircleActivity extends AppCompatActivity {
    Integer circle_id;
    SQLMessager sqlMessager;
    SQLiteDatabase db;
    List<ContactInfo> contactInfoList;
    ListView listContact;
    ContactAdapter adapter;
    private TextView txtTitleActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_users_circle);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        txtTitleActionBar = (TextView) findViewById(R.id.txtTitleActionBar);
        txtTitleActionBar.setText("Add Circle Users");
        listContact = (ListView) findViewById(R.id.listContact);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            circle_id = b.getInt("circle_id");
        }

        contactInfoList = new ArrayList<>();

        sqlMessager = new SQLMessager(AddUsersCircleActivity.this);

        db = sqlMessager.getWritableDatabase();
        Cursor c_serv = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE server='1' and " + SQLMessager.CONTACTS_USER_ID + "<>" + HttpConnectRecive.getUserId(AddUsersCircleActivity.this), null);
        if (c_serv.moveToFirst()) {
            int ideCollumn = c_serv.getColumnIndex("id");
            int nameCollumn = c_serv.getColumnIndex(SQLMessager.CONTACTS_NAME);
            int phoneCollumn = c_serv.getColumnIndex(SQLMessager.CONTACTS_PHONE);
            int photoCollumn = c_serv.getColumnIndex(SQLMessager.CONTACTS_PHOTO);
            int serverCollumn = c_serv.getColumnIndex(SQLMessager.CONTACTS_SERV);
            int statusCollumn = c_serv.getColumnIndex(SQLMessager.CONTACTS_STATUS);
            do {
                ContactInfo result_sql = new ContactInfo();
                result_sql.id_db = c_serv.getInt(ideCollumn);
                result_sql.name = c_serv.getString(nameCollumn);
                result_sql.phone = c_serv.getString(phoneCollumn);
                result_sql.photo = c_serv.getString(photoCollumn);
                result_sql.server = c_serv.getString(serverCollumn);
                result_sql.status = c_serv.getString(statusCollumn);
                result_sql.chek_cont = false;
                contactInfoList.add(result_sql);
            } while (c_serv.moveToNext());
        }
        Collections.sort(contactInfoList);

        adapter = new ContactAdapter(AddUsersCircleActivity.this, 0, contactInfoList);
        listContact.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean add_user_circle_flag = false;
                for (ContactInfo item : contactInfoList) {
                    if (item.chek_cont) {
                        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CIRCLES_CONTACT + " WHERE " + SQLMessager.CIRCLES_CONTACT_ID_CIR + "=" + circle_id + " and " + SQLMessager.CIRCLES_CONTACT_ID_CONT + "=" + item.id_db, null);
                        if (!c.moveToFirst()) {
                            ContentValues cv = new ContentValues();
                            cv.put(SQLMessager.CIRCLES_CONTACT_ID_CIR, circle_id);
                            cv.put(SQLMessager.CIRCLES_CONTACT_ID_CONT, item.id_db);
                            db.insert(SQLMessager.TABLE_CIRCLES_CONTACT, null, cv);
                            add_user_circle_flag = true;
                        }
                    }
                }
                if (add_user_circle_flag) {
                    onBackPressed();
                    finish();
                }
                else {
                    Snackbar.make(view, "Select users", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public class ContactAdapter extends ArrayAdapter<ContactInfo> {
        List<ContactInfo> listItem;
        private DisplayImageOptions options;
        Context contV;
        private Filter filter;


        public ContactAdapter(Context context, int resourceId, List<ContactInfo> objects) {
            super(context, resourceId, objects);
            contV = context;
            listItem = objects;
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.mipmap.profile_max)
                    .showImageForEmptyUri(R.mipmap.profile_max)
                    .showImageOnFail(R.mipmap.profile_max)
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
                holder.imgStatusUser = (ImageView) v.findViewById(R.id.imgStatusUser);
                holder.rltChatUserSelect = (RelativeLayout) v.findViewById(R.id.rltChatUserSelect);
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
            } else {
                holder.image.setImageResource(R.mipmap.profile_max);
            }

            if (item.status.toString().trim().equals("1")) {
                holder.imgStatusUser.setVisibility(View.VISIBLE);
                holder.imgStatusUser.setImageResource(R.mipmap.online);
            } else {
                holder.imgStatusUser.setVisibility(View.GONE);
            }

            if (item.chek_cont) {
                holder.clickLayout.setBackgroundResource(R.color.background_chek);
            }

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!item.chek_cont) {
                        holder.clickLayout.setBackgroundResource(R.color.background_chek);
                        item.chek_cont = true;
                        Log.e("View", "TRUE");
                    }
                    else {
                        holder.clickLayout.setBackgroundResource(R.color.background_unchek);
                        item.chek_cont = false;
                        Log.e("View", "FALSE");
                    }
                }
            });


            return v;
        }

        class ViewHolder {
            TextView name;
            TextView phone;
            ImageView image;
            ImageView imgStatusUser;
            RelativeLayout rltChatUserSelect;
            LinearLayout clickLayout;
        }

        public List<ContactInfo> getList() {
            return listItem;
        }

        @Override
        public Filter getFilter() {
            if (filter == null)
                filter = new AppFilter<ContactInfo>(listItem);
            return filter;
        }

        private class AppFilter<T extends ContactInfo> extends Filter {

            private ArrayList<T> sourceObjects;

            public AppFilter(List<T> objects) {
                sourceObjects = new ArrayList<T>();
                synchronized (this) {
                    sourceObjects.addAll(objects);
                }
            }

            @Override
            protected FilterResults performFiltering(CharSequence chars) {
                String filterSeq = chars.toString().toLowerCase();
                FilterResults result = new FilterResults();
                if (filterSeq != null && filterSeq.length() > 0) {
                    ArrayList<T> filter = new ArrayList<T>();

                    for (T object : sourceObjects) {
                        // the filtering itself:
                        if (object.name.toString().toLowerCase().contains(filterSeq))
                            filter.add(object);
                    }
                    result.count = filter.size();
                    result.values = filter;
                } else {
                    // add all objects
                    synchronized (this) {
                        result.values = sourceObjects;
                        result.count = sourceObjects.size();
                    }
                }
                return result;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                // NOTE: this function is *always* called from the UI thread.
                ArrayList<T> filtered = (ArrayList<T>) results.values;
                notifyDataSetChanged();
                clear();
                for (int i = 0, l = filtered.size(); i < l; i++)
                    add((ContactInfo) filtered.get(i));
                notifyDataSetInvalidated();
            }
        }
    }
}
