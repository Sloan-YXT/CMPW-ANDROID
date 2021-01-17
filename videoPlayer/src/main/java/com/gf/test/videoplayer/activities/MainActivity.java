package com.gf.test.videoplayer.activities;
import java.io.File;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gf.test.videoplayer.R;
import com.gf.test.videoplayer.adapter.BoardsAdapter;
import com.gf.test.videoplayer.entity.Alarm;
import com.gf.test.videoplayer.entity.Board;
import com.gf.test.videoplayer.entity.Constant;
import com.gf.test.videoplayer.entity.SocketStation;

import javax.xml.transform.Result;

import momo.cn.edu.fjnu.androidutils.utils.StorageUtils;

public class MainActivity extends Activity {
	private ListView lv;
	private List<Board> listData = new ArrayList<Board>();
	private BoardsAdapter adapter;
	private int hasConnected = 0;
	LinearLayout footer;
	TextView footerNotify;
	private Handler handler =  new Handler(Looper.myLooper())
	{
		@Override
		public void handleMessage(@NonNull Message msg) {
			switch (msg.what)
			{
				case 200:
					//修改List后再notify,否则实验发现notify 2次size为0，不正确--1.10
					//当前版本的安卓api修改一次链表notify一次，notify后不能再修改链表
					adapter.notifyDataSetChanged();
					if(listData.size()==0)
					{
						Toast.makeText(MainActivity.this, "暂时没有节点存在，请稍后重新登陆！", Toast.LENGTH_SHORT).show();
					}
					break;
				default:
					Toast.makeText(MainActivity.this,"数据请求失败！",Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.boards);
		SocketStation.activitys.add(this);
		PackageManager pm = getPackageManager();
		boolean permission;
		String sdStatus = Environment.getExternalStorageState();
		permission = PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", "com.zhengyuan.emcarsend");
		if (permission) {
			//"有这个权限"
			//Toast.makeText(Carout.this, "有权限", Toast.LENGTH_SHORT).show();
		} else {
			//"木有这个权限"
			//如果android版本大于等于6.0，权限需要动态申请
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 15);
			}
		}
		permission = PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.READ_EXTERNAL_STORAGE", "com.zhengyuan.emcarsend");
		if (permission) {
			//"有这个权限"
			//Toast.makeText(Carout.this, "有权限", Toast.LENGTH_SHORT).show();
		} else {
			//"木有这个权限"
			//如果android版本大于等于6.0，权限需要动态申请
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 15);
			}
		}

		File file = new File("/sdcard/CMPW");
		if(!file.mkdirs())
		{
			Log.e("MainActivity","mk root dir failed");
		}
		adapter = new BoardsAdapter(this);
		lv = (ListView) findViewById(R.id.lv);

		lv.setAdapter(adapter);
		adapter.setItemList(listData);
		footer = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.graph_foot, null);
		footerNotify = (TextView) footer.findViewById(R.id.graph_footer_text);
		lv.addFooterView(footer);
		lv.setFooterDividersEnabled(false);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
		int position, long id) {

				Board node = listData.get(position);
				Bundle data = new Bundle();
				data.putSerializable("node", node);
				Intent intent = new Intent(MainActivity.this, BoardInfo.class);
				intent.putExtras(data);
				MainActivity.this.startActivity(intent);
				hasConnected = 1;
				MainActivity.this.finish();
		}
		});
		lv.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
				{
					try {
						Log.e("MainActivity","ListView被刷新！");
						SocketStation.connfdOther.close();
						SocketStation.connfdData.close();
						SocketStation.connfdGraph.close();
						new Thread(new Runnable() {
							@Override
							public void run(){
								try {
									//不能把设置为Adapter List的list清空，只能重新建立一个List
									//估计原因是Adapter会不断试图访问List，而不是只访问一次
									listData = new ArrayList<Board>();
									SocketStation.connfdData = new Socket(Constant.ip, Constant.portData);
									SocketStation.connfdGraph = new Socket(Constant.ip, Constant.portGraph);
									SocketStation.connfdOther = new Socket(Constant.ip, Constant.portOther);
									int len;
									Log.e("Socket","85");
									InputStream in_boards = SocketStation.connfdOther.getInputStream();
									int[] len_pre = new int[4];
									for(int i=0;i<4;i++)
									{
										len_pre[i] = in_boards.read();
									}
									len = SocketStation.byteToInt(len_pre);
									Log.e("Socket", "len:" + len);
									byte[] boardData = new byte[len];
									in_boards.read(boardData, 0, len);
									String data = new String(boardData, "utf8");
									JSONObject data_handler = new JSONObject(data);
									int numer = data_handler.getInt("num");
									JSONArray nodes;
									if(!String.valueOf(data_handler.get("nodes")).equals("null")) {
										Log.e("MainActivity",String.valueOf(data_handler.get("nodes")));
										nodes = data_handler.getJSONArray("nodes");
									}
									else
									{
										nodes = new JSONArray();
									}
									for (int i = 0; i < numer; i++) {
										Board node = new Board();
										JSONObject j = nodes.getJSONObject(i);
										node.boardName = j.getString("name");
										node.boardLocation = j.getString("position");
										listData.add(node);
									}
									//adapter.setItemList(listData);
									Log.e("Socket",data);
									adapter.setItemList(listData);
									//adapter.notifyDataSetChanged();
									handler.sendEmptyMessage(200);
								}
								catch(Exception e)
								{
									Log.e("Socket", Log.getStackTraceString(e));
								}}
						} ).start();
					}
					catch (Exception e)
					{
						Log.e("MainActivity",Log.getStackTraceString(e));
					}
					MainActivity.this.footerNotify.setVisibility(View.VISIBLE);
				}
				else if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
				{
					MainActivity.this.footerNotify.setVisibility(View.INVISIBLE);
				}

			}

			@Override
			public void onScroll(AbsListView absListView, int i, int i1, int i2) {

			}
		});
		new Thread(new Runnable() {
			@Override
			public void run(){
				try {
					SocketStation.connfdData = new Socket(Constant.ip, Constant.portData);
					SocketStation.connfdGraph = new Socket(Constant.ip, Constant.portGraph);
					SocketStation.connfdOther = new Socket(Constant.ip, Constant.portOther);
					int len;
					Log.e("Socket","85");
					InputStream in_boards = SocketStation.connfdOther.getInputStream();
					int[] len_pre = new int[4];
					for(int i=0;i<4;i++)
					{
						len_pre[i] = in_boards.read();
					}
					len = SocketStation.byteToInt(len_pre);
					Log.e("Socket", "len:" + len);
					byte[] boardData = new byte[len];
					in_boards.read(boardData, 0, len);
					String data = new String(boardData, "utf8");
					Log.e("MainActivity","recv:"+data);
					JSONObject data_handler = new JSONObject(data);
					int numer = data_handler.getInt("num");
					JSONArray nodes;
					if(!String.valueOf(data_handler.get("nodes")).equals("null")) {
						Log.e("MainActivity",String.valueOf(data_handler.get("nodes")));
						nodes = data_handler.getJSONArray("nodes");
					}
					else
					{
						nodes = new JSONArray();
					}
					for (int i = 0; i < numer; i++) {
						Board node = new Board();
						JSONObject j = nodes.getJSONObject(i);
						node.boardName = j.getString("name");
						node.boardLocation = j.getString("position");
						listData.add(node);
					}
					Log.e("Socket",data);
					//adapter.notifyDataSetChanged();
					handler.sendEmptyMessage(200);
				}
			catch(Exception e)
			{
				Log.e("Socket", Log.getStackTraceString(e));
			}}
		} ).start();
	}
	@Override
	protected void onDestroy() {
		try {
			if(hasConnected==0) {
				SocketStation.connfdOther.close();
				SocketStation.connfdData.close();
				SocketStation.connfdGraph.close();
				Log.e("MainActivity","socket closed");
			}
			else
			{
				Log.e("MainActivity","socket not closed");
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
