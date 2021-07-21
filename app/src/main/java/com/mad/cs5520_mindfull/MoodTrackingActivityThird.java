package com.mad.cs5520_mindfull;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.HashMap;

public class MoodTrackingActivityThird extends DrawerClass {

    private DatabaseReference realtimeDatabase;
    private Button nextButton;
    private RangeSlider excitedSlider;
    private RangeSlider boredSlider;
    private String username = null;

    /**
     * onCreate() initializes database and adds sliders
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_mood_tracking_third, frameLayout);
        realtimeDatabase = FirebaseDatabase.getInstance().getReference();
        username = getIntent().getStringExtra("USERNAME");
        excitedSlider = (RangeSlider) findViewById(R.id.excited_slider);
        boredSlider = (RangeSlider) findViewById(R.id.bored_slider);
        nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realtimeDatabase.child("users").child(username).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        DataSnapshot snapshot = task.getResult();
                        String date = LocalDate.now().toString();
                        realtimeDatabase.child("users").child(username).child("moods").child(date).child("excited").setValue(excitedSlider.getValues().get(0));
                        realtimeDatabase.child("users").child(username).child("moods").child(date).child("bored").setValue(boredSlider.getValues().get(0));
                    }
                });

                Intent moodChartActivity = new Intent(view.getContext(), MoodChartActivity.class);
                moodChartActivity.putExtra("USERNAME", username);
                moodChartActivity.putExtra("MOODCHARTUSER", username);
                startActivity(moodChartActivity);
            }
        });
    }
}