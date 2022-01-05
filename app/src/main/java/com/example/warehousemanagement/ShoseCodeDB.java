package com.example.warehousemanagement;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ShoseCodeDB extends SQLiteOpenHelper {

    private final static int DBVersion=1;//版本
    private final static String DBName="ShoesDB";//DB name
    private final static String TableName="ShoesCode";//table name
    public ShoseCodeDB(@Nullable Context context) {
        super(context,DBName,null,DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL="CREATE TABLE IF NOT EXISTS "+TableName+"(_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                                                                "_TITLE VARCHAR(50), "+
                                                                "_CONTENT TEXT, "+
                                                                "Name VARCHAR(60), "+//鞋名
                                                                "Code VARCHAR(9), "+//鞋code
                                                                "Position VARCHAR(30)"+");";//位置
        db.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL="DROP TABLE "+TableName;
        db.execSQL(SQL);
    }
}
