package com.example.small.onetouchbutton10;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class SelectInquiryActivity extends AppCompatActivity {
    //  SQLiteDatabase db;
    DeclareDBHelper mHelper;
    private static final String DB_TABLE = "test";

    private TextView mLat;
    private TextView mLng;
    private TextView mDate;
    private TextView mContent;
    private ListView mListview;
    private GridView mGridview;
    private ArrayList<Bitmap> images;
    private VideoView mVideoview;
    private Button mPlay;
    private Button mStop;
    private LinearLayout mList;

    public SelectInquiryActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new DeclareDBHelper(this);
        Data data = null;

        Intent intent = getIntent();
        int rowid = intent.getIntExtra("row_id",0);
        data = mHelper.selectData(rowid);
        if(data == null){
            Log.d("SelectInquiry", "data 객체가 null");
        }

        if(data.getWhat() == 1){
            setContentView(R.layout.activity_select_inquiry);
            mLat = (TextView)findViewById(R.id.text_view_lat);
            mLng = (TextView)findViewById(R.id.text_view_lng);
            mDate = (TextView)findViewById(R.id.text_view_DeclareDate);
            mContent = (TextView)findViewById(R.id.text_view_DeclareContent);
         //   mListview = (ListView)findViewById(R.id.select_listview);
            mLat.setText("위도 : " + String.format("%.7f",data.getLat()));
            mLng.setText(" 경도 : " + String.format("%.7f",data.getLng()));
            mDate.setText(data.getDate());
            mContent.setText(data.getContent());
            images = mHelper.loadImage(rowid);

            mList = (LinearLayout)findViewById(R.id.main_linear);

            for(int i=0; i<images.size(); i++){
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final LinearLayout item = (LinearLayout)inflater.inflate(R.layout.select_activity_item,null);
                ImageView group_name = (ImageView)item.findViewById(R.id.select_imageView);
                group_name.setImageBitmap(images.get(i));

                mList.addView(item);
            }
            /*
            MylistAdapter  imagesadapter = new MylistAdapter(this, R.layout.select_activity_item, images);
            mListview.setAdapter(imagesadapter);
*/
        }else if(data.getWhat() == 0) {
            setContentView(R.layout.activity_select_inquiry2);
            mLat = (TextView) findViewById(R.id.text_view_lat);
            mLng = (TextView) findViewById(R.id.text_view_lng);
            mDate = (TextView) findViewById(R.id.text_view_DeclareDate);
            mContent = (TextView) findViewById(R.id.text_view_DeclareContent);
            mVideoview = (VideoView) findViewById(R.id.videoview);

            String path = mHelper.loadVideo(rowid);

            mLat.setText("위도 : " + String.valueOf(data.getLat()));
            mLng.setText(" 경도 : " + String.valueOf(data.getLng()));
            mDate.setText(data.getDate());
            mContent.setText(data.getContent());

            if (path == null) {
                Log.d("SelectInquiry", "path가 null 이야");
            }
            mVideoview.setVideoPath(path);

            final MediaController mc = new MediaController(SelectInquiryActivity.this);
            mVideoview.setMediaController(mc);
            mVideoview.requestFocus();

            mVideoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Toast.makeText(getApplicationContext(),
                            "동영상이 준비되었습니다.\n'재생' 버튼을 누르세요.", Toast.LENGTH_LONG)
                            .show();
                }
            });
        }
    }
/*
    private void playVideo(){
        mVideoview.seekTo(0);
        mVideoview.start();
    }

    private void stopVideo(){
        mVideoview.pause();
        mVideoview.stopPlayback();
    }

*/
/*
    class MylistAdapter extends BaseAdapter{
        Context maincon;
        LayoutInflater inflater;
        ArrayList<Bitmap> arSrc;
        int layout;

        public MylistAdapter(Context context, int alayout, ArrayList<Bitmap> aarSrc){
            maincon = context;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            arSrc = aarSrc;
            layout = alayout;
        }

        public int getCount(){
            return arSrc.size();
        }

        public Bitmap getItem(int position){
            return arSrc.get(position);
        }

        public long getItemId(int position){
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            final int pos = position;
            if(convertView == null){
                convertView = inflater.inflate(layout, parent, false);
            }
            ImageView img = (ImageView)convertView.findViewById(R.id.select_imageView);
            img.setImageBitmap(arSrc.get(position));

            return convertView;
        }
    }
    */
}




