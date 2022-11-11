package com.example.androidoptimize;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BigView bigView= findViewById(R.id.bigView);
        InputStream is=null;
        try {
            is= getResources().getAssets().open("picture.jpg");
            bigView.setImage(is);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}