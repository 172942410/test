package com.yixiaobao.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.yixiaobao.activity.bluetooth.BluetoothSet;
import com.yixiaobao.control.ControlData;

public class ControlActivity extends Activity{

	public static String TAG = "ControlActivity";
	private Button carSystemBtn;		//进入系统按钮
	private Button carWindowUpBtn;		//车窗上升按钮
	private Button carWindowDownBtn;	//车窗下降按钮
	private Button carDoorLock;			//车门锁定按钮
	private Button carDoorUnlock;		//车门解锁按钮
	private Button carRearviewOpen;		//车后视镜打开
	private Button carRearviewClose;	//车后视镜收起关闭
	private Button carTrunkOpen;		//车后备箱打开
	
	private Button carNextData;			//进入下个界面浏览汽车数据
	
	private OnClickListener listenerbutton;//
	
	Intent intent;
	
	private BluetoothSet mBluetoothSet = null;		//蓝牙对象
	
	private ControlData controlData;//指令类
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.control_activity);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		
		mBluetoothSet = BluetoothSet.getInstance(this,null);
		controlData = new ControlData();
		initView();
		setListen();
	}
	/**
	 * 初始化组件
	 */
	protected void initView(){
		carSystemBtn = (Button)findViewById(R.id.btn_car_system);
		carWindowUpBtn = (Button)findViewById(R.id.btn_car_window_up);
		carWindowDownBtn = (Button)findViewById(R.id.btn_car_window_down);
		carDoorLock = (Button)findViewById(R.id.btn_car_door_lock);
		carDoorUnlock = (Button)findViewById(R.id.btn_car_door_unlock);
		carRearviewOpen = (Button)findViewById(R.id.btn_car_rearview_open);
		carRearviewClose = (Button)findViewById(R.id.btn_car_rearview_close);
		carTrunkOpen = (Button)findViewById(R.id.btn_car_trunk_open);
		carNextData = (Button)findViewById(R.id.btn_next_data);
		
	}
	/**
	 * 设置监听
	 */
	protected void setListen(){
		intent = new Intent(ControlActivity.this,Mainactivity.class);
		listenerbutton = new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch(v.getId()){
				case R.id.btn_car_system:
					Log.i(TAG, "进入系统");
					sendCarSystemIn();
				break;
				case R.id.btn_car_window_up:
					Log.i(TAG, "发送车窗升起指令");
					sendCarWindowUp();
				break;
				case R.id.btn_car_window_down:
					Log.i(TAG, "车窗下降指令");
					sendCarWindowDown();
				break;
				case R.id.btn_car_door_lock:
					Log.i(TAG, "车门锁定指令");
					sendCarDoorLock();
				break;
				case R.id.btn_car_door_unlock:
					Log.i(TAG, "车门解锁指令");
					sendCarDoorUnlock();
				break;
				case R.id.btn_car_rearview_open:
					Log.i(TAG, "后视镜打开指令");
					sendCarRearviewOpen();
				break;
				case R.id.btn_car_rearview_close:
					Log.i(TAG, "后视镜收起指令");
					sendCarRearviewClose();
				break;
				case R.id.btn_car_trunk_open:
					Log.i(TAG, "后备箱开启指令");
					sendCarTrunkOpen();
				break;
				case R.id.btn_next_data:
				Log.i(TAG, "进入下一级页面");
				startActivity(intent);
				break;
				
				}
			}

			
		};
		carSystemBtn.setOnClickListener(listenerbutton);
		carDoorLock.setOnClickListener(listenerbutton);
		carDoorUnlock.setOnClickListener(listenerbutton);
		carRearviewClose.setOnClickListener(listenerbutton);
		carRearviewOpen.setOnClickListener(listenerbutton);
		carWindowDownBtn.setOnClickListener(listenerbutton);
		carWindowUpBtn.setOnClickListener(listenerbutton);
		carTrunkOpen.setOnClickListener(listenerbutton);
		carNextData.setOnClickListener(listenerbutton);
	}
	/**
	 * 进入系统指令
	 */
	protected void sendCarSystemIn() {
		// TODO Auto-generated method stub
//		mBluetoothSet.sendMessage(controlData.sendCarSystemIn().toString());
		mBluetoothSet.sendMessage(controlData.sendCarSystemIn());
	}
	/**
	 * 后备箱开启指令
	 */
	protected void sendCarTrunkOpen() {
		// TODO Auto-generated method stub
		mBluetoothSet.sendMessage(controlData.sendCarTrunkOpen());
	}
	/**
	 * 后视镜收起指令
	 */
	protected void sendCarRearviewClose() {
		// TODO Auto-generated method stub
		mBluetoothSet.sendMessage(controlData.sendCarRearviewClose());
	}
	/**
	 * 后视镜打开指令
	 */
	protected void sendCarRearviewOpen() {
		// TODO Auto-generated method stub
//		String str = controlData.sendCarRearviewOpen().toString();
//		Log.i(TAG,"str= "+str);
//		byte[] send = str.getBytes(); 
//    	for(int i=0;i<send.length;i++){
//    		Log.i(TAG,"send["+i+"] = "+send[i]);
//    	}
		mBluetoothSet.sendMessage(controlData.sendCarRearviewOpen());
	}
	/**
	 * 车门解锁指令
	 */
	protected void sendCarDoorUnlock() {
		// TODO Auto-generated method stub
		mBluetoothSet.sendMessage(controlData.sendCarDoorUnlock());
	}
	/**
	 * 发送车窗升起指令
	 */
	private void sendCarWindowUp() {
		// TODO Auto-generated method stub
		mBluetoothSet.sendMessage(controlData.sendCarWindowUp());
	}
	/**
	 * 车窗下降指令
	 */
	private void sendCarWindowDown() {
		// TODO Auto-generated method stub
		mBluetoothSet.sendMessage(controlData.sendCarWindowDown());
	}
	/**
	 * 车门锁定指令
	 */
	protected void sendCarDoorLock() {
		// TODO Auto-generated method stub
		mBluetoothSet.sendMessage(controlData.sendCarDoorLock());
	}

}
