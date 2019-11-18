package com.example.small.onetouchbutton10;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;


public class DeclareDBHelper extends SQLiteOpenHelper implements Serializable {
    private Context context;
    private static String DBNAME = "test1.db";
    static public SQLiteDatabase db;

    private static int VERSION = 1;

    public static final String ROW_iD = "_id";

    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final String DATE = "date";
    public static final String CONTENT = "content";
    public static final String WHAT = "what";
    public static final String NUMBER = "number";
    public static final String CAPTURE = "capture";
    public static final String VIDEO = "video";

    private static final String DB_TABLE = "test1";
    private static final String DB_TABLE2 = "test2";
    private static final String DB_TABLE3 = "test3";

    public DeclareDBHelper(Context context) {
        super(context, DBNAME, null, VERSION);
        this.context = context;
    }

    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DB_TABLE + "(" +
                ROW_iD + " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                LAT + " double ," +
                LNG + " double ," +
                DATE + " TEXT ," +
                CONTENT + " TEXT ," +
                WHAT + " integer" +
                " );";
        Log.d("DB", sql);

        String sql2 = "CREATE TABLE " + DB_TABLE2 + "(" +
                NUMBER + " long ," +
                CAPTURE + " blob " +
                " );";

        String sql3 = "CREATE TABLE " + DB_TABLE3 + "(" +
                NUMBER + " long ," +
                VIDEO + " TEXT" +
                " );";

        db.execSQL(sql);
        db.execSQL(sql2);
        db.execSQL(sql3);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " +  DB_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE2);
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE3);
        onCreate(db);
    }

    public Cursor resultquery() {
        Cursor cursor;

        db = getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM test1", null);

        return cursor;
    }

    public Data selectData(int index) {
        String date = null;
        String content = null;
        Data data = null;

        db = getReadableDatabase();
        String sql = "SELECT * from " + DB_TABLE + " where _id = " + String.valueOf(index) + ";";
        Cursor result = db.rawQuery(sql, null);

        if (result.moveToFirst()) {
            Double lat = result.getDouble(1);
            Double lng = result.getDouble(2);
            date = result.getString(3);
            content = result.getString(4);
            int what = result.getInt(5);
            data = new Data(lat, lng, date, content, what);
        }
        result.close();
        close();
        return data;
    }

    public void insertData(Data data) {
        db = getWritableDatabase();
        ContentValues row = new ContentValues();
        row.put("lat", data.getLat());
        row.put("lng", data.getLng());
        row.put("date", data.getDate());
        row.put("content", data.getContent());
        row.put("what", data.getWhat());
        db.insert(DB_TABLE,null,row);
        Log.d("DB","삽입 성공");
        close();
    }

    public void insertCapture(long captureID, byte[] capture) {
        db = getWritableDatabase();
        String sql = "INSERT INTO test2(number,capture) VALUES ( ? , ? );";
        Log.d("DB", " 된다1");
        SQLiteStatement p = db.compileStatement(sql);
        Log.d("DB", " 된다2");
        p.bindBlob(2, capture);
        Log.d("DB", " 된다3");
        p.bindLong(1, captureID);
        Log.d("DB", " 된다4 captureID =======>" + String.valueOf(captureID) );
        p.execute();
        close();
    }

    public void insertVideo(long videoID, String video){
        db = getWritableDatabase();
        String sql = "INSERT INTO test3(number,video) VALUES ( ? , ? );";
        Log.d("DB", " 된다1");
        SQLiteStatement p = db.compileStatement(sql);
        Log.d("DB", " 된다2");
        p.bindString(2, video);
        Log.d("DB", " 된다3");
        p.bindLong(1, videoID);
        Log.d("DB", " 된다4 captureID =======>" + String.valueOf(videoID) );
        p.execute();
        close();
    }

    public long captureID() {
        Cursor cursor;
        db = getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM test1", null);
        long capture_id = cursor.getCount();
        capture_id = capture_id + 1;
        close();
        return capture_id;
    }

    public ArrayList loadImage(long index){
        Cursor cursor;
        Bitmap bmp = null;
        ArrayList<Bitmap> images = new ArrayList<Bitmap>();
        db = getReadableDatabase();
        String sql = "SELECT * from " + DB_TABLE2 + " where number = " + String.valueOf(index) + ";";

        cursor = db.rawQuery(sql, null);

        Log.d("DB", " Arraylist 개수  =====>" + String.valueOf(cursor.getCount()) );

        if(cursor.moveToFirst()){
            do{
                bmp = getBitmap(cursor.getBlob(1));
                images.add(bmp);
            }while(cursor.moveToNext());
        }

        return images;
    }

    public String loadVideo(long index){
        Cursor cursor;
        db = getReadableDatabase();
        String path = null;
        String sql = "SELECT * from " + DB_TABLE3 + " where number = " + String.valueOf(index) + ";";

        cursor = db.rawQuery(sql,null);

        if(cursor.moveToFirst()){
            path = cursor.getString(1);
        }

        return path;
    }

    public Bitmap getBitmap(byte[] b){
        Bitmap bitmap = BitmapFactory.decodeByteArray(b,0, b.length);
        return bitmap;
    }
}
class Data {
    private double lat = 0;
    private double lng = 0;
    private String date = "";
    private String content = "";
    private int what;

    public Data(){}

    public Data(double lat, double lng, String date, String content, int what){
        this.lat = lat;
        this.lng = lng;
        this.date = date;
        this.content = content;
        this.what = what;
    }


    public double getLat() {
        return this.lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return this.lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getWhat(){ return this.what;}

    public void setWhat(int what){ this.what = what; }

}