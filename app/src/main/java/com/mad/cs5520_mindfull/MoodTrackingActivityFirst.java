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
import com.mad.cs5520_mindfull.diary_entry.DiaryCollectorActivity;

import java.time.LocalDate;

public class MoodTrackingActivityFirst extends DrawerClass {

    private DatabaseReference realtimeDatabase;
    private Button nextButton;
    private RangeSlider happySlider;
    private RangeSlider sadSlider;
    private String username = null;

    /**
     * onCreate() initializes database and adds functionality to sliders
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_mood_tracking_first, frameLayout);
        realtimeDatabase = FirebaseDatabase.getInstance().getReference();
        username = getIntent().getStringExtra("USERNAME");
        happySlider = (RangeSlider) findViewById(R.id.happy_slider);
        sadSlider = (RangeSlider) findViewById(R.id.sad_slider);
        nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realtimeDatabase.child("users").child(username).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        DataSnapshot snapshot = task.getResult();
                        String date = LocalDate.now().toString();
                        realtimeDatabase.child("users").child(username).child("moods").child(date).child("happy").setValue(happySlider.getValues().get(0));
                        realtimeDatabase.child("users").child(username).child("moods").child(date).child("sad").setValue(sadSlider.getValues().get(0));
                    }
                });

                Intent moodActivity = new Intent(view.getContext(), MoodTrackingActivitySecond.class);
                moodActivity.putExtra("USERNAME", username);
                moodActivity.putExtra("MOODCHARTUSER", username);
                startActivity(moodActivity);
            }
        });
    }
    public void openDiary(){
        Intent intent = new Intent(this, DiaryCollectorActivity.class);
        startActivity(intent);
    }
}