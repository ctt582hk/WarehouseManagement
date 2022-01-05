package com.example.warehousemanagement;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.sql.SQLData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {
    //database 表的欄名
    private String dbCodeName = "Code";
    private String dbNameName = "Name";
    private String dbPositionName = "Position";

    private int enter = 0;//按下按鈕次數
    private int maxLength = 9;
    private int sLength = 6;//要搜索的字元長度
    private Button SB;//搜索按鈕
    private EditText SET;//輸入欄位
    private ListView SLV;//list視窗
    private ShoseCodeDB db = null;
    private Cursor c = null;
    private View head = null;
    private SimpleAdapter sAdapter = null;
    private List<Map<String, Object>> items = null;
    private Map<String, Object> item = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        //初始化參數
        item = new HashMap<String, Object>();
        items = new ArrayList<Map<String, Object>>();
        SB = findViewById(R.id.search_searchButton);
        SET = findViewById(R.id.search_input);
        SLV = findViewById(R.id.search_LV);
        db = new ShoseCodeDB(SearchActivity.this);
        head = LayoutInflater.from(SearchActivity.this).inflate(R.layout.layout, null);
        SLV.addHeaderView(head);
        SLV.setAdapter(sAdapter);
        database_meth meth = new database_meth();


        //輸入按鈕動作
        SB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                c = db.getWritableDatabase().rawQuery("SELECT Name,Code,Position FROM ShoesCode ORDER BY Position COLLATE NOCASE ASC", null);
                c.moveToFirst();
                items.clear();//每次進入此流程先清除items資料
                String sCode = SET.getText().toString();//search code,取得要搜索的字串(code)
                sCode = meth.checkCode(sCode);//檢查輸入值是否大寫,否則全部轉為大寫
                char[] sCodeArray = sCode.toCharArray();
                //進行搜索
                //資料庫沒有資料時
                if (c.getCount() == 0) {
                    Toast.makeText(SearchActivity.this, R.string.emptyDatabase_name, Toast.LENGTH_LONG);
                    return;
                }
                //輸入字元超過預定值則彈出警告並返回
                if (meth.isOverLength(sCode)) {
                    Toast.makeText(SearchActivity.this, R.string.codeErrorLength, Toast.LENGTH_LONG).show();
                    return;
                }
                //搜索動作
                int stime = 0;//計算搜索到結果的次數
                try {
                    do {

                        int count = 0;//計算字元正確次數(見79行)
                        //取得當前c所指向的code中的值
                        String dbCode = c.getString(c.getColumnIndex(dbCodeName));
                        char[] dbCodeArray = dbCode.toCharArray();
                        int diff = Math.abs(dbCode.length() - sCode.length());//資料的長度和輸入的搜索字串的長度差值
                        if (dbCode.length() > sCode.length()) {
                            for (int j = 0; j < sCodeArray.length; j++) {
                                int arrayPosition = sCodeArray.length - j - 1;
                                if (dbCodeArray[arrayPosition + diff] == sCodeArray[arrayPosition]) {
                                    count++;
                                    stime++;
                                }
                            }
                        } else {//dbCode.length<sCode.length時
                            for (int j = 0; j < dbCodeArray.length; j++) {
                                int arrayPosition = dbCodeArray.length - j - 1;
                                if (dbCodeArray[arrayPosition] == sCodeArray[arrayPosition + diff]) {
                                    count++;
                                    stime++;
                                }
                            }
                        }

                        if (count == sCode.length()) {//如果count==sCode的字元長度,即此筆資料符合搜查,加入到items
                            item = new HashMap<String, Object>();
                            item.put(dbNameName, c.getString(c.getColumnIndex(dbNameName)));
                            item.put(dbCodeName, c.getString(c.getColumnIndex(dbCodeName)));
                            item.put(dbPositionName, c.getString(c.getColumnIndex(dbPositionName)));
                            items.add(item);
                        }
                    } while (c.moveToNext());
                } catch (Exception e) {
                    Log.e("error", e.toString());
                    return;
                }

                switch (stime) {//搜索失敗時彈出信息並返回
                    case 0:
                        Toast.makeText(SearchActivity.this, R.string.searchEmpty_name, Toast.LENGTH_LONG).show();
                        return;
                }
                if (enter == 0) {//第一次按按鈕
                    sAdapter = new SimpleAdapter(SearchActivity.this,
                            items,
                            R.layout.layout,
                            new String[]{dbNameName, dbCodeName, dbPositionName},
                            new int[]{R.id.tp1, R.id.tp2, R.id.tp3});
                    SLV.setAdapter(sAdapter);
                } else {//不是第一次按
                    sAdapter.notifyDataSetChanged();
                }

                enter = 1;
            }
        });

        //長按list view 中的 item動作
        SLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder AB = new AlertDialog.Builder(SearchActivity.this);
                String[] itemName = {getString(R.string.DataModify_name), getString(R.string.DataDelete_name)};
                HashMap<String, Object> currentItem = (HashMap<String, Object>) SLV.getItemAtPosition(position);
                if (position == SLV.getFirstVisiblePosition()) {
                    return false;
                }
                AB.setItems(itemName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int id=meth.getId(db,currentItem.get("Code").toString());//取得資料庫中的ID
                        switch (which) {
                            //修改選項
                            case 0:
                                meth.dataModifyDialog(SearchActivity.this, currentItem, db,SLV,id);
                                break;
                            //刪除選項
                            case 1:
                                meth.dataDeleteDialog(SearchActivity.this,db,currentItem,SLV,id,SET.getText().toString());
                                break;
                        }
                    }
                }).create().show();
                return true;
            }
        });
    }
}