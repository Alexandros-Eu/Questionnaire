package pada.pms.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * This class represents the Main Activity that handles the Questionnaire application.
 * It provides functionalities to presents a questionnaire to the user, handle user answers and
 * navigate through the questions.
 * <p>
 * The activity includes features such as displaying questions, managing countdown timer, selecting
 * and confirming answers and transitioning to the next activity upon completing the questionnaire.
 * <p>
 * It also incorporates methods for checking answers, updating the countdown timer display and
 * saving user responses.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Declaration of the UI elements
    TextView questionNumber;
    TextView question;

    TextView countdown;

    TextView[] answers;

    ImageView questionFlag;

    Button previous;

    Button confirm;

    Button next;

    Questionnaire questionnaire;    // Attribute to manage the set of questions

    Question currentQuestion;   // Attribute to represent the current question

    int currentQuestionNumber;

    int selectedAnswer;

    // Drawable object that we use in order to take the default theme color of the UI
    // and reapply it when the user changes an answer
    Drawable backDraw;

    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Connect with the activity main layout

        // Connect the UI elements by finding their ID
        questionNumber = findViewById(R.id.text_view_question_number);
        question = findViewById(R.id.text_view_question);
        questionFlag = findViewById(R.id.image_view_country_flag);
        countdown = findViewById(R.id.text_view_countdown);

        answers = new TextView[4];
        answers[0] = findViewById(R.id.text_view_answer_1);
        answers[1] = findViewById(R.id.text_view_answer_2);
        answers[2] = findViewById(R.id.text_view_answer_3);
        answers[3] = findViewById(R.id.text_view_answer_4);

        db = SQLiteDatabase.openDatabase(getApplicationContext().getFilesDir() + "/capitals.db", null, 0);  // Connect to the db

        // Set click listeners for the potential answers
        for(int i = 0; i < answers.length; i++)
        {
            answers[i].setOnClickListener(this);
        }

        // Connect the navigation buttons by finding their ID
        previous = findViewById(R.id.button_previous);
        confirm = findViewById(R.id.button_confirm);
        next = findViewById(R.id.button_next);

        // Set click listeners for the navigation buttons
        previous.setOnClickListener(this);
        confirm.setOnClickListener(this);
        next.setOnClickListener(this);

        // Store the background color of the first answer Text view so we can reapply it later
        backDraw = answers[0].getBackground();

        // Initialize the questionnaire using a singleton
        questionnaire = Questionnaire.getInstance(this);

        // Load the next question and set up the countdown timer
        doNext();
        setupCountDownTimer();

    }

    @Override
    public void onClick(View v) {

        if(v == previous)
        {
            questionnaire.goPreviousUnansweredQuestion();   // Move to the previous unanswered question
            questionnaire.setCurrentQuestionNumber(questionnaire.getCurrentQuestionNumber() - 1);
            doNext();   // Proceed to the previous question using doNext()
        }

        if(v == next)
        {
            doNext();   // Proceed to the next question
        }

        for(int i = 0; i < answers.length; i++)
        {
            if(v == answers[i])
            {
                selectedAnswer = i; // Choosing one of the potential answers
            }
        }

        doChangeAnswer();

        if(v == confirm)
        {
            if(selectedAnswer == -1)    // If no answer is selected you return back in order to select
            {
                return;
            }

            currentQuestion.setUserAnswer(selectedAnswer);  // If confirm is pressed the current selected answer gets selected
            //doNext();
            checkAnswer();  // Checks if the answer is right or wrong
            disableAnswers();   // After answering you can no longer change your answer
        }
    }

    void doNext()
    {
        currentQuestionNumber = questionnaire.goNextUnansweredQuestion();   // Move to the next unanswered question

        // If currentQuestionNumber is -1 it means that there are no longer unanswered questions
        // and the questionnaire must end
        if(currentQuestionNumber == -1)
        {
            String endOfQuestionnaire = "This is the end of the Questionnaire";
            nextActivity(endOfQuestionnaire);   // Start the next activity
        }
        else
        {
            currentQuestion = questionnaire.getCurrentQuestion();   // Get the current question

            for(int i = 0; i < answers.length; i++)
            {
                answers[i].setEnabled(true);    // Enable all answer options
            }

            String questionNumberText = "Question " + (currentQuestionNumber + 1);
            questionNumber.setText(questionNumberText);
            question.setText(currentQuestion.getQuestionText());

            String flagName = currentQuestion.getCountryFlag(); // Retrieve the name of the flag from the Question
            int flagResourceId = getResources().getIdentifier(flagName, "drawable", getPackageName());  // Get the drawable ID using the flag name
            questionFlag.setImageResource(flagResourceId);  // Set the image


            // Setting the potential answers for the current question
            for(int i = 0; i < currentQuestion.getNumberOfPotentialAnswers(); i++)
            {
                answers[i].setText(currentQuestion.getPotentialAnswer(i));
            }

            // If the question has 3 potential answers the 4th answer Text view gets disabled
            for(int i = currentQuestion.getNumberOfPotentialAnswers(); i < 4; i++)
            {
                answers[i].setEnabled(false);
                answers[i].setText("");
            }
        }

        // Resetting the selected answer after moving to another question
        selectedAnswer = -1;
    }

    void doChangeAnswer()   // Method to visually change the selected answer
    {
        for(int i = 0; i < answers.length; i++)
        {
            answers[i].setBackground(backDraw); // Set the default background color for each answer

            if(i == selectedAnswer)
            {
                answers[i].setBackgroundColor(Color.CYAN);  // If the current answer is the selected one, highlight it with CYAN color
            }
        }
    }

    void checkAnswer()  // Method to check if the submitted answer was correct
    {
            int correctAnswer = currentQuestion.getCorrectAnswer() - 1; // Assign the correct answer (converted to zero-based index)

            // If selected answer is correct mark it with green otherwise mark it with red and
            // the correct one with green
            if(selectedAnswer == correctAnswer)
            {
                answers[selectedAnswer].setBackgroundColor(Color.GREEN);
            }
            else
            {
                answers[selectedAnswer].setBackgroundColor(Color.RED);
                answers[correctAnswer].setBackgroundColor(Color.GREEN);
            }
    }

    void disableAnswers()   // Method to disable all answer options once an answer has been picked-confirmed
    {
        for(int i = 0; i < currentQuestion.getNumberOfPotentialAnswers(); i++)
        {
            answers[i].setEnabled(false);   // Disable each answer option
        }
    }


    // Writes score/correct answers, name, ID to a table in the db (if it doesn't exist it creates the table)
    private void writeDB(int score, int correctAnswers, String examineeName, String examineeID)
    {
        // Check if the table already exists
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='LEADERBOARD'", null);

        if (cursor != null)
        {
            if (cursor.getCount() > 0)  // Table already exists;
            {
                SQLiteStatement insertStatement = db.compileStatement("INSERT INTO LEADERBOARD VALUES (?, ?, ?, ?)");
                insertStatement.bindString(1, examineeID);
                insertStatement.bindString(2, examineeName);
                insertStatement.bindLong(3, correctAnswers);
                insertStatement.bindLong(4, score);
                insertStatement.execute();
            }
            else
            {
                SQLiteStatement tableStatement = db.compileStatement("CREATE TABLE LEADERBOARD (examinee_ID VARCHAR(50) PRIMARY KEY, examinee_name VARCHAR(50), correct_answers INTEGER, score INTEGER)");
                tableStatement.execute();

                SQLiteStatement insertStatement = db.compileStatement("INSERT INTO LEADERBOARD VALUES (?, ?, ?, ?)");
                insertStatement.bindString(1, examineeID);
                insertStatement.bindString(2, examineeName);
                insertStatement.bindLong(3, correctAnswers);
                insertStatement.bindLong(4, score);
                insertStatement.execute();
            }
        }

        cursor.close();
        db.close();

    }

    void setupCountDownTimer() {    // Method to set up and start the countdown timer
        CountDownTimer countDownTimer = createCountDownTimer(); // Create a CountDownTimer object
        countDownTimer.start(); // Start the countdown timer
    }

    CountDownTimer createCountDownTimer() {
        // Create and return a new CountDownTimer with a total duration of 3 minutes
        // and an interval of 1 second
        return new CountDownTimer(180000, 1000) {
            public void onTick(long millisUntilFinished) {
                updateCountdownDisplay(millisUntilFinished);
            }

            public void onFinish() {    // Method that will be executed when the countdown timer finishes
                countdown.setText("00:00"); // Indicates that the time is up
                String timeIsUp = "Time is up! This is the end of the Questionnaire";   // Notifies that the time is up
                nextActivity(timeIsUp); // Since time is up the user is moved to the next activity (score screen)
            }
        };
    }

    void updateCountdownDisplay(long millisUntilFinished) {

        NumberFormat f = new DecimalFormat("00");   // Object in order to format better the countdown 00s

        // Formatting from milliseconds to minutes & seconds
        long min = (millisUntilFinished / 60000) % 60;
        long sec = (millisUntilFinished / 1000) % 60;

        // Setting the display to formatted minutes & seconds
        countdown.setText(f.format(min) + ":" + f.format(sec));
    }

    void nextActivity(String toastMessage)  // Method to navigate to the next Activity
    {
        Toast endOfQuestionnaire = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG);
        endOfQuestionnaire.show();  // Toast message to indicate the end of the Questionnaire
        questionnaire.printAnswers();   // Printing to logcat (for debugging)

        Intent intent = getIntent();

        String examineeName = intent.getStringExtra("name");    // Retrieve the examinee's name from the previous activity
        String examineeID = intent.getStringExtra("ID");    // Retrieve the examinee's ID from the previous activity

        Intent scoreScreenIntent = new Intent(this, ScoreScreen.class);

        // Passing the data to the next Activity in case it's need it
        scoreScreenIntent.putExtra("examineeName", examineeName);
        scoreScreenIntent.putExtra("examineeID", examineeID);
        scoreScreenIntent.putExtra("score", questionnaire.getScore());
        scoreScreenIntent.putExtra("numberOfCorrectAnswers", questionnaire.getNumberOfCorrectAnswers());

        // Save the score, number of correct answers, examinee's name & ID to a text file on the phone/emulator's storage
        // questionnaire.saveTextFile(questionnaire.getScore(), questionnaire.getNumberOfCorrectAnswers(), examineeName, examineeID);
        writeDB(questionnaire.getScore(), questionnaire.getNumberOfCorrectAnswers(), examineeName, examineeID); // Writes them in a db
        startActivity(scoreScreenIntent);
        finish();
    }



}