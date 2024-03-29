package mobi.kolibri.messager.object;

import org.json.JSONObject;

/**
 * Created by root on 31.08.15.
 */
public class MessagInfo {
    public Integer id_messege;
    public String id_from;
    public String id_to;
    public String name_to;
    public String message;
    public String type_chat;
    public String attachment;
    public String duration;
    public String created;

    public static MessagInfo parseJson(JSONObject json) {
        MessagInfo result = new MessagInfo();

        try {
            result.id_from = json.getString("id_from");
            result.id_to = json.getString("id_to");
            result.name_to = json.getString("name_to");
            result.message = json.getString("message");
            result.type_chat = json.getString("type_chat");
            result.attachment = json.getString("attachment");
            result.duration = json.getString("duration");
            result.created = json.getString("created");
            return result;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
