package mobi.kolibri.messager.helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Messenger;
import android.util.Log;

import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import mobi.kolibri.messager.DashboardActivity;
import mobi.kolibri.messager.R;
import mobi.kolibri.messager.UILApplication;

/**
 * Created by root on 14.04.17.
 */

public class ExampleNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler
{
    // This fires when a notification is opened by tapping on it.
    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        /*OSNotificationAction.ActionType actionType = result.action.type;
        JSONObject data = result.notification.payload.additionalData;
        String customKey;

        Intent intent = new Intent(Messenger.app, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);


        int requestCode = 0;

        PendingIntent pendingIntent = PendingIntent.getActivity(UILApplication.app, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        android.support.v4.app.NotificationCompat.Builder noBuilder = new android.support.v4.app.NotificationCompat.Builder(Roshetta.app)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(largeIcon).setContentTitle(result.notification.payload.title)
                .setContentText(result.notification.payload.body)
                .setAutoCancel(true).setDefaults(android.app.Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent).setSound(sound);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, noBuilder.build()); //0 = ID of notification


        if (data != null) {
            customKey = data.optString("customkey", null);
            if (customKey != null)
                Log.i("OneSignalExample", "customkey set with value: " + customKey);
        }

        if (actionType == OSNotificationAction.ActionType.ActionTaken)
            Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);


        Log.i("OneSignalExample", "ExampleNotificationOpenedHandler");*/
    }
}
