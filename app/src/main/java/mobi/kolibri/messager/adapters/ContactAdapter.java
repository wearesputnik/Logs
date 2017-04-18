package mobi.kolibri.messager.adapters;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

import mobi.kolibri.messager.DashboardActivity;
import mobi.kolibri.messager.R;
import mobi.kolibri.messager.activity.ChatItemActivity;
import mobi.kolibri.messager.activity.ContactProfileActivity;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.ContactInfo;
import mobi.kolibri.messager.object.SQLMessager;

public class ContactAdapter extends ArrayAdapter<ContactInfo> {
    List<ContactInfo> listItem;
    private DisplayImageOptions options;
    Context contV;
    private Filter filter;


    public ContactAdapter (Context context, int resourceId,	List<ContactInfo> objects) {
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
            holder.send_sms = (TextView) v.findViewById(R.id.textView7);
            holder.image = (ImageView) v.findViewById(R.id.imageView5);
            holder.imgStatusUser = (ImageView) v.findViewById(R.id.imgStatusUser);
            holder.rltChatUserSelect = (RelativeLayout) v.findViewById(R.id.rltChatUserSelect);
            v.setTag(holder);
        }

        final ViewHolder holder = (ViewHolder) v.getTag();

        holder.name.setText(item.name);
        holder.phone.setText(item.phone);
        holder.send_sms.setVisibility(View.GONE);
        if (item.server.equals("0")) {
            holder.send_sms.setVisibility(View.VISIBLE);
            holder.send_sms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PendingIntent pi = PendingIntent.getActivity(contV, 0,
                            new Intent(contV, DashboardActivity.class), 0);
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(item.phone, null, "Hello install Logs messager", pi, null);
                }
            });
        }
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
        }
        else {
            holder.imgStatusUser.setVisibility(View.GONE);
        }

        if (item.server.equals("1")) {
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(contV, ContactProfileActivity.class);
                    i.putExtra("user_id", item.id_db);
                    i.putExtra("server", "1");
                    contV.startActivity(i);
                }
            });
            holder.rltChatUserSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SQLMessager sqlMessager;
                    SQLiteDatabase db;
                    sqlMessager = new SQLMessager(contV);
                    db = sqlMessager.getWritableDatabase();
                    final Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE id=" + item.id_db, null);
                    final int useridCollumn = c.getColumnIndex(SQLMessager.CONTACTS_USER_ID);
                    if (c.moveToFirst()) {
                        Cursor c_chat = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CHAT + " WHERE " + SQLMessager.CHAT_JSON_INTERLOCUTOR + "='[{\"user_id\":\"" + c.getString(useridCollumn) + "\"}]' and " + SQLMessager.CHAT_TYPE + "='regular'", null);
                        if (c_chat.moveToFirst()) {
                            int chatidCollumn = c_chat.getColumnIndex("id");
                            int typeCollumn = c_chat.getColumnIndex(SQLMessager.CHAT_TYPE);
                            Intent i = new Intent(contV, ChatItemActivity.class);
                            i.putExtra("user_id_from", Integer.parseInt(c.getString(useridCollumn)));
                            i.putExtra("chat_id", c_chat.getInt(chatidCollumn));
                            i.putExtra("type", c_chat.getString(typeCollumn));
                            contV.startActivity(i);
                        } else {
                            Intent i = new Intent(contV, ChatItemActivity.class);
                            i.putExtra("user_id_from", Integer.parseInt(c.getString(useridCollumn)));
                            i.putExtra("chat_id", 0);
                            i.putExtra("type", "regular");
                            contV.startActivity(i);
                        }
                    }
                }
            });
        }
        if (item.server.equals("2")) {
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(contV, ContactProfileActivity.class);
                    i.putExtra("user_id", item.user_id);
                    i.putExtra("server", "2");
                    contV.startActivity(i);
                }
            });
            holder.rltChatUserSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SQLMessager sqlMessager;
                    SQLiteDatabase db;
                    sqlMessager = new SQLMessager(contV);
                    db = sqlMessager.getWritableDatabase();

                    Cursor c_chat = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CHAT + " WHERE " + SQLMessager.CHAT_JSON_INTERLOCUTOR + "='[{\"user_id\":\"" + item.user_id + "\"}]' and " + SQLMessager.CHAT_TYPE + "='regular'", null);
                    if (c_chat.moveToFirst()) {
                        int chatidCollumn = c_chat.getColumnIndex("id");
                        int typeCollumn = c_chat.getColumnIndex(SQLMessager.CHAT_TYPE);
                        Intent i = new Intent(contV, ChatItemActivity.class);
                        i.putExtra("user_id_from", item.user_id);
                        i.putExtra("chat_id", c_chat.getInt(chatidCollumn));
                        i.putExtra("type", c_chat.getString(typeCollumn));
                        contV.startActivity(i);
                    } else {
                        final Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE user_id=" + item.user_id, null);
                        if (c.moveToFirst()) {
                            Intent i = new Intent(contV, ChatItemActivity.class);
                            i.putExtra("user_id_from", item.user_id);
                            i.putExtra("chat_id", 0);
                            i.putExtra("type", "regular");
                            contV.startActivity(i);
                        }
                        else {
                            ContentValues cv = new ContentValues();
                            cv.put(SQLMessager.CONTACTS_NAME, item.name);
                            cv.put(SQLMessager.CONTACTS_PHONE, item.phone);
                            cv.put(SQLMessager.CONTACTS_PHOTO, item.photo);
                            cv.put(SQLMessager.CONTACTS_USER_ID, item.user_id);
                            cv.put(SQLMessager.CONTACTS_SERV, "1");
                            long id_db = db.insert(SQLMessager.TABLE_CONTACTS, null, cv);
                            if (id_db != 0) {
                                Intent i = new Intent(contV, ChatItemActivity.class);
                                i.putExtra("user_id_from", item.user_id);
                                i.putExtra("chat_id", 0);
                                i.putExtra("type", "regular");
                                contV.startActivity(i);
                            }
                        }
                    }
                }
            });
        }
        return v;
    }

    class ViewHolder {
        TextView name;
        TextView phone;
        TextView send_sms;
        ImageView image;
        ImageView imgStatusUser;
        RelativeLayout rltChatUserSelect;
    }

    public List<ContactInfo> getList(){
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
