package com.example.small.onetouchbutton10;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ListViewAdapter extends CursorAdapter{

    private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>();

    //public ListViewAdapter(){ }

    public ListViewAdapter(Context context, Cursor cursor){
        super(context,cursor,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        return LayoutInflater.from(context).inflate(R.layout.resultlistview_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor){
        ImageView iconImageView = (ImageView) view.findViewById(R.id.imageView1);
        TextView contentTextView = (TextView) view.findViewById(R.id.textview1);
        TextView latTextView = (TextView) view.findViewById(R.id.textview2);
        TextView lngTextView = (TextView) view.findViewById(R.id.textview3);
        TextView dateTextView = (TextView) view.findViewById(R.id.textview4);


        Drawable image = ContextCompat.getDrawable(context,R.drawable.camera);
        String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
        Double lat = cursor.getDouble(cursor.getColumnIndexOrThrow("lat"));
        String latStr = String.format("%.7f",lat);
        Double lng = cursor.getDouble(cursor.getColumnIndexOrThrow("lng"));
        String lngStr = String.format("%.7f",lng);
        String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

        iconImageView.setImageDrawable(image);
        contentTextView.setText(content);
        latTextView.setText(latStr);
        lngTextView.setText(lngStr);
        dateTextView.setText(date);
    }

    /*
    @Override
    public int getCount(){
        return listViewItemList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final int pos = position;
        final Context context = parent.getContext();

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.resultlistview_item,parent,false);
        }

        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1);
        TextView contentTextView = (TextView) convertView.findViewById(R.id.textview1);
        TextView latTextView = (TextView) convertView.findViewById(R.id.textview2);
        TextView lngTextView = (TextView) convertView.findViewById(R.id.textview3);
        TextView dateTextView = (TextView) convertView.findViewById(R.id.textview4);

        ListViewItem listViewItem = listViewItemList.get(position);

        iconImageView.setImageDrawable(listViewItem.getIcon());
        contentTextView.setText(listViewItem.getTitle());
        latTextView.setText(listViewItem.getLat());
        lngTextView.setText(listViewItem.getLng());
        dateTextView.setText(listViewItem.getDate());

        return convertView;
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public Object getItem(int position){
        return listViewItemList.get(position);
    }

    public void addItem(Drawable icon, String content, String lat, String lng, String date){
        ListViewItem item = new ListViewItem();

        item.setIcon(icon);
        item.setContent(content);
        item.setLat(lat);
        item.setLng(lng);
        item.setDate(date);

        listViewItemList.add(item);
    }
    */

}
