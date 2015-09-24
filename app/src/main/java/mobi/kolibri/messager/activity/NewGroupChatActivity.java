package mobi.kolibri.messager.activity;

import android.app.Dialog;
import android.content.ContentValues;
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
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.ContactInfo;
import mobi.kolibri.messager.object.SQLMessager;

public class NewGroupChatActivity extends AppCompatActivity {
    ListView listContact;
    ContactAdapter adapter;
    SQLMessager sqlMessager;
    String type_chat;
    Button btnAddGroupChat;
    List<ContactInfo> listGroupCont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group_chat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.back_from_chats);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            type_chat = b.getString("type");
        }

        btnAddGroupChat = (Button) findViewById(R.id.btnAddGroupChat);
        listContact = (ListView) findViewById(R.id.listView3);
        adapter = new ContactAdapter(NewGroupChatActivity.this);
        listContact.setAdapter(adapter);

        sqlMessager = new SQLMessager(NewGroupChatActivity.this);
        listGroupCont = new ArrayList<>();
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
                result_sql.chek_cont = false;
                listGroupCont.add(result_sql);
            } while (c.moveToNext());
        }

        for (ContactInfo item : listGroupCont) {
            adapter.add(item);
        }

        adapter.notifyDataSetChanged();

        btnAddGroupChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray arrayUser = new JSONArray();
                for (ContactInfo item : listGroupCont) {
                    JSONObject itemJs = new JSONObject();
                    try {
                        itemJs.put("user_id", item.user_id.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    arrayUser.put(itemJs);
                }
                JSONObject itemJsUs = new JSONObject();
                try {
                    itemJsUs.put("user_id", HttpConnectRecive.getUserId(NewGroupChatActivity.this));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                arrayUser.put(itemJsUs);
                if (arrayUser.length() > 0) {
                    DialogNameGroupChat(arrayUser.toString());
                }
            }
        });

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
                v = vi.inflate(R.layout.group_chat_contact_item, null);
                ViewHolder holder = new ViewHolder();
                holder.name = (TextView) v.findViewById(R.id.textView5);
                holder.image = (ImageView) v.findViewById(R.id.imageView2);
                holder.checkBox = (CheckBox) v.findViewById(R.id.checkBox);
                v.setTag(holder);
            }

            final ViewHolder holder = (ViewHolder) v.getTag();

            holder.name.setText(item.name);
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
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.checkBox.isChecked()) {
                        item.chek_cont = true;
                    } else {
                        item.chek_cont = false;
                    }
                }
            });

            return v;
        }

        class ViewHolder {
            TextView name;
            ImageView image;
            CheckBox checkBox;
        }

    }

    private void DialogNameGroupChat(final String jsonStr) {

        final Dialog dialog = new Dialog(NewGroupChatActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_name_group_chat);
;
        final EditText edtNameGroup = (EditText) dialog.findViewById(R.id.edtNameGroup);
        Button btnSaveGroupName = (Button) dialog.findViewById(R.id.btnSaveGroupName);

        final ContentValues cv = new ContentValues();
        final SQLiteDatabase db = sqlMessager.getWritableDatabase();

        btnSaveGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long chat_id_db = 0;
                cv.put(SQLMessager.CHAT_TYPE, type_chat);
                cv.put(SQLMessager.CHAT_NAME, edtNameGroup.getText().toString());
                cv.put(SQLMessager.CHAT_JSON_INTERLOCUTOR, jsonStr);
                cv.put(SQLMessager.CHAT_READ, "1");
                chat_id_db = db.insert(SQLMessager.TABLE_CHAT, null, cv);
                if (chat_id_db > 0) {
                    Intent i = new Intent(NewGroupChatActivity.this, GroupChatItemActivity.class);
                    i.putExtra("chat_id", Integer.parseInt(chat_id_db + ""));
                    startActivity(i);
                    dialog.dismiss();
                    finish();
                }
            }
        });


        dialog.show();
    }
}
