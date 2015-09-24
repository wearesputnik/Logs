package mobi.kolibri.messager.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.activity.ChatItemActivity;
import mobi.kolibri.messager.activity.GroupChatItemActivity;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.ChatInfo;
import mobi.kolibri.messager.object.SQLMessager;

/**
 * Created by root on 01.09.15.
 */
public class ChatAdapter extends ArrayAdapter<ChatInfo>{
    List<ChatInfo> listItem;
    private DisplayImageOptions options;
    SQLMessager sqlMessager;
    Context contV;
    SQLiteDatabase db;

    public ChatAdapter (Context context) {
        super(context, 0);
        listItem = new ArrayList<>();
        sqlMessager = new SQLMessager(context);
        contV = context;
        db = sqlMessager.getWritableDatabase();
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
        final ChatInfo item = getItem(position);


        View v = convertView;
        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.chat_item, null);
            ViewHolder holder = new ViewHolder();
            holder.image = (ImageView) v.findViewById(R.id.imageView5);
            holder.name = (TextView) v.findViewById(R.id.textView3);
            holder.clickLayout = (LinearLayout) v.findViewById(R.id.clickLayout);
            v.setTag(holder);
        }

        final ViewHolder holder = (ViewHolder) v.getTag();

        String user_id_from = "0";

        try {
            JSONArray jsonUsers = new JSONArray(item.json_user);
            if (jsonUsers.length() == 1) {
                JSONObject json = jsonUsers.getJSONObject(0);
                user_id_from = json.getString("user_id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (item.type_chat.trim().equals("group")) {
            holder.name.setText(item.name);
            holder.image.setImageResource(R.mipmap.group_4);
            holder.clickLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(contV, GroupChatItemActivity.class);
                    i.putExtra("chat_id", item.id);
                    i.putExtra("type", item.type_chat);
                    contV.startActivity(i);
                }
            });
        }
        else {
            Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE " + SQLMessager.CONTACTS_USER_ID + "='" + user_id_from + "'", null);
            if (c.moveToFirst()) {
                int nameCollumn = c.getColumnIndex(SQLMessager.CONTACTS_NAME);
                int photoCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHOTO);

                holder.name.setText(c.getString(nameCollumn));
                if (c.getString(photoCollumn) != null) {
                    String url_img = HttpConnectRecive.URLP + c.getString(photoCollumn);
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
                    holder.image.setImageResource(R.mipmap.profile_min);
                }
            }

            final String finalUser_id_from = user_id_from;
            holder.clickLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(contV, ChatItemActivity.class);
                    i.putExtra("user_id_from", Integer.parseInt(finalUser_id_from));
                    i.putExtra("chat_id", item.id);
                    i.putExtra("type", item.type_chat);
                    contV.startActivity(i);
                }
            });
        }

        return v;
    }

    class ViewHolder {
        TextView name;
        ImageView image;
        LinearLayout clickLayout;
    }
}
