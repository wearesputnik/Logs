package mobi.kolibri.messager.object;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by root on 13.08.15.
 */
public class SQLMessager extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "messager";

    /* table api_key*/
    public static final String TABLE_APP_ID = "appid";
    public static final String APP_ID = "appid";
    public static final String USER_ID = "user_id";
    public static final String CREATE_TABLE_APP_ID = "CREATE TABLE IF NOT EXISTS " + TABLE_APP_ID + " ( id integer primary key autoincrement, "
            + APP_ID + " TEXT, " + USER_ID + " TEXT)";

    /* table contacts*/
    public static final String TABLE_CONTACTS = "contacts";
    public static final String CONTACTS_USER_ID = "user_id";
    public static final String CONTACTS_NAME = "name";
    public static final String CONTACTS_PHONE = "phone";
    public static final String CONTACTS_PHOTO = "photo";
    public static final String CONTACTS_SUMMARY = "summary";
    public static final String CONTACTS_SERV = "server";
    public static final String CREATE_TABLE_CONTACTS = "CREATE TABLE IF NOT EXISTS " + TABLE_CONTACTS + " ( id integer primary key autoincrement, "
            + CONTACTS_USER_ID + " TEXT, " + CONTACTS_NAME + " TEXT, " + CONTACTS_PHONE + " TEXT, " + CONTACTS_PHOTO + " TEXT, " + CONTACTS_SUMMARY +
            " TEXT, " + CONTACTS_SERV + " TEXT)";

    /* table chat*/
    public static final String TABLE_CHAT = "chat";
    public static final String CHAT_NAME = "name";
    public static final String CHAT_JSON_INTERLOCUTOR = "json_interlocutor";
    public static final String CHAT_TYPE = "type_chat";
    public static final String CHAT_READ = "read";
    public static final String CREATE_TABLE_CHAT = "CREATE TABLE IF NOT EXISTS " + TABLE_CHAT + " ( id integer primary key autoincrement, "
            + CHAT_JSON_INTERLOCUTOR + " TEXT, " + CHAT_TYPE + " TEXT, " + CHAT_READ + " TEXT, " + CHAT_NAME + " TEXT)";

    /* table messager*/
    public static final String TABLE_MESSAGER = "messager";
    public static final String MESSAGER_FROM_ID = "id_from";
    public static final String MESSAGER_TO_ID = "id_to";
    public static final String MESSAGER_MESSAG = "messag";
    public static final String MESSAGER_SERVER = "server";
    public static final String MESSAGER_CHAT_ID = "chat_id";
    public static final String CREATE_TABLE_MESSAGER = "CREATE TABLE IF NOT EXISTS " + TABLE_MESSAGER + " ( id integer primary key autoincrement, "
            + MESSAGER_FROM_ID + " TEXT, " + MESSAGER_TO_ID + " TEXT, " + MESSAGER_MESSAG + " TEXT," + MESSAGER_SERVER + " TEXT, " + MESSAGER_CHAT_ID + " TEXT)";

    public SQLMessager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
