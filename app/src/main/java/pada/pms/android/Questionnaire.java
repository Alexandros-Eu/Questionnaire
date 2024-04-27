package pada.pms.android;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class represents a questionnaire for an app. It manages a set of questions, tracks the
 * current question being displayed, and provides methods for navigation, answering questions,
 * scoring and saving results.
 */
public class Questionnaire {
    private ArrayList<Question> Questions;  // ArrayList to store Question objects

    private int currentQuestionNumber;  // Tracker of the current question

    private static Questionnaire instance = null;   // Singleton instance

    private static Context cont;    // Android application context

    private SQLiteDatabase db;

    private Questionnaire()
    {
        Questions = new ArrayList();
        currentQuestionNumber = -1;
//        loadTextFile(); // Load questions from a text file (used as a database)
        loadDatabase();
        drawQuestions();    // Shuffle and select a subset of questions
    }

    // A singleton that works as a constructor restricting the questionnaire to ONLY one instance
    // The singleton gets or creates an instance of the Questionnaire
    public static Questionnaire getInstance(Context contextualization)
    {
        cont = contextualization;
        if(instance == null)
        {
            instance = new Questionnaire();
        }

        return instance;
    }

    public int getCurrentQuestionNumber()
    {
        return currentQuestionNumber;
    }

    public void setCurrentQuestionNumber(int currentQuestionNumber)
    {
        this.currentQuestionNumber = currentQuestionNumber;
    }

    private void loadTextFile() // Method to load questions from a text file (used as a database)
    {
        int numberOfQuestions;
        AssetManager managingDirector = cont.getAssets();
        System.out.println("*** !!!");

        try(BufferedReader BR = new BufferedReader(new InputStreamReader(managingDirector.open("Questionnaire.txt"))))
        {
            System.out.println("*** !!!");
            numberOfQuestions = Integer.parseInt(BR.readLine());

            for(int i = 0; i < numberOfQuestions; i++)
            {
                System.out.println("*** ///");

                Question question = new Question();
                question.setQuestionText(BR.readLine());
                int numberOfAnswers = Integer.parseInt(BR.readLine());
                question.setCorrectAnswer(Integer.parseInt(BR.readLine()));

                for(int j = 0; j < numberOfAnswers; j++)
                {
                    question.setPotentialAnswer(BR.readLine());
                }

                Questions.add(question);
            }
        }
        catch(IOException error)
        {
            System.err.println("Something went wrong.. exception while reading the database");
        }
    }

    private void loadDatabase()
    {
        // Open the database file
        db = SQLiteDatabase.openDatabase(cont.getFilesDir() + "/capitals.db", null, 0);

        Cursor cursor = db.rawQuery(
                "SELECT Questions.question_text, Questions.number_of_answers, Questions.correct_answer, " +
                        "Questions.potential_answer_one, Questions.potential_answer_two, Questions.potential_answer_three, " +
                        "Questions.potential_answer_four, Countries.country_flag " +
                        "FROM Questions " +
                        "INNER JOIN Countries ON Countries.country_id = Questions.country_id", null);
        Questions.clear();


            if(cursor != null && cursor.moveToFirst())
            {
                do
                {
                    int questionTextIndex = cursor.getColumnIndex("question_text");
                    int questionNumberOfAnswersIndex = cursor.getColumnIndex("number_of_answers");
                    int correctAnswerIndex = cursor.getColumnIndex("correct_answer");
                    int potentialAnswerOneIndex = cursor.getColumnIndex("potential_answer_one");
                    int potentialAnswerTwoIndex = cursor.getColumnIndex("potential_answer_two");
                    int potentialAnswerThreeIndex = cursor.getColumnIndex("potential_answer_three");
                    int potentialAnswerFourIndex = cursor.getColumnIndex("potential_answer_four");
                    int countryFlagIndex = cursor.getColumnIndex("country_flag");


                    if(questionTextIndex != -1 && questionNumberOfAnswersIndex != -1 && correctAnswerIndex != -1 && potentialAnswerOneIndex != -1 && potentialAnswerTwoIndex != -1 && potentialAnswerThreeIndex != -1 && potentialAnswerFourIndex != -1)
                    {
                        Question question = new Question();
                        question.setQuestionText(cursor.getString(questionTextIndex));
                        question.setCorrectAnswer(cursor.getInt(correctAnswerIndex));
                        question.setPotentialAnswer(cursor.getString(potentialAnswerOneIndex));
                        question.setPotentialAnswer(cursor.getString(potentialAnswerTwoIndex));
                        question.setPotentialAnswer(cursor.getString(potentialAnswerThreeIndex));
                        question.setCountryFlag(cursor.getString(countryFlagIndex));

                        if(cursor.getString(potentialAnswerFourIndex) != null)
                        {
                            question.setPotentialAnswer(cursor.getString(potentialAnswerFourIndex));
                        }

                        Questions.add(question);
                    }
                }
                while(cursor.moveToNext());
            }

    }

