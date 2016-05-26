package mobi.kolibri.messager.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.TransitionDrawable;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    Button btnDeleteChat, btnGroupChat, btnSecretChat, btnChat;
    boolean isVisibleClear;
    List<ChatInfo> listChat;
    SQLiteDatabase db;
    private ImageView plus, imgAddChat, imgRemoveChat;
    private RelativeLayout plus_count, relLayoutChat, relLayoutNewChat;
    private LinearLayout btnNewChatLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        listChatView = (ListView) rootView.findViewById(R.id.listChatView);
        listChatView.setDividerHeight(0);
        adapter = new ChatAdapter(getActivity());
        listChatView.setAdapter(adapter);
        btnDeleteChat = (Button) rootView.findViewById(R.id.btnDeleteChat);
        btnGroupChat = (Button) rootView.findViewById(R.id.btnGroupChat);
        btnSecretChat = (Button) rootView.findViewById(R.id.btnSecretChat);
        btnChat = (Button) rootView.findViewById(R.id.btnChat);
        isVisibleClear = false;
        btnDeleteChat.setVisibility(View.GONE);
        listChat = new ArrayList<>();
        relLayoutChat = (RelativeLayout) rootView.findViewById(R.id.relLayoutChat);
        relLayoutNewChat = (RelativeLayout) rootView.findViewById(R.id.relLayoutNewChat);
        btnNewChatLayout = (LinearLayout) rootView.findViewById(R.id.btnNewChatLayout);

        sqlMessager = new SQLMessager(getActivity());
        db = sqlMessager.getWritableDatabase();

        plus = (ImageView) rootView.findViewById(R.id.plus_right_bottom);
        imgAddChat = (ImageView) rootView.findViewById(R.id.imgAddChat);
        imgRemoveChat = (ImageView) rootView.findViewById(R.id.imgRemoveChat);
        plus_count = (RelativeLayout) rootView.findViewById(R.id.plus_cont);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionDrawable transition = (TransitionDrawable) v.getBackground();
                if (plus_count.getVisibility() == View.GONE) {
                    relLayoutChat.setVisibility(View.VISIBLE);
                    plus_count.setVisibility(View.VISIBLE);
                    Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.menuplus);
                    anim.reset();
                    plus_count.clearAnimation();
                    plus_count.startAnimation(anim);
                   // v.setBackgroundResource(R.mipmap.fab_ic_add_up);
                    transition.startTransition(500);

                    /*Animation animc = AnimationUtils.loadAnimation(getActivity(), R.anim.menupluscount);
                    animc.reset();
                    count_plus_menu.clearAnimation();
                    count_plus_menu.startAnimation(animc);*/
                } else {
                    relLayoutChat.setVisibility(View.GONE);
                    plus_count.setVisibility(View.GONE);
                    transition.reverseTransition(500);
                  //  v.setBackgroundResource(R.mipmap.fab_ic_add_down);
                }
            }
        });
        relLayoutNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                relLayoutNewChat.setVisibility(View.GONE);
                btnNewChatLayout.setVisibility(View.GONE);
            }
        });
        btnGroupChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relLayoutNewChat.setVisibility(View.GONE);
                btnNewChatLayout.setVisibility(View.GONE);
                Intent i = new Intent(getActivity(), NewGroupChatActivity.class);
                i.putExtra("type", "group");
                startActivity(i);
            }
        });
        btnSecretChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relLayoutNewChat.setVisibility(View.GONE);
                btnNewChatLayout.setVisibility(View.GONE);
                Intent i = new Intent(getActivity(), NewChatActivity.class);
                i.putExtra("type", "secret");
                startActivity(i);
            }
        });
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relLayoutNewChat.setVisibility(View.GONE);
                btnNewChatLayout.setVisibility(View.GONE);
                Intent i = new Intent(getActivity(), NewChatActivity.class);
                i.putExtra("type", "regular");
                startActivity(i);
            }
        });

        imgAddChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DialogChat();
                plus_count.setVisibility(View.GONE);
                relLayoutChat.setVisibility(View.GONE);
                TransitionDrawable transition = (TransitionDrawable) plus.getBackground();
                transition.reverseTransition(500);
                relLayoutNewChat.setVisibility(View.VISIBLE);
                btnNewChatLayout.setVisibility(View.VISIBLE);
                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.menuplus);
                anim.reset();
                btnNewChatLayout.clearAnimation();
                btnNewChatLayout.startAnimation(anim);
            }
        });
        imgRemoveChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                isVisibleClear = true;
