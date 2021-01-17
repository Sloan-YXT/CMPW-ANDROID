package com.gf.test.videoplayer.entity;

import android.app.Activity;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketStation {
    public static Socket connfdData,connfdGraph,connfdOther;
    public static int byteToInt(int []a)
    {
        int res = 0;
        for (int i=0; i<4; i++) {
            res <<= 8;
            res |= a[i];
        }
        return res;
    }
    public static byte[] intToByte(int a) {
        byte [] data = new byte[4];
        data[0] = (byte)((a>>24)&0xff);
        data[1] = (byte)((a>>16)&0xff);
        data[2] = (byte)((a>>8)&0xff);
        data[3] = (byte)((a)&0xff);
        return data;
    }
    public static int vCode;
    public static List<Activity> activitys = new ArrayList<Activity>();
    public static void  exit()
    {
        for(Activity e:activitys)
        {
            e.finish();
        }
        System.exit(0);
    }
}
