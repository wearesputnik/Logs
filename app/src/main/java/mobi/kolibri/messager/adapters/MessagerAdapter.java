package mobi.kolibri.messager.adapters;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.activity.ChatItemActivity;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.MessagInfo;
import mobi.kolibri.messager.object.SQLMessager;

/**
 * Created by root on 01.09.15.
 */
public class MessagerAdapter extends ArrayAdapter<MessagInfo>{
    List<MessagInfo> listItem;
    Context contV;
    String user_id;
    Bitmap m_currentBitmap;
    SQLMessager sqlMessager;
    SQLiteDatabase db;
    private DisplayImageOptions options;
    private HashMap<TextView,CountDownTimer> counters;

    public MessagerAdapter (Context context, String user_id_A) {
        super(context, 0);
        listItem = new ArrayList<MessagInfo>();
        contV = context;
        user_id = user_id_A;
        options = new DisplayImageOptions.Builder()
                //.showImageOnLoading(R.mipmap.profile_min)
                // .showImageForEmptyUri(R.mipmap.profile_min)
                // .showImageOnFail(R.mipmap.profile_min)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        this.counters = new HashMap<TextView, CountDownTimer>();
        sqlMessager = new SQLMessager(context);
        db = sqlMessager.getWritableDatabase();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final MessagInfo item = getItem(position);


        View v = convertView;
        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.messager_item, null);
            ViewHolder holder = new ViewHolder();
            holder.rlToMessager = (RelativeLayout) v.findViewById(R.id.rlToMessager);
            holder.rlFromMesseger = (RelativeLayout) v.findViewById(R.id.rlFromMesseger);
            holder.textToMessager = (TextView) v.findViewById(R.id.textToMessager);
            holder.textFromMessager = (TextView) v.findViewById(R.id.textFromMessager);
            holder.textViewTimeTo = (TextView) v.findViewById(R.id.textViewTimeTo);
            holder.textViewTimeFrom = (TextView) v.findViewById(R.id.textViewTimeFrom);
            holder.messagePhoto1 = (ImageView) v.findViewById(R.id.messagePhoto1);
            holder.messagePhoto2 = (ImageView) v.findViewById(R.id.messagePhoto2);
            v.setTag(holder);
        }

        final ViewHolder holder = (ViewHolder) v.getTag();

        if (item.id_from.equals(user_id)) {
            holder.rlToMessager.setVisibility(View.VISIBLE);
            holder.rlFromMesseger.setVisibility(View.GONE);
            holder.textToMessager.setText(item.message);
            holder.textToMessager.setVisibility(View.VISIBLE);
            holder.textViewTimeTo.setText(item.created + " " + item.duration);
            holder.messagePhoto2.setVisibility(View.GONE);
            if (!item.attachment.equals("")) {
                //holder.textToMessager.setVisibility(View.GONE);
                holder.messagePhoto2.setVisibility(View.VISIBLE);
                String url_img = HttpConnectRecive.URLP + item.attachment;
                ImageLoader.getInstance()
                    .displayImage(url_img, holder.messagePhoto2, options, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            if (!item.duration.equals("5")) {
                                if (getImageUri(contV, loadedImage) != null) {
                                    item.duration = "5";
                                    ContentValues cv_ms = new ContentValues();
                                    cv_ms.put(SQLMessager.MESSAGER_DURATION, "5");
                                    db.update(SQLMessager.TABLE_MESSAGER, cv_ms, "id=?", new String[]{"" + item.id_messege});
                                }
                            }
                        }
                    }, new ImageLoadingProgressListener() {
                        @Override
                        public void onProgressUpdate(String imageUri, View view, int current, int total) {

                        }
                    });
                holder.messagePhoto2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Dialog dialog = new Dialog(contV);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_chat_photo);
                        final ImageView imgPhotoChat = (ImageView) dialog.findViewById(R.id.imageView7);

                        if (item.attachment != null) {
                            String url_img = HttpConnectRecive.URLP + item.attachment;
                            ImageLoader.getInstance()
                                .displayImage(url_img, imgPhotoChat, options, new SimpleImageLoadingListener() {
                                    @Override
                                    public void onLoadingStarted(String imageUri, View view) {

                                    }

                                    @Override
                                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                                    }

                                    @Override
                                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {


                                    }
                                }, new ImageLoadingProgressListener() {
                                    @Override
                                    public void onProgressUpdate(String imageUri, View view, int current, int total) {

                                    }
                                });
                        }
                        else {
                            dialog.dismiss();
                        }

                        imgPhotoChat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                imgPhotoChat.setImageBitmap(null);
                                notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                });


            }

        }
        else {
            holder.rlToMessager.setVisibility(View.GONE);
            holder.rlFromMesseger.setVisibility(View.VISIBLE);
            holder.textFromMessager.setText(item.message);
            holder.textViewTimeFrom.setText(item.created);
            holder.textFromMessager.setVisibility(View.VISIBLE);
            holder.messagePhoto1.setVisibility(View.GONE);
            if (item.attachment != null) {
                holder.textFromMessager.setVisibility(View.GONE);
                holder.messagePhoto1.setVisibility(View.VISIBLE);
                holder.messagePhoto1.setImageBitmap(LoadBitmap(item.attachment, m_currentBitmap, 800, 850));
                holder.messagePhoto1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Dialog dialog = new Dialog(contV);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_chat_photo);
                        final ImageView imgPhotoChat = (ImageView) dialog.findViewById(R.id.imageView7);

                        if (item.attachment != null) {
                            imgPhotoChat.setImageBitmap(LoadBitmapFull(item.attachment, m_currentBitmap));
                        }
                        else {
                            dialog.dismiss();
                        }

                        imgPhotoChat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                imgPhotoChat.setImageURI(null);
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                });
            }
        }

        return v;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(),
                inImage, "LogsMesager", null);
        return Uri.parse(path);
    }

    private static Bitmap LoadBitmapFull(String localPath, Bitmap bitmapToReuse)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(localPath, options);

        options.inJustDecodeBounds = false;
        options.inMutable = true;
        options.inBitmap = bitmapToReuse;

        Bitmap newBitmap = BitmapFactory.decodeFile(localPath, options);

        return newBitmap;
    }

    private static Bitmap LoadBitmap(String localPath, Bitmap bitmapToReuse, int width, int height)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(localPath, options);

        options.inSampleSize = CalculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        options.inMutable = true;
        options.inBitmap = bitmapToReuse;

        Bitmap newBitmap = BitmapFactory.decodeFile(localPath, options);

        return newBitmap;
    }

    private static int CalculateInSampleSize(BitmapFactory.Options options, int maxWidth, int maxHeight)
    {
        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        int inSampleSize = 1;

        if (actualHeight > maxHeight || actualWidth > maxWidth)
        {
            while ((actualHeight / inSampleSize) > maxHeight && (actualWidth / inSampleSize) > maxWidth)
                inSampleSize *= 2;
        }

        return inSampleSize;
    }

    class ViewHolder {
        RelativeLayout rlToMessager;
        RelativeLayout rlFromMesseger;
        TextView textToMessager;
        TextView textFromMessager;
        TextView textViewTimeTo;
        TextView textViewTimeFrom;
        ImageView messagePhoto1;
        ImageView messagePhoto2;
    }

}
