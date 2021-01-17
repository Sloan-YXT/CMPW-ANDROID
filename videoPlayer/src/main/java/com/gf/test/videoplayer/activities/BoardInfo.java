package com.gf.test.videoplayer.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Trace;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gf.test.videoplayer.R;
import com.gf.test.videoplayer.entity.Board;
import com.gf.test.videoplayer.entity.BoardData;
import com.gf.test.videoplayer.entity.Constant;
import com.gf.test.videoplayer.entity.SocketStation;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class BoardInfo extends Activity {
    private TextView temp, humi, name, location;
    private Button alarm, video, monthData;
    private int needToShut =1;
    Board board;
    SharedPreferences nodeDataNum;
    SharedPreferences.Editor editor;
    File fileRoot;
    ImageView alarm_sign;
    ImageView backGroud;
    TextView noGood;
    //Button btn = findViewById(R.id.gr)
    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 200:
                    try {
                        String data = msg.obj.toString();
                        JSONObject jdata = new JSONObject(data);
                        String type = jdata.getString("type");
                        Log.e("BoardInfo",msg.obj.toString());
                        if(type.equals("cmd"))
                        {
                            String content = jdata.getString("content");
                            if(content.equals("breset"))
                            {
                                SocketStation.connfdOther.close();
                                SocketStation.connfdGraph.close();
                                SocketStation.connfdData.close();
                                Toast.makeText(BoardInfo.this, "节点连接断开！", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(BoardInfo.this,MainActivity.class);
                                startActivity(intent);
                                needToShut = 0;
                                finish();
                            }
                        }
                        else if(type.equals("data"))
                        {
                            String temp = jdata.getString("temp");
                            String humi = jdata.getString("humi");
                            String location = jdata.getString("position");
                            BoardInfo.this.temp.setText("温度："+temp+"℃");
                            BoardInfo.this.humi.setText("湿度："+humi+"%RH");
                            BoardInfo.this.location.setText("地点:" + location);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.e("BoardInfo",Log.getStackTraceString(e));
                    }
                    break;
                case 300:
                        Log.e("BoardInfo","Alarm message:"+msg.obj.toString());
                        if(msg.obj.toString().equals("0"))
                        {
                            noGood.setText("当前无异常");
                            alarm_sign.setVisibility(View.INVISIBLE);
                            backGroud.setImageResource(R.drawable.ok);
                        }
                        else{
                            noGood.setText("有警告消息未查看！");
                            alarm_sign.setVisibility(View.VISIBLE);
                            backGroud.setImageResource(R.drawable.not_ok);
                        }
                        break;
                case 500:
                    Toast.makeText(BoardInfo.this,"Connection with server has been shutdown",Toast.LENGTH_SHORT).show();
                    Log.e("BoardInfo","Server has been shutdown");
                    try {
                        Thread.sleep(3000);
                    }
                    catch (Exception e)
                    {
                        Log.e("BoardInfo",Log.getStackTraceString(e));
                    }
                    SocketStation.exit();
                    break;
                default:
                    Toast.makeText(BoardInfo.this, "数据请求失败！", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        board = (Board) intent.getSerializableExtra("node");
        setContentView(R.layout.board_watch);
        SocketStation.activitys.add(this);
        temp = findViewById(R.id.temp_data);
        humi = findViewById(R.id.humi_data);
        name = findViewById(R.id.board_name);
        humi.setText("还未收到数据");
        temp.setText("还未收到数据");
        location = findViewById(R.id.board_location);
        name.setText("名称:" + board.boardName);
        location.setTextColor(Color.RED);
        location.setText("地点:" + board.boardLocation);
        location.setTextColor(Color.BLUE);
        alarm = findViewById(R.id.alarm_btn);
        video = findViewById(R.id.tv_btn);
        noGood = findViewById(R.id.noGoodText);
        backGroud = findViewById(R.id.imageSmile);
        //nodeDataNum =  getSharedPreferences(Constant.SHARED_PREFERENCE_NAME,MODE_PRIVATE);
        //editor = nodeDataNum.edit();
        monthData = findViewById(R.id.mdata_btn);
        fileRoot = new File("/sdcard/CMPW/"+board.boardName);
        alarm_sign = findViewById(R.id.alarm_img);
        if(!fileRoot.exists())
        {
            fileRoot.mkdirs();
        }
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BoardInfo.this, JavaActivity.class);
                String vaddr = "rtmp://"+Constant.ip+"/livevideo"+SocketStation.vCode;
                intent.putExtra("extra_url",vaddr);
                startActivity(intent);
            }
        });
        monthData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(BoardInfo.this, BoardDataActivity.class);
                intent1.putExtra("board name",board.boardName);
                startActivity(intent1);
            }
        });
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OutputStream outOther = SocketStation.connfdOther.getOutputStream();
                            JSONObject connData = new JSONObject();
                            connData.put("type", "connect");
                            connData.put("board name", board.boardName);
                            connData.put("client name", "Anymous");//for the time being no certification verification is needed
                            int len = connData.toString().getBytes().length;
                            byte [] lenSend = SocketStation.intToByte(len);
                            outOther.write(lenSend);
                            outOther.write(connData.toString().getBytes());
                            InputStream inOther = SocketStation.connfdOther.getInputStream();
                            int [] vcode_dir = new int[4];
                            for(int i=0;i<4;i++)
                            {
                                vcode_dir[i] = 0;
                                vcode_dir[i] = inOther.read();
                            }
                            SocketStation.vCode = SocketStation.byteToInt(vcode_dir);
                            Log.e("BoardInfo",String.valueOf(SocketStation.vCode));

                        }
                        catch (SocketException e)
                        {
                            Log.e("BoardInfo",Log.getStackTraceString(e));
                            handler.sendEmptyMessage(500);
                        }
                        catch (Exception e) {
                            Log.e("BoardInfo",Log.getStackTraceString(e));
                        }
                    }
                }
        ).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while ((true)) {
                    try {
                        int num;
                        File rootDir = new File(fileRoot.toString());
                        Log.e("BoardInfo", "rootDir:" + rootDir.toString());
                        File[] files = rootDir.listFiles();
                        if (files == null) {
                            num = 0;
                        } else {
                            num = files.length;
                        }
                        Message msg = handler.obtainMessage();
                        msg.what = 300;
                        msg.obj = num;
                        handler.sendMessage(msg);
                        Thread.sleep(500);
                    }
                    catch (Exception e)
                    {
                        Log.e("BoardInfo",Log.getStackTraceString(e));
                    }
                }
            }
        }).start();
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        int num;
                        try {
                            while (true) {
                                //num = nodeDataNum.getInt("graph num", 0);

                                int len;
                                int[] preLen = new int[4];
                                InputStream in_graph = SocketStation.connfdGraph.getInputStream();
                                //DataInputStream in = new DataInputStream(in_graph);
                                for(int i=0;i<4;i++)
                                {
                                    preLen[i] = in_graph.read();
                                    Log.e("BoardInfo","prelen element=="+String.valueOf(preLen[i]));
                                }
                                len = SocketStation.byteToInt(preLen);
                                Log.e("BoardInfo","!!!!!!!!!!!!Len = "+String.valueOf(len));
                                if(len==-1)
                                {
                                    Log.e("BoardInfo",String.valueOf(SocketStation.connfdGraph.isClosed()));
                                    handler.sendEmptyMessage(500);
                                    Thread.sleep(10000);
                                    continue;
                                }
                                String picturePath = fileRoot+"/"+String.valueOf(System.currentTimeMillis())+".jpg";
                                Log.e("BoardInfo","preparing to write picture to"+picturePath);
                                FileOutputStream outPicture = new FileOutputStream(picturePath);
                                int data;
                                int rest = len;
                                for(int i=0;i<len;i++)
                                {
                                    data = in_graph.read();
                                    outPicture.write(data);
                                }
                                outPicture.close();
                                //num++;
                                //editor.putInt("graph num",num);
                                //editor.apply();
                            }
                        }
                        catch (SocketException e)
                        {
                            Log.e("BoardInfo",Log.getStackTraceString(e));
                            handler.sendEmptyMessage(500);
                        }
                        catch (Exception e)
                        {
                            Log.e("BoardInfo",Log.getStackTraceString(e));
                        }
                    }
                }
        ).start();
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InputStream dataIn = SocketStation.connfdData.getInputStream();
                            int[] lenPre = new int[4];
                            int len;
                            while(true) {
                                //Thread.sleep(1000);
                                Log.e("BoardInfo","before recv len");
                                for(int i=0;i<4;i++)
                                {
                                    lenPre[i] = dataIn.read();
                                }
                                Log.e("BoardInfo","After recv len:"+lenPre);
                                len = SocketStation.byteToInt(lenPre);
                                if(len==-1)
                                {
                                    Log.e("BoardInfo",String.valueOf(SocketStation.connfdGraph.isClosed()));
                                    handler.sendEmptyMessage(500);
                                    Thread.sleep(10000);
                                    continue;
                                }
                                Log.e("BoardInfo","After transform len:"+len);
                                byte[] data = new byte[len];
                                for(int i=0;i<len;i++)
                                {
                                    data[i] = (byte)dataIn.read();
                                    if(data[i]==-1)
                                    {
                                        Log.e("BoardInfo",String.valueOf(SocketStation.connfdGraph.isClosed()));
                                        handler.sendEmptyMessage(500);
                                        Thread.sleep(10000);
                                        continue;
                                    }
                                }
                                Message msg = handler.obtainMessage();
                                msg.obj = new String(data, "utf8");
                                msg.what = 200;
                                handler.sendMessage(msg);
                            }
                        }
                        catch (Exception e)
                        {
                            Log.e("BoardInfo",Log.getStackTraceString(e));
                        }
                    }
                }
        ).start();
        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(BoardInfo.this,AlarmActivity.class);
                intent1.putExtra("path","/sdcard/CMPW/"+board.boardName);
                startActivity(intent1);
            }
        });
    }
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        try {
//            SocketStation.connfdOther.close();
//            SocketStation.connfdData.close();
//            SocketStation.connfdGraph.close();
//            Thread.sleep(100);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        SocketStation.connfdData = new Socket(Constant.ip, Constant.portData);
//                        SocketStation.connfdGraph = new Socket(Constant.ip, Constant.portGraph);
//                        SocketStation.connfdOther = new Socket(Constant.ip, Constant.portOther);
//                    }
//                    catch (Exception e)
//                    {
//                        Log.e("BoardInfo",Log.getStackTraceString(e));
//                    }
//                }
//            }).start();
//
//        }
//        catch (Exception e)
//        {
//            Log.e("BoardInfo",Log.getStackTraceString(e));
//        }
//        return super.onKeyDown(keyCode, event);
//    }


    @Override
    protected void onDestroy() {
        try {
            if(needToShut==1) {
                SocketStation.connfdOther.close();
                SocketStation.connfdGraph.close();
                SocketStation.connfdData.close();
            }
        }
        catch (Exception e)
        {
            Log.e("BoardInfo",Log.getStackTraceString(e));
        }
        SocketStation.activitys.remove(this);
        super.onDestroy();
    }
}