package pada.pms.android;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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

    private void readDB()
    {
        db = SQLiteDatabase.openDatabase(getApplicationContext().getFilesDir() + "/capitals.db", null, 0);  // Connect to the db

        // Execute the query and get the cursor
        Cursor cursor = db.rawQuery("SELECT examinee_ID, examinee_name, correct_answers, score FROM LEADERBOARD", null);

        if (cursor.moveToFirst()) {
            do
            {
                int idIndex = cursor.getColumnIndex("examinee_ID");
                int nameIndex = cursor.getColumnIndex("examinee_name");
                int answersIndex = cursor.getColumnIndex("correct_answers");
                int scoreIndex = cursor.getColumnIndex("score");

                if (idIndex != -1 && nameIndex != -1 && answersIndex != -1 && scoreIndex != -1)
                {
                    int examineeID = cursor.getInt(idIndex);
                    String examineeName = cursor.getString(nameIndex);
                    int correctAnswers = cursor.getInt(answersIndex);
                    int score = cursor.getInt(scoreIndex);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();

    }

}
