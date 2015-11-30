package mobi.kolibri.messager.http;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
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

import mobi.kolibri.messager.Utils;
import mobi.kolibri.messager.object.ContactInfo;
import mobi.kolibri.messager.object.GroupMessagerInfo;
import mobi.kolibri.messager.object.MessagInfo;
import mobi.kolibri.messager.object.ProfileInfo;
import mobi.kolibri.messager.object.SQLMessager;

public class HttpConnectRecive {
    public static final String URL = "http://kolibri.mobi/messager/index.php/api/";
    public static final String URLP = "http://kolibri.mobi/messager";
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
    public static HttpClient http;
    public static String api_key = null;

    public static SQLMessager sqlMessager;

    public static String getApiKey(Context c) {
        String result = "";
        sqlMessager = new SQLMessager(c);

        SQLiteDatabase db = sqlMessager.getWritableDatabase();
        Cursor ca = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_APP_ID, null);
        if (ca.moveToFirst()) {
            int appidColIndex = ca.getColumnIndex(sqlMessager.APP_ID);
            result = ca.getString(appidColIndex);
        }
        return result;
    }

    public static String getUserId(Context c) {
        String result = "";
        sqlMessager = new SQLMessager(c);

        SQLiteDatabase db = sqlMessager.getWritableDatabase();
        Cursor ca = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_APP_ID, null);
        if (ca.moveToFirst()) {
            int useridColIndex = ca.getColumnIndex(sqlMessager.USER_ID);
            result = ca.getString(useridColIndex);
        }
        return result;
    }

    public static Integer CreateAccaunt(String email, String password, String phone) {
        Integer user_id = 0;

        http = new DefaultHttpClient();
        ClientConnectionManager mgr = http.getConnectionManager();
        HttpParams params = http.getParams();
        http = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
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

        ServiceHandler sh = new ServiceHandler();

        String jsonStr = sh.makeServiceCall(URL + ACTIVATION + "?id=" + id + "&key=" + key, ServiceHandler.GET);
        Log.e("ACTIVATION: ", "=> " + jsonStr);

        try {
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

        http = new DefaultHttpClient();
        ClientConnectionManager mgr = http.getConnectionManager();
        HttpParams params = http.getParams();
        http = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
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
            String jsonStr = Utils.streamToString(response
                    .getEntity().getContent());
            Log.e("LOGIN_ACCAUNT", jsonStr);
            JSONObject json = new JSONObject(jsonStr);
            Integer status = json.getInt("status");
            if (status == 0) {
                JSONObject result_json = json.getJSONObject("result");
                result = result_json.getString("user_id");

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

        ServiceHandler sh = new ServiceHandler();

        String jsonStr = sh.makeServiceCall(URL + GET_PROFILE + "?app_key=" + getApiKey(c), ServiceHandler.GET);
        Log.e("GET_PROFILE: ", "=> " + jsonStr);
        Log.e("URL: ", URL + GET_PROFILE + "?app_key=" + getApiKey(c));

        try {
            JSONObject json = new JSONObject(jsonStr);

            result = ProfileInfo.parseJson(json.getJSONObject("result"));

            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ProfileInfo setProfile(Context c, Integer id, ProfileInfo item_form, Integer photo_witch) {
        ProfileInfo result = new ProfileInfo();
        http = new DefaultHttpClient();
        ClientConnectionManager mgr = http.getConnectionManager();
        HttpParams params = http.getParams();
        http = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
                mgr.getSchemeRegistry()), params);
        HttpPost request = new HttpPost(URL + SET_PROFILE + "?user_id=" + id + "&app_key=" + getApiKey(c));

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
            String jsonStr = Utils.streamToString(response.getEntity()
                    .getContent());
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

        ServiceHandler sh = new ServiceHandler();

        String jsonStr = sh.makeServiceCall(URL + NEW_APPKEY + "?id=" + id, ServiceHandler.GET);
        Log.e("ACTIVATION: ", "=> " + jsonStr);

        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONObject result_json = json.getJSONObject("result");
            result = result_json.getString("app_key");

            ContentValues cv = new ContentValues();
            SQLiteDatabase db = sqlMessager.getWritableDatabase();
            cv.put(SQLMessager.APP_ID, result_json.getString("app_key"));
            //cv.put("id", 1);
            db.update(SQLMessager.TABLE_APP_ID, cv, "id=?", new String[] {"1"});


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

        http = new DefaultHttpClient();
        ClientConnectionManager mgr = http.getConnectionManager();
        HttpParams params = http.getParams();
        http = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
        HttpPost request = new HttpPost(URL + CONTACT_SERVER + "?app_key=" + getApiKey(c));

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

    public static MessagInfo setMessage(Context c, MessagInfo item_msg, Integer photo_witch) {
        MessagInfo result = new MessagInfo();

        http = new DefaultHttpClient();
        ClientConnectionManager mgr = http.getConnectionManager();
        HttpParams params = http.getParams();
        http = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
                mgr.getSchemeRegistry()), params);
        HttpPost request = new HttpPost(URL + SET_MESSAGER + "?app_key=" + getApiKey(c));

        Charset charset = Charset.forName("UTF-8");
        MultipartEntity form = new MultipartEntity(HttpMultipartMode.STRICT);

        try {
            form.addPart("user_id_from", new StringBody(item_msg.id_from, charset));
            form.addPart("message", new StringBody(item_msg.message, charset));
            form.addPart("type_chat", new StringBody(item_msg.type_chat, charset));
            form.addPart("created", new StringBody(item_msg.created, charset));


            if (photo_witch == 1) {
                form.addPart("photo_witch", new StringBody("" + photo_witch, charset));
                form.addPart("duration", new StringBody(item_msg.duration, charset));
                form.addPart("photo", new FileBody(new File(item_msg.attachment)));
            }
            else {
                form.addPart("photo_witch", new StringBody("" + photo_witch, charset));
            }

            request.setEntity(form);
            HttpResponse response = http.execute(request);
            String jsonStr = Utils.streamToString(response
                    .getEntity().getContent());
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

        ServiceHandler sh = new ServiceHandler();
        String jsonStr = "";
        String urlStr = "";

        if (user_from.equals("0")) {
            urlStr = URL + GET_MESSAGER + "?app_key=" + getApiKey(c);
        }
        else {
            urlStr = URL + GET_MESSAGER + "?user_from=" + user_from + "&app_key=" + getApiKey(c);
        }
        jsonStr = sh.makeServiceCall(urlStr, ServiceHandler.GET);
        Log.e("GET_MESSAGER", urlStr);
      ///  Log.e("GET_MESSAGER", jsonStr);

        try {
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

    public static String setGroupMessage(Context c, GroupMessagerInfo item_msg, Integer photo_witch) {
        String result = null;

        http = new DefaultHttpClient();
        ClientConnectionManager mgr = http.getConnectionManager();
        HttpParams params = http.getParams();
        http = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
        HttpPost request = new HttpPost(URL + SET_GROUP_MESSAGER + "?app_key=" + getApiKey(c));

        Charset charset = Charset.forName("UTF-8");
        MultipartEntity form = new MultipartEntity(HttpMultipartMode.STRICT);

        try {
            form.addPart("json_users", new StringBody(item_msg.json_users, charset));
            form.addPart("message", new StringBody(item_msg.message, charset));
            form.addPart("chat_name", new StringBody(item_msg.chat_name, charset));
            form.addPart("type_chat", new StringBody(item_msg.type_chat, charset));


            if (photo_witch == 1) {
                form.addPart("photo_witch", new StringBody("" + photo_witch, charset));
                form.addPart("duration", new StringBody(item_msg.duration, charset));
                form.addPart("photo", new FileBody(new File(item_msg.attachment)));
            }
            else {
                form.addPart("photo_witch", new StringBody("" + photo_witch, charset));
            }

            request.setEntity(form);
            HttpResponse response = http.execute(request);
            String jsonStr = Utils.streamToString(response
                    .getEntity().getContent());
            Log.e("SET_GROUP_MESSAGER", jsonStr);

            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<GroupMessagerInfo> getGroupMessager(Context c) {
        List<GroupMessagerInfo> result = new ArrayList<>();

        ServiceHandler sh = new ServiceHandler();
        String jsonStr = "";
        String urlStr = "";

        urlStr = URL + GET_GROUP_MESSAGER + "?app_key=" + getApiKey(c);

        jsonStr = sh.makeServiceCall(urlStr, ServiceHandler.GET);
        Log.e("GET_GROUP_MESSAGER", urlStr);
///        Log.e("GET_GROUP_MESSAGER", jsonStr);

        try {
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

        http = new DefaultHttpClient();
        ClientConnectionManager mgr = http.getConnectionManager();
        HttpParams params = http.getParams();
        http = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
        HttpPost request = new HttpPost(URL + GET_GROUP_MESSAGER + "?app_key=" + getApiKey(c));

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

    public static String AttacmentPhotoMessage(Context c) {
        String result = null;

        http = new DefaultHttpClient();
        ClientConnectionManager mgr = http.getConnectionManager();
        HttpParams params = http.getParams();
        http = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
                mgr.getSchemeRegistry()), params);
        HttpPost request = new HttpPost(URL + SET_PROFILE + "?user_id=" + "&app_key=" + getApiKey(c));

        Charset charset = Charset.forName("UTF-8");
        MultipartEntity form = new MultipartEntity(HttpMultipartMode.STRICT);

        try {


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
