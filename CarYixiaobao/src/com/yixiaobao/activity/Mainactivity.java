package com.yixiaobao.activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


import com.yixiaobao.activity.BluetoothSetSession;
import com.yixiaobao.activity.Mainactivity;
import com.yixiaobao.activity.bluetooth.BluetoothSet;
import com.yixiaobao.activity.bluetooth.DeviceListActivity;

import android.R.integer;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

public class Mainactivity extends Activity {
	private static final int REQUEST_CONNECT_DEVICE = 1;
	
	public static final String HIS_RECORD_ACTION = "com.iest527.hisrecord.action";
	public static final String HIS_RECORD_SEND_OK_ACTION = "com.iest527.hissendok.action";
	
	private TextView mTitle;    					//标题   
	private AlertDialog exitAlertDialog = null;
	private BluetoothSet mBluetoothSet = null;		//蓝牙对象
	BluetoothSetSession mBluetoothSetSession = null;
	private ImageView imgMore = null;
	private long exitTime = 0;
		
	//车辆实时数据
	private TextView txvBAT;
	private TextView txvRPM;
	private TextView txvVSS;
	private TextView txvTP;
	private TextView txvLOD;
	private TextView txvECT;
	private TextView txvMPG;
	private TextView txvAVM;
	private TextView txvFLI;
	private TextView txvDTN;
	
	//车辆统计
	private TextView txvDST;
	private TextView txvADST;
	private TextView txvTDST;
	private TextView txvFUE;
	private TextView txvTFUE;
	private TextView txvCACC;
	private TextView txvCDEC;
	
	//驾驶习惯
	private TextView txvTPC;
	private TextView txvTMT;
	private TextView txvTST;
	private TextView txvAWT;
	private TextView txvASP;
	private TextView txvMSP;
	private TextView txvMRP;
	private TextView txvTACC;
	private TextView txvTDEC;
	
	//故障诊断
	private TextView txvTCC;
	private TextView txvTCD;
	
	//设备信息
	private TextView txvPROTOCOL;
	private TextView txvSN;
	private TextView txvBTName;
	private TextView txvHARDVER;
	private TextView txvSOFTVER;
	
	//按键定义
	private Button btnStart;
	private Button btnStop;
	private Button btnSysmenu;
	private Button btnCompanyinfo;//进入下一级页面
	Intent intent;
	
	private getDataThread mgetDataThread;
	
	private String Field_Cur_Date;
	private String Field_Cur_Time;
	
