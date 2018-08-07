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


public class Page2Activity extends Activity {

    private Button nextButton;
    private Button preButton;
    private ImageView studentAnswer7;
    private ImageView studentAnswer8;
    private ImageView studentAnswer9;
    private ImageView studentAnswer10;
    private ImageView studentAnswer11;
    private ImageView studentAnswer12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_page2);

        nextButton = (Button) findViewById(R.id.button_page2next);
        preButton = (Button) findViewById(R.id.button_page2pre);
        studentAnswer7 = (ImageView) findViewById(R.id.student_answer7);
        studentAnswer8 = (ImageView) findViewById(R.id.student_answer8);
        studentAnswer9 = (ImageView) findViewById(R.id.student_answer9);
        studentAnswer10 = (ImageView) findViewById(R.id.student_answer10);
        studentAnswer11 = (ImageView) findViewById(R.id.student_answer11);
        studentAnswer12 = (ImageView) findViewById(R.id.student_answer12);

        nextButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(),Page3Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        preButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(),Page1Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        displayStudentAnswer();

    }

    private void displayStudentAnswer(){
        Bitmap bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/6.png");
        if(bitmap != null)
            studentAnswer7.setImageBitmap(bitmap);
        else

        bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/7.png");
        if(bitmap != null)
            studentAnswer8.setImageBitmap(bitmap);

        bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/8.png");
        if(bitmap != null)
            studentAnswer9.setImageBitmap(bitmap);

        bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/9.png");
        if(bitmap != null)
            studentAnswer10.setImageBitmap(bitmap);

        bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/10.png");
        if(bitmap != null)
            studentAnswer11.setImageBitmap(bitmap);

        bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/11.png");
        if(bitmap != null)
            studentAnswer12.setImageBitmap(bitmap);
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
