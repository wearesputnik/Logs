package mobi.kolibri.messager.fragment;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.ArrayList;
import java.util.List;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.activity.CircleItemActivity;
import mobi.kolibri.messager.object.CiclesInfo;
import mobi.kolibri.messager.object.SQLMessager;

public class CirclesFragment extends Fragment {
    GridView listCircles;
    CirclesAdapter circlesAdapter;
    List<CiclesInfo> ciclesInfoList;
    SQLMessager sqlMessager;
    private String[] circle_name = {
            "Family",
            "Friends",
            "Work",
            "Hobby"
    };
    SQLiteDatabase db;
    private boolean circle_delete;
    FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_circles, container, false);


        listCircles = (GridView) rootView.findViewById(R.id.listCircles);
        circlesAdapter = new CirclesAdapter(getActivity());

        listCircles.setAdapter(circlesAdapter);
        sqlMessager = new SQLMessager(getActivity());
        ciclesInfoList = new ArrayList<>();

        db = sqlMessager.getWritableDatabase();
        circle_delete = false;
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        Cursor c_cir = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CIRCLES, null);
        if (c_cir.moveToFirst()) {
            int ideCollumn = c_cir.getColumnIndex("id");
            int nameCollumn = c_cir.getColumnIndex(SQLMessager.CIRCLES_NAME);
            do {
                CiclesInfo result_sql = new CiclesInfo();
                result_sql.id_circle = c_cir.getInt(ideCollumn);
                result_sql.name_circle = c_cir.getString(nameCollumn);
                result_sql.type_circle = "circle";
                ciclesInfoList.add(result_sql);
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
                ciclesInfoList.add(result_sql);
            }
        }

        CiclesInfo result_sql = new CiclesInfo();
        result_sql.id_circle = 0;
        result_sql.name_circle = "add";
        result_sql.type_circle = "circle_add";
        ciclesInfoList.add(result_sql);

        for (CiclesInfo item : ciclesInfoList) {
            circlesAdapter.add(item);
        }

        circlesAdapter.notifyDataSetChanged();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (circle_delete) {
                    for (CiclesInfo item : ciclesInfoList) {
                        if (item.type_circle.equals("circle")) {
                            if (item.check) {
                                db.delete(SQLMessager.TABLE_CIRCLES, "id=?", new String[]{item.id_circle.toString()});
                                db.delete(SQLMessager.TABLE_CIRCLES_CONTACT, SQLMessager.CIRCLES_CONTACT_ID_CIR + "=?", new String[]{item.id_circle.toString()});
                            }
                        }
                    }
                    circle_delete = false;
                    onResume();
                }
                else {
                    circle_delete = true;
                    onResume();
                    Snackbar.make(view, "Select circle for delete", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        circlesAdapter.clear();
        ciclesInfoList.clear();

        Cursor c_cir = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CIRCLES, null);
        if (c_cir.moveToFirst()) {
            int ideCollumn = c_cir.getColumnIndex("id");
            int nameCollumn = c_cir.getColumnIndex(SQLMessager.CIRCLES_NAME);
            do {
                CiclesInfo result_sql = new CiclesInfo();
                result_sql.id_circle = c_cir.getInt(ideCollumn);
                result_sql.name_circle = c_cir.getString(nameCollumn);
                result_sql.type_circle = "circle";
                result_sql.check = false;
                ciclesInfoList.add(result_sql);
            } while (c_cir.moveToNext());
        }

        CiclesInfo result_sql = new CiclesInfo();
        result_sql.id_circle = 0;
        result_sql.name_circle = "add";
        result_sql.type_circle = "circle_add";
        result_sql.check = false;
        ciclesInfoList.add(result_sql);

        for (CiclesInfo item : ciclesInfoList) {
            circlesAdapter.add(item);
        }

        circlesAdapter.notifyDataSetChanged();
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
                Cursor c_cir = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CIRCLES, null);
                boolean find_name = false;
                if (c_cir.moveToFirst()) {
                    int nameCollumn = c_cir.getColumnIndex(SQLMessager.CIRCLES_NAME);
                    do {
                        if (edtCircleName.getText().toString().equalsIgnoreCase(c_cir.getString(nameCollumn))) {
                            find_name = true;
                        }
                    } while (c_cir.moveToNext());
                }
                if (!find_name) {
                    ContentValues cv = new ContentValues();
                    cv.put(SQLMessager.CIRCLES_NAME, edtCircleName.getText().toString());
                    db.insert(SQLMessager.TABLE_CIRCLES, null, cv);
                    onResume();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getActivity(), "This circle already exists", Toast.LENGTH_LONG).show();
                    edtCircleName.setText("");
                }
            }
        });

        dialog.show();
    }

    public class CirclesAdapter extends ArrayAdapter<CiclesInfo> {
        List<CiclesInfo> listItem;
        Context contV;

        public CirclesAdapter (Context context) {
            super(context, 0);
            contV = context;
            listItem = new ArrayList<>();
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
                holder.relativeLayout = (RelativeLayout) v.findViewById(R.id.relativeLayout);
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
                if (!circle_delete) {
                    holder.relativeLayout.setBackgroundResource(R.drawable.circle_baground);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(contV, CircleItemActivity.class);
                            i.putExtra("circle_id", item.id_circle);
                            contV.startActivity(i);
                        }
                    });
                }
                else {
                    if (item.check) {
                        holder.relativeLayout.setBackgroundResource(R.drawable.select_circle_baground);
                    }
                    else {
                        holder.relativeLayout.setBackgroundResource(R.drawable.circle_baground);
                    }
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!item.check) {
                                holder.relativeLayout.setBackgroundResource(R.drawable.select_circle_baground);
                                item.check = true;
                            }
                            else {
                                holder.relativeLayout.setBackgroundResource(R.drawable.circle_baground);
                                item.check = false;
                            }
                        }
                    });
                }
            }
            return v;
        }

        class ViewHolder {
            ImageView image;
            TextView name_circle;
            RelativeLayout relativeLayout;
        }

        public List<CiclesInfo> getList(){
            return listItem;
        }
    }

}