	public static ProgressDialog mpDialog = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main_activity);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		
        mTitle = (TextView) findViewById(R.id.title_left_text);
        //Set up the custom title
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        
//        mBluetoothSet = new BluetoothSet(this, mTitle);
        mBluetoothSet =  BluetoothSet.getInstance(this, mTitle);
        //设置共享对象，后面的Activity对象只需读取对象数据即可
        mBluetoothSetSession = (BluetoothSetSession)getApplication();
        mBluetoothSetSession.setBluetoothSet(mBluetoothSet);
        mBluetoothSetSession.setHandler(mHandler);
        
        if (!mBluetoothSet.isSupported()){
        	Toast.makeText(this, "蓝牙 SPP 不支持!", Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }
        
        //加载退出对话框
		exitAlertDialog = new AlertDialog.Builder(Mainactivity.this).create();
		exitAlertDialog.setIcon(R.drawable.ic_exit_small);
		exitAlertDialog.setTitle("Exit");
		exitAlertDialog.setMessage("Confirm to exit?");
		
		exitAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "NO",
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog,int i){	
				dialog.cancel();
			}
		});
		exitAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "YES",
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog,int i){
				finish();
			}
		});
		
		mpDialog = new ProgressDialog(Mainactivity.this);  
        mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置风格为圆形进度条  
        mpDialog.setTitle("DTC");//设置标题  
        mpDialog.setMessage("Searching...");  
        mpDialog.setIndeterminate(false);//设置进度条是否为不明确  
        mpDialog.setCancelable(false);//设置进度条是否可以按退回键取消  
		
		//按键定义
		btnStart = (Button)findViewById(R.id.btn_start);
		btnStop = (Button)findViewById(R.id.btn_stop);
		btnSysmenu = (Button)findViewById(R.id.btn_sysmenu);
		btnCompanyinfo = (Button)findViewById(R.id.txv_companyinfo);
		intent = new Intent(this,ControlActivity.class);
		
		btnStop.setEnabled(false);
		btnStart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!mBluetoothSet.isConnected()){
					Toast.makeText(getApplicationContext(), "Not connected bluetooth devices!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if (btnStart.isEnabled())   btnStart.setEnabled(false);					
				if(!btnStop.isEnabled())	btnStop.setEnabled(true);
				mgetDataThread = new getDataThread(mHandler);
				mgetDataThread.start();	
			}
		});
		btnStop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (btnStop.isEnabled()) btnStop.setEnabled(false);
				if (!btnStart.isEnabled()) btnStart.setEnabled(true);
				
				if (mgetDataThread.getCurState()){
					mgetDataThread.cancel();
					for(int i = 0;i<5;i++){
						clearContents(i);
					}
				}			
			}
		});
		btnCompanyinfo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(intent);
			}
		});
			
		//初始化组件
		initWidget();
		
		
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mainactivity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){			
			case R.id.menu_bt_config:					
					OpenDevicesList();
				return true;
			
			case R.id.menu_get_hbt:
					mBluetoothSet.sendMessage("ATHBT\r\n");
				break;
			case R.id.menu_get_device:
					mBluetoothSet.sendMessage("ATI\r\n");
				break;
			case R.id.menu_get_dtc:
				mBluetoothSet.sendMessage("ATDTC\r\n");
				mpDialog.show();
				break;
			case R.id.menu_clr_dtc:
				//清除故障码之后，自动再发送一次读取故障码指令
				mBluetoothSet.sendMessage("ATCDI\r\n");
				break;
			case R.id.menu_get_realtime:		
				//清空当前时钟
				setCurDate(null);
				setCurTime(null);
				mBluetoothSet.sendMessage("ATNOW\r\n");
				new showCurrentTime().start();
				break;
			case R.id.menu_set_realtime:
				new modifyRTC().start();
				break;
			case R.id.menu_get_hisrecord:
				Intent intent = new Intent(Mainactivity.this,HistoryActivity.class);
				startActivity(intent);
				break;
			case R.id.menu_clear_hisrecord:
				clearHistoryRecord();
				break;
			
			case R.id.menu_close_amt:					
				mBluetoothSet.sendMessage("ATSOFF\r\n");
				clearContents(1);
				break;
			case R.id.menu_open_amt:
					mBluetoothSet.sendMessage("ATSON\r\n");
				break;
			case R.id.menu_set_adj:
				adjustTotalDistance();	
				break;
			case R.id.menu_reset:
				mBluetoothSet.sendMessage("ATZ\r\n");
				break;
			case R.id.menu_appexit:
				exitAlertDialog.show();
			default:
				break;		
		}
	
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		// Stop the Bluetooth chat services
		if ((mgetDataThread != null) && (!mgetDataThread.isInterrupted())) mgetDataThread.cancel();
		
		mBluetoothSet.stopBTService();
        mBluetoothSet = null;		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
