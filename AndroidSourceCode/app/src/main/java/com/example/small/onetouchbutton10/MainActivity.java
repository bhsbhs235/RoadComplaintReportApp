package com.example.small.onetouchbutton10;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.graphics.Bitmap;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_BLUETOOTH_ENABLE = 100;

    private TextView mConnectionStatus;
    static BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName = null;
    private Button mBluetooth;
    private Button mDeclare;
    private Button mResultInquiry;
    private Button mMyInformation;
    private Button mAppInformation;
    private Button mPreferences;
    static boolean isConnectionError = false;
    private boolean mBound = false;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CONNECT_DEVICE = 1;

    BluetoothService mBluetoothService = null;

    public static final String TOAST = "Toast";
    public static final String DEVICE_NAME = "device name";
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_READ = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_DEVICE_NAME = 1;

    public DeclareDBHelper mHelper;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "+++++ ON CREATE +++++");
        setContentView(R.layout.activity_main);

        mHelper = new DeclareDBHelper(this);
        //   MyApplication.setDB(mHelper);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            showErrorDialog("이 기기는 블루투스를 지원하지 않습니다");
            return;
        }
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableintent, REQUEST_BLUETOOTH_ENABLE);
        }
        else{
            setuplayout();
            /*
            Intent devicelistintent = new Intent("bae.DeviceList");
            startActivityForResult(devicelistintent,REQUEST_CONNECT_DEVICE);
            */
        }
        mBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent devicelistintent = new Intent("bae.DeviceList");
                startActivityForResult(devicelistintent,REQUEST_CONNECT_DEVICE);
                setup();
            }
        });
        mDeclare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent declareintent = new Intent(MainActivity.this,DeclareActivity.class);
                startActivity(declareintent);
            }
        });
        mResultInquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultinquiry = new Intent(MainActivity.this,ResultInquiryActivity.class);
                startActivity(resultinquiry);
            }
        });
        mPreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent preferences = new Intent(MainActivity.this,PreferencesActivity.class);
                startActivity(preferences);
            }
        });
        mMyInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent information= new Intent(MainActivity.this,MyInformation.class);
                startActivity(information);
            }
        });
        mAppInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent menual= new Intent(MainActivity.this,Menual.class);
                startActivity(menual);
            }
        });
    }
    public void setuplayout(){
        mConnectionStatus = (TextView)findViewById(R.id.connection_status_textview);
        mBluetooth = (Button)findViewById(R.id.button_Bluetooth);
        mDeclare = (Button)findViewById(R.id.button_Declare);
        mResultInquiry = (Button)findViewById(R.id.button_ResultInquiry);
        mMyInformation = (Button)findViewById(R.id.button_MyInformation);
        mAppInformation = (Button)findViewById(R.id.button_AppInformation);
        mPreferences = (Button)findViewById(R.id.button_Preferences);
    }

    public void onStart(){
        super.onStart();
        Log.e(TAG, "+++++ ON START +++++");
    }

    public void setup(){
        mBluetoothService = new BluetoothService(this, mHandler);
    }

    public void onResume(){
        super.onResume();
        Log.d(TAG, "ON Resume");
    }

    private void sendMessage(String message){
        byte[] send = message.getBytes();
        mBluetoothService.write(send);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult 결과" + resultCode);
        switch (requestCode) {
            case REQUEST_BLUETOOTH_ENABLE:
                /*
                if (resultCode == RESULT_OK) {
                    // Devicelistactivity
                    setup();
                    Intent devicelistintent = new Intent("bae.DeviceList");
                    startActivityForResult(devicelistintent,REQUEST_CONNECT_DEVICE);
                }
                */
                if (resultCode == RESULT_CANCELED) {
                    showQuitDialog(" 블루투스를 활성화 하십시오");
                }
                break;
            case REQUEST_CONNECT_DEVICE:
                if(resultCode == Activity.RESULT_OK){
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mBluetoothService.connect(device);
                    Intent intent = new Intent(this,BluetoothService.class);
                    bindService(intent,srvConn, Context.BIND_AUTO_CREATE);
                }
                break;
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(mBound) {
            unbindService(srvConn);
            mBound = false;
        }
        mHelper.close();
    }

    ServiceConnection srvConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mBluetoothService = ((BluetoothService.BluetoothBinder)binder).getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
            mBound = false;
        }
    };

    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
               /*
                case MESSAGE_WRITE:
                    Log.i(TAG,"handler MESSAGE_WRITE");
                    break;
                case MESSAGE_READ:

                    Bitmap bmp = (Bitmap)msg.obj;
                    if(bmp == null){
                        Toast.makeText(getApplicationContext(),"비트맵이 널이다",Toast.LENGTH_SHORT).show();
                    }else{
                        mImage.setImageBitmap(bmp);
                    }
                    break;
                 */
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),mConnectedDeviceName + "와 연결됬다",Toast.LENGTH_LONG).show();
                    mConnectionStatus.setText(mConnectedDeviceName );
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),msg.getData().getString(TOAST),Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };

    public void showQuitDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }

    public void showErrorDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if ( isConnectionError  ) {
                    isConnectionError = false;
                    finish();
                }
            }
        });
        builder.create().show();
    }
}
