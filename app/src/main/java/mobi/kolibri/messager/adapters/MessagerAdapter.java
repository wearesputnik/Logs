package mobi.kolibri.messager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
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

    public MessagerAdapter (Context context, String user_id_A) {
        super(context, 0);
        listItem = new ArrayList<MessagInfo>();
        contV = context;
        user_id = user_id_A;
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
            v.setTag(holder);
        }

        final ViewHolder holder = (ViewHolder) v.getTag();

        if (item.id_from.equals(user_id)) {
            holder.rlToMessager.setVisibility(View.VISIBLE);
            holder.rlFromMesseger.setVisibility(View.GONE);
            holder.textToMessager.setText(item.message);
        }
        else {
            holder.rlToMessager.setVisibility(View.GONE);
            holder.rlFromMesseger.setVisibility(View.VISIBLE);
            holder.textFromMessager.setText(item.message);
        }

        return v;
    }

    class ViewHolder {
        RelativeLayout rlToMessager;
        RelativeLayout rlFromMesseger;
        TextView textToMessager;
        TextView textFromMessager;
    }
}
