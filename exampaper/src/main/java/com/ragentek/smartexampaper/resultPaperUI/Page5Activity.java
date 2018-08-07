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
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import com.ragentek.smartexampaper.R;


public class Page5Activity extends Activity {

    private Button nextButton;
    private Button preButton;
    private ImageView currentAnswer;
    private ImageView studentAnswer;
    private TextView questionTitle;
    private int current_num = 21;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_page5);

        nextButton = (Button) findViewById(R.id.button_page5next);
        preButton = (Button) findViewById(R.id.button_page5pre);
        currentAnswer = (ImageView) findViewById(R.id.current_question21);
        studentAnswer = (ImageView) findViewById(R.id.student_answer21);
        questionTitle = (TextView) findViewById(R.id.title_question21);

        nextButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                diaplayNextQuestion();
            }
        });

        preButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                displayPreQuestion();
            }
        });
    }

    private void diaplayNextQuestion(){
        current_num ++;
        displayPic(current_num);

    }

    private void displayPreQuestion(){
        current_num --;

        if(current_num == 20){
            Intent intent = new Intent(getApplicationContext(),Page4Activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        displayPic(current_num);

    }

    private void displayPic(int id){
        nextButton.setVisibility(View.VISIBLE);

        if(id == 21) {
            questionTitle.setText("21");
            currentAnswer.setImageDrawable(getDrawable(R.drawable.qus21));
            Bitmap bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/20.png");
            if(bitmap != null)
                studentAnswer.setImageBitmap(bitmap);
        }else if(id == 22){
            questionTitle.setText("22");
            currentAnswer.setImageDrawable(getDrawable(R.drawable.qus22));
            Bitmap bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/21.png");
            if(bitmap != null)
                studentAnswer.setImageBitmap(bitmap);

        }else if(id == 23){
            questionTitle.setText("23");
            currentAnswer.setImageDrawable(getDrawable(R.drawable.qus23));
            Bitmap bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/22.png");
            if(bitmap != null)
                studentAnswer.setImageBitmap(bitmap);

        }else if(id == 24){
            questionTitle.setText("24");
            currentAnswer.setImageDrawable(getDrawable(R.drawable.qus24));
            Bitmap bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/23.png");
            if(bitmap != null)
                studentAnswer.setImageBitmap(bitmap);

        }else if(id == 25){
            questionTitle.setText("25");
            currentAnswer.setImageDrawable(getDrawable(R.drawable.qus25));
            Bitmap bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/24.png");
            if(bitmap != null)
                studentAnswer.setImageBitmap(bitmap);

            nextButton.setVisibility(View.GONE);

        }

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
