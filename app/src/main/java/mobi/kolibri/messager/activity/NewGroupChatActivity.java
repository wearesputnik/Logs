package mobi.kolibri.messager.activity;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import mobi.kolibri.messager.object.CiclesInfo;
import mobi.kolibri.messager.object.ContactInfo;
import mobi.kolibri.messager.object.SQLMessager;

public class NewGroupChatActivity extends AppCompatActivity {
    ListView listContact;
    ContactAdapter adapter;
    CirclesAdapter adapterCircle;
    SQLMessager sqlMessager;
    String type_chat;
    Button btnPeopleChat, btnCirclesChat;
    List<ContactInfo> listGroupCont;
    List<CiclesInfo> listGroupCircl;
    private TextView txtTitleActionBar;
    private boolean people_or_circle;
    SQLiteDatabase db;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group_chat);

        people_or_circle = true;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        txtTitleActionBar = (TextView) findViewById(R.id.txtTitleActionBar);
        txtTitleActionBar.setText("New Group Chat");

        Bundle b = getIntent().getExtras();
        if (b != null) {
            type_chat = b.getString("type");
        }

        btnPeopleChat = (Button) findViewById(R.id.btnPeopleChat);
        btnCirclesChat = (Button) findViewById(R.id.btnCirclesChat);
        listContact = (ListView) findViewById(R.id.listView3);
        adapter = new ContactAdapter(NewGroupChatActivity.this);
        adapterCircle = new CirclesAdapter(NewGroupChatActivity.this);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        btnPeopleChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                people_or_circle = true;
                onRestart();
            }
        });
        btnCirclesChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                people_or_circle = false;
                onRestart();
            }
        });

        sqlMessager = new SQLMessager(NewGroupChatActivity.this);
        listGroupCont = new ArrayList<>();
        listGroupCircl = new ArrayList<>();
        db = sqlMessager.getWritableDatabase();


        if (people_or_circle) {
            fab.setVisibility(View.VISIBLE);
            listContact.setAdapter(adapter);
            btnPeopleChat.setBackgroundResource(R.drawable.custom_active_part);
            btnCirclesChat.setBackgroundResource(R.drawable.custom_buttom_part);

            Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE " + SQLMessager.CONTACTS_SERV + "='1' and " + SQLMessager.CONTACTS_USER_ID + "<>" + HttpConnectRecive.getUserId(NewGroupChatActivity.this), null);
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
        }
        else {
            fab.setVisibility(View.GONE);
            listContact.setAdapter(adapterCircle);
            btnCirclesChat.setBackgroundResource(R.drawable.custom_active_part);
            btnPeopleChat.setBackgroundResource(R.drawable.custom_buttom_part);
            adapter.clear();
            Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CIRCLES, null);
            if (c.moveToFirst()) {
                int idCollumn = c.getColumnIndex("id");
                int nameCollumn = c.getColumnIndex(SQLMessager.CIRCLES_NAME);
                do {
                    Cursor c_circl_user = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CIRCLES_CONTACT + " WHERE " + SQLMessager.CIRCLES_CONTACT_ID_CIR + "=" + c.getString(idCollumn), null);
                    if (c_circl_user.moveToFirst()) {
                        CiclesInfo result_sql = new CiclesInfo();
                        result_sql.id_circle = c.getInt(idCollumn);
                        result_sql.name_circle = c.getString(nameCollumn);
                        result_sql.check = false;
                        listGroupCircl.add(result_sql);
                    }
                } while (c.moveToNext());
            }

            for (CiclesInfo item : listGroupCircl) {
                adapterCircle.add(item);
            }

            adapterCircle.notifyDataSetChanged();
        }



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray arrayUser = new JSONArray();
                for (ContactInfo item : listGroupCont) {
                    if (item.chek_cont) {
                        JSONObject itemJs = new JSONObject();
                        try {
                            itemJs.put("user_id", item.user_id.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        arrayUser.put(itemJs);
                    }
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
    protected void onRestart() {
        super.onRestart();
        adapter.clear();
        adapterCircle.clear();
        listGroupCont.clear();
        listGroupCircl.clear();
        if (people_or_circle) {
            fab.setVisibility(View.VISIBLE);
            listContact.setAdapter(adapter);
            btnPeopleChat.setBackgroundResource(R.drawable.custom_active_part);
            btnCirclesChat.setBackgroundResource(R.drawable.custom_buttom_part);
            Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE " + SQLMessager.CONTACTS_SERV + "='1' and " + SQLMessager.CONTACTS_USER_ID + "<>" + HttpConnectRecive.getUserId(NewGroupChatActivity.this), null);
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
        }
        else {
            fab.setVisibility(View.GONE);
            listContact.setAdapter(adapterCircle);
            btnCirclesChat.setBackgroundResource(R.drawable.custom_active_part);
            btnPeopleChat.setBackgroundResource(R.drawable.custom_buttom_part);
            Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CIRCLES, null);
            if (c.moveToFirst()) {
                int idCollumn = c.getColumnIndex("id");
                int nameCollumn = c.getColumnIndex(SQLMessager.CIRCLES_NAME);
                do {
                    Cursor c_circl_user = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CIRCLES_CONTACT + " WHERE " + SQLMessager.CIRCLES_CONTACT_ID_CIR + "=" + c.getString(idCollumn), null);
                    if (c_circl_user.moveToFirst()) {
                        CiclesInfo result_sql = new CiclesInfo();
                        result_sql.id_circle = c.getInt(idCollumn);
                        result_sql.name_circle = c.getString(nameCollumn);
                        result_sql.check = false;
                        listGroupCircl.add(result_sql);
                    }
                } while (c.moveToNext());
            }

            for (CiclesInfo item : listGroupCircl) {
                adapterCircle.add(item);
            }

            adapterCircle.notifyDataSetChanged();
        }
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
                v = vi.inflate(R.layout.group_chat_contact_item, null);
                ViewHolder holder = new ViewHolder();
                holder.name = (TextView) v.findViewById(R.id.textView5);
                holder.image = (ImageView) v.findViewById(R.id.imageView2);
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
                holder.image.setImageResource(R.mipmap.profile_max);
            }

            if (item.chek_cont) {
                v.setBackgroundResource(R.color.background_chek);
            }

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!item.chek_cont) {
                        view.setBackgroundResource(R.color.background_chek);
                        item.chek_cont = true;
                        Log.e("View", "TRUE");
                    }
                    else {
                        view.setBackgroundResource(R.color.background_unchek);
                        item.chek_cont = false;
                        Log.e("View", "FALSE");
                    }
                }
            });

            return v;
        }

        class ViewHolder {
            TextView name;
            ImageView image;
        }

    }

    public class CirclesAdapter extends ArrayAdapter<CiclesInfo> {
        List<CiclesInfo> listItem;
        private DisplayImageOptions options;
        Context contV;

        public CirclesAdapter (Context context) {
            super(context, 0);
            contV = context;
            listItem = new ArrayList<CiclesInfo>();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final CiclesInfo item = getItem(position);


            View v = convertView;
            if (v == null) {
                LayoutInflater vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.group_circle_item, null);
                ViewHolder holder = new ViewHolder();
                holder.name_circle = (TextView) v.findViewById(R.id.name_circle);
                holder.name_circle_rlt = (TextView) v.findViewById(R.id.textView3);
                v.setTag(holder);
            }

            final ViewHolder holder = (ViewHolder) v.getTag();

            holder.name_circle.setVisibility(View.VISIBLE);
            holder.name_circle.setText(item.name_circle);
            holder.name_circle_rlt.setText(item.name_circle);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    JSONArray arrayUser = new JSONArray();
                    Cursor c_circl_user = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CIRCLES_CONTACT + " WHERE " + SQLMessager.CIRCLES_CONTACT_ID_CIR + "=" + item.id_circle, null);
                    if (c_circl_user.moveToFirst()) {
                        int useridCollumn = c_circl_user.getColumnIndex(SQLMessager.CIRCLES_CONTACT_ID_CONT);
                        do {
                            Cursor c_con = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE id=" + c_circl_user.getString(useridCollumn), null);
                            if (c_con.moveToFirst()) {
                                int idUserCollumn = c_con.getColumnIndex(SQLMessager.CONTACTS_USER_ID);
                                JSONObject itemJs = new JSONObject();
                                try {
                                    itemJs.put("user_id", c_con.getString(idUserCollumn));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                arrayUser.put(itemJs);
                            }
                        } while (c_circl_user.moveToNext());
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

            return v;
        }

        class ViewHolder {
            TextView name_circle_rlt;
            TextView name_circle;
        }

        public List<CiclesInfo> getList(){
            return listItem;
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
                if (!edtNameGroup.getText().toString().trim().equals("")) {
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
                else {
                    Snackbar.make(v, "Enter the name group", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });


        dialog.show();
    }
}
