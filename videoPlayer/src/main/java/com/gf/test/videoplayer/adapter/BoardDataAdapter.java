package com.gf.test.videoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gf.test.videoplayer.R;
import com.gf.test.videoplayer.entity.Board;
import com.gf.test.videoplayer.entity.BoardData;

import java.util.List;

public class BoardDataAdapter extends BaseAdapter {
    private List<BoardData> itemList;
    public List<BoardData> getItemList() {
        return itemList;
    }
    public void setItemList(List<BoardData> itemList) {
        this.itemList = itemList;
    }

    private Context context;
    private LayoutInflater inflater;
    public BoardDataAdapter(Context context){
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
        BoardDataAdapter.ViewHolder vh = null;
        if(convertView == null){
            convertView  = inflater.inflate(R.layout.board_data_item,null);
            vh = new BoardDataAdapter.ViewHolder();
            vh.boardDataTitle = (TextView) convertView.findViewById(R.id.board_data_item_title);
            vh.boardDataLocation = (TextView) convertView.findViewById(R.id.board_data_item_location);
            vh.boardDataTime = (TextView) convertView.findViewById(R.id.board_data_item_time);
            vh.boardDataTemp = (TextView) convertView.findViewById(R.id.board_data_item_temp);
            vh.boardDataHumi = (TextView) convertView.findViewById(R.id.board_data_item_humi);
            convertView.setTag(vh);
        }else {
            vh = (BoardDataAdapter.ViewHolder) convertView.getTag();
        }
        BoardData item = itemList.get(position);
        vh.boardDataTitle.setText("节点"+item.boardName+":"+position);
        vh.boardDataLocation.setText("地点:"+item.boardLocation);
        vh.boardDataTime.setText("时间:"+item.year+"年"+item.month+"月"+item.date+"日"+"周"+item.weekDay+" "+item.time);
        vh.boardDataTemp.setText("温度:"+item.temp);
        vh.boardDataHumi.setText("湿度:"+item.humi);
        return convertView;
    }
    private class ViewHolder {
        TextView boardDataTitle,boardDataTime,boardDataLocation,boardDataTemp,boardDataHumi;
    }
}