package mobi.kolibri.messager;


import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;
import com.parse.PushService;

import mobi.kolibri.messager.helper.ParseUtils;
import mobi.kolibri.messager.http.AppConfig;
import mobi.kolibri.messager.object.SQLMessager;

public class UILApplication extends Application {
	private static UILApplication mInstance;
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

		SQLiteDatabase db = sqlMessager.getWritableDatabase();
		Cursor ca = db.rawQuery("SELECT * FROM " + SQLMessager.TABLE_APP_ID, null);
		if (ca.moveToFirst()) {
			int useridColIndex = ca.getColumnIndex(sqlMessager.USER_ID);

			ParseUtils.registerParse(this, "users_" + ca.getString(useridColIndex));
		}

		initImageLoader(getApplicationContext());

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