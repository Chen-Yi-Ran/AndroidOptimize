package com.example.androidoptimize;



import android.app.Activity;
import android.os.Bundle;


import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BigView bigView= findViewById(R.id.bigView);
//        bigView.setOnClickListener();
        InputStream is=null;
        try {
            is= getResources().getAssets().open("test.jpg");
            bigView.setImage(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO: 1.通过Url从服务器获取图片 2.可以将图片进行传入，直接将图片进行显示 2.加入图片缓冲功能



    }
}