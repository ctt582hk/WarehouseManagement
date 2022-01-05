package com.example.warehousemanagement;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataInputActivity extends AppCompatActivity {
    private Button DIB,deleteAlldata;//搜索按鈕
    private EditText DIinputcode,DIinputposition,DIinputShoesName;//輸入欄位(code,position)
    private ListView DILV;//list視窗
    private ShoseCodeDB db=null;//資料庫名拼錯,沒法改,只能沿用
    private Cursor c=null;
    private TextView DIname;
    private SimpleAdapter sAdapter=null;
    private database_meth meth=null;
    private AlertDialog.Builder AD=null;
    private int EnterCount=0;//計算輸入按鈕被按下多少次
    //把輸入的數據轉換成<key,value>型態
    private List<Map<String,Object>>items=null;//items為所有item的集合
    private Map<String,Object>item=null;//單一item的資料
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_input);
        //初始化參數
        DIB=findViewById(R.id.DI_inputButton);
        DIinputcode=findViewById(R.id.DI_inputcode);
        DIinputposition=findViewById(R.id.DI_inputposition);
        DIinputShoesName=findViewById(R.id.DI_inputname);
        DILV=findViewById(R.id.DI_LV);
        db=new ShoseCodeDB(this);
        items=new ArrayList<Map<String,Object>>();
        item=new HashMap<String, Object>();
        View head= LayoutInflater.from(this).inflate(R.layout.layout,null);//head item
        AD= new AlertDialog.Builder(DataInputActivity.this);
        DILV.addHeaderView(head);
        DILV.setAdapter(sAdapter);
        meth=new database_meth();//調用database_meth函數
        deleteAlldata=findViewById(R.id.deleteAlldata);

        //設置刪除所有data按鈕
        deleteAlldata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder warnning=new AlertDialog.Builder(DataInputActivity.this);
                warnning.setTitle(R.string.deleteAllData_warning_name);
                warnning.setPositiveButton(R.string.yes_name, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        meth.DeleteAllData(db);
                    }
                });
                warnning.setNegativeButton(R.string.no_name, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                warnning.create().show();
            }
        });


        //設置對LV內Item長按時動作
        DILV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String DataModify_name=getString(R.string.DataModify_name).toString();
                final String DataDelete_name=getString(R.string.DataDelete_name).toString();
                final String[] DialogMenuName={DataModify_name,DataDelete_name};
                //禁止head item被長按時彈出dialog
                if(position==DILV.getFirstVisiblePosition()){
                    return false;
                }

                AD.setItems(DialogMenuName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //取得當前item的值
                        HashMap<String,Object> currentItem=(HashMap<String,Object>) DILV.getItemAtPosition(position);
                        switch (which){
                            case 0:
                                //設置修改選項動作
                                //按下後彈出新的dialog以供修改
                                AlertDialog.Builder MAB=new AlertDialog.Builder(DataInputActivity.this);
                                //新dialog基本設置
                                View modifyView=View.inflate(DataInputActivity.this,R.layout.layout_datamodify,null);
                                MAB.setView(modifyView);
                                MAB.setTitle(getString(R.string.DataModify_name));

                                //設置所有edittext的預設文字,其為當前選取的item中對應的值
                                        //此為傳值址運算,所以可以這樣做
                                EditText Modify_View_name_et=modifyView.findViewById(R.id.DataModify_Name_et);
                                Modify_View_name_et.setText(currentItem.get("Name").toString(),null);
                                EditText Modify_View_code_et=modifyView.findViewById(R.id.DataModify_Code_et);
                                Modify_View_code_et.setText(currentItem.get("Code").toString(),null);
                                EditText Modify_View_position_et=modifyView.findViewById(R.id.DataModify_Position_et);
                                Modify_View_position_et.setText(currentItem.get("Position").toString(),null);

                                //按下確定時動作
                                MAB.setPositiveButton(getString(R.string.confirm_name), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //取得輸入的新的值
                                        EditText Modify_name_et=modifyView.findViewById(R.id.DataModify_Name_et);
                                        String Modify_name=Modify_name_et.getText().toString();
                                        EditText Modify_code_et=modifyView.findViewById(R.id.DataModify_Code_et);
                                        String Modify_code=Modify_code_et.getText().toString();
                                        EditText Modify_position_et=modifyView.findViewById(R.id.DataModify_Position_et);
                                        String Modify_position=meth.checkPosition(Modify_position_et.getText().toString());//檢查position第一個字元是否大寫,
                                                                                                                            // 如否則轉為大寫,此規則僅適用於英文
                                        if(!meth.isMaxLength(Modify_code)){
                                            Toast.makeText(DataInputActivity.this,R.string.errorLength_name,Toast.LENGTH_LONG).show();
                                            return;
                                        };
                                        //把新的值輸入database
                                        String[] changeName={"Name","Code","Position"};
                                        String[] changeData={Modify_name,Modify_code,Modify_position};
                                        String[] oldData={currentItem.get("Name").toString(),currentItem.get("Code").toString(),currentItem.get("Position").toString()};
                                        for(int i=0;i<changeName.length;i++) {
                                            String Cname = changeName[i];
                                            String newdata = changeData[i];
                                            String olddata = oldData[i];
                                            try {
                                                int id=meth.getId(db,currentItem.get("Code").toString());
                                                meth.UpdateDatabaseData(db,Cname,newdata,id);
                                            } catch (Exception e) {
                                                //更新失敗跳出信息,並結束該子程式
                                                Toast.makeText(DataInputActivity.this, getString(R.string.update_fail_name) + ",i=" + Integer.toString(i), Toast.LENGTH_LONG).show();
                                                Log.e("update Error", e.toString());
                                                return;
                                            }
                                        }
                                        //更新成功信息
                                        Toast.makeText(DataInputActivity.this, R.string.update_success_name, Toast.LENGTH_SHORT).show();

                                        //更新listview
                                        items.clear();
                                        Cursor modifyC=db.getWritableDatabase().rawQuery("SELECT Name,Code,Position FROM ShoesCode",null);
                                        modifyC.moveToLast();

                                        for(int i=0;i<EnterCount;i++){
                                            item=new HashMap<String, Object>();//宣告item為新的item
                                            String name=modifyC.getString(modifyC.getColumnIndex(changeName[0]));
                                            String code=modifyC.getString(modifyC.getColumnIndex(changeName[1]));
                                            String position=modifyC.getString(modifyC.getColumnIndex(changeName[2]));
                                            item.put("Name",name);
                                            item.put("Code",code);
                                            item.put("Position",position);
                                            items.add(item);
                                            modifyC.moveToPrevious();
                                        }
                                        sAdapter.notifyDataSetChanged();//更新sAdapter
                                    }
                                });
                                MAB.setNegativeButton(getString(R.string.cancel_name), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        return;
                                    }
                                }).create().show();
                                break;


                            case 1:
                                //設置刪除選項動作
                                //設定刪除所需變數
                                String delete_name_data=currentItem.get("Name").toString();
                                String delete_name="Name";
                                String delete_code_data=currentItem.get("Code").toString();
                                String delete_code="Code";
                                String delete_position_data=currentItem.get("Position").toString();
                                String delete_position="Position";
                                //設置刪除警告dialog基本設定
                                AlertDialog.Builder DeleteWarningDialog=new AlertDialog.Builder(DataInputActivity.this);
                                DeleteWarningDialog.setTitle(getString(R.string.deleteWaring_name));
                                    //設置 刪除警告dialog 取消按鈕動作
                                DeleteWarningDialog.setNegativeButton(getString(R.string.cancel_name), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        return;
                                    }
                                });
                                    //設置 刪除警告dialog 確定按鈕動作
                                DeleteWarningDialog.setPositiveButton(getString(R.string.confirm_name), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try{
                                            int id=meth.getId(db,currentItem.get("Code").toString());
                                            meth.DeleteDatabaseData(db,id);
                                        }catch (Exception e){
                                            Log.e("progress error","function DeleteDatabaseData failed,error msg:"+e.toString());
                                        }
                                        //更新listvew
                                        Cursor deleteC=db.getWritableDatabase().rawQuery("SELECT Name,Code,Position FROM ShoesCode",null);
                                        deleteC.moveToLast();
                                        int deleteCount=EnterCount-1;
                                        items.clear();
                                        if(deleteCount>0){
                                            do{
                                                item=new HashMap<String, Object>();
                                                item.put(delete_name,deleteC.getString(deleteC.getColumnIndex(delete_name)));
                                                item.put(delete_code,deleteC.getString(deleteC.getColumnIndex(delete_code)));
                                                item.put(delete_position,deleteC.getString(deleteC.getColumnIndex(delete_position)));
                                                items.add(item);
                                                deleteCount--;
                                            }while(deleteC.moveToPrevious() && deleteCount==0);
                                        }
                                        sAdapter.notifyDataSetChanged();
                                    }
                                });
                                DeleteWarningDialog.setView(null);
                                DeleteWarningDialog.create().show();
                        }
                    }
                }).create().show();
                return true;
            }
        });

        //設置輸入按鈕
        DIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterCount++;
                //檢查position edittext的第一個字母是否大寫
                String check=meth.checkPosition(DIinputposition.getText().toString());
                DIinputposition.setText(check);
                DIinputcode.setText(meth.checkCode(DIinputcode.getText().toString()));
                if(!meth.isMaxLength(DIinputcode.getText().toString())){
                    Toast.makeText(DataInputActivity.this,R.string.errorLength_name,Toast.LENGTH_LONG).show();
                    return;
                };
                //把界面上的資料加入資料庫
                add(DIinputShoesName.getText().toString(),DIinputcode.getText().toString(),DIinputposition.getText().toString());
                c=db.getWritableDatabase().rawQuery("SELECT Name,Code,Position FROM ShoesCode",null);
                c.moveToLast();
                //把資料輸入item
                String name = c.getString(c.getColumnIndex("Name"));
                String code = c.getString(c.getColumnIndex("Code"));
                String position = c.getString(c.getColumnIndex("Position"));

                //若輸入的code長度不在6~9以內,則彈出錯誤訊息並返回
                if(code.length()<6 || code.length()>9){
                    Toast.makeText(DataInputActivity.this,R.string.codeErrorLength,Toast.LENGTH_LONG);
                    return;
                }
                item=new HashMap<String, Object>();//每次按下按鈕需宣告為新的hashmap,否則會被當成與上次按鈕同一個的item,而不是新的item
                                                   //即每次執行items.add時只會覆蓋上一次新增的item,
                                                   //而不會增加新的item
                item.put("Name", name);
                item.put("Code", code);
                item.put("Position", position);
                //把item加入items
                items.add(item);
                Collections.reverse(items);

                //創建自訂樣式
                sAdapter=new SimpleAdapter(
                        DataInputActivity.this,//此函數在那個activity執行
                        items,//要插入的items
                        R.layout.layout,//自訂的顯示layout
                        new String[]{"Name","Code","Position"},//從items中取出key中的資料
                        new int[]{R.id.tp1,R.id.tp2,R.id.tp3});//資料輸出在layout中的哪個元件
                DILV.setAdapter(sAdapter);
            }
        });

    }

    //把資料加入資料庫
    private void add(String name,String code,String position) {
        ContentValues values=new ContentValues();
        values.put("Name",name.toString());
        values.put("Code",code.toString());
        values.put("Position",position.toString());
        db.getWritableDatabase().insert("ShoesCode",null,values);
    }
}