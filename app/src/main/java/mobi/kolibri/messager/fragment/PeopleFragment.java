package mobi.kolibri.messager.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.UILApplication;
import mobi.kolibri.messager.activity.ChatItemActivity;
import mobi.kolibri.messager.adapters.ContactAdapter;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.ContactInfo;
import mobi.kolibri.messager.object.ProfileInfo;
import mobi.kolibri.messager.object.SQLMessager;


public class PeopleFragment extends Fragment {
    List<ContactInfo> contactInfoList;
    List<ContactInfo> contactInfoListAll;
    ListView listContact;
    EditText searchContact = null;
    ContactAdapter adapter = null;
    SQLMessager sqlMessager;
    Button buttonSearch;
    SQLiteDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_people, container, false);

        contactInfoList = new ArrayList<>();
        contactInfoListAll = new ArrayList<>();
        listContact = (ListView) rootView.findViewById(R.id.listContact);
        searchContact = (EditText) rootView.findViewById(R.id.searchContact);
        buttonSearch = (Button) rootView.findViewById(R.id.buttonSearch);
        //searchContact.addTextChangedListener(filterTextWatcher);
        listContact.setDividerHeight(0);

        sqlMessager = new SQLMessager(getActivity());

        db = sqlMessager.getWritableDatabase();
        Cursor c_serv = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE server='1' and " + SQLMessager.CONTACTS_USER_ID + "<>" + UILApplication.UserID, null);
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
                contactInfoList.add(result_sql);
            } while (c_serv.moveToNext());
        }
        Collections.sort(contactInfoList);
        for(ContactInfo itemS : contactInfoList) {
            contactInfoListAll.add(itemS);
        }
        contactInfoList.clear();

        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE server='0'", null);
        if (c.moveToFirst()) {
            int ideCollumn = c.getColumnIndex("id");
            int nameCollumn = c.getColumnIndex(SQLMessager.CONTACTS_NAME);
            int phoneCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHONE);
            int photoCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHOTO);
            int useridCollumn = c.getColumnIndex(SQLMessager.CONTACTS_USER_ID);
            int serverCollumn = c.getColumnIndex(SQLMessager.CONTACTS_SERV);
            do {
                ContactInfo result_sql = new ContactInfo();
                result_sql.id_db = c.getInt(ideCollumn);
                result_sql.name = c.getString(nameCollumn);
                result_sql.phone = c.getString(phoneCollumn);
                result_sql.photo = c.getString(photoCollumn);
                result_sql.server = c.getString(serverCollumn);
                result_sql.user_id = c.getInt(useridCollumn);
                result_sql.status = "0";
                contactInfoList.add(result_sql);
            } while (c.moveToNext());
        }
        Collections.sort(contactInfoList);
        for(ContactInfo itemT : contactInfoList) {
            contactInfoListAll.add(itemT);
        }

        adapter = new ContactAdapter (getActivity(), 0, contactInfoListAll);
        listContact.setAdapter(adapter);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchContact.getText().toString().trim().equals("")) {
                    onResume();
                }
                else {
                    new getMessegAllTask().execute();
                }
            }
        });

        return rootView;
    }

//    private TextWatcher filterTextWatcher = new TextWatcher() {
//
//        @Override
//        public void afterTextChanged(Editable s) {
//            // TODO Auto-generated method stub
//
//        }
//
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count,
//                                      int after) {
//            // TODO Auto-generated method stub
//
//        }
//
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before,
//                                  int count) {
//            if (adapter != null) {
//                adapter.getFilter().filter(s);
//            } else {
//                Log.d("filter", "no filter availible");
//            }
//        }
//    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        contactInfoListAll.clear();
        contactInfoList.clear();
        Cursor c_serv = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE server='1' and " + SQLMessager.CONTACTS_USER_ID + "<>" + UILApplication.UserID, null);
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
                contactInfoList.add(result_sql);
            } while (c_serv.moveToNext());
        }
        Collections.sort(contactInfoList);
        for(ContactInfo itemS : contactInfoList) {
            contactInfoListAll.add(itemS);
        }
        contactInfoList.clear();

        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CONTACTS + " WHERE server='0'", null);
        if (c.moveToFirst()) {
            int ideCollumn = c.getColumnIndex("id");
            int nameCollumn = c.getColumnIndex(SQLMessager.CONTACTS_NAME);
            int phoneCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHONE);
            int photoCollumn = c.getColumnIndex(SQLMessager.CONTACTS_PHOTO);
            int useridCollumn = c.getColumnIndex(SQLMessager.CONTACTS_USER_ID);
            int serverCollumn = c.getColumnIndex(SQLMessager.CONTACTS_SERV);
            do {
                ContactInfo result_sql = new ContactInfo();
                result_sql.id_db = c.getInt(ideCollumn);
                result_sql.name = c.getString(nameCollumn);
                result_sql.phone = c.getString(phoneCollumn);
                result_sql.photo = c.getString(photoCollumn);
                result_sql.server = c.getString(serverCollumn);
                result_sql.user_id = c.getInt(useridCollumn);
                result_sql.status = "0";
                contactInfoList.add(result_sql);
            } while (c.moveToNext());
        }
        Collections.sort(contactInfoList);
        for(ContactInfo itemT : contactInfoList) {
            contactInfoListAll.add(itemT);
        }
        adapter = new ContactAdapter (getActivity(), 0, contactInfoListAll);
        listContact.setAdapter(adapter);
    }

    class getMessegAllTask extends AsyncTask<String, String, List<ContactInfo>> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @SuppressWarnings("static-access")
        protected List<ContactInfo> doInBackground(String... params) {
            List<ContactInfo> result = HttpConnectRecive.getInstance().getGlobalSearh(searchContact.getText().toString(), getActivity());
            return result;
        }

        protected void onPostExecute(List<ContactInfo> result) {
            if (result != null) {
                contactInfoListAll.clear();
                contactInfoList.clear();
                for (ContactInfo item : result) {
                    ContactInfo result_sql = new ContactInfo();
                    result_sql.id_db = item.id_db;
                    result_sql.name = item.name;
                    result_sql.phone = item.phone;
                    result_sql.photo = item.photo;
                    result_sql.server = "2";
                    result_sql.user_id = item.user_id;
                    result_sql.status = "0";
                    contactInfoList.add(result_sql);
                }
                Collections.sort(contactInfoList);
                for(ContactInfo itemT : contactInfoList) {
                    contactInfoListAll.add(itemT);
                }
                adapter = new ContactAdapter (getActivity(), 0, contactInfoListAll);
                listContact.setAdapter(adapter);
            }
            super.onPostExecute(result);
        }
    }
}
