package mobi.kolibri.messager.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mobi.kolibri.messager.R;
import mobi.kolibri.messager.http.HttpConnectRecive;
import mobi.kolibri.messager.object.MessagInfo;

/**
 * Created by root on 01.09.15.
 */
public class MessagerAdapter extends ArrayAdapter<MessagInfo>{
    List<MessagInfo> listItem;
    Context contV;
    String user_id;
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
                .cacheInMemory(false)
                .cacheOnDisk(false)
                .considerExifParams(false)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        this.counters = new HashMap<TextView, CountDownTimer>();
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
            holder.textToTimer = (TextView) v.findViewById(R.id.textView6);
            holder.messagePhoto1 = (ImageView) v.findViewById(R.id.messagePhoto1);
            holder.messagePhoto2 = (ImageView) v.findViewById(R.id.messagePhoto2);
            v.setTag(holder);
        }

        final ViewHolder holder = (ViewHolder) v.getTag();

        if (item.id_from.equals(user_id)) {
            holder.rlToMessager.setVisibility(View.VISIBLE);
            holder.rlFromMesseger.setVisibility(View.GONE);
            holder.textToMessager.setText(item.message);
            holder.messagePhoto2.setVisibility(View.GONE);
            holder.textToTimer.setVisibility(View.GONE);
            if (!item.attachment.equals("")) {
                holder.messagePhoto2.setVisibility(View.VISIBLE);
                ///holder.textToTimer.setVisibility(View.VISIBLE);

                /*final TextView tv = holder.textToTimer;

                CountDownTimer cdt = counters.get(holder.textToTimer);
                if(cdt!=null)
                {
                    cdt.cancel();
                    cdt=null;
                }
                long difference = Integer.parseInt(item.duration) * 1000;

                cdt = new CountDownTimer(difference, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        tv.setText("" + millisUntilFinished / 1000);
                    }

                    @Override
                    public void onFinish() {
                        tv.setText("Finished");

                    }
                };

                counters.put(tv, cdt);*/

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


                        }
                    }, new ImageLoadingProgressListener() {
                        @Override
                        public void onProgressUpdate(String imageUri, View view, int current, int total) {

                        }
                    });

                ///cdt.start();

            }

        }
        else {
            holder.rlToMessager.setVisibility(View.GONE);
            holder.rlFromMesseger.setVisibility(View.VISIBLE);
            holder.textFromMessager.setText(item.message);
            holder.messagePhoto1.setVisibility(View.GONE);
            if (item.attachment != null) {
                holder.messagePhoto1.setVisibility(View.VISIBLE);
                holder.messagePhoto1.setImageBitmap(BitmapFactory.decodeFile(item.attachment));
            }
        }

        return v;
    }

    class ViewHolder {
        RelativeLayout rlToMessager;
        RelativeLayout rlFromMesseger;
        TextView textToMessager;
        TextView textFromMessager;
        TextView textToTimer;
        ImageView messagePhoto1;
        ImageView messagePhoto2;
    }

}
