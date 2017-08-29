package mobi.kolibri.messager.http;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mobi.kolibri.messager.UILApplication;
import mobi.kolibri.messager.Utils;
import mobi.kolibri.messager.object.ContactInfo;
import mobi.kolibri.messager.object.GroupMessagerInfo;
import mobi.kolibri.messager.object.MessagInfo;
import mobi.kolibri.messager.object.ProfileInfo;
import mobi.kolibri.messager.object.SQLMessager;

public class HttpConnectRecive {
    public static final String URL = "http://www.wearesputnik.com/messager/index.php/api/";
    public static final String URLP = "http://www.wearesputnik.com/messager";
    public static final String REGISTRATION = "create_account";
    public static final String LOGIN = "account_login";
    public static final String ACTIVATION = "activate";
    public static final String GET_PROFILE = "get_profile";
    public static final String SET_PROFILE = "set_profile";
    public static final String NEW_APPKEY = "get_appkey";
    public static final String CONTACT_SERVER = "contacts_server";
    public static final String SET_MESSAGER = "set_messager";
    public static final String GET_MESSAGER = "get_messager";
    public static final String SET_GROUP_MESSAGER = "set_group_messeger";
    public static final String GET_GROUP_MESSAGER = "get_group_messager";
    public static final String STATUS_USERS = "online_users";
    public static final String GET_GLOBAL_SEARCH = "get_global_search";
    public static final String GET_PROFILE_SERVER = "get_profile_server";
    public static HttpClient http;
    public static String api_key = null;

    public static SQLMessager sqlMessager;

