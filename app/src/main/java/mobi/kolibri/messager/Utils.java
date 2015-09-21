package mobi.kolibri.messager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by root on 13.08.15.
 */
public class Utils {

    public static String streamToString(InputStream stream) {

        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader buff = new BufferedReader(reader);
        StringBuffer strBuff = new StringBuffer();

        String s;
        try {
            while ((s = buff.readLine()) != null) {
                strBuff.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strBuff.toString();
    }
}
