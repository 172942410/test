package com.yixiaobao.activity;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import android.app.Application;
import android.os.Environment;
import android.os.Handler;

import com.yixiaobao.activity.bluetooth.BluetoothSet;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class BluetoothSetSession extends Application {
	private BluetoothSet mBluetoothSet = null;			//传递蓝牙对象
	private Handler mHandler = null;
	
	public static Logger gLogger;//
	
	public BluetoothSetSession(){
		super();
		configLog();
	}
	public BluetoothSet getBluetoothSet(){
		return mBluetoothSet;
	}
	
	public void setBluetoothSet(BluetoothSet mbts) {
		this.mBluetoothSet = mbts;
	}
	
	public void setHandler(Handler handler)
	{
		this.mHandler = handler;
	}
	
	public Handler getHandler()
	{
		return mHandler;
	}


	/**
	 * 配置log日志输出
	 */
	public void configLog() {
		final LogConfigurator logConfigurator = new LogConfigurator();
		logConfigurator.setFileName(Environment.getExternalStorageDirectory()
				+ File.separator + "carYixiaobao_log4j.log");
		// Set the root log level
		logConfigurator.setRootLevel(Level.DEBUG);
		// Set log level of a specific logger
		logConfigurator.setLevel("org.apache", Level.ERROR);
		logConfigurator.configure();

//		 gLogger = Logger.getLogger(this.getClass());
		gLogger = Logger.getLogger("carBao");
		gLogger.debug("start... 记录易小宝的log日志文件");
	}
}
