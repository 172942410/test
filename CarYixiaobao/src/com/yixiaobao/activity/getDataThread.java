package com.yixiaobao.activity;

import com.yixiaobao.activity.bluetooth.BluetoothSet;

import android.R.bool;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class getDataThread extends Thread{

	public static final int RESPONSE_OK = 0x00;
	public static final int RESPONSE_OBD_RT = 0x01;
	public static final int RESPONSE_OBD_AMT = 0x02;
	public static final int RESPONSE_OBD_HBT = 0x03;
	public static final int RESPONSE_OBD_DVC = 0x04;
	public static final int RESPONSE_OBD_DTC = 0x05;
	public static final int RESPONSE_OBD_RTC = 0x06;
	public static final int RESPONSE_OBD_RTC_SET_OK = 0x07;
	public static final int RESPONSE_OBD_HIS_RECORD = 0x08;
	public static final int RESPONSE_OBD_HIS_RECORD_SEND_OK = 0x09;
		
	private static String commData = "";
	
	private Handler mHandler;
	
	private boolean isStop;
	
	public getDataThread(Handler handler)
	{
		this.mHandler = handler;
		isStop = false;
	}
	@Override
	public void run() {
		Looper.prepare();
		// TODO Auto-generated method stub	
		isStop = true;
		while(true){
			if (!isStop) break;
			try {
				sleep(20L);
				
				if ((getCommData().trim() == "") || (getCommData() == null)) continue;
				
				String ReceiveBuffer = getCommData();	
				String sResponseType;
				String strArray[] = getCommData().split(",");
				Message msg = null;
								
				//车辆实时数据
				if ((strArray[0].equals("$OBD-RT")) && (strArray.length == 11))
				{
					msg = mHandler.obtainMessage(RESPONSE_OBD_RT);
					sResponseType = "RPS_RT";
				}
				//车辆统计数据，默认打开，关闭指令"ATOFF"
				else if((strArray[0].equals("$OBD-AMT")) && (strArray.length == 8))
				{
					msg = mHandler.obtainMessage(RESPONSE_OBD_AMT);
					sResponseType = "RPS_AMT";
				}
				//驾驶习惯数据，发送"ATHBT"指令
				else if((strArray[0].equals("$OBD-HBT")) && (strArray.length == 10))
				{
					msg = mHandler.obtainMessage(RESPONSE_OBD_HBT);
					sResponseType = "RPS_HBT";
				}
				//设备信息输数据，发送“ATI”指令
				else if ( (strArray[0].equals("$iEST527")) && (strArray.length == 6))
				{
					msg = mHandler.obtainMessage(RESPONSE_OBD_DVC);
					sResponseType = "RPS_DVC";
				}
				//车辆诊断数据，发送“ATDTC”指令
				else if ( (strArray[0].equals("$OBD-DTC")) && (strArray.length == 3))
				{
					msg = mHandler.obtainMessage(RESPONSE_OBD_DTC);
					sResponseType = "RPS_DTC";
				}
				//获取当前时间，发送“ATNOW”指令
				else if ( (strArray[0].equals("$OBD-RTC")) && (strArray.length == 3))
				{						
					msg = mHandler.obtainMessage(RESPONSE_OBD_RTC);
					sResponseType = "RPS_RTC";
				}
				//设置RTC时钟
				else if ((strArray[0].equals("$iEST527")) && (strArray.length == 2) )
				{
					if((strArray[1].contains("SET DATE OK")) || (strArray[1].contains("SET TIME OK")) )
					{
						msg = mHandler.obtainMessage(RESPONSE_OBD_RTC_SET_OK);
						sResponseType = "RTC_SET_OK";
					}
					else
					{
						msg = mHandler.obtainMessage(RESPONSE_OK);
						sResponseType = "RPS_OK";
					}
				}
				//收到历史记录信息
				else if ( (strArray[0].equals("$OBD-HIS")) && (strArray.length == 15))
				{							
					msg = mHandler.obtainMessage(RESPONSE_OBD_HIS_RECORD);
					//Log.d("getDataThread", ReceiveBuffer);
					sResponseType = "RPS_HIS_RECORD";
				}
				//历史记录信息发送完成
				else if ( (strArray[0].equals("$iEST527")) && (strArray.length == 3))
				{											
					msg = mHandler.obtainMessage(RESPONSE_OBD_HIS_RECORD_SEND_OK);
					//Log.d("getDataThread", ReceiveBuffer);
					sResponseType = "RPS_HIS_SENDOK";					
				}
				else {
					msg = mHandler.obtainMessage(RESPONSE_OK);
					sResponseType = "RPS_OK";
				}
				
				//发送消息，更新UI
				Bundle bundle = new Bundle();
				bundle.putString(sResponseType, ReceiveBuffer);
				msg.setData(bundle);
				msg.sendToTarget();
				
				//清空缓存
				setCommData("");
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Looper.loop();
	}
	
	public boolean getCurState()
	{
		return isStop;
	}
	
	//停止线程
	public void cancel() {		
		isStop = false;
		setCommData("");
	}
	
	/**
	 * 获取串口数据
	 */
	public synchronized static String getCommData(){
		return commData;
	}
	
	/**
	 * 设置接收串口数据
	 * @param value
	 */
	public synchronized static void setCommData(String value){
		commData = value;
	}
	
	public static String cutString(String src){
		if ((src.trim() == "") || (src == null)) return "";
		
		return src.substring(src.indexOf("=") + 1);
	}
}
