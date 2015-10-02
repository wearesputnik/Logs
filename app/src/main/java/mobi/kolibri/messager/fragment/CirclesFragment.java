package mobi.kolibri.messager.fragment;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.activity.CircleItemActivity;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.CiclesInfo;
import mobi.kolibri.messager.object.ContactInfo;
import mobi.kolibri.messager.object.SQLMessager;

public class CirclesFragment extends Fragment {
    List<CiclesInfo> contactInfoList;
    List<CiclesInfo> contactInfoListCircle;
    ListView listContact;
    ListView listCircles;
    ContactAdapter adapter;
    CirclesAdapter circlesAdapter;
    SQLMessager sqlMessager;
    int resumeColor;
    private String[] circle_name = {
            "Family",
            "Friends",
            "Work",
            "Hobby"
    };
    SQLiteDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_circles, container, false);

        contactInfoList = new ArrayList<>();
        listContact = (ListView) rootView.findViewById(R.id.listContact);
        listCircles = (ListView) rootView.findViewById(R.id.listCircles);
        adapter = new ContactAdapter(getActivity());
        circlesAdapter = new CirclesAdapter(getActivity());
        listContact.setAdapter(adapter);
        listContact.setDividerHeight(0);
        listContact.setOnItemLongClickListener(myOnItemLongClickListener);
        listCircles.setAdapter(circlesAdapter);
        listCircles.setDividerHeight(0);
        listCircles.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        sqlMessager = new SQLMessager(getActivity());

        db = sqlMessager.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS, null);
        if (c.moveToFirst()) {
            int ideCollumn = c.getColumnIndex("id");
            int nameCollumn = c.getColumnIndex(SQLMessager.CONTACTS_NAME);
            int phoneCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHONE);
            int photoCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHOTO);
            do {
                CiclesInfo result_sql = new CiclesInfo();
                result_sql.id_db = c.getInt(ideCollumn);
                result_sql.name = c.getString(nameCollumn);
                result_sql.phone = c.getString(phoneCollumn);
                result_sql.photo = c.getString(photoCollumn);
                result_sql.type_circle = "contact";
                adapter.add(result_sql);
                contactInfoList.add(result_sql);
            } while (c.moveToNext());
        }

        Cursor c_cir = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CIRCLES, null);
        if (c_cir.moveToFirst()) {
            int ideCollumn = c_cir.getColumnIndex("id");
            int nameCollumn = c_cir.getColumnIndex(SQLMessager.CIRCLES_NAME);
            do {
                CiclesInfo result_sql = new CiclesInfo();
                result_sql.id_circle = c_cir.getInt(ideCollumn);
                result_sql.name_circle = c_cir.getString(nameCollumn);
                result_sql.type_circle = "circle";
                circlesAdapter.add(result_sql);
            } while (c_cir.moveToNext());
        }
        else {
            for (int i = 0; i < circle_name.length; i++) {
                ContentValues cv = new ContentValues();
                cv.put(SQLMessager.CIRCLES_NAME, circle_name[i]);
                long id = db.insert(SQLMessager.TABLE_CIRCLES, null, cv);
                CiclesInfo result_sql = new CiclesInfo();
                result_sql.id_circle = (int) id;
                result_sql.name_circle = circle_name[i];
                result_sql.type_circle = "circle";
                circlesAdapter.add(result_sql);
            }
        }

        CiclesInfo result_sql = new CiclesInfo();
        result_sql.id_circle = 0;
        result_sql.name_circle = "add";
        result_sql.type_circle = "circle_add";
        circlesAdapter.add(result_sql);

        adapter.notifyDataSetChanged();
        circlesAdapter.notifyDataSetChanged();

        resumeColor  = getResources().getColor(android.R.color.background_light);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        circlesAdapter.clear();

        Cursor c_cir = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CIRCLES, null);
        if (c_cir.moveToFirst()) {
            int ideCollumn = c_cir.getColumnIndex("id");
            int nameCollumn = c_cir.getColumnIndex(SQLMessager.CIRCLES_NAME);
            do {
                CiclesInfo result_sql = new CiclesInfo();
                result_sql.id_circle = c_cir.getInt(ideCollumn);
                result_sql.name_circle = c_cir.getString(nameCollumn);
                result_sql.type_circle = "circle";
                circlesAdapter.add(result_sql);
            } while (c_cir.moveToNext());
        }

        CiclesInfo result_sql = new CiclesInfo();
        result_sql.id_circle = 0;
        result_sql.name_circle = "add";
        result_sql.type_circle = "circle_add";
        circlesAdapter.add(result_sql);

        circlesAdapter.notifyDataSetChanged();
    }

    AdapterView.OnItemLongClickListener myOnItemLongClickListener = new AdapterView.OnItemLongClickListener(){

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            CiclesInfo selectedItem = (CiclesInfo)(parent.getItemAtPosition(position));

            ContactAdapter associatedAdapter = (ContactAdapter)(parent.getAdapter());
            List<CiclesInfo> associatedList = associatedAdapter.getList();

            PassObject passObj = new PassObject(view, selectedItem, associatedList);

            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(data, shadowBuilder, passObj, 0);

            return true;
        }

    };

    private class ContactAdapter extends ArrayAdapter<CiclesInfo> {
        List<CiclesInfo> listItem;
        private DisplayImageOptions options;


        public ContactAdapter (Context context) {
            super(context, 0);
            listItem = new ArrayList<CiclesInfo>();
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
            final CiclesInfo item = getItem(position);


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

            v.setOnDragListener(new ItemOnDragListener(item));

            return v;
        }

        class ViewHolder {
            TextView name;
            TextView phone;
            ImageView image;
        }

        public List<CiclesInfo> getList(){
            return listItem;
        }

    }

    private class CirclesAdapter extends ArrayAdapter<CiclesInfo> {
        List<CiclesInfo> listItem;
        private DisplayImageOptions options;


        public CirclesAdapter (Context context) {
            super(context, 0);
            listItem = new ArrayList<CiclesInfo>();
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
            final CiclesInfo item = getItem(position);


            View v = convertView;
            if (v == null) {
                LayoutInflater vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.circle_item, null);
                ViewHolder holder = new ViewHolder();
                holder.image = (ImageView) v.findViewById(R.id.imageView3);
                holder.name_circle = (TextView) v.findViewById(R.id.name_circle);
                v.setTag(holder);
            }

            final ViewHolder holder = (ViewHolder) v.getTag();
            if (item.type_circle.equals("circle_add")) {
                holder.image.setVisibility(View.VISIBLE);
                holder.name_circle.setVisibility(View.INVISIBLE);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogAddCircle();
                    }
                });
            }
            else {
                holder.image.setVisibility(View.INVISIBLE);
                holder.name_circle.setVisibility(View.VISIBLE);
                holder.name_circle.setText(item.name_circle);
                v.setOnDragListener(new ItemOnDragListener(item));
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), CircleItemActivity.class);
                        i.putExtra("circle_id", item.id_circle);
                        startActivity(i);
                    }
                });
            }
            return v;
        }

        class ViewHolder {
            ImageView image;
            TextView name_circle;
        }

        public List<CiclesInfo> getList(){
            return listItem;
        }

    }

    public void DialogAddCircle() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_circle);
        final EditText edtCircleName = (EditText) dialog.findViewById(R.id.edtCircleName);
        Button btnAddCircle = (Button) dialog.findViewById(R.id.btnAddCircle);


        btnAddCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues cv = new ContentValues();
                cv.put(SQLMessager.CIRCLES_NAME, edtCircleName.getText().toString());
                db.insert(SQLMessager.TABLE_CIRCLES, null, cv);
                CircleUpdate();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void CircleUpdate() {
        circlesAdapter.clear();

        Cursor c_cir = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CIRCLES, null);
        if (c_cir.moveToFirst()) {
            int ideCollumn = c_cir.getColumnIndex("id");
            int nameCollumn = c_cir.getColumnIndex(SQLMessager.CIRCLES_NAME);
            do {
                CiclesInfo result_sql = new CiclesInfo();
                result_sql.id_circle = c_cir.getInt(ideCollumn);
                result_sql.name_circle = c_cir.getString(nameCollumn);
                result_sql.type_circle = "circle";
                circlesAdapter.add(result_sql);
            } while (c_cir.moveToNext());
        }

        CiclesInfo result_sql = new CiclesInfo();
        result_sql.id_circle = 0;
        result_sql.name_circle = "add";
        result_sql.type_circle = "circle_add";
        circlesAdapter.add(result_sql);

        circlesAdapter.notifyDataSetChanged();
    }

    class PassObject{
        View view;
        CiclesInfo item;
        List<CiclesInfo> srcList;

        PassObject(View v, CiclesInfo i, List<CiclesInfo> s){
            view = v;
            item = i;
            srcList = s;
        }
    }

    class ItemOnDragListener implements View.OnDragListener {

        CiclesInfo  me;

        ItemOnDragListener(CiclesInfo i){
            me = i;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    //Log.e("DRAG and DROP", "Item ACTION_DRAG_STARTED: " + me.name + "\n");
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                   // Log.e("DRAG and DROP", "Item ACTION_DRAG_ENTERED: " + me.name + "\n");
                    //v.setBackgroundColor(0x30000000);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                  //  Log.e("DRAG and DROP", "Item ACTION_DRAG_EXITED: " + me.name + "\n");
                   // v.setBackgroundColor(resumeColor);
                    break;
                case DragEvent.ACTION_DROP:

                    if (me.type_circle.equals("circle")) {
                        PassObject passObj = (PassObject) event.getLocalState();
                        View view = passObj.view;
                        CiclesInfo passedItem = passObj.item;
                        List<CiclesInfo> srcList = passObj.srcList;
                        ListView oldParent = (ListView) view.getParent();
                        ContactAdapter srcAdapter = (ContactAdapter) (oldParent.getAdapter());

                        ListView newParent = (ListView) v.getParent();
                        CirclesAdapter destAdapter = (CirclesAdapter) (newParent.getAdapter());
                        List<CiclesInfo> destList = destAdapter.getList();

                        int removeLocation = srcList.indexOf(passedItem);
                        int insertLocation = destList.indexOf(me);
                        Log.e("DRAG and DROP", "Item ACTION_DROP: " + me.name + " " + passedItem.name + "\n");
                        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CIRCLES_CONTACT + " WHERE " + SQLMessager.CIRCLES_CONTACT_ID_CIR + "=" + me.id_circle + " and " + SQLMessager.CIRCLES_CONTACT_ID_CONT + "=" + passedItem.id_db, null);
                        if (!c.moveToFirst()) {
                            ContentValues cv = new ContentValues();
                            cv.put(SQLMessager.CIRCLES_CONTACT_ID_CIR, me.id_circle);
                            cv.put(SQLMessager.CIRCLES_CONTACT_ID_CONT, passedItem.id_db);
                            db.insert(SQLMessager.TABLE_CIRCLES_CONTACT, null, cv);
                        }
    /*
     * costomise circles and contacts object for drag and drop
     */
                    }

                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                   // Log.e("DRAG and DROP", "Item ACTION_DRAG_ENDED: " + me.name + "\n");
                default:
                    break;
            }

            return true;
        }

    }


}
