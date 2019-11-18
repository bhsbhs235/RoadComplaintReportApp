package com.example.small.onetouchbutton10;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Parcelable;

public class DeviceListActivity extends AppCompatActivity {

    private static final String TAG = "DeviceListActivity";

    public static String EXTRA_DEVICE_ADDRESS = null;

    static BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDeviceArrayAdapter;
    // public ArrayList<String> pairedList = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        setResult(Activity.RESULT_CANCELED);

        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mNewDeviceArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        ListView pariedListView = (ListView) findViewById(R.id.paired_devices);
        pariedListView.setAdapter(mPairedDevicesArrayAdapter);
        pariedListView.setOnItemClickListener(mDeviceClickListener);

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDeviceArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);


        IntentFilter filter = new IntentFilter((BluetoothDevice.ACTION_FOUND));
        this.registerReceiver(mReceiver,filter);

        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter2);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);

            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }

        }
        else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(mBluetoothAdapter != null){
            mBluetoothAdapter.cancelDiscovery();
        }

        this.unregisterReceiver(mReceiver);
    }

    public void doDiscovery(){
        Log.d(TAG, "doDiscovery()");

        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }

        mBluetoothAdapter.startDiscovery();
    }

    public OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            mBluetoothAdapter.cancelDiscovery();

            String  info = ((TextView) v).getText().toString();
            String  address = info.substring(info.length()-17);

            Intent deviceintent  = new Intent();
            deviceintent.putExtra(EXTRA_DEVICE_ADDRESS,address);
            setResult(Activity.RESULT_OK,deviceintent);
            finish();
        }
    };

    // 이부분 수정
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent brintent) {
            String action = brintent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = brintent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() != BluetoothDevice.BOND_BONDED)
                {
                    mNewDeviceArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    if(mNewDeviceArrayAdapter.getCount() == 0){
                        String noDevices = getResources().getText(R.string.none_found).toString();
                        mNewDeviceArrayAdapter.add(noDevices);
                    }
                }
            }
        }
    };

}