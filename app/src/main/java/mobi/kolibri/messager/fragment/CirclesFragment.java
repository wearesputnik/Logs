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
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
        listCircles.setAdapter(circlesAdapter);
        listCircles.setDividerHeight(0);

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
                contactInfoList.add(result_sql);
                if (i < 10) {
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
            ContactInfo selectedItem = (ContactInfo)(parent.getItemAtPosition(position));

            ContactAdapter associatedAdapter = (ContactAdapter)(parent.getAdapter());
            List<ContactInfo> associatedList = associatedAdapter.getList();

            PassObject passObj = new PassObject(view, selectedItem, associatedList);

            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(data, shadowBuilder, passObj, 0);

            return true;
        }

    };

    private static class MyDragShadowBuilder extends View.DragShadowBuilder {
        private static Drawable shadow;

        public MyDragShadowBuilder(View v) {
            super(v);
            shadow = new ColorDrawable(Color.LTGRAY);
        }

        @Override
        public void onProvideShadowMetrics (Point size, Point touch){
            int width = getView().getWidth();
            int height = getView().getHeight();

            shadow.setBounds(0, 0, width, height);
            size.set(width, height);
            touch.set(width / 2, height / 2);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            shadow.draw(canvas);
        }

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

            v.setOnDragListener(new ItemOnDragListener(item));

            return v;
        }

        class ViewHolder {
            TextView name;
            TextView phone;
            ImageView image;
        }

        public List<ContactInfo> getList(){
            return listItem;
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
               /// holder.image = (ImageView) v.findViewById(R.id.imageView3);
                v.setTag(holder);
            }

            final ViewHolder holder = (ViewHolder) v.getTag();

           // holder.image.setImageResource(R.mipmap.profile_min);

            return v;
        }

        class ViewHolder {
            ImageView image;
        }

    }

    class PassObject{
        View view;
        ContactInfo item;
        List<ContactInfo> srcList;

        PassObject(View v, ContactInfo i, List<ContactInfo> s){
            view = v;
            item = i;
            srcList = s;
        }
    }

    class ItemOnDragListener implements View.OnDragListener {

        ContactInfo  me;

        ItemOnDragListener(ContactInfo i){
            me = i;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                   // prompt.append("Item ACTION_DRAG_STARTED: " + "\n");
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                   // prompt.append("Item ACTION_DRAG_ENTERED: " + "\n");
                    v.setBackgroundColor(0x30000000);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                  //  prompt.append("Item ACTION_DRAG_EXITED: " + "\n");
                    v.setBackgroundColor(resumeColor);
                    break;
                case DragEvent.ACTION_DROP:
                 ///   prompt.append("Item ACTION_DROP: " + "\n");

                    PassObject passObj = (PassObject)event.getLocalState();
                    View view = passObj.view;
                    ContactInfo passedItem = passObj.item;
                    List<ContactInfo> srcList = passObj.srcList;
                    ListView oldParent = (ListView)view.getParent();
                    ContactAdapter srcAdapter = (ContactAdapter)(oldParent.getAdapter());

                    ListView newParent = (ListView)v.getParent();
                    ContactAdapter destAdapter = (ContactAdapter)(newParent.getAdapter());
                    List<ContactInfo> destList = destAdapter.getList();

                    int removeLocation = srcList.indexOf(passedItem);
                    int insertLocation = destList.indexOf(me);
    /*
     * If drag and drop on the same list, same position,
     * ignore
     */
                    if(srcList != destList || removeLocation != insertLocation){
                        /*if(removeItemToList(srcList, passedItem)){
                            destList.add(insertLocation, passedItem);
                        }*/

                        srcAdapter.notifyDataSetChanged();
                        destAdapter.notifyDataSetChanged();
                    }

                  //  v.setBackgroundColor(resumeColor);

                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                   // prompt.append("Item ACTION_DRAG_ENDED: "  + "\n");
                    v.setBackgroundColor(resumeColor);
                default:
                    break;
            }

            return true;
        }

    }

}
