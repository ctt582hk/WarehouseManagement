package com.example.warehousemanagement;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ComponentActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;

public class database_meth{
    public final String table_name="Name";
    public final String table_code="Code";
    public final String table_position="Position";

    public List<Map<String,Object>> searchByCode(ShoseCodeDB db,String sCode){//搜索資料庫中Code欄中符合 sCode 的資料,db code 9字元;search code 6~9字元

        List<Map<String,Object>> items=new ArrayList<Map<String, Object>>();
        HashMap<String,Object> item=new HashMap<String, Object>();
        Cursor c=db.getWritableDatabase().rawQuery("SELECT Name,Code,Position FROM ShoesCode ORDER BY Position COLLATE NOCASE ASC",null);
        c.moveToFirst();
        char[] sCodeArray=sCode.toCharArray();//要檢索的字串轉為字元陣列
        do{
            int correct_count=0;
            try {
                char[] dbCodeArray = c.getString(c.getColumnIndex("Code")).toCharArray();
            }catch (Exception e){
                Log.e("error",e.toString());
            }
            String dbCode=c.getString(c.getColumnIndex("Code"));
            char[] dbCodeArray = dbCode.toCharArray();
            int diff=Math.abs(dbCodeArray.length-sCodeArray.length);

            for(int i=0;i<sCodeArray.length;i++){
                if(sCodeArray[i]==dbCodeArray[i+diff]){
                    correct_count++;
                }
            }

            if(correct_count==sCodeArray.length){
                item=new HashMap<String, Object>();
                item.put(table_name,c.getString(c.getColumnIndex(table_name)));
                item.put(table_code,c.getString(c.getColumnIndex(table_code)));
                item.put(table_position,c.getString(c.getColumnIndex(table_position)));
                items.add(item);
            }


        }while(c.moveToNext());

        return items;
    }

    public void DeleteDatabaseData(ShoseCodeDB db,int id){//data為要刪除資料,注意,此function為刪除data所在的那一行
        //db.getWritableDatabase().delete("ShoesCode",name+"=?",new String[]{data});
        db.getWritableDatabase().execSQL("DELETE FROM ShoesCode WHERE _id="+id);
    }

    public void DeleteAllData(ShoseCodeDB db){
        db.getWritableDatabase().execSQL("DELETE FROM ShoesCode");
    }

    public boolean UpdateDatabaseData(ShoseCodeDB db,String Cname,String newdata,int id){//Cname=欲改變的值所在的欄名
        ContentValues Mcv=new ContentValues();
        Mcv.put(Cname,newdata);
        //db.getWritableDatabase().update("ShoesCode",Mcv,"_id=?",new String[]{String.valueOf(id)});
        try {
            db.getWritableDatabase().execSQL("UPDATE ShoesCode SET " + Cname + "='" + newdata + "' WHERE _id=" + id);
        }catch (Exception e){
            Log.e("updata error",e.toString());
            return false;
        }
        return true;
    }

    public void DatabaseDataClean(ShoseCodeDB db){
        db.getWritableDatabase().execSQL("DELETE FROM ShoesCode");
    }

    public String checkPosition(String position){//檢查position第一個字元是否大寫,如否則轉為大寫,此規則僅適用於英文,若輸入其它語言則返回原值
        char[] position_Array=position.toCharArray();
        if(position_Array[0]>=97 && position_Array[0]<=122){
            position_Array[0]-=32;
            String position_new=new String(position_Array);
            return position_new;
        }
        else{
            return position;
        }
    }

    public String checkCode(String code){
        char[] codeArray=code.toCharArray();
        int count=0;
        for(count=0;count<codeArray.length;count++){
            if(codeArray[count]>=97 && codeArray[count]<=122){
                codeArray[count]-=32;

            }
        }
        String newCode=new String(codeArray);
        return newCode;
    }

    public int getId(ShoseCodeDB db,String code){//尋找data所在的id
        //取得當前選取的item在資料庫中的id
        Cursor c=db.getWritableDatabase().rawQuery("SELECT Code,_id FROM ShoesCode",null);
        int id=-1;
        c.moveToFirst();
        do{
            if(c.getString(c.getColumnIndex("Code")).equals(code)){
                id=c.getInt(c.getColumnIndex("_id"));
                break;
            }
        }while(c.moveToNext());
        return id;
    }

    boolean isOverLength(String code){
        char[] codeArray=code.toCharArray();
        if(codeArray.length>9 || codeArray.length<6){
            return true;
        }
        else {
            return false;
        }
    }

    boolean isMaxLength(String code){
        switch (code.length()){
            case 9:
                return true;
        }
        return false;
    }

