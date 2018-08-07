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


public class Page3Activity extends Activity {

    private Button nextButton;
    private Button preButton;
    private ImageView studentAnswer13;
    private ImageView studentAnswer14;
    private ImageView studentAnswer15;
    private ImageView studentAnswer16;
    private ImageView studentAnswer17;
    private ImageView studentAnswer18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_page3);

        nextButton = (Button) findViewById(R.id.button_page3next);
        preButton = (Button) findViewById(R.id.button_page3pre);
        studentAnswer13 = (ImageView) findViewById(R.id.student_answer13);
        studentAnswer14 = (ImageView) findViewById(R.id.student_answer14);
        studentAnswer15 = (ImageView) findViewById(R.id.student_answer15);
        studentAnswer16 = (ImageView) findViewById(R.id.student_answer16);
        studentAnswer17 = (ImageView) findViewById(R.id.student_answer17);
        studentAnswer18 = (ImageView) findViewById(R.id.student_answer18);

        nextButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(),Page4Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        preButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(),Page2Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        displayStudentAnswer();

    }

    private void displayStudentAnswer(){
        Bitmap bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/12.png");
        if(bitmap != null)
            studentAnswer13.setImageBitmap(bitmap);

        bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/13.png");
        if(bitmap != null)
            studentAnswer14.setImageBitmap(bitmap);

        bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/14.png");
        if(bitmap != null)
            studentAnswer15.setImageBitmap(bitmap);

        bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/15.png");
        if(bitmap != null)
            studentAnswer16.setImageBitmap(bitmap);

        bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/16.png");
        if(bitmap != null)
            studentAnswer17.setImageBitmap(bitmap);

        bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/17.png");
        if(bitmap != null)
            studentAnswer18.setImageBitmap(bitmap);
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
