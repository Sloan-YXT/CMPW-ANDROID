package com.gf.test.videoplayer.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gf.test.videoplayer.R;
import com.gf.test.videoplayer.adapter.AlarmsAdapter;
import com.gf.test.videoplayer.entity.Alarm;
import com.gf.test.videoplayer.entity.Board;
import com.gf.test.videoplayer.entity.Constant;
import com.gf.test.videoplayer.entity.SocketStation;

import java.io.File;
import java.util.ArrayList;

public class AlarmActivity extends Activity {
    ListView alarms;
    AlarmsAdapter alarmsAdapter;
    SharedPreferences sdata;
    SharedPreferences.Editor editor;
    LinearLayout footer;
    TextView footerNotify;
    File rootDir;
    ArrayList<Alarm> alarmList = new ArrayList<Alarm>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        SocketStation.activitys.add(this);
        alarms = (ListView)findViewById(R.id.alarms);
        alarmsAdapter = new AlarmsAdapter(this);
        alarms.setAdapter(alarmsAdapter);
        footer = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.graph_foot, null);
        footerNotify = (TextView) footer.findViewById(R.id.graph_footer_text);
        alarms.addFooterView(footer);
        alarms.setFooterDividersEnabled(false);
        sdata =  getSharedPreferences(Constant.SHARED_PREFERENCE_NAME,MODE_PRIVATE);
        //int num = sdata.getInt("graph num",0);
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
         rootDir = new File(path);
         if(!rootDir.exists())
         {
             Log.e("AlarmActivity","root DIR not exists");
             if(!rootDir.mkdirs())
             {
                 Log.e("AlarmActivity","mkdir failed");
             }
         }
        File []graphs = rootDir.listFiles();
        int num;
        if(graphs!=null) {
             num = graphs.length;
         }
         else
         {
             Log.e("AlarmActivity",rootDir.getAbsolutePath());
             Log.e("AlarmActivity",String.valueOf(rootDir.isDirectory()));
             num=0;
         }
        for(int i=0;i<num;i++)
        {
            Alarm node = new Alarm();
            node.setType("图像");
            node.setPath(graphs[i].getPath());
            alarmList.add(node);
        }
        alarmsAdapter.setItemList(alarmList);
        alarmsAdapter.notifyDataSetChanged();
        if(alarmList.size()==0)
        {
            Toast.makeText(AlarmActivity.this, "暂时没有报警消息", Toast.LENGTH_SHORT).show();
            try {
                Thread.sleep(1);
            }
            catch (Exception e)
            {
                Log.e("AlarmActivity",Log.getStackTraceString(e));
            }
            //AlarmActivity.this.finish();
        }
        alarms.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                {
                    File []graphs = AlarmActivity.this.rootDir.listFiles();
                    int num = graphs.length;
                    alarmList = new ArrayList<Alarm>();
                    for(int i=0;i<num;i++)
                    {
                        Alarm node = new Alarm();
                        node.setType("图像");
                        node.setPath(graphs[i].getPath());
                        alarmList.add(node);
                    }
                    AlarmActivity.this.footerNotify.setVisibility(View.VISIBLE);
                    alarmsAdapter.setItemList(alarmList);
                }
                else if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
                {
                    alarmsAdapter.notifyDataSetChanged();
                    if(alarmList.size()==0)
                    {
                        Toast.makeText(AlarmActivity.this, "暂时没有报警消息", Toast.LENGTH_SHORT).show();
                        try {
                            Thread.sleep(1);
                        }
                        catch (Exception e)
                        {
                            Log.e("AlarmActivity",Log.getStackTraceString(e));
                        }
                        //AlarmActivity.this.finish();
                    }
                    AlarmActivity.this.footerNotify.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
        alarms.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Alarm node = alarmList.get(position);
                Bundle data = new Bundle();
                data.putSerializable("path",node.getPath());
                Intent intent = new Intent(AlarmActivity.this, GraphShow.class);
                intent.putExtras(data);
                AlarmActivity.this.startActivity(intent);
                AlarmActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        SocketStation.activitys.remove(this);
        super.onDestroy();
    }
}