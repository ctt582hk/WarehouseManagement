package com.example.warehousemanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Resources res;
    DisplayMetrics dm;
    Configuration cf;
    Button ch_language,exit,login;
    AlertDialog.Builder ADBer;
    SharedPreferences share;
    Intent login_intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化share參數
        share=getSharedPreferences("",MODE_PRIVATE);
        //轉換成上次選擇的語言
        SwitchLanguage(share.getString("language","en"));
        setContentView(R.layout.activity_main);

        //初始化其他參數
        ADBer=new AlertDialog.Builder(MainActivity.this);
        exit=findViewById(R.id.ex);
        login=findViewById((R.id.login_main));
        //未設定登錄頁面,先跳至選擇頁面
        login_intent=new Intent(MainActivity.this,ChoiceActivity.class);
        ch_language=findViewById(R.id.ch_language);

        //設置登錄按鈕
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(login_intent);
            }
        });
        //設置更改語言按鈕
        ch_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ADBer.setItems(R.array.language_dialog, new DialogInterface.OnClickListener() {
                            @Override
                            //設置彈出選項
                            public void onClick(DialogInterface dialog, int which) {
                                String language=null;
                                //根據使用者的選擇設置語系
                                switch (which){
                                    case 0:
                                        //cf.locale=Locale.TAIWAN;
                                        language="TAIWAN";
                                        break;
                                    case 1:
                                        //cf.locale=Locale.CHINA;
                                        language="CHINA";
                                        break;
                                    case 2:
                                        //cf.locale=Locale.ENGLISH;
                                        language="ENGLISH";
                                        break;
                                }
                                //儲存選擇的語言的代號
                                SwitchLanguage(language);
                                //更新Configuration
                                res.updateConfiguration(cf,dm);
                                recreate();
                            }
                        }).create().show();
            }
        });

        //設置結束按鈕
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //傳換語言
    protected void SwitchLanguage(String language){
        res=getResources();
        dm=res.getDisplayMetrics();
        cf=res.getConfiguration();
        if(language.equals("TAIWAN")){
            cf.locale=Locale.TAIWAN;
        }
        else if(language.equals("CHINA")){
            cf.locale=Locale.CHINA;
        }
        else if(language.equals("ENGLISH")){
            cf.locale=Locale.ENGLISH;
        }
        res.updateConfiguration(cf,dm);
        share.edit().putString("language",language).commit();
    }
}