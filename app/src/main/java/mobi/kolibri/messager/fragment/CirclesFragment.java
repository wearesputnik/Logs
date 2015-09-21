package mobi.kolibri.messager.fragment;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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

public class CirclesFragment extends Fragment {
    List<ContactInfo> contactInfoList;
    List<ContactInfo> contactInfoListCircle;
    ListView listContact;
    ListView listCircles;
    ContactAdapter adapter;
    CirclesAdapter circlesAdapter;
    SQLMessager sqlMessager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_circles, container, false);

        contactInfoList = new ArrayList<>();
        listContact = (ListView) rootView.findViewById(R.id.listContact);
        listCircles = (ListView) rootView.findViewById(R.id.listCircles);
        adapter = new ContactAdapter(getActivity());
        circlesAdapter = new CirclesAdapter(getActivity());
        listContact.setAdapter(adapter);
        listCircles.setAdapter(circlesAdapter);

        sqlMessager = new SQLMessager(getActivity());

        SQLiteDatabase db = sqlMessager.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS, null);
        if (c.moveToFirst()) {
            int i = 0;
            int nameCollumn = c.getColumnIndex(SQLMessager.CONTACTS_NAME);
            int phoneCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHONE);
            int photoCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHOTO);
            do {
                ContactInfo result_sql = new ContactInfo();
                result_sql.name = c.getString(nameCollumn);
                result_sql.phone = c.getString(phoneCollumn);
                result_sql.photo = c.getString(photoCollumn);
                adapter.add(result_sql);
                if (i < 10) {
                    i++;
                    circlesAdapter.add(result_sql);
                }
            } while (c.moveToNext());
        }

        adapter.notifyDataSetChanged();
        circlesAdapter.notifyDataSetChanged();

        return rootView;
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

    private class CirclesAdapter extends ArrayAdapter<ContactInfo> {
        List<ContactInfo> listItem;
        private DisplayImageOptions options;


        public CirclesAdapter (Context context) {
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
                v = vi.inflate(R.layout.circle_item, null);
                ViewHolder holder = new ViewHolder();
                holder.image = (ImageView) v.findViewById(R.id.imageView3);
                v.setTag(holder);
            }

            final ViewHolder holder = (ViewHolder) v.getTag();

            holder.image.setImageResource(R.mipmap.profile_min);

            return v;
        }

        class ViewHolder {
            ImageView image;
        }

    }
}
