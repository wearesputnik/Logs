package mobi.kolibri.messager.fragment;

import android.content.ClipData;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
        listContact.setOnItemClickListener(listOnItemClickListener);
        listCircles.setAdapter(circlesAdapter);
        listCircles.setDividerHeight(0);
        listCircles.setOnItemClickListener(listOnItemClickListener);

        sqlMessager = new SQLMessager(getActivity());

        SQLiteDatabase db = sqlMessager.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS, null);
        if (c.moveToFirst()) {
            int i = 0;
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
                adapter.add(result_sql);
                contactInfoList.add(result_sql);
                if (i < 4) {
                    i++;
                    circlesAdapter.add(result_sql);
                }
            } while (c.moveToNext());
        }

        adapter.notifyDataSetChanged();
        circlesAdapter.notifyDataSetChanged();

        resumeColor  = getResources().getColor(android.R.color.background_light);

        return rootView;
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
               /// holder.image = (ImageView) v.findViewById(R.id.imageView3);
                v.setTag(holder);
            }

            final ViewHolder holder = (ViewHolder) v.getTag();

           // holder.image.setImageResource(R.mipmap.profile_min);
            v.setOnDragListener(new ItemOnDragListener(item));
            return v;
        }

        class ViewHolder {
            ImageView image;
        }

        public List<CiclesInfo> getList(){
            return listItem;
        }

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


                    PassObject passObj = (PassObject)event.getLocalState();
                    View view = passObj.view;
                    CiclesInfo passedItem = passObj.item;
                    List<CiclesInfo> srcList = passObj.srcList;
                    ListView oldParent = (ListView)view.getParent();
                    ContactAdapter srcAdapter = (ContactAdapter)(oldParent.getAdapter());

                    ListView newParent = (ListView)v.getParent();
                    CirclesAdapter destAdapter = (CirclesAdapter)(newParent.getAdapter());
                    List<CiclesInfo> destList = destAdapter.getList();

                    int removeLocation = srcList.indexOf(passedItem);
                    int insertLocation = destList.indexOf(me);
                    Log.e("DRAG and DROP", "Item ACTION_DROP: " + me.name + " " + passedItem.name + "\n");
    /*
     * costomise circles and contacts object for drag and drop
     */
                    if(srcList != destList || removeLocation != insertLocation){
                        if(removeItemToList(srcList, passedItem)){
                            destList.add(insertLocation, passedItem);
                        }

                        srcAdapter.notifyDataSetChanged();
                        destAdapter.notifyDataSetChanged();
                    }

                    v.setBackgroundColor(resumeColor);

                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                   // Log.e("DRAG and DROP", "Item ACTION_DRAG_ENDED: " + me.name + "\n");
                default:
                    break;
            }

            return true;
        }

    }

    private boolean removeItemToList(List<CiclesInfo> l, CiclesInfo it){
        boolean result = l.remove(it);
        return result;
    }

    AdapterView.OnItemClickListener listOnItemClickListener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Toast.makeText(getActivity(),
                    ((CiclesInfo) (parent.getItemAtPosition(position))).name,
                    Toast.LENGTH_SHORT).show();
        }

    };

}
