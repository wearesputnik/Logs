package mobi.kolibri.messager.object;

import org.json.JSONObject;

/**
 * Created by root on 18.08.15.
 */
public class ProfileInfo {
    public String firstname;
    public String lastname;
    public String phone;
    public String photo;
    public String summary;

    public static ProfileInfo parseJson(JSONObject json) {
        ProfileInfo result = new ProfileInfo();
        try {
            result.firstname = json.getString("firstname");
            result.lastname = json.getString("lastname");
            result.phone = json.getString("phone");
            result.photo = json.getString("photo");
            result.summary = json.getString("summary");
            return result;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
