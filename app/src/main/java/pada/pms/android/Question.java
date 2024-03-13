package pada.pms.android;

import java.util.ArrayList;

/**
 * The Question class represents a single question in a questionnaire.
 * It encapsulates the question text, potential answers, correct answer and user's selected answer.
 */
public class Question
{
    private String questionText;    // Variable to store the question text

    private String pictureName;

    private ArrayList<String> potentialAnswers; // Variable to store the list of potential answers

    private int correctAnswer;  // Variable to store the index of the correct answer (from 0 .. to 3)

    private int userAnswer; // Variable to store the user's selected answer (index 0 to 3 / -1 indicates no answer)

    public Question()
    {
        potentialAnswers = new ArrayList();
        correctAnswer = -1; // Initialize the value to -1 as for 'no answer'
        userAnswer = -1;    // Initialize the value to -1 as for 'no answer'
    }

    public String getQuestionText()
    {
        return questionText;
    }

    public void setQuestionText(String questionText)
    {
        this.questionText = questionText;
    }

    public int getCorrectAnswer()
    {
        return correctAnswer;
    }

    public void setCorrectAnswer(int correctAnswer)
    {
        this.correctAnswer = correctAnswer;
    }

    public int getUserAnswer()
    {
        return userAnswer;
    }

    public void setUserAnswer(int userAnswer)
    {
        this.userAnswer = userAnswer;
        this.userAnswer++;  // Increment by 1 due to the difference in index between txt and languages (0 -> 1)
    }

    public String getPotentialAnswer(int numberOfAnswer)
    {
        return potentialAnswers.get(numberOfAnswer);
    }

    public void setPotentialAnswer(String potentialAnswer)
    {
        potentialAnswers.add(potentialAnswer);
    }

    public boolean isAnswered() // Method that checks if a question has been answered
    {
        if(userAnswer != -1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isCorrect() // Method that checks if a user answer to a question is correct
    {
        if(userAnswer == correctAnswer)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public int getNumberOfPotentialAnswers()
    {
        return potentialAnswers.size();
    }

}
