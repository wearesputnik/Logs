package mobi.kolibri.messager.object;

import org.json.JSONObject;

/**
 * Created by root on 23.09.15.
 */
public class GroupMessagerInfo {
    public String id_from;
    public String id_to;
    public String name_to;
    public String message;
    public String type_chat;
    public String chat_name;
    public String json_users;
    public String attachment;
    public String duration;

    public static GroupMessagerInfo parseJson(JSONObject json) {
        GroupMessagerInfo result = new GroupMessagerInfo();

        try {
            result.id_from = json.getString("id_from");
            result.id_to = json.getString("id_to");
            result.name_to = json.getString("name_to");
            result.message = json.getString("message");
            result.type_chat = json.getString("type_chat");
            result.chat_name = json.getString("chat_name");
            result.json_users = json.getString("json_users");
            result.attachment = json.getString("attachment");
            result.duration = json.getString("duration");
            return result;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
