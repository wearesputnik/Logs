package mobi.kolibri.messager;


import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.SQLMessager;

public class UILApplication extends Application {
	private static UILApplication mInstance;
	public static HttpConnectRecive restInstance = null;
	SQLMessager sqlMessager;
	SQLiteDatabase db;

	@Override
	public void onCreate() {
		super.onCreate();
		sqlMessager = new SQLMessager(getApplicationContext());
		db = sqlMessager.getWritableDatabase();

		db.execSQL(SQLMessager.CREATE_TABLE_APP_ID);
		db.execSQL(SQLMessager.CREATE_TABLE_CONTACTS);
		db.execSQL(SQLMessager.CREATE_TABLE_CHAT);
		db.execSQL(SQLMessager.CREATE_TABLE_MESSAGER);
		db.execSQL(SQLMessager.CREATE_TABLE_CIRCLES);
		db.execSQL(SQLMessager.CREATE_TABLE_CIRCLES_CONTACT);

		initImageLoader(getApplicationContext());

		OneSignal.startInit(this)
			.inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)// to hide dialog
			.setNotificationOpenedHandler(new NotificationOpenedHandler())
			.autoPromptLocation(true)
			.init();
		mInstance = this;
	}

	public static void initImageLoader(Context context) {
		ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
		config.threadPriority(Thread.NORM_PRIORITY - 2);
		config.denyCacheImageMultipleSizesInMemory();
		config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
		config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
		config.tasksProcessingOrder(QueueProcessingType.LIFO);
		config.writeDebugLogs(); // Remove for release app

		ImageLoader.getInstance().init(config.build());
	}

	public static synchronized UILApplication getInstance() {
		return mInstance;
	}

	private class NotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
		// This fires when a notification is opened by tapping on it.
		@Override
		public void notificationOpened(OSNotificationOpenResult result) {
			OSNotificationAction.ActionType actionType = result.action.type;
			JSONObject data = result.notification.payload.additionalData;
			String customKey;

			if (data != null) {
				customKey = data.optString("customkey", null);
				if (customKey != null)
					Log.i("OneSignalExample", "customkey set with value: " + customKey);
			}

			if (actionType == OSNotificationAction.ActionType.ActionTaken)
				Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);

			Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);


			int requestCode = 0;

			PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
			Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

			android.support.v4.app.NotificationCompat.Builder noBuilder = new android.support.v4.app.NotificationCompat.Builder(getApplicationContext())
					.setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(largeIcon).setContentTitle(result.notification.payload.title)
					.setContentText(result.notification.payload.body)
					.setAutoCancel(true).setDefaults(android.app.Notification.DEFAULT_ALL)
					.setContentIntent(pendingIntent);


			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(0, noBuilder.build()); //0 = ID of notification
		}
	}
}