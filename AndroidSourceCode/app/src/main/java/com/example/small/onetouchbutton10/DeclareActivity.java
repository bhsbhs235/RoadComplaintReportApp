package com.example.small.onetouchbutton10;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.*;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class DeclareActivity extends AppCompatActivity {
    private Button mLocate;
    private TextView mTvplace;
    private ImageView mImage1;
    private ImageView mImage2;
    private ImageView mImage3;
    private RadioGroup mRadioGroup;
    private Button mSuccess;
    private EditText mEdit;
    private Uri mImageUri;
    double lat = 0;
    double lng = 0;
    static public Bitmap bmp;
    DeclareDBHelper mHelper;

    private static final int REQUEST_LatLng = 1;
    private static final int PICK_FROM_ALBUM = 2;

    long mNow;
    Date mDate;
    SimpleDateFormat mFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_declare);
        mHelper = new DeclareDBHelper(this);

        mLocate = (Button) findViewById(R.id.button_locate);
        mTvplace = (TextView) findViewById(R.id.tvPlace);
        mImage1 = (ImageView)findViewById(R.id.image1);
        mImage2 = (ImageView)findViewById(R.id.image2);
        mImage3 = (ImageView)findViewById(R.id.image3);
        mEdit = (EditText)findViewById(R.id.declare_edittext);
        mRadioGroup = (RadioGroup)findViewById(R.id.radiogroup);
        mRadioGroup.setOnCheckedChangeListener(mRadioCheck);
        mSuccess = (Button)findViewById(R.id.success);

        mLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("map");
                startActivityForResult(intent,REQUEST_LatLng);
            }
        });

        mImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doTakeAlbumAction(111);
            }
        });

        mImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doTakeAlbumAction(222);
            }
        });

        mImage3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doTakeAlbumAction(333);
            }
        });

        mSuccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Data data = new Data();
                data.setLat(lat);
                data.setLng(lng);
                data.setDate(getTime());
                if ( mEdit.getText().toString().length() == 0 ) { data.setContent("내용이 없습니다");}else {data.setContent(mEdit.getText().toString());}
                data.setWhat(1);
                long capture_id = mHelper.captureID();

                mImage1.buildDrawingCache();
                Bitmap bitmap = mImage1.getDrawingCache();
                byte[] capture = getByteArrayFromBitmap(bitmap);
                mHelper.insertCapture(capture_id,capture);

                mImage2.buildDrawingCache();
                bitmap = mImage2.getDrawingCache();
                capture = getByteArrayFromBitmap(bitmap);
                mHelper.insertCapture(capture_id,capture);

                mImage3.buildDrawingCache();
                bitmap = mImage3.getDrawingCache();
                capture = getByteArrayFromBitmap(bitmap);
                mHelper.insertCapture(capture_id,capture);

                mHelper.insertData(data);

                Toast.makeText(getApplicationContext(),
                        "정상적으로 신고 하였습니다", Toast.LENGTH_LONG)
                        .show();
            }
        });


    }

    public void doTakeAlbumAction(int FLAG){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, FLAG);
    }

    public byte[] getByteArrayFromBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] data = stream.toByteArray();

        return data;
    }


    public String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat2.format(mDate);
    }

    RadioGroup.OnCheckedChangeListener mRadioCheck =
            new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(group.getId() == R.id.radiogroup)
                        switch (checkedId){
                            case R.id.a:
                                mEdit.setText("낙하물 신고입니담");
                                break;
                            case R.id.b:
                                mEdit.setText("포트홀 신고입니답");
                                break;
                            case R.id.c:
                                mEdit.setText("기타 도로 불편사항신고입니다 삉삉");
                                break;
                        }
                }
            };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode != RESULT_OK)return;

        if(requestCode == REQUEST_LatLng && resultCode == Activity.RESULT_OK){
            lat = data.getDoubleExtra("Lat",0);
            lng = data.getDoubleExtra("Lng",0);
            mTvplace.setText("위도 : " + String.format("%.7f",lat) + " 경도 : " + String.format("%.7f",lng));
        }

        if( requestCode == 111 || requestCode==222 || requestCode==333){
            mImageUri = data.getData();
            try{
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(),mImageUri);
            }catch(IOException e){
                e.printStackTrace();
            }

            switch(requestCode)
            {
                case 111:
                {
                    mImage1.setImageBitmap(bmp);
                    break;
                }
                case 222:
                {
                    mImage2.setImageBitmap(bmp);
                    break;
                }
                case 333:
                {
                    mImage3.setImageBitmap(bmp);
                }
            }
        }
    }
}
