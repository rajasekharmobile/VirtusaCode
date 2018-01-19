package coding.virtusa.virtusacodingchallenge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by rajasekharmittapalli on 1/19/18.
 */

public class ListAdapter extends BaseAdapter {
    private ISSResponse mISSResponse;
    private LayoutInflater mInflater;
    public ListAdapter(LayoutInflater inflater, ISSResponse response) {
        mISSResponse = response;
        mInflater = inflater;
    }

    @Override
    public int getCount() {
        return mISSResponse.mTimings.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView  = mInflater.inflate(android.R.layout.simple_list_item_1,null,false);
        }

        //@TODO add viewholder pattern
        TextView lTextView = (TextView)convertView.findViewById(android.R.id.text1);

        lTextView.setText("Date: "+getDateCurrentTimeZone(Long.parseLong(mISSResponse.mTimings.get(position).risetime))+" Duration: "+mISSResponse.mTimings.get(position).duration+" MS");

        return convertView;
    }

    public  String getDateCurrentTimeZone(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp * 1000);
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        }catch (Exception e) {
        }
        return "";
    }

}
