package pada.pms.android;

import android.content.Intent;
import android.content.res.AssetManager;
import android.icu.util.Output;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The SplashScreen class represents an activity that serves as the initial screen where the user
 * enters their name & ID before proceeding to the questionnaire (Main Activity).
 * This class also holds a text view with the names of the students that created this app.
 */

public class SplashScreen extends AppCompatActivity implements View.OnClickListener {   // implements onClickListener in order for the button to work

    Button btnContinue; // Button to continue to the Main Activity

    TextView examineeName;  // Text view for the examinee's name

    TextView examineeID;    // Text view for the examinee's ID

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_layout);  // Connect the splash screen layout

        // Connect the UI elements by finding their ID
        btnContinue = findViewById(R.id.button_continue);
        examineeName = findViewById(R.id.edit_text_examinee_name);
        examineeID = findViewById(R.id.edit_text_examinee_ID);

        btnContinue.setEnabled(false);
        btnContinue.setOnClickListener(this);   // Set up a listener for the "Continue" button

        if(!fileExists("capitals.db"))
        {
            copyDB("capitals.db");
        }

        btnContinue.setEnabled(true);

    }


    @Override
    public void onClick(View v)
    {
        // Check if either the examinee's name or ID is not filled
        if(examineeName.getText().length() == 0 || examineeID.getText().length() == 0)
        {
            // Display a toast warning if information is missing
            Toast warning = Toast.makeText(getApplicationContext(), "Please fill in your information", Toast.LENGTH_LONG);
            warning.show();
        }
        else
        {
            // If both name and ID are filled create an intent to start the Main Activity
            Intent intent = new Intent(this, MainActivity.class);

            // Pass the examinee's name & ID as extras to the next activity
            intent.putExtra("name", examineeName.getText().toString());
            intent.putExtra("ID", examineeID.getText().toString());
            startActivity(intent);
        }

    }

    boolean fileExists(String fileName)
    {
        File file = new File(getApplicationContext().getFilesDir(), fileName);
        return file.exists();
    }

    void copyDB(String dbName)
    {
        AssetManager assetMan = getAssets();
        InputStream input;
        OutputStream output;
        byte[] buffer;
        int BR;

        try
        {
            input = assetMan.open(dbName);
            File filePath = new File(getApplicationContext().getFilesDir(), dbName);
            output = new FileOutputStream(filePath);
            buffer = new byte[1024];

            while((BR = input.read(buffer)) != -1)
            {
                output.write(buffer, 0, BR);
            }

            input.close();
            output.flush();
            output.close();
        }
        catch(IOException err)
        {
            System.out.println("*** IOException: " + err.getMessage());
            Toast tstErr = Toast.makeText(getApplicationContext(), "IO Error during DB loading...", Toast.LENGTH_LONG);
            tstErr.show();
        }


    }


}
