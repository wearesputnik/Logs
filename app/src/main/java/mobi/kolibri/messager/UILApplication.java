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
	public static String AppIDkey;
	public static String UserID;

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

		/*OneSignal.startInit(this)
			.inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)// to hide dialog
			.autoPromptLocation(true)
			.init();*/
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
}