    void saveTextFile(int score, int correctAnswers, String examineeName, String examineeID)    // Method to save test results to a text file (in phone's storage)
    {
        AssetManager savingDirector = cont.getAssets();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(cont.getFilesDir(), "Database.txt"))))
        {
            writer.write(Integer.toString(score));  // saving score
            writer.newLine();
            writer.write(Integer.toString(correctAnswers)); // saving number of correct answers
            writer.newLine();
            writer.write(examineeName); // saving examinee name
            writer.newLine();
            writer.write(examineeID);   // saving examinee ID
        }
        catch(IOException error)
        {
            System.err.println("Error saving data to file");
        }

    }

    private void drawQuestions()    // Method to shuffle and select a subset of questions
    {
        Collections.shuffle(Questions);
        Questions = new ArrayList<>(Questions.subList(0, 10));
    }

    public int getNumberOfQuestions()
    {
        return Questions.size();
    }

    public Question getQuestion(int questionNumber)
    {
        return Questions.get(questionNumber);
    }

    public Question getCurrentQuestion()
    {
        return Questions.get(currentQuestionNumber);
    }

    public int getNumberOfAnsweredQuestions()
    {
        int count = 0;

        for(int i = 0; i < getNumberOfQuestions(); i++)
        {
            if(getQuestion(i).getUserAnswer() != -1)
            {
                count++;
            }
        }
        return count;
    }

    public int getNumberOfUnansweredQuestions()
    {
        int count = 0;

        for(int i = 0; i < getNumberOfQuestions(); i++)
        {
            if(getQuestion(i).getUserAnswer() == -1)
            {
                count++;
            }
        }

        return count;
    }

    public int goNext()
    {
        currentQuestionNumber++;

        if(currentQuestionNumber == getNumberOfQuestions())
        {
            currentQuestionNumber = 0;
        }

        return currentQuestionNumber;
    }

    public int goPrevious()
    {
        currentQuestionNumber--;

        if(currentQuestionNumber == -1)
        {
            currentQuestionNumber = getNumberOfQuestions() - getNumberOfQuestions();
        }

        return currentQuestionNumber;
    }

    public int goNextUnansweredQuestion()
    {
        if(getNumberOfUnansweredQuestions() == 0)
        {
            return -1;
        }

        do
        {
            currentQuestionNumber++;

            if(currentQuestionNumber == getNumberOfQuestions())
            {
                currentQuestionNumber = 0;
            }
        }
        while(getQuestion(currentQuestionNumber).isAnswered() == true);

        return currentQuestionNumber;
    }

    public int goPreviousUnansweredQuestion()
    {
        if(getNumberOfUnansweredQuestions() == 0)
        {
            return -1;
        }

        do
        {
            currentQuestionNumber--;

            if(currentQuestionNumber == -1)
            {
                currentQuestionNumber = getNumberOfQuestions() - 1;
            }
        }
        while(getQuestion(currentQuestionNumber).isAnswered() == true);

        return currentQuestionNumber;
    }


    public void printAll()  // Method to print details of all questions (used for logcat - debugging purposes)
    {
        int i, j;

        System.out.println("*** HERE!!!!!!!");

        for(i = 0; i < getNumberOfQuestions(); i++)
        {
            Question question = getQuestion(i);

            System.out.println("*** " + "Question : " + (i + 1) + " " + question.getQuestionText());
            System.out.println("*** " + "Number of Answers : " + question.getNumberOfPotentialAnswers());
            System.out.println("*** " + "Correct Answer : " + question.getCorrectAnswer());

            for(j = 0; j < question.getNumberOfPotentialAnswers(); j++)
            {
                System.out.println("*** " + "Answer : " + (j + 1) + " " + question.getPotentialAnswer(j));
            }
        }
    }

    public void printAnswers()  // Another method that prints the question number and user answers of all questions
    {
        for(int i = 0; i < getNumberOfQuestions(); i++)
        {
            System.out.println("*** Question Number : " + (i + 1) + " " + "User Answer : " + getQuestion(i).getUserAnswer());
        }
    }

    public int getScore()   // Method to calculate the total score
    {
        int score = 0;

        for(int i = 0; i < getNumberOfQuestions(); i++)
        {
            if(getQuestion(i).isCorrect())
            {
                score += 10;    // Every correct answers gives 10 pts
            }
        }

        return score;
    }

    public int getNumberOfCorrectAnswers()  // Method to get the number of correctly answered questions
    {
        int count = 0;

        for(int i = 0; i < getNumberOfQuestions(); i++)
        {
            if(getQuestion(i).isCorrect())
            {
                count++;
            }
        }

        return count;
    }

}
