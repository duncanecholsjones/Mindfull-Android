package com.mad.cs5520_mindfull;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference realtimeDatabase;
    private EditText usernameInput;
    private EditText passwordInput;
    private Button signinButton;
    private boolean callMoodChartIntent = false;

    /**
     * onCreate() creates main instance
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        realtimeDatabase = FirebaseDatabase.getInstance().getReference();
        usernameInput = (EditText) findViewById(R.id.input_username);
        passwordInput = (EditText) findViewById(R.id.input_password);
        signinButton = (Button) findViewById(R.id.signin_button);

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                realtimeDatabase.child("users").child(usernameInput.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        DataSnapshot snapshot = task.getResult();
                        if (usernameInput.getText().toString().equals(snapshot.child("username").getValue()) &&
                                passwordInput.getText().toString().equals(snapshot.child("password").getValue())) {

                            Intent moodChartActivity = new Intent(view.getContext(), MoodChartActivity.class);
                            moodChartActivity.putExtra("USERNAME", usernameInput.getText().toString());
                            moodChartActivity.putExtra("MOODCHARTUSER", usernameInput.getText().toString());
                            startActivity(moodChartActivity);
                        } else {
                            Toast.makeText(MainActivity.this, "Username not found / Password incorrect. Try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
    private void callMoodChart() {
        if (callMoodChartIntent) {
            Intent moodChartActivity = new Intent(this, MoodChartActivity.class);
            moodChartActivity.putExtra("USERNAME", usernameInput.getText().toString());
            startActivity(moodChartActivity);
        }
    }
}