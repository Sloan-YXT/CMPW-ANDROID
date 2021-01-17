/**
 * 
 */
package com.gf.test.videoplayer.activities;

import java.io.File;

import org.xutils.DbManager;
import org.xutils.x;

import momo.cn.edu.fjnu.androidutils.base.BaseApplication;

public class UdpPlayerApplication extends BaseApplication{
	public static DbManager mDBManager;
	
	@Override
	public void onCreate() {
		super.onCreate();
		 DbManager.DaoConfig dbConfig = new DbManager.DaoConfig().setDbDir(new File(ConstData.DB_DIRECTORY))
	                .setDbName(ConstData.DB_NAME).setDbVersion(ConstData.DB_VERSION).setAllowTransaction(true)
	                .setDbOpenListener(new DbManager.DbOpenListener() {
	                    @Override
	                    public void onDbOpened(DbManager db) {
	                        // ����WAL, ��д����������޴�
	                        db.getDatabase().enableWriteAheadLogging();
	                    }
	                }).setDbUpgradeListener(null);
	        if (null == mDBManager)
	            mDBManager = x.getDb(dbConfig);
		mDBManager = x.getDb(dbConfig);
	}
}
