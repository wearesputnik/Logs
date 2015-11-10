package mobi.kolibri.messager.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.activity.NewChatActivity;
import mobi.kolibri.messager.activity.NewGroupChatActivity;
import mobi.kolibri.messager.adapters.ChatAdapter;
import mobi.kolibri.messager.object.ChatInfo;
import mobi.kolibri.messager.object.SQLMessager;

public class ChatFragment extends Fragment {
    ListView listChatView;
    SQLMessager sqlMessager;
    ChatAdapter adapter;
    Button btnDeleteChat;
    boolean isVisibleClear;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        listChatView = (ListView) rootView.findViewById(R.id.listChatView);
        listChatView.setDividerHeight(0);
        adapter = new ChatAdapter(getActivity());
        listChatView.setAdapter(adapter);
        btnDeleteChat = (Button) rootView.findViewById(R.id.btnDeleteChat);
        isVisibleClear = false;
        btnDeleteChat.setVisibility(View.GONE);

        sqlMessager = new SQLMessager(getActivity());

        SQLiteDatabase db = sqlMessager.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CHAT, null);
        if (c.moveToFirst()) {
            int idCollumn = c.getColumnIndex("id");
            int nameCollumn = c.getColumnIndex(SQLMessager.CHAT_JSON_INTERLOCUTOR);
            int typeCollumn = c.getColumnIndex(SQLMessager.CHAT_TYPE);
            int nameCCollumn = c.getColumnIndex(SQLMessager.CHAT_NAME);
            do {
                ChatInfo result_sql = new ChatInfo();
                result_sql.id = c.getInt(idCollumn);
                result_sql.json_user = c.getString(nameCollumn);
                result_sql.type_chat = c.getString(typeCollumn);
                result_sql.name = c.getString(nameCCollumn);
                result_sql.cheked = false;
                result_sql.is_viseble = isVisibleClear;

                adapter.add(result_sql);
            } while (c.moveToNext());
        }
        adapter.notifyDataSetChanged();

        Updater u = new Updater();
        u.start();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.clear();
        SQLiteDatabase db = sqlMessager.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_CHAT, null);
        if (c.moveToFirst()) {
            int idCollumn = c.getColumnIndex("id");
            int nameCollumn = c.getColumnIndex(SQLMessager.CHAT_JSON_INTERLOCUTOR);
            int typeCollumn = c.getColumnIndex(SQLMessager.CHAT_TYPE);
            int nameCCollumn = c.getColumnIndex(SQLMessager.CHAT_NAME);
            do {
                ChatInfo result_sql = new ChatInfo();
                result_sql.id = c.getInt(idCollumn);
                result_sql.json_user = c.getString(nameCollumn);
                result_sql.type_chat = c.getString(typeCollumn);
                result_sql.name = c.getString(nameCCollumn);
                result_sql.cheked = false;
                result_sql.is_viseble = isVisibleClear;

                adapter.add(result_sql);
            } while (c.moveToNext());
        }
        adapter.notifyDataSetChanged();

        if (isVisibleClear) {
            btnDeleteChat.setVisibility(View.VISIBLE);
        }
        else {
            btnDeleteChat.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_chat:
                DialogChat();
                break;
            case R.id.action_clear:
                isVisibleClear = true;
                onResume();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class Updater extends Thread {
        public boolean stopped = false;

        public void run() {
            try {
                while (!stopped) {
                    // Активность списка
                    getActivity().runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    onResume();
                                }
                            }
                    );
                    try {
                        Thread.sleep(7000);
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private void DialogChat() {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_chat);
        ImageButton btnRegChat = (ImageButton) dialog.findViewById(R.id.btnRegChat);
        ImageButton btnSecChat = (ImageButton) dialog.findViewById(R.id.btnSecChat);
        ImageButton btnGroupChat = (ImageButton) dialog.findViewById(R.id.btnGroupChat);
        ImageButton btnCancel = (ImageButton) dialog.findViewById(R.id.btnCancel);

        btnRegChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), NewChatActivity.class);
                i.putExtra("type", "regular");
                startActivity(i);
                dialog.dismiss();
            }
        });

        btnSecChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), NewChatActivity.class);
                i.putExtra("type", "secret");
                startActivity(i);
                dialog.dismiss();
            }
        });

        btnGroupChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), NewGroupChatActivity.class);
                i.putExtra("type", "group");
                startActivity(i);
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
