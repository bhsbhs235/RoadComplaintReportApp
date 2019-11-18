package com.example.small.onetouchbutton10;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

public class ResultInquiryActivity extends AppCompatActivity {
    DeclareDBHelper mHelper;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_inquiry);
        mHelper = new DeclareDBHelper(this);

        ListView listview;
        ListViewAdapter adapter;

        cursor = mHelper.resultquery();
        adapter = new ListViewAdapter(this, cursor);

        listview = (ListView) findViewById(R.id.result_listview);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(onItemClickListener);

    }


    public void onPause(){
        super.onPause();
    }


    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Log.d("onItem" , " 동작은하네 ㅅㅂ");
            Cursor cursor1 = (Cursor)parent.getAdapter().getItem(position);

            Intent intent = new Intent(ResultInquiryActivity.this,SelectInquiryActivity.class);
            int rowid = cursor1.getInt(cursor.getColumnIndex("_id"));
            intent.putExtra("row_id", rowid);

            startActivity(intent);
        }
    };

}
