package com.example.small.onetouchbutton10;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class PreferencesActivity extends AppCompatActivity {

    private BluetoothService mService;
    private Button mCamera;
    private Button mVideo;
    private Button m3cap;
    private Button m5cap;
    private Button m8cap;
    private Button m45sec;
    private Button m60sec;
    private Button m90sec;
    private Button muuid;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        mCamera = (Button)findViewById(R.id.button_camera);
        mVideo = (Button)findViewById(R.id.button_video);
        m3cap = (Button)findViewById(R.id.button_3);
        m5cap = (Button)findViewById(R.id.button_5);
        m8cap = (Button)findViewById(R.id.button_8);
        m45sec = (Button)findViewById(R.id.button_45);
        m60sec = (Button)findViewById(R.id.button_60);
        m90sec = (Button)findViewById(R.id.button_90);

        startServiceMethod();

        if(mService == null) {
            Log.d("bindService", "mService가 null  1111111");
        }
        else{
            Log.d("bindService", "mService가 null 아니라고 ㅅㅂ111111");
        }

        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendaction("preferences");sendMessage("1");Json.jsondata.setWhat(1);
            }
        });
        mVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendaction("preferences"); sendMessage("2");Json.jsondata.setWhat(0);
            }
        });
        m3cap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendaction("preferences");sendMessage("3");Json.jsondata.setCameracnt(3);
            }
        });
        m5cap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendaction("preferences");sendMessage("4");Json.jsondata.setCameracnt(5);
            }
        });
        m8cap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendaction("preferences");sendMessage("5");Json.jsondata.setCameracnt(8);
            }
        });
        m45sec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendaction("preferences");sendMessage("6");Json.jsondata.setVideosec(45);
            }
        });
        m60sec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendaction("preferences");sendMessage("7");Json.jsondata.setVideosec(60);
            }
        });
        m90sec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendaction("preferences");sendMessage("8");Json.jsondata.setVideosec(90);
            }
        });
        /*
        muuid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendaction("uuid");sendMessage("abcdefg");
            }
        });
        */
    }

    public void sendaction(String action){
        sendMessage(action);
        try{
            Thread.sleep(5);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("bindService", "onServiceConnected 실행");
            BluetoothService.BluetoothBinder binder = (BluetoothService.BluetoothBinder)service;
            mService = binder.getService();
            if(mService == null) {
                Log.d("bindService", "mService가 null 2222222");
            }
            else{
                Log.d("bindService", "mService가 null 아니라고 ㅅㅂ222222222");
            }
            mBound = true;
            mService.registerCallback(mCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
            Log.d("bindService", "서비스 종료");
        }
    };

    private BluetoothService.ICallback mCallback = new BluetoothService.ICallback() {
        @Override
        public void recvData() {

        }
    };


    public void startServiceMethod(){
        Intent service = new Intent(this, BluetoothService.class);
        bindService(service, mConnection, Context.BIND_AUTO_CREATE);
    }


    private void sendMessage(String message){
        byte[] send = message.getBytes();
        mService.write(send);
    }


}
