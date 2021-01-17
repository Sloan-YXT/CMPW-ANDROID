package com.gf.test.videoplayer.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gf.test.videoplayer.R;
import com.gf.test.videoplayer.entity.Alarm;
import com.gf.test.videoplayer.entity.BoardData;
import com.gf.test.videoplayer.entity.Constant;
import com.gf.test.videoplayer.entity.SocketStation;

import java.io.File;

public class GraphShow extends Activity {


    SharedPreferences data ;
    SharedPreferences.Editor editor ;
    ImageView graph ;
    Button btn_del ;
    Button btn_bck ;
    String path,rootDir;
    File file;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_show);
        SocketStation.activitys.add(this);
        //data  = getSharedPreferences(Constant.SHARED_PREFERENCE_NAME,MODE_PRIVATE);
        graph = (ImageView) findViewById(R.id.graph_data);
        btn_del = (Button)findViewById(R.id.graph_delete);
        btn_bck = (Button)findViewById(R.id.graph_back);
        //editor = data.edit();
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        rootDir = path.substring(0,path.lastIndexOf('/'));
        file = new File(path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        btn_bck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(GraphShow.this, AlarmActivity.class);
                intent1.putExtra("path",GraphShow.this.rootDir);
                startActivity(intent1);
                GraphShow.this.finish();
            }
        });
        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
        graph.setImageBitmap(bitmap);
        btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(file.exists()&&file.isFile())
                {
                    file.delete();
                    Toast.makeText(GraphShow.this,path+" has been deleted!",Toast.LENGTH_SHORT);
                    Intent intent1 = new Intent(GraphShow.this, AlarmActivity.class);
                    intent1.putExtra("path",GraphShow.this.rootDir);
                    startActivity(intent1);
                    GraphShow.this.finish();
                    //finish();
                }
                else
                {
                    Toast.makeText(GraphShow.this,path+" doesn't exit!",Toast.LENGTH_SHORT);
                    Intent intent1 = new Intent(GraphShow.this, AlarmActivity.class);
                    intent1.putExtra("path",GraphShow.this.rootDir);
                    startActivity(intent1);
                    GraphShow.this.finish();
                    //finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        SocketStation.activitys.remove(this);
        super.onDestroy();
    }
}