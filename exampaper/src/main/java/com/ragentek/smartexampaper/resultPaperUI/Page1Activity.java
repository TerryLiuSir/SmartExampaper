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

public class Page1Activity extends Activity {

    private Button nextButton;
    private ImageView studentAnswer1;
    private ImageView studentAnswer2;
    private ImageView studentAnswer3;
    private ImageView studentAnswer4;
    private ImageView studentAnswer5;
    private ImageView studentAnswer6;
    private ImageView checkQuestion1;
    private ImageView checkQuestion2;
    private ImageView checkQuestion3;
    private ImageView checkQuestion4;
    private ImageView checkQuestion5;
    private ImageView checkQuestion6;
    private RecognizeLetter mRecognize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_page1);

        nextButton = (Button) findViewById(R.id.button_page1next);
        studentAnswer1 = (ImageView) findViewById(R.id.student_answer1);
        studentAnswer2 = (ImageView) findViewById(R.id.student_answer2);
        studentAnswer3 = (ImageView) findViewById(R.id.student_answer3);
        studentAnswer4 = (ImageView) findViewById(R.id.student_answer4);
        studentAnswer5 = (ImageView) findViewById(R.id.student_answer5);
        studentAnswer6 = (ImageView) findViewById(R.id.student_answer6);

        checkQuestion1 = (ImageView) findViewById(R.id.check_question1);
        checkQuestion2 = (ImageView) findViewById(R.id.check_question2);
        checkQuestion3 = (ImageView) findViewById(R.id.check_question3);
        checkQuestion4 = (ImageView) findViewById(R.id.check_question4);
        checkQuestion5 = (ImageView) findViewById(R.id.check_question5);
        checkQuestion6 = (ImageView) findViewById(R.id.check_question6);


        nextButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(),Page2Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        mRecognize = new RecognizeLetter(getAssets());

        displayStudentAnswer();
    }

    private void displayStudentAnswer(){
        Bitmap bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/0.png");
        if(bitmap != null){
            studentAnswer1.setImageBitmap(bitmap);

            if(answerIsRight("D", bitmap) == true){
                checkQuestion1.setImageResource(android.R.drawable.checkbox_on_background);
            }
            else{
                checkQuestion1.setImageResource(android.R.drawable.btn_dialog);
            }
        }


        bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/1.png");
        if(bitmap != null){
            studentAnswer2.setImageBitmap(bitmap);

            if(answerIsRight("A", bitmap) == true){
                checkQuestion2.setImageResource(android.R.drawable.checkbox_on_background);
            }
            else{
                checkQuestion2.setImageResource(android.R.drawable.btn_dialog);
            }
        }


        bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/2.png");
        if(bitmap != null){
            studentAnswer3.setImageBitmap(bitmap);

            if(answerIsRight("C", bitmap) == true){
                checkQuestion3.setImageResource(android.R.drawable.checkbox_on_background);
            }
            else{
                checkQuestion3.setImageResource(android.R.drawable.btn_dialog);
            }
        }

        bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/3.png");
        if(bitmap != null){
            studentAnswer4.setImageBitmap(bitmap);

            if(answerIsRight("C", bitmap) == true){
                checkQuestion4.setImageResource(android.R.drawable.checkbox_on_background);
            }
            else{
                checkQuestion4.setImageResource(android.R.drawable.btn_dialog);
            }
        }

        bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/4.png");
        if(bitmap != null){
            studentAnswer5.setImageBitmap(bitmap);

            if(answerIsRight("A", bitmap) == true){
                checkQuestion5.setImageResource(android.R.drawable.checkbox_on_background);
            }
            else{
                checkQuestion5.setImageResource(android.R.drawable.btn_dialog);
            }
        }

        bitmap = getLoacalBitmap("/mnt/sdcard/a_examPaper/5.png");
        if(bitmap != null){
            studentAnswer6.setImageBitmap(bitmap);

            if(answerIsRight("B", bitmap) == true){
                checkQuestion6.setImageResource(android.R.drawable.checkbox_on_background);
            }
            else{
                checkQuestion6.setImageResource(android.R.drawable.btn_dialog);
            }
        }
    }

    public static Bitmap getLoacalBitmap(String file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return BitmapFactory.decodeStream(fileInputStream);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public Boolean answerIsRight(String a, Bitmap bitmap){
        if(bitmap == null)
            return false;
        int[] result = mRecognize.getPredict(bitmap);
        if (result.length > 0) {
            if(a.equals(String.valueOf(mRecognize.toLetter(result[0])))) {
                return true;
            }
            else{
                return false;
            }
        }
        return false;
    }


}
