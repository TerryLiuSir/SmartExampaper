package com.ragentek.smartexampaper.resultPaperUI;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import com.ragentek.smartexampaper.R;


public class Page4Activity extends Activity {

    private Button nextButton;
    private Button preButton;
    private ImageView studentAnswer19;
    private ImageView studentAnswer20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_page4);

        nextButton = (Button) findViewById(R.id.button_page4next);
        preButton = (Button) findViewById(R.id.button_page4pre);
        studentAnswer19 = (ImageView) findViewById(R.id.student_answer19);
        studentAnswer20 = (ImageView) findViewById(R.id.student_answer20);

        nextButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(),Page5Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        preButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(),Page3Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        displayStudentAnswer();

    }

    private void displayStudentAnswer(){
        Bitmap bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/18.png");
        if(bitmap != null)
            studentAnswer19.setImageBitmap(bitmap);

        bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/19.png");
        if(bitmap != null)
            studentAnswer20.setImageBitmap(bitmap);
    }

    public static Bitmap getLoacalBitmap(String file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return BitmapFactory.decodeStream(fileInputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
