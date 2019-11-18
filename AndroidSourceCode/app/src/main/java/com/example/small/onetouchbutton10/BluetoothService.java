package com.example.small.onetouchbutton10;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;


public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter mBluetoothAdapter;
    private static Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    static private WriteThread mWriteThread;
    String mSdpath = "/storage/emulated/0";
    String ext = Environment.getExternalStorageState();
    public DeclareDBHelper mHelper;
    SimpleDateFormat mFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String mProvider;
    static public double lat = 1234;
    static public double lng = 5678;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    LocationManager mLocMan;
    SQLiteDatabase db;

    public class BluetoothBinder extends Binder {
        BluetoothService getService() { return BluetoothService.this;}
    }
    BluetoothBinder mBinder = new BluetoothBinder();


    public BluetoothService(Context context, Handler handler) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
        mHelper = new DeclareDBHelper(context);
    }

    public BluetoothService(){}

    public interface ICallback{
        public void recvData();
    }

    public ICallback mCallback;

    public void registerCallback(ICallback cb){
        mCallback = cb;
    }

    public void write(byte[] out){
        mWriteThread.write(out);
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e){
                Log.e(TAG,"ConnectThread create() failed", e);
            }
            mmSocket = tmp;
            Log.i(TAG, "ConnetThread 성공");
        }

        public void run(){
            Log.i(TAG, "mConnectThread 실행");
            setName("ConnectThread");

            mBluetoothAdapter.cancelDiscovery();

            try{
                mmSocket.connect();
                Log.i(TAG, "connect() 성공");
            } catch(IOException e){
                connectionFailed();
                Log.e(TAG, "connet() 실패",e);
                try{
                    mmSocket.close();
                }catch (IOException closeExeption){}
                return;
            }
            synchronized (BluetoothService.this){
                mConnectThread = null;
            }
            connected(mmSocket, mmDevice);
        }
        public void cancel(){
            try{
                mmSocket.close();
            }catch(IOException e){
                Log.e(TAG, "ConnectThread 연결 취소되었습니다",e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private BluetoothSocket mmSocket;
        private InputStream mmInputStream;
        long mNow;
        Date mDate;
        SimpleDateFormat mFormat1 = new SimpleDateFormat("yyyyMMddHHmmss");
        Data data = new Data();

        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG, "ConnectedThread 생성");
            mmSocket = socket;
            InputStream tmpIn = null;
            try{
                tmpIn = socket.getInputStream();
            }catch(IOException e){
                Log.e(TAG, "tmp 소켓이 생성 안됬음",e);
            }
            mmInputStream = tmpIn;
        }

        public void run() {
            Looper.prepare();
            Log.i(TAG, "mConnectedThread 시~~~작");
            Bitmap bmp;
            byte[] buffer = new byte[256];
            int bytes;
            String action = "";
            String readMessage = "";
            while (true) {
                try {
                    bytes = mmInputStream.read(buffer);
                    action = new String(buffer, 0, bytes);
                    Log.d("action" , "## action ## =>>>> " + action);
                    // mmInputStream.reset();
                } catch (IOException e) {
                    Log.e(TAG, "액션 수신 에러");
                    continue;
                }

                switch (action) {
                    case "jsondata":
                        try {
                            bytes = mmInputStream.read(buffer);
                            readMessage = new String(buffer, 0, bytes);
                            Log.d(TAG, readMessage);
                        } catch (IOException e) {
                            Log.e(TAG, "환경설정파일 수신 에러", e);
                        }

                        try {
                            JSONObject first = new JSONObject(readMessage);
                            Json.jsondata.setWhat(first.getInt("what"));
                            JSONObject camera = first.getJSONObject("camera");
                            Json.jsondata.setCameracnt(camera.getInt("count"));
                            JSONObject video = first.getJSONObject("video");
                            Json.jsondata.setVideosec(video.getInt("seconds"));
                            Log.i(TAG, "제이슨 데이터 수신 성공");
                            int videosec = Json.jsondata.getVideosec();
                            String str = Integer.toString(videosec);
                            Log.i(TAG, str);

                        } catch (JSONException e) {
                            Log.e(TAG, "JSON 데이터 수신 에러", e);
                        }
                        break;
                    case "datasend":
                        data.setLat(lat);
                        data.setLng(lng);
                        data.setDate(getTime());
                        data.setContent("불편사항 신고입니다");
                        int what = Json.jsondata.getWhat();
                        int cameracnt = Json.jsondata.getCameracnt();
                        String a = String.valueOf(what);
                        Log.i(TAG, "카메라 or 비디오 => " + a);
                        if (what == 1) {
                            long capture_id = mHelper.captureID();
                            for (int i = 0; i < cameracnt; i++) {
                                String file =  "/" + getDate() + "(" + String.valueOf(i)+ ")" + ".png";
                                Log.d("Capture",file + "시작");
                                BufferedInputStream bufferedInputStream = new BufferedInputStream(mmInputStream);
                                Log.d("Capture", bufferedInputStream.toString());
                                bmp = BitmapFactory.decodeStream(bufferedInputStream);
                                Log.d("Capture",file  + "비트맵");
                                // saveBitmapToFileCache(bmp,dir,file);
                                byte[] capture = getByteArrayFromBitmap(bmp);
                                mHelper.insertCapture(capture_id,capture);
                            }
                            data.setWhat(1);
                            mHelper.insertData(data);
                            Log.d("Capture", "사진 받는거 된거 같은데?");

                        } else if(what == 0){
                            String datasizeStr = "";
                            data.setLat(lat);
                            data.setLng(lng);
                            data.setDate(getTime());
                            data.setContent("불편사항 신고입니다");
                            int datasize = 0;
                            try {
                                bytes = mmInputStream.read(buffer);
                                datasizeStr = new String(buffer, 0, bytes);
                                Log.d("Video", datasizeStr);
                                datasize = Integer.parseInt(datasizeStr);
                            } catch (IOException e) {
                                Log.e("Video", "###사이즈 수신 에러###", e);
                            }
                            String dir = mSdpath + "/Video";
                            File dirname = new File(dir);
                            if( !dirname.exists()){
                                dirname.mkdir();
                            }
                            String file =  dir + "/" + getDate()  + ".mp4";
                            Log.d("Video", "mSdpath => " + mSdpath);
                            Log.d("Video", "file => " + file);
                            Log.d("Video","첫번째 관문 통과");
                            byte[] buf = new byte[datasize];

                            int readBytes;
                            int filesize = 0;
                            int current = 0;
                            try {
                                File video = new File(file);
                                FileOutputStream fos = new FileOutputStream(video);
                                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fos);
                                readBytes = mmInputStream.read(buf,0,buf.length);
                                current = readBytes;
                                Log.d("Video","두번째 관문 통과");
                                do{
                                    readBytes = mmInputStream.read(buf,current,(buf.length-current));
                                    filesize = buf.length-current;
                                    if(readBytes>=0) current += readBytes;
                                    Log.d("Video","readBytes =>>>>" + readBytes + "current =>>>>" + current + "남은양" + String.valueOf(filesize) );
                                }while(readBytes > 0);
                                Log.d("Video","세번째 관문 통과");
                                long video_id = mHelper.captureID();
                                bufferedOutputStream.write(buf,0,current);
                                bufferedOutputStream.flush();
                                mHelper.insertVideo( video_id, file );
                                Log.d("Video","네번째 관문 통과");
                                fos.close();
                                bufferedOutputStream.close();
                            }catch (IOException e){
                                e.printStackTrace();
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            Log.d("Video","완벽해 ㅊㅋㅊㅋ!!!!!!");
                            data.setWhat(0);
                            mHelper.insertData(data);
                        }else{

                        }
                        /*
                         db.insert("test",null,row);
                         */
                }//switch
            }// while
        }

        public byte[] getByteArrayFromBitmap(Bitmap bitmap){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] data = stream.toByteArray();

            return data;
        }


        public void cancel(){
            try{
                mmSocket.close();
            }catch (IOException e){
                Log.e(TAG, "소켓 연결 실패해서 연결을 닫는다", e);
            }
        }

        public String getTime(){
            mNow = System.currentTimeMillis();
            mDate = new Date(mNow);
            return mFormat2.format(mDate);
        }
        public String getDate(){
            mNow = System.currentTimeMillis();
            mDate = new Date(mNow);
            return mFormat1.format(mDate);
        }
    }

    private class WriteThread extends Thread {
        private BluetoothSocket mmSocket;
        private OutputStream mmOutputStream;

        public WriteThread(BluetoothSocket socket) {
            Log.d(TAG, "WriteThread 생성");
            mmSocket = socket;
            OutputStream tmpOut = null;
            try {
                tmpOut = mmSocket.getOutputStream();

            } catch (IOException e) {
                Log.e(TAG, "tmp socket 생성 실패", e);
            }

            mmOutputStream = tmpOut;

        }

        public void run(){
            Looper.prepare();
            Log.i(TAG, "WriteThread 실행");

        }

        public void write(byte[] buffer){
            try{
                Log.d("mWriteThread", "정상작동 ++++> " + String.valueOf(buffer));
                mmOutputStream.write(buffer);
            }catch(IOException e){
                Log.e(TAG, "write()중 오류",e);
            }
        }

        public void cancel(){
            try{
                mmSocket.close();
            }catch (IOException e){
                Log.e(TAG, "소켓 연결 실패해서 연결을 닫는다", e);
            }
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind 실행");
        return mBinder;
    }

    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"BletoothService started");
        if(ext.equals(Environment.MEDIA_MOUNTED)){
            Log.i(TAG,"SDcard 마운트 되있음 삉삉");
            mSdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.d(TAG,mSdpath);
            //mSdpath = "/storage/emulated/0";
        }else{
            Log.i(TAG,"SDcard 마운트 안되 ㅅㅂ");
            mSdpath = Environment.MEDIA_UNMOUNTED;
        }
        mLocMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mProvider = mLocMan.getBestProvider(new Criteria(),true);
        if(mHelper == null) {
            Log.d(TAG, "mHelper 가 null이다1");
        }else{
            Log.d(TAG, "mHelper 가 null아니다 그럼 머야 1");
        }
        // mHelper = MyApplication.getDB();
        //mHelper = ((MyApplication)this.getApplication()).getDB();
        if(mHelper == null) {
            Log.d(TAG, "mHelper 가 null이다2");
        }else{
            Log.d(TAG, "mHelper 가 null아니다 그럼 머야 2");
            try{
                db = mHelper.getWritableDatabase();
            }catch(SQLiteException e){
                e.printStackTrace();
            }

        }
        isGPSEnabled = mLocMan.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = mLocMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
        mLocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    public void onDestroy(){
        super.onDestroy();
        stop();
        Log.d(TAG,"Destroyed");
        mLocMan.removeUpdates(locationListener);
    }
    public synchronized void connect(BluetoothDevice device){
        Log.d(TAG, "연결됨 :" + device);

        if(mConnectedThread != null){
            mConnectedThread.cancel();
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();

    }
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device){
        Log.d(TAG, "Connected() 실행");

        if(mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if(mConnectedThread != null){mConnectedThread.cancel();mConnectedThread=null;}
        if(mWriteThread != null){mWriteThread.cancel();;mWriteThread=null;}
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        mWriteThread = new WriteThread(socket);
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mWriteThread.start();

        mHandler.sendMessage(msg);
    }

    public synchronized void stop(){
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    private void connectionFailed(){
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "기기간의 연결실패");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("bluetooth location", " onLocationChanged 작동!!");
            lat = location.getLatitude();
            lng = location.getLongitude();
            Log.d("bluetooth location", " lat +++++>" + String.valueOf(lat));
            Log.d("bluetooth location", " lng +++++>" +  String.valueOf(lng));
            // locationTag = false;

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

}