//                onResume();
                plus_count.setVisibility(View.GONE);
                relLayoutChat.setVisibility(View.GONE);
                TransitionDrawable transition = (TransitionDrawable) plus.getBackground();
                transition.reverseTransition(500);
            }
        });
        relLayoutChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Click", "click layout");
                if (plus_count.getVisibility() == View.VISIBLE) {
                    relLayoutChat.setVisibility(View.GONE);
                    TransitionDrawable transition = (TransitionDrawable) plus.getBackground();
                    plus_count.setVisibility(View.GONE);
                    transition.reverseTransition(500);
                }
            }
        });

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

                listChat.add(result_sql);
            } while (c.moveToNext());
        }

        for (ChatInfo item : listChat) {
            if (!ChatRemoweTime(item.id)) {
                adapter.add(item);
            }
        }
        adapter.notifyDataSetChanged();

        Updater u = new Updater();
        u.start();

        btnDeleteChat.setOnClickListener(btnDeleteChatLisener);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.clear();
        listChat.clear();
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

                listChat.add(result_sql);
            } while (c.moveToNext());
        }

        for (ChatInfo item : listChat) {
            if (!ChatRemoweTime(item.id)) {
                adapter.add(item);
            }
        }
        adapter.notifyDataSetChanged();

        if (isVisibleClear) {
            btnDeleteChat.setVisibility(View.VISIBLE);
        }
        else {
            btnDeleteChat.setVisibility(View.GONE);
        }

    }

    private boolean ChatRemoweTime(Integer chat_id) {
        String formattedDateB = null;
        Cursor c = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_MESSAGER + " WHERE " + SQLMessager.MESSAGER_CHAT_ID + "='" + chat_id + "' ORDER BY id DESC", null);
        if (c.moveToFirst()) {
            int createdCollumn = c.getColumnIndex(SQLMessager.MESSAGER_CREATED);
            formattedDateB = c.getString(createdCollumn);
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(cal.getTime());
            Date date_end = null;
            Date date_begin = null;
            try {
                date_end = df.parse(formattedDate);
                date_begin = df.parse(formattedDateB);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long diffInMillisec = date_end.getTime() - date_begin.getTime();
            long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMillisec);
            long seconds = diffInSec % 60;
            diffInSec/= 60;
            long minutes =diffInSec % 60;
            diffInSec /= 60;
            long hours = diffInSec % 24;
            diffInSec /= 24;
            long days = diffInSec;
            Log.e("LOG HOURS", "" + days + " date_b " + formattedDateB + " date_e " + formattedDate);
            if (days >= 1) {
                db.delete(SQLMessager.TABLE_MESSAGER, SQLMessager.MESSAGER_CHAT_ID + "=?", new String[]{chat_id.toString()});
                db.delete(SQLMessager.TABLE_CHAT, "id=?", new String[]{chat_id.toString()});
                return true;
            }
        }



        return false;
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

    View.OnClickListener btnDeleteChatLisener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (ChatInfo item : listChat) {
                if (item.cheked) {
                    db.delete(SQLMessager.TABLE_MESSAGER, SQLMessager.MESSAGER_CHAT_ID + "=?", new String[]{item.id.toString()});
                    db.delete(SQLMessager.TABLE_CHAT, "id=?", new String[]{item.id.toString()});
                }
            }
            isVisibleClear = false;
            onResume();
        }
    };
}
