package com.example.warehousemanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChoiceActivity extends AppCompatActivity {

    //宣告全域變數
    Button search_choice,review_choice,input_choice;
    Intent search_intent,view_intent,data_input_intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        //初始化參數
        search_choice=findViewById(R.id.serch_choice);
        review_choice=findViewById(R.id.review_choic);
        input_choice=findViewById(R.id.input_choice);
        search_intent=new Intent(this,SearchActivity.class);
        view_intent=new Intent(this,AllViewActivity.class);
        data_input_intent=new Intent(this,DataInputActivity.class);

        //設置搜索按鈕
        search_choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(search_intent);
            }
        });

        //設置檢閱按鈕
        review_choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(view_intent);
            }
        });

        //設置資料輸入按鈕
        input_choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(data_input_intent);
            }
        });
        
    }
}