    public void dataModifyDialog(Context context, HashMap<String,Object>currentItem, ListView LV,int id, ShoseCodeDB db,int type,String condition){
        //type表示修改後的data以什麼方式印出,0=無;1=以condition為條件順序輸出;2=以condition為條件反序輸出

        List<Map<String,Object>> items=new ArrayList<Map<String, Object>>();
        final HashMap<String, Object>[] item = new HashMap[]{new HashMap<String, Object>()};
        //設置修改選項動作
        //按下後彈出新的dialog以供修改
        androidx.appcompat.app.AlertDialog.Builder MAB=new androidx.appcompat.app.AlertDialog.Builder(context);
        //新dialog基本設置
        View modifyView=View.inflate(context,R.layout.layout_datamodify,null);
        MAB.setView(modifyView);
        MAB.setTitle(context.getString(R.string.DataModify_name));

        //設置所有edittext的預設文字,其為當前選取的item中對應的值
        //此為傳值址運算,所以可以這樣做
        EditText Modify_View_name_et=modifyView.findViewById(R.id.DataModify_Name_et);
        Modify_View_name_et.setText(currentItem.get("Name").toString(),null);
        EditText Modify_View_code_et=modifyView.findViewById(R.id.DataModify_Code_et);
        Modify_View_code_et.setText(currentItem.get("Code").toString(),null);
        EditText Modify_View_position_et=modifyView.findViewById(R.id.DataModify_Position_et);
        Modify_View_position_et.setText(currentItem.get("Position").toString(),null);

        //按下確定時動作
        MAB.setPositiveButton(context.getString(R.string.confirm_name), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //取得輸入的新的值
                EditText Modify_name_et=modifyView.findViewById(R.id.DataModify_Name_et);
                String Modify_name=Modify_name_et.getText().toString();
                EditText Modify_code_et=modifyView.findViewById(R.id.DataModify_Code_et);
                String Modify_code=Modify_code_et.getText().toString();
                EditText Modify_position_et=modifyView.findViewById(R.id.DataModify_Position_et);
                String Modify_position=checkPosition(Modify_position_et.getText().toString());//檢查position第一個字元是否大寫,
                // 如否則轉為大寫,此規則僅適用於英文

                //檢查code是否超出長度
                if(isOverLength(Modify_code)){
                    Toast.makeText(context,R.string.codeErrorLength,Toast.LENGTH_LONG).show();
                    return;
                };
                //把新的值輸入database
                String[] changeName={"Name","Code","Position"};
                String[] changeData={Modify_name,Modify_code,Modify_position};
                String[] oldData={currentItem.get("Name").toString(),currentItem.get("Code").toString(),currentItem.get("Position").toString()};
                for(int i=0;i<changeName.length;i++) {
                    String Cname = changeName[i];
                    String newdata = changeData[i];
                    try {
                        UpdateDatabaseData(db,Cname,newdata,id);
                    } catch (Exception e) {
                        //更新失敗跳出信息,並結束該子程式
                        Toast.makeText(context, context.getString(R.string.update_fail_name) + ",i=" + Integer.toString(i), Toast.LENGTH_LONG).show();
                        Log.e("update Error", e.toString());
                        return;
                    }
                }
                //更新成功信息
                Toast.makeText(context, R.string.update_success_name, Toast.LENGTH_SHORT).show();

                //更新listview
                items.clear();
                Cursor modifyC=null;
                if(condition!=null){
                    switch (type){
                        case 0:
                            modifyC=db.getWritableDatabase().rawQuery("SELECT Name,Code,Position FROM ShoesCode ",null);
                            break;
                        case 1:
                            modifyC=db.getWritableDatabase().rawQuery("SELECT Name,Code,Position FROM ShoesCode ORDER BY "+condition+" COLLATE NOCASE ASC",null);
                            break;
                        case 2:
                            modifyC=db.getWritableDatabase().rawQuery("SELECT Name,Code,Position FROM ShoesCode ORDER BY "+condition+" COLLATE NOCASE DESC",null);
                    }
                }
                else{
                    modifyC=db.getWritableDatabase().rawQuery("SELECT Name,Code,Position FROM ShoesCode ",null);
                }


                modifyC.moveToFirst();

                do{
                    item[0] =new HashMap<String, Object>();//宣告item為新的item
                    String name=modifyC.getString(modifyC.getColumnIndex(changeName[0]));
                    String code=modifyC.getString(modifyC.getColumnIndex(changeName[1]));
                    String position=modifyC.getString(modifyC.getColumnIndex(changeName[2]));
                    item[0].put("Name",name);
                    item[0].put("Code",code);
                    item[0].put("Position",position);
                    items.add(item[0]);
                }while(modifyC.moveToNext());

                SimpleAdapter Madapter=new SimpleAdapter(context,
                        items,
                        R.layout.layout,
                        new String[]{"Name","Code","Position"},
                        new int[]{R.id.tp1,R.id.tp2,R.id.tp3});
                LV.setAdapter(Madapter);
            }
        });
        MAB.setNegativeButton(context.getString(R.string.cancel_name), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        }).create().show();
    }

    public void dataModifyDialog(Context context, HashMap<String,Object>currentItem, ShoseCodeDB db,ListView LV,int id){//修改資料不輸出到ListView
        final List<Map<String, Object>>[] items = new List[]{new ArrayList<Map<String, Object>>()};
        //設置修改選項動作
        //按下後彈出新的dialog以供修改
        androidx.appcompat.app.AlertDialog.Builder MAB=new androidx.appcompat.app.AlertDialog.Builder(context);
        //新dialog基本設置
        View modifyView=View.inflate(context,R.layout.layout_datamodify,null);
        MAB.setView(modifyView);
        MAB.setTitle(context.getString(R.string.DataModify_name));
        final String[] newDataName = {null};
        final String[] newDataCode = {null};
        final String[] newDataPosition = {null};

        //設置所有edittext的預設文字,其為當前選取的item中對應的值
        //此為傳值址運算,所以可以這樣做
        EditText Modify_View_name_et=modifyView.findViewById(R.id.DataModify_Name_et);
        Modify_View_name_et.setText(currentItem.get("Name").toString(),null);
        EditText Modify_View_code_et=modifyView.findViewById(R.id.DataModify_Code_et);
        Modify_View_code_et.setText(currentItem.get("Code").toString(),null);
        EditText Modify_View_position_et=modifyView.findViewById(R.id.DataModify_Position_et);
        Modify_View_position_et.setText(currentItem.get("Position").toString(),null);

        //按下確定時動作
        MAB.setPositiveButton(context.getString(R.string.confirm_name), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //取得輸入的新的值
                EditText Modify_name_et=modifyView.findViewById(R.id.DataModify_Name_et);
                String Modify_name=Modify_name_et.getText().toString();
                newDataName[0] =new String(Modify_name);
                EditText Modify_code_et=modifyView.findViewById(R.id.DataModify_Code_et);
                String Modify_code=Modify_code_et.getText().toString();
                Modify_code=checkCode(Modify_code);
                if(!isMaxLength(Modify_code)){
                    Toast.makeText(context,R.string.errorLength_name,Toast.LENGTH_LONG).show();
                    return;
                }
                newDataCode[0] =new String(Modify_code);
                EditText Modify_position_et=modifyView.findViewById(R.id.DataModify_Position_et);
                String Modify_position=checkPosition(Modify_position_et.getText().toString());//檢查position第一個字元是否大寫,
                // 如否則轉為大寫,此規則僅適用於英文
                newDataPosition[0] =new String(Modify_position);

                //檢查code是否超出長度
                if(isOverLength(Modify_code)){
                    Toast.makeText(context,R.string.codeErrorLength,Toast.LENGTH_LONG).show();
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
                        UpdateDatabaseData(db,Cname,newdata,id);
                    } catch (Exception e) {
                        //更新失敗跳出信息,並結束該子程式
                        Toast.makeText(context, context.getString(R.string.update_fail_name) + ",i=" + Integer.toString(i), Toast.LENGTH_LONG).show();
                        Log.e("update Error", e.toString());
                        return;
                    }
                }
                //更新成功信息
                Toast.makeText(context, R.string.update_success_name, Toast.LENGTH_SHORT).show();

                items[0] = searchByCode(db, Modify_code);
                SimpleAdapter sAdapter=new SimpleAdapter(context,items[0],R.layout.layout,new String[]{table_name, table_code, table_position}, new int[]{R.id.tp1, R.id.tp2, R.id.tp3});
                LV.setAdapter(sAdapter);
            }
        });
        MAB.setNegativeButton(context.getString(R.string.cancel_name), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        }).create().show();
    }

    public void dataDeleteDialog(Context context,ShoseCodeDB db,HashMap<String,Object> currentItem,ListView LV,int type,String condition,int id){
        //type=顯示到listview的方式,0=隨database _id印出,1=順序印出,2=反序印出
        //condition=以condition為條件印出
        //設定刪除所需變數
        List<Map<String,Object>> items=new ArrayList<Map<String, Object>>();
        final HashMap<String, Object>[] item = new HashMap[]{new HashMap<String, Object>()};
        String delete_name_data=currentItem.get("Name").toString();
        String delete_name="Name";
        String delete_code_data=currentItem.get("Code").toString();
        String delete_code="Code";
        String delete_position_data=currentItem.get("Position").toString();
        String delete_position="Position";
        //設置刪除警告dialog基本設定
        AlertDialog.Builder DeleteWarningDialog=new AlertDialog.Builder(context);
        DeleteWarningDialog.setTitle(context.getString(R.string.deleteWaring_name));
        //設置 刪除警告dialog 取消按鈕動作
        DeleteWarningDialog.setNegativeButton(context.getString(R.string.cancel_name), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        //設置 刪除警告dialog 確定按鈕動作
        DeleteWarningDialog.setPositiveButton(context.getString(R.string.confirm_name), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //更新listvew
                Cursor deleteC=null;
                switch (type){
                    case 0:
                        deleteC=db.getWritableDatabase().rawQuery("SELECT Name,Code,Position FROM ShoesCode",null);
                        break;
                    case 1:
                        deleteC=db.getWritableDatabase().rawQuery("SELECT Name,Code,Position FROM ShoesCode ORDER BY '"+condition+"' COLLATE NOCASE ASC",null);
                        break;
                    case 2:
                        deleteC=db.getWritableDatabase().rawQuery("SELECT Name,Code,Position FROM ShoesCode ORDER BY '"+condition+"' COLLATE NOCASE DESC",null);
                        break;
                    default:
                        Log.e("dataDelete error","type not correct");
                        return;
                }

                try{
                    DeleteDatabaseData(db,id);
                }catch (Exception e){
                    Log.e("progress error","function DeleteDatabaseData failed,error msg:"+e.toString());
                }

                SimpleAdapter sAdapter=null;
                if(deleteC.getCount()!=0){
                    deleteC.moveToFirst();
                    items.clear();
                    do{
                        item[0]=new HashMap<String, Object>();
                        item[0].put(delete_name,deleteC.getString(deleteC.getColumnIndex(delete_name)));
                        item[0].put(delete_code,deleteC.getString(deleteC.getColumnIndex(delete_code)));
                        item[0].put(delete_position,deleteC.getString(deleteC.getColumnIndex(delete_position)));
                        items.add(item[0]);
                    }while(deleteC.moveToNext());
                    sAdapter=new SimpleAdapter(context,
                            items,R.layout.layout,
                            new String[]{"Name","Code","Position"},
                            new int[]{R.id.tp1,R.id.tp2,R.id.tp3});
                }
                LV.setAdapter(sAdapter);
            }
        });
        DeleteWarningDialog.setView(null);
        DeleteWarningDialog.create().show();
    }

    public void dataDeleteDialog(Context context,ShoseCodeDB db,HashMap<String,Object> currentItem,ListView LV,int id,String sCode){
        //LV=目標listview,id=選取的item的值在database中的id,sCode=在database中搜索sCode相關者後印出,當sCode為null時全部印出
        //設定刪除所需變數
        final List<Map<String, Object>>[] items = new List[]{new ArrayList<Map<String, Object>>()};
        final HashMap<String, Object>[] item = new HashMap[]{new HashMap<String, Object>()};

        //設置刪除警告dialog基本設定
        AlertDialog.Builder DeleteWarningDialog=new AlertDialog.Builder(context);
        DeleteWarningDialog.setTitle(context.getString(R.string.deleteWaring_name));
        //設置 刪除警告dialog 取消按鈕動作
        DeleteWarningDialog.setNegativeButton(context.getString(R.string.cancel_name), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        //設置 刪除警告dialog 確定按鈕動作
        DeleteWarningDialog.setPositiveButton(context.getString(R.string.confirm_name), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //更新listvew
                try{
                    DeleteDatabaseData(db,id);
                }catch (Exception e){
                    Log.e("progress error","function DeleteDatabaseData failed,error msg:"+e.toString());
                }
                Cursor deleteC=db.getWritableDatabase().rawQuery("SELECT Name,Code,Position FROM ShoesCode ORDER BY Position COLLATE NOCASE ASC",null);
                SimpleAdapter sAdapter=null;
                if(deleteC.getCount()!=0){
                    if(sCode!=null){
                        items[0] =searchByCode(db,sCode);
                    }else{//sCode==null時全部data印出
                        deleteC.moveToFirst();
                        do{
                            item[0]=new HashMap<String, Object>();
                            item[0].put(table_name,deleteC.getString(deleteC.getColumnIndex(table_name)));
                            item[0].put(table_code,deleteC.getString(deleteC.getColumnIndex(table_code)));
                            item[0].put(table_position,deleteC.getString(deleteC.getColumnIndex(table_position)));
                            items[0].add(item[0]);
                        }while(deleteC.moveToNext());
                    }

                    sAdapter=new SimpleAdapter(context,
                            items[0],R.layout.layout,
                            new String[]{"Name","Code","Position"},
                            new int[]{R.id.tp1,R.id.tp2,R.id.tp3});

                }else{
                    Toast.makeText(context,"delete function error!",Toast.LENGTH_SHORT).show();
                    return;
                }
                LV.setAdapter(sAdapter);
            }
        });
        DeleteWarningDialog.setView(null);
        DeleteWarningDialog.create().show();
    }
}

