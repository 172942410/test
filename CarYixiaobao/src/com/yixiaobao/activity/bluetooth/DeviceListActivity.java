package com.yixiaobao.activity.bluetooth;

import java.util.ArrayList;
import java.util.List;

import com.yixiaobao.activity.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This Activity appears as a dialog. It lists any paired devices and devices
 * detected in the area after discovery. When a device is chosen by the user,
 * the MAC address of the device is sent back to the parent Activity in the
 * result Intent.
 */
public class DeviceListActivity extends Activity
{
	// Debugging
	private static final String TAG = "DeviceListActivity";
	private static final boolean D = true;

	// Return Intent extra
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	// Member fields
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
	List<String> lstDevices = new ArrayList<String>();
	private static Boolean hasDevices;
	private ProgressDialog mpDialog = null;
	
		
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_device_list);

		// Set result CANCELED incase the user backs out
		setResult(Activity.RESULT_CANCELED);

		// Initialize the button to perform device discovery
		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				lstDevices.clear();
				doDiscovery();
				//v.setVisibility(View.GONE);
			}
		});

		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices		
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name,lstDevices);

		// Find and set up the ListView for newly discovered devices
		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);

		// Register for broadcasts when a device is discovered
		IntentFilter found_filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, found_filter);

		// Register for broadcasts when discovery has finished
		IntentFilter discovery_filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, discovery_filter);
		
		// Get the local Bluetooth adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBtAdapter != null)
			doDiscovery();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		// Make sure we're not doing discovery anymore
		if (mBtAdapter != null)
		{
			mBtAdapter.cancelDiscovery();
		}

		// Unregister broadcast listeners
		this.unregisterReceiver(mReceiver);
	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery()
	{
		if (D) Log.d(TAG, "doDiscovery()");

		// Indicate scanning in the title
		//setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);

		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering())
		{
			mBtAdapter.cancelDiscovery();
		}
		hasDevices = false;
		// Request discover from BluetoothAdapter 
		mBtAdapter.startDiscovery();
		mpDialog = new ProgressDialog(DeviceListActivity.this);  
        mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置风格为圆形进度条  
        mpDialog.setTitle("Remind");//设置标题  
        mpDialog.setMessage("Scaning the bluetooth devices...");  
        mpDialog.setIndeterminate(false);//设置进度条是否为不明确  
        mpDialog.setCancelable(true);//设置进度条是否可以按退回键取消  
        mpDialog.setButton("Stop", new DialogInterface.OnClickListener(){  

            @Override  
            public void onClick(DialogInterface dialog, int which) {  
                dialog.cancel();  
                if (mBtAdapter.isDiscovering()){
                	mBtAdapter.cancelDiscovery();
                }
            }  
        });  
        mpDialog.show();     
	}

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3)
		{
			// Cancel discovery because it's costly and we're about to connect
			mBtAdapter.cancelDiscovery();

			// Get the device MAC address, which is the last 17 chars in the
			// View
			if (hasDevices){
				String info = ((TextView) v).getText().toString();
				String address = info.substring(info.length() - 17);
	
				// Create the result Intent and include the MAC address
				Intent intent = new Intent();
				intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
	
				// Set result and finish this Activity
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		}
	};

	
	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action))
			{
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = (BluetoothDevice)intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String tempString;
				if(device.getBondState() == BluetoothDevice.BOND_NONE){
					tempString = "Status: UnPaired\n";
				}
				else {
					tempString = "Status: Paired\n";
				}
			
				//添加设备				
				tempString += device.getName() + "\n"
						+ device.getAddress();
				//防止重复添加
				if (lstDevices.indexOf(tempString) == -1){
					lstDevices.add(tempString);
					mNewDevicesArrayAdapter.notifyDataSetChanged();
				}
				//mNewDevicesArrayAdapter.add(device.getName() + "\n"
				//		+ device.getAddress());
				hasDevices = true;
				
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{
				// When discovery is finished, change the Activity title
				//setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.select_device);
				if (mNewDevicesArrayAdapter.getCount() == 0)
				{
					String noDevices = getResources().getText(
							R.string.none_found).toString();
					mNewDevicesArrayAdapter.add(noDevices);
					hasDevices = false;					
				}
				mpDialog.cancel();
			}
		}
	};

}
