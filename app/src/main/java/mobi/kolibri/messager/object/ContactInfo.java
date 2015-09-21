package mobi.kolibri.messager.object;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by root on 14.08.15.
 */
public class ContactInfo {
    public String name;
    public String phone;
    public Integer user_id;
    public String photo;
    public String summary;
    public Integer id_db;
    public boolean chek_cont;

    public static ContactInfo parseJson(JSONObject json) {
        ContactInfo result = new ContactInfo();

        try {
            result.user_id = json.getInt("user_id");
            result.name = json.getString("name");
            result.phone = json.getString("phone");
            result.photo = json.getString("photo");
            result.summary = json.getString("summary");
            result.id_db = json.getInt("id_db");
            return result;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String stringJson(List<ContactInfo> listContacts) {
        String result = null;

        JSONArray contact_array = new JSONArray();

        for (ContactInfo item : listContacts) {
            JSONObject item_json = new JSONObject();
            if (item.user_id == null) {
                try {
                    item_json.put("phone", item.phone);
                    item_json.put("id_db", item.id_db);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                contact_array.put(item_json);
            }
        }

        result = contact_array.toString();

        return result;
    }
}
