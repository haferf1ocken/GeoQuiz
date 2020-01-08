package android.bignerdranch.com;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.Arrays;

public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final int REQUEST_CODE_CHEAT = 0;

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mCheatButton;
    private Button mRestartButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private TextView mQuestionTextView;
    private TextView mCurrentQuestionTextView;
    private TextView mCountHintsTextView;
    private TextView mApiLevelTextView;
    private LinearLayout mBgElement;

    private Question[] mQuestionBank = new Question[]{
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true),
    };

    private int mCurrentIndex = 0;
    private int countAnswers = 0;
    private int countTrueAnswers = 0;
    private  int mCountHints = mQuestionBank.length/2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.activity_quiz);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if(savedInstanceState != null){
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
        }

        mBgElement = (LinearLayout) findViewById(R.id.bgElement);

        mCurrentQuestionTextView = (TextView) findViewById(R.id.current_question);
        mCurrentQuestionTextView.setTypeface(null, Typeface.BOLD);
        mCurrentQuestionTextView.setText("Question " + (mCurrentIndex + 1) + " of " + mQuestionBank.length);

        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);

        mTrueButton = (Button) findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                checkAnswer(true);
                countAnswers++;
                if (mQuestionBank[mCurrentIndex].isAnswerTrue()){
                    countTrueAnswers++;
                    mBgElement.setBackgroundColor(Color.GREEN);
                } else {
                    mBgElement.setBackgroundColor(Color.RED);
                }
                percantageOfCorrectAnswers(countAnswers, countTrueAnswers);
            }
        });

        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                checkAnswer(false);
                countAnswers++;
                if (!mQuestionBank[mCurrentIndex].isAnswerTrue()){
                    countTrueAnswers++;
                    mBgElement.setBackgroundColor(Color.GREEN);
                } else {
                    mBgElement.setBackgroundColor(Color.RED);
                }
                percantageOfCorrectAnswers(countAnswers, countTrueAnswers);
            }
        });

        mNextButton = (ImageButton) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener(){
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v){
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                isAnswered(mCurrentIndex);
                updateQuestion();
                mBgElement.setBackgroundColor(Color.WHITE);
            }
        });

        mPrevButton = (ImageButton) findViewById(R.id.previous_button);
        mPrevButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mCurrentIndex = (mCurrentIndex - 1) % mQuestionBank.length;
                if (mCurrentIndex == -1)
                    mCurrentIndex = mQuestionBank.length - 1;
                isAnswered(mCurrentIndex);
                updateQuestion();
                mBgElement.setBackgroundColor(Color.WHITE);
            }
        });

        mCheatButton = (Button) findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });

        mCountHintsTextView = (TextView) findViewById(R.id.count_hints);
        mCountHintsTextView.setText(mCountHints + " hint(s) left");

        mRestartButton = (Button) findViewById(R.id.restart_button);
        mRestartButton.setVisibility(View.GONE);
        mRestartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartQuiz();
            }
        });

        mApiLevelTextView = (TextView) findViewById(R.id.api_level);
        mApiLevelTextView.setText("API level " + Build.VERSION.SDK_INT);

        updateQuestion();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
            mQuestionBank[mCurrentIndex].setCheatered(CheatActivity.wasAnswerShown(data));
            if (mQuestionBank[mCurrentIndex].isCheatered()) {
                mCountHints--;
                if (mCountHints <= 0) {
                    mCheatButton.setEnabled(false);
                    mCountHintsTextView.setText("Hints are over!");
                } else {
                    mCountHintsTextView.setText(mCountHints + " hint(s) left");
                }
            }
        }
    }

    private void updateQuestion(){
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
        mBgElement.setBackgroundColor(mQuestionBank[mCurrentIndex].getBgColor());
        mCurrentQuestionTextView.setText("Question " + (mCurrentIndex + 1) +
                " of " + mQuestionBank.length);
    }

    private void checkAnswer(boolean userPressedTrue){
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();

        int messageResId = 0;

        if (mQuestionBank[mCurrentIndex].isCheatered()){
            messageResId = R.string.judgment_toast;
        } else {

            if (userPressedTrue == answerIsTrue) {
                messageResId = R.string.correct_toast;
            } else {
                messageResId = R.string.incorrect_toast;
            }
        }

        mTrueButton.setEnabled(false);
        mFalseButton.setEnabled(false);
        mQuestionBank[mCurrentIndex].setAnswered(true);

        Toast.makeText(QuizActivity.this, messageResId, Toast.LENGTH_SHORT).show();
    }

    private void isAnswered(int index){
        boolean isQuestionAnswered = mQuestionBank[index].answered();
            mTrueButton.setEnabled(!isQuestionAnswered);
            mFalseButton.setEnabled(!isQuestionAnswered);
    }

    private void percantageOfCorrectAnswers(int countAnswers, int countTrueAnswers){
        if (countAnswers == mQuestionBank.length){
            double percent = countTrueAnswers * 100.0 / countAnswers;
            Toast.makeText(QuizActivity.this, "Percentage of correct answers "
                    + Math.round(percent) + "%", Toast.LENGTH_SHORT).show();
            mRestartButton.setVisibility(View.VISIBLE);
        }
    }

    private void restartQuiz() {
        this.recreate();
        mCurrentIndex = 0;
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }
}
