package com.gf.test.videoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gf.test.videoplayer.R;
import com.gf.test.videoplayer.entity.Alarm;
import com.gf.test.videoplayer.entity.Board;

import java.util.List;

public class AlarmsAdapter extends BaseAdapter {
    private List<Alarm> itemList;
    public List<Alarm> getItemList() {
        return itemList;
    }
    public void setItemList(List<Alarm> itemList) {
        this.itemList = itemList;
    }

    private Context context;
    private LayoutInflater inflater;
    public AlarmsAdapter(Context context){
        this.context = context;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return itemList == null ? 0 : itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if(convertView == null){
            convertView  = inflater.inflate(R.layout.alarm_item,null);
            vh = new ViewHolder();
            vh.head = (TextView) convertView.findViewById(R.id.alarm_item_title);
            vh.type = (TextView) convertView.findViewById(R.id.alarm_item_type);
            convertView.setTag(vh);
        }else {
            vh = (ViewHolder) convertView.getTag();
        }
        Alarm item = itemList.get(position);
        vh.head.setText("警告消息"+position);
        vh.type.setText(itemList.get(position).getType());
        return convertView;
    }
    private class ViewHolder {
        TextView head,type;
    }
}
