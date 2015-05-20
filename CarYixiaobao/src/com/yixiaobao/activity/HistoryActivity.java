package com.yixiaobao.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.yixiaobao.activity.BluetoothSetSession;
import com.yixiaobao.activity.bluetooth.BluetoothSet;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class HistoryActivity extends Activity implements OnClickListener {
	
	private static final int CMD_REQUEST_BY_DATE = 0x01;
	private static final int CMD_REQUEST_ALL = 0x02;
	
	private Button btnselectdate;
	private Button btnquerybydate;
	private Button btnqueryall;
	private EditText edtSelectDate;
	private ListView mListView;
	
	DatePickerDialog datePickerDialog;
	private BluetoothSetSession mBluetoothSetSession;
	private BluetoothSet mBluetoothSet;
	
	private SimpleAdapter mScanItemSimpleAdapter;
	private ArrayList<HashMap<String, Object>> mList;
	private ArrayList<String> RecordList;
	private ProgressDialog mDialog = null;
	private int Command_Type;
	private IntentFilter filter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_activity);
		
		btnselectdate = (Button)findViewById(R.id.btn_select_date);
		btnquerybydate = (Button)findViewById(R.id.btn_query_by_date);
		btnqueryall = (Button)findViewById(R.id.btn_query_all);
		edtSelectDate = (EditText)findViewById(R.id.edt_select_date);
				
		btnselectdate.setOnClickListener(this);
		btnquerybydate.setOnClickListener(this);
		btnqueryall.setOnClickListener(this);
		
		//获取ListView对象  
		//下面是数据映射关系,mFrom和mTo按顺序一一对应  
		String[] mFrom = new String[]{"ID","StartTime","EndTime","TotalTime","HisDst","HisTime","IdleTime","DrvFuel","IdlFuel","TotalFuel","MaxRPM","AvgVSS","MaxVSS","HotTime","SpeedUp","SlowDown"};  
		int[] mTo = new int[]{R.id.txv_his_id_value,R.id.txv_his_start_time_value,R.id.txv_his_end_time_value,R.id.txv_his_total_time_value,R.id.txv_his_dst_value,
							  R.id.txv_his_time_value,R.id.txv_his_Idle_Time_value,R.id.txv_his_drv_fue_value,R.id.txv_his_idl_fue_value,R.id.txv_his_total_fue_value,R.id.txv_his_max_rpm_value,
							  R.id.txv_his_avg_speed_value,R.id.txv_his_max_vss_value,R.id.txv_his_hot_time_value,R.id.txv_his_speed_up_value,R.id.txv_his_slow_dwon_value};
		mList = new ArrayList<HashMap<String, Object>>();
		mScanItemSimpleAdapter = new SimpleAdapter(this, mList, R.layout.his_record_detail, mFrom, mTo);
		
		//绑定适配器
		mListView = (ListView)findViewById(R.id.lsv_his_record);
		mListView.setAdapter(mScanItemSimpleAdapter);
		
		mBluetoothSetSession = (BluetoothSetSession)getApplication();
		mBluetoothSet = mBluetoothSetSession.getBluetoothSet();	
		
	}
	
	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String strArray[] = null;
			//接收到数据
			if(intent.getAction().equals(Mainactivity.HIS_RECORD_ACTION))
			{
				String sRecord = intent.getExtras().getString("RPS_HIS_RECORD");
				Log.d("broadcastReceiver", sRecord);
				addRecordList(sRecord);
			}
			else {
				if(intent.getAction().equals(Mainactivity.HIS_RECORD_SEND_OK_ACTION))
				{
					String sRes = intent.getExtras().getString("RPS_HIS_SENDOK");
					Log.d("broadcastReceiver", sRes);
					strArray = sRes.split(",");
					
					int RecordCount = Integer.parseInt(getDataThread.cutString(strArray[1]));
					AddHisRecord(getRecordList());
					Toast.makeText(HistoryActivity.this, "Return " + String.valueOf(RecordCount) +  " Records", Toast.LENGTH_SHORT).show();
					clearRecordList();
					if(mDialog.isShowing()) mDialog.dismiss();
				}
			}
		}
	};
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		unregisterReceiver(broadcastReceiver);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		filter = new IntentFilter();  
		filter.addAction(Mainactivity.HIS_RECORD_ACTION);
		filter.addAction(Mainactivity.HIS_RECORD_SEND_OK_ACTION);
		registerReceiver(broadcastReceiver, filter);
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_select_date:
				getSelectDate();				
			break;
		case R.id.btn_query_by_date:
			initRecordWidget();
			if ((edtSelectDate.getText() != null) && (edtSelectDate.length() == 8))
			{
				setCmdType(CMD_REQUEST_BY_DATE);
				new QueryData().start();
			}
			else
			{
				Toast.makeText(HistoryActivity.this, "Please Select Date", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_query_all:			
			initRecordWidget();
			setCmdType(CMD_REQUEST_ALL);
			new QueryData().start();
			break;
		default:
			break;
		}
	}
	
	private void AddHisRecord(ArrayList<String> HisRecord)
	{
		for(int index = 0;index < HisRecord.size();index++)
		{
			String Record = (String)HisRecord.get(index);
			//Log.d("ADDHISRECORD", Record);
			
			String strArry[] = null;
			strArry = Record.split(",");
			HashMap<String, Object> mMap = new HashMap<String, Object>();
			
			mMap.put("ID", getDataThread.cutString(strArry[1]));		//序号	
			String StartTime = getStandardDateTime(getDataThread.cutString(strArry[2]),getDataThread.cutString(strArry[3]));
			String EndTime = getStandardDateTime(getDataThread.cutString(strArry[4]),getDataThread.cutString(strArry[5]));		
			mMap.put("StartTime", StartTime);//开始时间			
			mMap.put("EndTime", EndTime);	//结束时间
			String TotalTime = getTimeDiff(StartTime, EndTime);
			if(TotalTime != null)
				mMap.put("TotalTime", TotalTime);									//总时长
			else
				mMap.put("TotalTime", "N/A min");
			
			String DrvDis =  getDataThread.cutString(strArry[6]);
			mMap.put("HisDst",DrvDis);			//本次里程	
			
			String DrvieTime = getDataThread.cutString(strArry[7]);
			mMap.put("HisTime",DrvieTime);										//行驶时长		
			
			String IdleTime = getIdelTime(TotalTime, DrvieTime);				//怠速时长
			if(IdleTime != null)
				mMap.put("IdleTime", IdleTime);
			else
				mMap.put("IdleTime", "N/A min");
			
			String DrvFuel = getDataThread.cutString(strArry[8]);
			String IdleFuel = getDataThread.cutString(strArry[9]);
			mMap.put("DrvFuel",DrvFuel);			//行驶耗油			
			mMap.put("IdlFuel",IdleFuel);			//怠速耗油
				
			String TotalFuel = getTotalFuel(DrvFuel, IdleFuel);					//总耗油
			if(TotalFuel != null)
				mMap.put("TotalFuel", TotalFuel);
			else
				mMap.put("TotalFuel", "N/A L");
			
			mMap.put("MaxRPM", getDataThread.cutString(strArry[11]));			//最高转速
			
			String AvgSpeed = getAvgSpeed(DrvieTime, DrvDis);					//平均车速
			if(AvgSpeed != null)
				mMap.put("AvgVSS", AvgSpeed);
			else
				mMap.put("AvgVSS", "N/A km/h");
			mMap.put("MaxVSS", getDataThread.cutString(strArry[10]));			//最高车速
			mMap.put("HotTime", getDataThread.cutString(strArry[12]));			//热车时间
			
			String sharpAcc = getDataThread.cutString(strArry[13]);
			String sharpDec = getDataThread.cutString(strArry[14]);
			
			mMap.put("SpeedUp",sharpAcc.substring(0,sharpAcc.length()-2) + " Times" );			//急加速
			mMap.put("SlowDown",sharpDec.substring(0,sharpDec.length()-2) + " Times" );			//急减速
			
			mList.add(mMap);
			
		}
		mScanItemSimpleAdapter.notifyDataSetChanged();
	}
	
	/**
	 * 组合成标准日期格式	
	 * @param dateString	日期
	 * @param timeString	时间
	 * @return
	 */
	public String getStandardDateTime(String dateString,String timeString)
	{
		String reTime = null;
		String realTimeString = dateString + " " + timeString;				
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd HHmmss");		
		Date curDateTime = null;
		try {
			curDateTime = sdf.parse(realTimeString);
			
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		reTime = sDateFormat.format(curDateTime);
		
		return reTime;
	}

	/**
	 * 获取总时长
	 * @param S_Time
	 * @param E_time
	 * @return
	 */
	public String getTimeDiff(String S_Time,String E_time)
	{
		String timeDiff = null;
		float minutes = 0;
		
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date StartTime = sDateFormat.parse(S_Time);
			Date EndTime = sDateFormat.parse(E_time);
			
			long diff = EndTime.getTime() - StartTime.getTime();
			
			if(diff > 0)
				minutes = diff * 1.00f / (1000 * 60);
			else
				minutes = 0.00f; 
			
			timeDiff = String.format("%.2fmin", minutes);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return timeDiff;		
	}
	
	/**
	 * 获取怠速时间
	 * @param TotalTime		总时长
	 * @param DrvTime		行驶时长
	 * @return
	 */
	
	public String getIdelTime(String TotalTime,String DrvTime)
	{
		String IdleTime = null;
		String temp_TotalTime=null,temp_DrvTime=null;
		float t_time = 0.00f,d_time=0.00f,idelTime=0.00f;
		
		temp_TotalTime = TotalTime.substring(0, TotalTime.length()-3);
		temp_DrvTime = DrvTime.substring(0, DrvTime.length()-3);
		try {
			t_time = Float.parseFloat(temp_TotalTime);
			d_time = Float.parseFloat(temp_DrvTime);
			
			idelTime = t_time - d_time;
			if(idelTime <= 0.00f)
				idelTime = 0.00f;
			
			IdleTime= String.format("%.2fmin", idelTime);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return IdleTime;
	}
	
	/**
	 * 获取总耗油量
	 * @param DrvFuel	行驶耗油量
	 * @param IdleFuel	怠速耗油量
	 * @return
	 */
	public String getTotalFuel(String DrvFuel,String IdleFuel)
	{
		String TotalFuel=null;
		String temp_DrvFuel=null,temp_IdleFuel=null;
		float d_fuel=0.00f,i_fuel=0.00f,t_fuel=0.00f;
		
		temp_DrvFuel = DrvFuel.substring(0,DrvFuel.length()-1);
		temp_IdleFuel = IdleFuel.substring(0,IdleFuel.length()-1);
		
		try {
			d_fuel = Float.parseFloat(temp_DrvFuel);
			i_fuel = Float.parseFloat(temp_IdleFuel);
			
			t_fuel = d_fuel + i_fuel;
			if(t_fuel <= 0.00f)
				t_fuel = 0.00f;
			
			TotalFuel = String.format("%.2fL", t_fuel);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return TotalFuel;
	}
	
	/**
	 * 获取平均车速
	 * @param DrvTime	行驶时间
	 * @param DrvDis	行驶里程	
	 * @return
	 */
	public String getAvgSpeed(String DrvTime,String DrvDis)
	{
		String AvgSpeed = null;
		String temp_drvtime=null,temp_drvdis=null;
		float f_drvTime = 0.00f,f_drvDis=0.00f,f_avgspeed=0.00f;
	
		temp_drvtime = DrvTime.substring(0,DrvTime.length()-3);
		temp_drvdis = DrvDis.substring(0,DrvDis.length()-2);
		
		try {
			f_drvTime = Float.parseFloat(temp_drvtime);
			f_drvDis = Float.parseFloat(temp_drvdis);
			
			if(f_drvTime > 0)
				f_avgspeed = ( f_drvDis / (f_drvTime / 60));
			else
				f_avgspeed = 0.00f;
			
			AvgSpeed = String.format("%.0fkm/h", f_avgspeed);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return AvgSpeed;
	}
	
	public void addRecordList(String value)
	{
		this.RecordList.add(value);
	}
	
	public ArrayList<String> getRecordList()
	{
		return this.RecordList;
	}
	
	public void clearRecordList()
	{
		this.RecordList.clear();
	}
	
	public void setCmdType(int value)
	{
		if((value == CMD_REQUEST_BY_DATE) || (value == CMD_REQUEST_ALL))
			this.Command_Type = value;
		else 
			this.Command_Type = 0;		
	}
	
	public int getCmdType()
	{
		return this.Command_Type;
	}
	
	public void initRecordWidget()
	{
		//清除列表项
		RecordList = new ArrayList<String>();
		mList.clear();
		mScanItemSimpleAdapter.notifyDataSetChanged();
	}
	
	/**
	 * 弹出对话框，选择日期
	 */
	private void getSelectDate()
	{
		Calendar calendar = Calendar.getInstance();
		datePickerDialog = new DatePickerDialog(this, new OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				// TODO Auto-generated method stub
				edtSelectDate.setText(String.format("%04d", year) + String.format("%02d", monthOfYear + 1) + String.format("%02d", dayOfMonth));
			}
		}, calendar.get(calendar.YEAR), calendar.get(calendar.MONTH), calendar.get(calendar.DAY_OF_MONTH));
		datePickerDialog.show();
	}
		
	class QueryData extends Thread
	{				
		@Override
		public void run() {
			Looper.prepare();
			// TODO Auto-generated method stub
			mDialog = new ProgressDialog(HistoryActivity.this);
			mDialog.setMessage("Requesting Data...");
			mDialog.setCancelable(true); 
			mDialog.show();
			
			//按日期获取
			if(getCmdType() == CMD_REQUEST_BY_DATE)
			{
				mBluetoothSet.sendMessage("ATHIS=" + edtSelectDate.getText().toString().substring(2) + "\r\n");				
			}
			//获取所有记录
			else {
				mBluetoothSet.sendMessage("ATHISA\r\n");
			}	
			Looper.loop();
		}		
	}
}