    private HttpConnectRecive() {
        http = new DefaultHttpClient();
        ClientConnectionManager mgr = http.getConnectionManager();
        HttpParams params = http.getParams();
        http = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
                mgr.getSchemeRegistry()), params);
    }

    public static HttpConnectRecive getInstance() {
        synchronized (HttpConnectRecive.class) {
            if (UILApplication.restInstance == null) {
                UILApplication.restInstance = new HttpConnectRecive();
            }
        }
        return UILApplication.restInstance;
    }

    public static String getPlayerId(Context c) {
        String result = "";
        sqlMessager = new SQLMessager(c);

        SQLiteDatabase db = sqlMessager.getWritableDatabase();
        Cursor ca = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_APP_ID, null);
        if (ca.moveToFirst()) {
            int playeridColIndex = ca.getColumnIndex(sqlMessager.PLAYER_ID);
            result = ca.getString(playeridColIndex);
        }
        return result;
    }

    /*public static String getUserId(Context c) {
        String result = "";
        sqlMessager = new SQLMessager(c);

        SQLiteDatabase db = sqlMessager.getWritableDatabase();
        Cursor ca = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_APP_ID, null);
        if (ca.moveToFirst()) {
            int useridColIndex = ca.getColumnIndex(sqlMessager.USER_ID);
            result = ca.getString(useridColIndex);
        }
        return result;
    }*/

    public static Integer CreateAccaunt(String email, String password, String phone) {
        Integer user_id = 0;
        HttpPost request = new HttpPost(URL + REGISTRATION);

        List<BasicNameValuePair> parameters = Arrays.asList(
                new BasicNameValuePair("email", email),
                new BasicNameValuePair("password", password),
                new BasicNameValuePair("phone", phone));


        try {
            UrlEncodedFormEntity form = new UrlEncodedFormEntity(parameters,
                    "UTF-8");
            request.setEntity(form);
            HttpResponse response = http.execute(request);
            String jsonStr = Utils.streamToString(response
                    .getEntity().getContent());
            Log.e("CREATE_ACCAUNT", jsonStr);
            JSONObject json = new JSONObject(jsonStr);
            JSONObject result = json.getJSONObject("result");
            user_id = result.getInt("user_id");

            return user_id;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String Activation(Integer id, String key) {
        String result = "";
        HttpGet request = new HttpGet(URL + ACTIVATION + "?id=" + id + "&key=" + key);

        try {
            HttpResponse response = http.execute(request);
            String jsonStr = Utils.streamToString(response.getEntity().getContent());
            Log.e("ACTIVATION: ", "=> " + jsonStr);
            JSONObject json = new JSONObject(jsonStr);
            JSONObject result_json = json.getJSONObject("result");
            result = result_json.getString("app_key");

            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String Login(String login, String password, Context context) {
        String result = "";

        HttpPost request = new HttpPost(URL + LOGIN);

        List<BasicNameValuePair> parameters = Arrays.asList(
                new BasicNameValuePair("email", login),
                new BasicNameValuePair("password", password));
        sqlMessager = new SQLMessager(context);

        try {
            UrlEncodedFormEntity form = new UrlEncodedFormEntity(parameters,
                    "UTF-8");
            request.setEntity(form);
            HttpResponse response = http.execute(request);
            String jsonStr = Utils.streamToString(response.getEntity().getContent());
            Log.e("LOGIN_ACCAUNT", jsonStr);
            JSONObject json = new JSONObject(jsonStr);
            Integer status = json.getInt("status");
            if (status == 0) {
                JSONObject result_json = json.getJSONObject("result");
                result = result_json.getString("user_id");

                UILApplication.UserID = result;

                ContentValues cv = new ContentValues();
                SQLiteDatabase db = sqlMessager.getWritableDatabase();
                cv.put(SQLMessager.APP_ID, result_json.getString("app_key"));
                cv.put(SQLMessager.USER_ID, result_json.getString("user_id"));
                db.insert(SQLMessager.TABLE_APP_ID, null, cv);
            }
            else {
                return null;
            }

            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ProfileInfo getProfile(Context c) {
        ProfileInfo result = new ProfileInfo();

        HttpGet request = new HttpGet(URL + GET_PROFILE + "?app_key=" + UILApplication.AppIDkey + "&playerId=" + getPlayerId(c));
        Log.e("LOGIN_ACCAUNT", URL + GET_PROFILE + "?app_key=" + UILApplication.AppIDkey + "&playerId=" + getPlayerId(c));
        try {
            HttpResponse response = http.execute(request);
            String jsonStr = Utils.streamToString(response.getEntity().getContent());
            Log.e("LOGIN_ACCAUNT", jsonStr);
            JSONObject json = new JSONObject(jsonStr);
            result = ProfileInfo.parseJson(json.getJSONObject("result"));
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ProfileInfo setProfile(ProfileInfo item_form, Integer photo_witch) {
        ProfileInfo result = new ProfileInfo();

        HttpPost request = new HttpPost(URL + SET_PROFILE + "?user_id=" + UILApplication.UserID + "&app_key=" + UILApplication.AppIDkey);
        //Log.e("SET_PROFILE", URL + SET_PROFILE + "?user_id=" + getUserId(c) + "&app_key=" + getApiKey(c));
        Charset charset = Charset.forName("UTF-8");
        MultipartEntity form = new MultipartEntity(HttpMultipartMode.STRICT);

        try {
            form.addPart("firstname", new StringBody(item_form.firstname, charset));
            form.addPart("lastname", new StringBody(item_form.lastname, charset));
            form.addPart("phone", new StringBody(item_form.phone, charset));
            form.addPart("summary", new StringBody(item_form.summary, charset));

            if (photo_witch == 1) {
                form.addPart("photo_witch", new StringBody("" + photo_witch, charset));
                form.addPart("photo", new FileBody(new File(item_form.photo)));
            }
            else {
                form.addPart("photo_witch", new StringBody("" + photo_witch, charset));
            }

            request.setEntity(form);
            HttpResponse response = http.execute(request);

            String jsonStr = Utils.streamToString(response.getEntity().getContent());
            Log.e("SET_PROFILE", jsonStr);
            JSONObject json = new JSONObject(jsonStr);

            result = ProfileInfo.parseJson(json.getJSONObject("result"));

            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String newAppKey(Context c, Integer id) {
        String result = "";

        sqlMessager = new SQLMessager(c);

        HttpGet request = new HttpGet(URL + NEW_APPKEY + "?id=" + id + "&playerId=" + getPlayerId(c));
        try {
            HttpResponse response = http.execute(request);
            String jsonStr = Utils.streamToString(response
                    .getEntity().getContent());

            JSONObject json = new JSONObject(jsonStr);
            JSONObject result_json = json.getJSONObject("result");
            result = result_json.getString("app_key");

            ContentValues cv = new ContentValues();
            SQLiteDatabase db = sqlMessager.getWritableDatabase();
            cv.put(SQLMessager.APP_ID, result_json.getString("app_key"));
            db.update(SQLMessager.TABLE_APP_ID, cv, "id=?", new String[] {"1"});
            UILApplication.AppIDkey = result_json.getString("app_key");
            UILApplication.UserID = id.toString();


            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ContactInfo ContactServer(Context c, String json) {
        ContactInfo result = new ContactInfo();

        sqlMessager = new SQLMessager(c);
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = sqlMessager.getWritableDatabase();

        HttpPost request = new HttpPost(URL + CONTACT_SERVER + "?app_key=" + UILApplication.AppIDkey);

        List<BasicNameValuePair> parameters = Arrays.asList(
                new BasicNameValuePair("contacts", json));

        try {
            UrlEncodedFormEntity form = new UrlEncodedFormEntity(parameters,
                    "UTF-8");
            request.setEntity(form);
            HttpResponse response = http.execute(request);
            String jsonStr = Utils.streamToString(response
                    .getEntity().getContent());
            Log.e("CONTACT_SERVER", jsonStr);

            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray data = jsonObject.getJSONArray("result");
            for (int i = 0; i < data.length(); i++) {
                result = ContactInfo.parseJson(data.getJSONObject(i));
                if (result != null) {
                    cv.put(SQLMessager.CONTACTS_USER_ID, result.user_id.toString());
                    cv.put(SQLMessager.CONTACTS_PHOTO, result.photo);
                    if (!result.name.equals(" ")) {
                        cv.put(SQLMessager.CONTACTS_NAME, result.name);
                    }
                    cv.put(SQLMessager.CONTACTS_SUMMARY, result.summary);
                    cv.put(SQLMessager.CONTACTS_STATUS, "0");
                    cv.put(SQLMessager.CONTACTS_SERV, "1");
                    db.update(SQLMessager.TABLE_CONTACTS, cv, "id=?", new String[]{result.id_db + ""});
                }
            }



            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MessagInfo setMessage(Context c, MessagInfo item_msg) {
        MessagInfo result = new MessagInfo();

        HttpPost request = new HttpPost(URL + SET_MESSAGER + "?app_key=" + UILApplication.AppIDkey);

        Charset charset = Charset.forName("UTF-8");
        MultipartEntity form = new MultipartEntity(HttpMultipartMode.STRICT);

        try {
            form.addPart("user_id_from", new StringBody(item_msg.id_from, charset));
            form.addPart("message", new StringBody(item_msg.message, charset));
            form.addPart("type_chat", new StringBody(item_msg.type_chat, charset));
            form.addPart("created", new StringBody(item_msg.created, charset));

            if (item_msg.attachment != null) {
                form.addPart("photo_witch", new StringBody("1", charset));
                form.addPart("duration", new StringBody(item_msg.duration, charset));
                form.addPart("photo", new FileBody(new File(item_msg.attachment)));
            }
            else {
                form.addPart("photo_witch", new StringBody("0", charset));
            }

            request.setEntity(form);
            HttpResponse response = http.execute(request);
            String jsonStr = Utils.streamToString(response.getEntity().getContent());
            Log.e("SET_MESSAGER", jsonStr);

            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<MessagInfo> getMessage(Context c, String user_from) {
        List<MessagInfo> result = new ArrayList<>();

        String jsonStr = "";
        String urlStr = "";

        if (user_from.equals("0")) {
            urlStr = URL + GET_MESSAGER + "?app_key=" + UILApplication.AppIDkey;
        }
        else {
            urlStr = URL + GET_MESSAGER + "?user_from=" + user_from + "&app_key=" + UILApplication.AppIDkey;
        }
        HttpGet request = new HttpGet(urlStr);

        try {
            HttpResponse response = http.execute(request);
            jsonStr = Utils.streamToString(response
                    .getEntity().getContent());
            if (!jsonStr.equals("")) {
                JSONObject jsonObject = new JSONObject(jsonStr);
                JSONArray jsonArray = jsonObject.getJSONArray("result");
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        MessagInfo item = MessagInfo.parseJson(jsonArray.getJSONObject(i));
                        if (item != null) {
                            result.add(item);
                        }
                    }
                } else {
                    return null;
                }
            }
            else {
                return null;
            }

            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String setGroupMessage(Context c, GroupMessagerInfo item_msg) {
        String result = null;

        HttpPost request = new HttpPost(URL + SET_GROUP_MESSAGER + "?app_key=" + UILApplication.AppIDkey);

        Charset charset = Charset.forName("UTF-8");
        MultipartEntity form = new MultipartEntity(HttpMultipartMode.STRICT);

        try {
            form.addPart("json_users", new StringBody(item_msg.json_users, charset));
            form.addPart("message", new StringBody(item_msg.message, charset));
            form.addPart("chat_name", new StringBody(item_msg.chat_name, charset));
            form.addPart("type_chat", new StringBody(item_msg.type_chat, charset));
            form.addPart("created", new StringBody(item_msg.created, charset));
            form.addPart("id_db", new StringBody(item_msg.id_messege.toString(), charset));


            if (item_msg.attachment != null) {
                form.addPart("photo_witch", new StringBody("1", charset));
                form.addPart("duration", new StringBody(item_msg.duration, charset));
                form.addPart("photo", new FileBody(new File(item_msg.attachment)));
            }
            else {
                form.addPart("photo_witch", new StringBody("0", charset));
            }

            request.setEntity(form);
            HttpResponse response = http.execute(request);
            String jsonStr = Utils.streamToString(response
                    .getEntity().getContent());
            Log.e("SET_GROUP_MESSAGER", jsonStr);
            result = jsonStr;

            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<GroupMessagerInfo> getGroupMessager(Context c) {
        List<GroupMessagerInfo> result = new ArrayList<>();

        String jsonStr = "";
        String urlStr = "";

        urlStr = URL + GET_GROUP_MESSAGER + "?app_key=" + UILApplication.AppIDkey;

        HttpGet request = new HttpGet(urlStr);

        try {
            HttpResponse response = http.execute(request);
            jsonStr = Utils.streamToString(response
                    .getEntity().getContent());
            if (!jsonStr.equals("")) {
                JSONObject jsonObject = new JSONObject(jsonStr);
                JSONArray jsonArray = jsonObject.getJSONArray("result");
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        GroupMessagerInfo item = GroupMessagerInfo.parseJson(jsonArray.getJSONObject(i));
                        if (item != null) {
                            result.add(item);
                        }
                    }
                } else {
                    return null;
                }
            }
            else {
                return null;
            }

            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<GroupMessagerInfo> postGroupMessager(Context c, String json_user) {
        List<GroupMessagerInfo> result = new ArrayList<>();

        HttpPost request = new HttpPost(URL + GET_GROUP_MESSAGER + "?app_key=" + UILApplication.AppIDkey);

        List<BasicNameValuePair> parameters = Arrays.asList(
                new BasicNameValuePair("json_users", json_user));

        try {
            UrlEncodedFormEntity form = new UrlEncodedFormEntity(parameters,
                "UTF-8");
            request.setEntity(form);
            HttpResponse response = http.execute(request);
            String jsonStr = Utils.streamToString(response
                .getEntity().getContent());
            Log.e("GET_GROUP_MESSAGER", jsonStr);
            if (!jsonStr.equals("")) {
                JSONObject jsonObject = new JSONObject(jsonStr);
                JSONArray jsonArray = jsonObject.getJSONArray("result");
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        GroupMessagerInfo item = GroupMessagerInfo.parseJson(jsonArray.getJSONObject(i));
                        if (item != null) {
                            result.add(item);
                        }
                    }
                } else {
                    return null;
                }
            }
            else {
                return null;
            }

            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ContactInfo statusContact (Context c, String json_str) {
        ContactInfo result = new ContactInfo();

        sqlMessager = new SQLMessager(c);
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = sqlMessager.getWritableDatabase();

        HttpPost request = new HttpPost(URL + STATUS_USERS + "?app_key=" + UILApplication.AppIDkey);

        List<BasicNameValuePair> parameters = Arrays.asList(
                new BasicNameValuePair("contacts", json_str));
//        Log.e("STATUS_USERS", json_str);
        Log.e("STATUS_USERS", URL + STATUS_USERS + "?app_key=" + UILApplication.AppIDkey);
        try {
            UrlEncodedFormEntity form = new UrlEncodedFormEntity(parameters,
                    "UTF-8");
            request.setEntity(form);
            HttpResponse response = http.execute(request);
            String jsonStr = Utils.streamToString(response
                    .getEntity().getContent());
            Log.e("STATUS_USERS", jsonStr);

            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray data = jsonObject.getJSONArray("result");
            for (int i = 0; i < data.length(); i++) {
                result = ContactInfo.parseJsonStatus(data.getJSONObject(i));
                if (result != null) {
                    cv.put(SQLMessager.CONTACTS_STATUS, result.status);

                    db.update(SQLMessager.TABLE_CONTACTS, cv, "id=?", new String[]{result.id_db + ""});
                }
            }

            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<ContactInfo> getGlobalSearh(String name, Context c) {
        List<ContactInfo> result = new ArrayList<>();

        HttpPost request = new HttpPost(URL + GET_GLOBAL_SEARCH + "?app_key=" + UILApplication.AppIDkey);

        List<BasicNameValuePair> parameters = Arrays.asList(
                new BasicNameValuePair("name", name));

        try {
            UrlEncodedFormEntity form = new UrlEncodedFormEntity(parameters,
                    "UTF-8");
            request.setEntity(form);
            HttpResponse response = http.execute(request);
            String jsonStr = Utils.streamToString(response.getEntity().getContent());
            Log.e("GET_GLOBAL_SEARCH", jsonStr);
            if (!jsonStr.equals("")) {
                JSONObject jsonObject = new JSONObject(jsonStr);
                JSONArray jsonArray = jsonObject.getJSONArray("result");
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        ContactInfo item = ContactInfo.parseJson(jsonArray.getJSONObject(i));
                        if (item != null) {
                            result.add(item);
                        }
                    }
                } else {
                    return null;
                }
            }
            else {
                return null;
            }

            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ProfileInfo getUserServerProfile(String user_id, Context c) {
        ProfileInfo result = new ProfileInfo();
        String jsonStr = "";

        HttpGet request = new HttpGet(URL + GET_PROFILE_SERVER + "?app_key=" + UILApplication.AppIDkey + "&user_id=" + user_id);
        try {
            HttpResponse response = http.execute(request);
            jsonStr = Utils.streamToString(response
                    .getEntity().getContent());
            if (!jsonStr.equals("")) {
                JSONObject jsonObject = new JSONObject(jsonStr);
                JSONObject jsonArray = jsonObject.getJSONObject("result");
                result = ProfileInfo.parseJson(jsonArray);
            }
            else {
                return null;
            }

            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
