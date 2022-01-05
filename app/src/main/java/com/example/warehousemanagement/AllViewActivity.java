package com.example.warehousemanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllViewActivity extends AppCompatActivity {

    private SimpleAdapter sAdapter = null;
    private List<Map<String, Object>> items = null;
    private HashMap<String, Object> item = null;
    private ListView av_view = null;
    private View headItem = null;
    private ShoseCodeDB db = null;
    private static final String tableVar_name = "Name";
    private static final String tableVar_code = "Code";
    private static final String tableVar_position = "Position";
    private database_meth meth = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_view);

        db = new ShoseCodeDB(AllViewActivity.this);
        meth = new database_meth();
        av_view = findViewById(R.id.AV_lv);
        headItem = LayoutInflater.from(AllViewActivity.this).inflate(R.layout.layout, null);
        av_view.addHeaderView(headItem);
        //db.getWritableDatabase().execSQL("DELETE FROM ShoesCode");

        //載入database並將資料排序
        Cursor databaseC = null;
        try {
            databaseC = db.getWritableDatabase().rawQuery("SELECT Name,Code,Position FROM ShoesCode ORDER BY Position COLLATE NOCASE ASC", null);
            //若資料庫沒有資料時
            if (databaseC.getCount() == 0) {
                Toast.makeText(AllViewActivity.this,R.string.emptyDatabase_name,Toast.LENGTH_LONG);
                return;
            }else{
                //儲存在items
                if (databaseC.moveToFirst()) {
                    items = new ArrayList<Map<String, Object>>();//初始化items
                    do {
                        String name = databaseC.getString(databaseC.getColumnIndex(tableVar_name));
                        String code = databaseC.getString(databaseC.getColumnIndex(tableVar_code));
                        String position = databaseC.getString(databaseC.getColumnIndex(tableVar_position));
                        item = new HashMap<String, Object>();//每次loop都需要重新初始化item,使其成為新的item
                        item.put(tableVar_name, name);
                        item.put(tableVar_code, code);
                        item.put(tableVar_position, position);
                        items.add(item);
                    } while (databaseC.moveToNext());
                    //設置listvew初始設定
                    sAdapter = new SimpleAdapter(AllViewActivity.this,
                            items,
                            R.layout.layout,
                            new String[]{"Name", "Code", "Position"},
                            new int[]{R.id.tp1, R.id.tp2, R.id.tp3});
                    av_view.setAdapter(sAdapter);
                } else {
                    Log.e("cursor error", "database can't move to first");
                }
            }
        } catch (Exception e) {
            Log.e("cursor error", e.toString());
            return;
        }

        //設置長按item時的動作
        av_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String DataModify_name = getString(R.string.DataModify_name).toString();
                final String DataDelete_name = getString(R.string.DataDelete_name).toString();
                final String[] DialogMenuName = {DataModify_name, DataDelete_name};
                //禁止head item被長按時彈出dialog
                if (position == av_view.getFirstVisiblePosition()) {
                    return false;
                }
                //設置第一層dialog(item:修改,删除)
                AlertDialog.Builder AD = new AlertDialog.Builder(AllViewActivity.this);
                AD.setItems(DialogMenuName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String, Object> currentItem = (HashMap<String, Object>) av_view.getItemAtPosition(position);
                        //取得當前選取的item在資料庫中的id
                        Cursor c=db.getWritableDatabase().rawQuery("SELECT Code,_id FROM ShoesCode",null);
                        int id=-1;
                        c.moveToFirst();
                        do{
                            if(c.getString(c.getColumnIndex("Code")).equals(currentItem.get("Code").toString())){
                                id=c.getInt(c.getColumnIndex("_id"));
                                break;
                            }
                        }while(c.moveToNext());

                        switch (which) {
                            case 0://修改選項
                                //根據ID數值有不同的行為
                                switch (id){
                                    case -1:
                                        Toast.makeText(AllViewActivity.this,"id error!not find data in database!",Toast.LENGTH_LONG).show();
                                        return;
                                    default:
                                        meth.dataModifyDialog(AllViewActivity.this, currentItem, av_view,id, db, 1, "Position");
                                }

                                break;
                            case 1://刪除選項
                                switch (id){
                                    case -1:
                                        Toast.makeText(AllViewActivity.this,"id error!not find data in database!",Toast.LENGTH_LONG).show();
                                        return;
                                    default:
                                        meth.dataDeleteDialog(AllViewActivity.this, db, currentItem, av_view,id,null);
                                }
                                break;
                        }
                    }
                }).create().show();
                return true;
            }
        });
    }
}