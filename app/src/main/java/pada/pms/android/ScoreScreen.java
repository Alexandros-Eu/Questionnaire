package pada.pms.android;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * The ScoreScreen class represents the activity responsible for displaying the score, number of
 * correct answers, examinee's name & ID after completing the questionnaire.
 */

public class ScoreScreen extends AppCompatActivity {

    TextView correctAnswers;    // Text view for the number of correct answers

    TextView score; // Text view for the score

    TextView examineeName;  // Text view for the examinee's name

    TextView examineeID;    // Text view for the examinee's ID

    SQLiteDatabase db;

    List<Integer> correctAnswersList = new ArrayList<>();

    List<Integer> scoreList = new ArrayList<>();

    List<String> nameList = new ArrayList<>();

    List<String> idList = new ArrayList<>();

    ConstraintLayout constraintLayout;

    private static final int MAX_COLUMNS = 3;    // Maximum number of TextViews per row in the leaderboard

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_screen_layout);   // Connect the score screen layout

        // Connect the UI elements by finding their ID
        correctAnswers = findViewById(R.id.text_view_correct_answers);
        score = findViewById(R.id.text_view_score);
        examineeName = findViewById(R.id.text_view_participant_name);
        examineeID = findViewById(R.id.text_view_participant_ID);
        constraintLayout = findViewById(R.id.constraint_layout_score_screen);

        readDB();
        readLeaderboard();
        createTextViews();

    }

    // Creates the appropriate text views dynamically in order to create a leaderboard with every player's score & details
    private void createTextViews() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setId(View.generateViewId());

        constraintLayout.addView(linearLayout);

        TextView textViewLeaderboard = findViewById(R.id.text_view_leaderboard_title);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        // Set constraints to position the LinearLayout below the TextView
        constraintSet.connect(linearLayout.getId(), ConstraintSet.TOP, textViewLeaderboard.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(linearLayout.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(linearLayout.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

        constraintSet.applyTo(constraintLayout);

        for (int i = 0; i < idList.size(); i++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER_HORIZONTAL);

            String name = nameList.get(i);
            String id = idList.get(i);
            int correctAnswers = correctAnswersList.get(i);
            int score = scoreList.get(i);

            TextView nameTV = new TextView(this);
            TextView idTV = new TextView(this);
            TextView answersTV = new TextView(this);
            TextView scoreTV = new TextView(this);

            nameTV.setText(name);
            idTV.setText(id);
            String fullAnswers = String.valueOf(correctAnswers + "/10");
            answersTV.setText(fullAnswers);
            String fullScore = String.valueOf(score) + "pts";
            scoreTV.setText(fullScore);

            nameTV.setTextColor(Color.parseColor("#00FFFF"));
            idTV.setTextColor(Color.parseColor("#00FFFF"));
            answersTV.setTextColor(Color.parseColor("#00FFFF"));
            scoreTV.setTextColor(Color.parseColor("#00FFFF"));

            // Set layout parameters
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            int marginEndInPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
            layoutParams.setMarginEnd(marginEndInPixels);

            nameTV.setLayoutParams(layoutParams);
            idTV.setLayoutParams(layoutParams);
            answersTV.setLayoutParams(layoutParams);
            scoreTV.setLayoutParams(layoutParams);


            rowLayout.addView(nameTV);
            rowLayout.addView(idTV);
            rowLayout.addView(answersTV);
            rowLayout.addView(scoreTV);

            linearLayout.addView(rowLayout);
        }

    }





    private void readLeaderboard()  // Reads from db all player's scores & details and adds them to a designated list
    {
        db = SQLiteDatabase.openDatabase(getApplicationContext().getFilesDir() + "/capitals.db", null, 0);  // Connect to the db
        Cursor cursor = db.rawQuery("SELECT * FROM LEADERBOARD ORDER BY score DESC", null);

        if(cursor.moveToFirst())
        {
            do
            {
                int idIndex = cursor.getColumnIndex("examinee_ID");
                int nameIndex = cursor.getColumnIndex("examinee_name");
                int answersIndex = cursor.getColumnIndex("correct_answers");
                int scoreIndex = cursor.getColumnIndex("score");

                if (idIndex != -1 && nameIndex != -1 && answersIndex != -1 && scoreIndex != -1)
                {
                    String examineeID = cursor.getString(idIndex);
                    String examineeName = cursor.getString(nameIndex);
                    int correctAnswers = cursor.getInt(answersIndex);
                    int score = cursor.getInt(scoreIndex);

                    correctAnswersList.add(correctAnswers);
                    scoreList.add(score);
                    nameList.add(examineeName);
                    idList.add(examineeID);
                }
            } while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

    private void readDB()   // Reads from db and sets the current's players score & details
    {
        db = SQLiteDatabase.openDatabase(getApplicationContext().getFilesDir() + "/capitals.db", null, 0);  // Connect to the db

        // Execute the query and get the cursor
        Cursor cursor = db.rawQuery("SELECT examinee_ID, examinee_name, correct_answers, score FROM LEADERBOARD", null);

        this.score.setText("");
        this.correctAnswers.setText("");
        this.examineeName.setText("");
        this.examineeID.setText("");

        if (cursor.moveToFirst())
        {
            do
            {
                int idIndex = cursor.getColumnIndex("examinee_ID");
                int nameIndex = cursor.getColumnIndex("examinee_name");
                int answersIndex = cursor.getColumnIndex("correct_answers");
                int scoreIndex = cursor.getColumnIndex("score");

                if (idIndex != -1 && nameIndex != -1 && answersIndex != -1 && scoreIndex != -1)
                {
                    String examineeID = cursor.getString(idIndex);
                    String examineeName = cursor.getString(nameIndex);
                    int correctAnswers = cursor.getInt(answersIndex);
                    int score = cursor.getInt(scoreIndex);

                    String scoreStr = "Your score is: " + score + "pts!";
                    String correctAnswersStr = "You answered " + correctAnswers + " out of 10 questions correctly!";


                    this.examineeID.setText(examineeID);
                    this.examineeName.setText(examineeName);
                    this.correctAnswers.setText(correctAnswersStr);
                    this.score.setText(scoreStr);
                }
                else
                {
                    System.out.println("*** SOMETHING HAS GONE WRONG ***");
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

    }

    private void readTxt()
    {
        int score;  // Variable that will be used to pass to a "score string" variable
        int correctAnswers; // Variable that will be used to pass to a "correct answers" string variable
        String examineeName;
        String examineeID;


        // Creating a Database.txt at the storage of the app in the phone / emulator
        // where the score, correct answers and  examinee's name/ID will be saved
        try (FileInputStream fileData = openFileInput("Database.txt");
             InputStreamReader inputData = new InputStreamReader(fileData);
             BufferedReader readerData = new BufferedReader(inputData))
        {
            // Clearing first the text view before passing information to display
            this.score.setText("");
            this.correctAnswers.setText("");
            this.examineeName.setText("");
            this.examineeID.setText("");

            String line;

            for(int i = 0; i < 4; i++)  // Read each line from the file and update the corresponding Text view
            {
                if(i == 0)
                {
                    line = readerData.readLine();
                    score = Integer.parseInt(line);
                    String scoreString = "Your score is: " + score + "pts!";
                    this.score.setText(scoreString);
                }
                else if(i == 1)
                {
                    line = readerData.readLine();
                    correctAnswers = Integer.parseInt(line);
                    String correctAnswersString = "You answered " + correctAnswers + " out of 10 questions correctly!";
                    this.correctAnswers.setText(correctAnswersString);
                }
                else if(i == 2)
                {
                    line = readerData.readLine();
                    examineeName = line;
                    this.examineeName.setText(examineeName);
                }
                else
                {
                    line = readerData.readLine();
                    examineeID = line;
                    this.examineeID.setText(examineeID);
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.err.println("There was an IO exception error");
        }
    }

}