//		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){  
//	        if((System.currentTimeMillis()-exitTime) > 2000){  
//	            Toast.makeText(getApplicationContext(), R.string.string_exit_info, Toast.LENGTH_SHORT).show();                                
//	            exitTime = System.currentTimeMillis();   
//	        } else {//连续2秒内按返回键则退出
//	            finish();
//	            System.exit(0);
//	        }
//	        return true;   
//	    }
	    return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
	}

	@Override
	protected synchronized void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mBluetoothSet.startBTService();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		mBluetoothSet.openBluetooth();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
				if (resultCode == Activity.RESULT_OK) {
	                // Get the device MAC address
	                String address = data.getExtras()
	                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
	                Log.d("BTAddress", address);
	                mBluetoothSet.ConnectDevices(address);	                
	            }
			break;

		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	
	private void OpenDevicesList(){
		Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	}
		
	
	private void initWidget(){
				
		//车辆实时数据
		txvBAT = (TextView)findViewById(R.id.txv_bat_value);
		txvRPM = (TextView)findViewById(R.id.txv_rpm_value);
		txvVSS = (TextView)findViewById(R.id.txv_vss_value);
		txvTP = (TextView)findViewById(R.id.txv_tp_value);
		txvLOD = (TextView)findViewById(R.id.txv_lod_value);
		txvECT = (TextView)findViewById(R.id.txv_ect_value);
		txvMPG = (TextView)findViewById(R.id.txv_mpg_value);
		txvAVM = (TextView)findViewById(R.id.txv_avm_value);
		txvFLI = (TextView)findViewById(R.id.txv_fli_value);
		txvDTN = (TextView)findViewById(R.id.txv_dtn_value);
		
		//车辆统计数据
		txvDST = (TextView)findViewById(R.id.txv_dst_value);
		txvADST = (TextView)findViewById(R.id.txv_adst_value);
		txvTDST = (TextView)findViewById(R.id.txv_tdst_value);
		txvFUE = (TextView)findViewById(R.id.txv_fue_value);
		txvTFUE = (TextView)findViewById(R.id.txv_tfue_value);
		txvCACC = (TextView)findViewById(R.id.txv_acc_value);
		txvCDEC = (TextView)findViewById(R.id.txv_dec_value);
		
		//驾驶习惯数据
		txvTPC = (TextView)findViewById(R.id.txv_tpc_value);
		txvTMT = (TextView)findViewById(R.id.txv_tmt_value);
		txvTST = (TextView)findViewById(R.id.txv_tst_value);
		txvAWT = (TextView)findViewById(R.id.txv_awt_value);
		txvASP = (TextView)findViewById(R.id.txv_asp_value);
		txvMSP = (TextView)findViewById(R.id.txv_msp_value);
		txvMRP = (TextView)findViewById(R.id.txv_mrp_value);
		txvTACC = (TextView)findViewById(R.id.txv_tacc_value);
		txvTDEC = (TextView)findViewById(R.id.txv_tdec_value);
		
		//设备信息
		txvPROTOCOL = (TextView)findViewById(R.id.txv_protocol_value);
		txvSN = (TextView)findViewById(R.id.txv_sn_value);
		txvBTName = (TextView)findViewById(R.id.txv_btname_value);
		txvHARDVER = (TextView)findViewById(R.id.txv_hardver_value);
		txvSOFTVER = (TextView)findViewById(R.id.txv_softver_value);
		
		//车辆诊断
		txvTCC = (TextView)findViewById(R.id.txv_tcc_value);
		txvTCD = (TextView)findViewById(R.id.txv_tcd_value);
		
	}
	
	/**
	 * 清除组件显示内容，车辆实时数据不清除
	 * @param iFlag
	 */
	private void clearContents(final int iFlag){
		switch (iFlag) {
		case 0:
			txvBAT.setText("---- V");
			txvRPM.setText("---- rpm");
			txvVSS.setText("---- km/h");
			txvTP.setText("---- %");
			txvLOD.setText("---- %");
			txvECT.setText("---- C");
			txvMPG.setText("---- L/100km");
			txvAVM.setText("---- L/100km");
			txvFLI.setText("---- %");
			txvDTN.setText("----");
			break;
		case 1:			//车辆统计数据
			txvDST.setText("---- km");
			txvADST.setText("---- km");
			txvTDST.setText("---- km");
			txvFUE.setText("---- L");
			txvTFUE.setText("---- L");
			txvCACC.setText("---- t");
			txvCDEC.setText("---- t");
			break;
		case 2:			//驾驶习惯数据
			txvTPC.setText(R.string.lbl_field_tpc_times);
			txvTMT.setText("---- h");
			txvTST.setText("---- h");
			txvAWT.setText("---- s");
			txvASP.setText("---- km/h");
			txvMSP.setText("---- km/h");
			txvMRP.setText("---- rpm");
			txvTACC.setText("---- t");
			txvTDEC.setText("---- t");
			break;
		case 3:			//设备信息
			txvPROTOCOL.setText("----");
			txvSN.setText("----");
			txvBTName.setText("----");
			txvHARDVER.setText("----");
			txvSOFTVER.setText("----");
			break;
		case 4:			//车辆诊断
			txvTCC.setText("----");
			txvTCD.setText("----");
			break;
		default:
			break;
		}
	}
			
	private final Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Bundle bundle = msg.getData();
			String strArray[] = null;
			switch (msg.what) {
			case getDataThread.RESPONSE_OK:
					String sOK = bundle.getString("RPS_OK");
					Toast.makeText(getApplicationContext(), sOK, Toast.LENGTH_SHORT).show();
					break;
			case getDataThread.RESPONSE_OBD_RT:
					String sRT = bundle.getString("RPS_RT");
					strArray = sRT.split(",");
					
					txvBAT.setText(getDataThread.cutString(strArray[1]));
					txvRPM.setText(getDataThread.cutString(strArray[2]));
					txvVSS.setText(getDataThread.cutString(strArray[3]));
					txvTP.setText(getDataThread.cutString(strArray[4]));
					txvLOD.setText(getDataThread.cutString(strArray[5]));
					txvECT.setText(getDataThread.cutString(strArray[6]));
					txvMPG.setText(getDataThread.cutString(strArray[7]));
					txvAVM.setText(getDataThread.cutString(strArray[8]));
					txvFLI.setText(getDataThread.cutString(strArray[9]));
					txvDTN.setText(getDataThread.cutString(strArray[10]));
				break;
			case getDataThread.RESPONSE_OBD_AMT:
					String sAMT = bundle.getString("RPS_AMT");
					strArray = sAMT.split(",");
					
					txvDST.setText(getDataThread.cutString(strArray[1]));
					txvADST.setText(getDataThread.cutString(strArray[2]));
					txvTDST.setText(getDataThread.cutString(strArray[3]));
					txvFUE.setText(getDataThread.cutString(strArray[4]));
					txvTFUE.setText(getDataThread.cutString(strArray[5]));
					txvCACC.setText(getDataThread.cutString(strArray[6]));
					txvCDEC.setText(getDataThread.cutString(strArray[7]));
				break;			
			case getDataThread.RESPONSE_OBD_HBT:
					String sHBT = bundle.getString("RPS_HBT");
					strArray = sHBT.split(",");
					
					txvTPC.setText(getDataThread.cutString(strArray[1]));
					txvTMT.setText(getDataThread.cutString(strArray[2]));
					txvTST.setText(getDataThread.cutString(strArray[3]));
					txvAWT.setText(getDataThread.cutString(strArray[4]));
					txvASP.setText(getDataThread.cutString(strArray[5]));
					txvMSP.setText(getDataThread.cutString(strArray[6]));
					txvMRP.setText(getDataThread.cutString(strArray[7]));
					txvTACC.setText(getDataThread.cutString(strArray[8]));
					txvTDEC.setText(getDataThread.cutString(strArray[9]));
				break;
			case getDataThread.RESPONSE_OBD_DVC:
					String sDVC = bundle.getString("RPS_DVC");
					strArray = sDVC.split(",");
					
					txvPROTOCOL.setText(getDataThread.cutString(strArray[1]));
					txvSN.setText(getDataThread.cutString(strArray[2]));
					txvBTName.setText(getDataThread.cutString(strArray[3]));
					txvHARDVER.setText(getDataThread.cutString(strArray[4]));
					txvSOFTVER.setText(getDataThread.cutString(strArray[5]));				
				break;
			case getDataThread.RESPONSE_OBD_DTC:
					String sDTC = bundle.getString("RPS_DTC");
					strArray = sDTC.split(",");
					
					txvTCC.setText(getDataThread.cutString(strArray[1]));
					txvTCD.setText(getDataThread.cutString(strArray[2]));
					
					if(mpDialog.isShowing())
						mpDialog.cancel();
				break;
			case getDataThread.RESPONSE_OBD_RTC:					
					String sRTC = bundle.getString("RPS_RTC");
					strArray = sRTC.split(",");
					
					setCurDate(getDataThread.cutString(strArray[1]));
					setCurTime(getDataThread.cutString(strArray[2]));					
				break;
			case getDataThread.RESPONSE_OBD_RTC_SET_OK:
					Toast.makeText(getApplicationContext(), R.string.msg_rtc_set_ok, Toast.LENGTH_SHORT).show();
				break;
				
			case getDataThread.RESPONSE_OBD_HIS_RECORD:
				//Log.i("Handler", bundle.getString("RPS_HIS_RECORD"));
				Intent HisRecordIntent = new Intent(HIS_RECORD_ACTION);
				HisRecordIntent.putExtra("RPS_HIS_RECORD", bundle.getString("RPS_HIS_RECORD"));
				sendBroadcast(HisRecordIntent);
				break;
			case getDataThread.RESPONSE_OBD_HIS_RECORD_SEND_OK:		
				//Log.i("Handler", bundle.getString("RPS_HIS_SENDOK"));
				Intent SendOkIntent = new Intent(HIS_RECORD_SEND_OK_ACTION);
				SendOkIntent.putExtra("RPS_HIS_SENDOK", bundle.getString("RPS_HIS_SENDOK"));
				sendBroadcast(SendOkIntent);
				break;
			default:
				break;
			}
		}		
	};
	/**
	 * Adjust ODO
	 */
	private void adjustTotalDistance(){
		AlertDialog dialog = new AlertDialog.Builder(Mainactivity.this).create();
		LayoutInflater inflater = (LayoutInflater)Mainactivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.edittext_dialog, null); 
		
		dialog.setTitle(R.string.string_odo_info);
		dialog.setIcon(R.drawable.ic_adjust_small);
		dialog.setCancelable(true);
		dialog.setView(view);
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {  
	            public void onClick(DialogInterface dialog, int which) {  
	                EditText edtAdjust = (EditText) view.findViewById(R.id.edt_adj_value);  
	                if ((edtAdjust.getText() == null) || (edtAdjust.getText().toString().trim() == "")) return;
	                
	                if (Double.parseDouble(edtAdjust.getText().toString()) >= 0) {
	                	mBluetoothSet.sendMessage("ATADJ=" + edtAdjust.getText().toString() + "\r\n");
	                }
	            }  
		});
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int which) {  
						dialog.cancel();  
				}  
		}) ;					
		dialog.show(); 
	}
	
	
	
	public void setCurDate(String value)
	{
		this.Field_Cur_Date = value;
	}
	
	public String getCurDate()
	{
		return this.Field_Cur_Date;
	}
	
	public void setCurTime(String value)
	{
		this.Field_Cur_Time = value;
	}
	
	public String getCurTime()
	{
		return this.Field_Cur_Time;
	}
	
	public String getRTC()
	{
		String reTime = null;
		
		if((getCurDate() == null) || (getCurTime() == null))
		{
			return "N/A";
		}
		else
		{
			String realTimeString = getCurDate() + " " + getCurTime();				
			SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd HHmmss");		
			Date curDateTime = null;
			try {
				curDateTime = sdf.parse(realTimeString);
				
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Log.d("TIME", String.valueOf(curDateTime));
			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			reTime = sDateFormat.format(curDateTime);
			Log.d("TIME", reTime);
			return reTime;
		}
	}
	
	/**
	 * 显示当前时间
	 */
	class showCurrentTime extends Thread
	{
		public void run()
		{	
			Looper.prepare();
			try {	
				
				sleep(2000L);
								
				AlertDialog.Builder timeDialog = new Builder(Mainactivity.this);
				timeDialog.setMessage("Current RTC:"+ getRTC());
				timeDialog.setTitle("RTC");
				
				timeDialog.setNegativeButton(R.string.msg_return, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
				timeDialog.create().show();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Looper.loop();
		}
	}
	
	/**
	 * 修改时钟
	 */
	class modifyRTC extends Thread
	{
		public void run()
		{
			Looper.prepare();
			
			AlertDialog dialog = new AlertDialog.Builder(Mainactivity.this).create();
			LayoutInflater inflater = (LayoutInflater)Mainactivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
			final View view = inflater.inflate(R.layout.modify_datetime, null); 
			
			dialog.setTitle(R.string.title_modify_rtc);
			dialog.setIcon(R.drawable.ic_set_time_small);
			dialog.setCancelable(true);
			dialog.setView(view);
			//初始化时钟
			final DatePicker datePicker = (DatePicker)view.findViewById(R.id.datePicker1);
			final TimePicker timePicker = (TimePicker)view.findViewById(R.id.timePicker1);
	    	Calendar calendar = Calendar.getInstance();
	    	
	    	datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),  calendar.get(Calendar.DAY_OF_MONTH),
	    			new DatePicker.OnDateChangedListener() {
						
						@Override
						public void onDateChanged(DatePicker view, int year, int monthOfYear,
								int dayOfMonth) {
							// TODO Auto-generated method stub
							
						}
					});
	    	timePicker.setIs24HourView(true);
	    	timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
	    	timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE)); 
	    	Log.d("TIME", String.valueOf(calendar.get(Calendar.MINUTE)));
	    	
	    	timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
				
				@Override
				public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
					// TODO Auto-generated method stub
					
				}
			});
	    	
			dialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK",
					new DialogInterface.OnClickListener() {
				
		            public void onClick(DialogInterface dialog, int which) 
		            {  
		            	String dateString = String.format("%04d",datePicker.getYear()) + 
		            						String.format("%02d", datePicker.getMonth() + 1)  + String.format("%02d",datePicker.getDayOfMonth());
		            	
		                mBluetoothSet.sendMessage("ATDATE="+dateString.substring(2) + "\r\n");
		                try {
							sleep(1000L);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		                
		                String timeString = String.format("%02d", timePicker.getCurrentHour()) +
		                		String.format("%02d",timePicker.getCurrentMinute()) + "00";
		                mBluetoothSet.sendMessage("ATTIME=" + timeString + "\r\n");
		            }
			});
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {  
					public void onClick(DialogInterface dialog, int which) {  
							dialog.cancel();  
					}  
			}) ;					
			dialog.show();
			
			Looper.loop();
		}
		
	}	
	
	private void clearHistoryRecord()
	{
		AlertDialog alertDialog = null;
		alertDialog = new AlertDialog.Builder(Mainactivity.this).create();
		alertDialog.setIcon(R.drawable.ic_clear_driving_recoid_small);
		alertDialog.setTitle("Erase");
		alertDialog.setMessage("Confirm to erase?");
		
		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "NO",
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog,int i){	
				dialog.cancel();
			}
		});
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "YES",
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog,int i){
				mBluetoothSet.sendMessage("ATHZ\r\n");
			}
		});
		alertDialog.show();
	}	
}
