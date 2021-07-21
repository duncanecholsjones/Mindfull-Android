package com.mad.cs5520_mindfull;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.android.datatransport.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MoodChartActivity extends DrawerClass {

    private LineChart moodChart;
    private DatabaseReference realtimeDatabase;
    private String username;
    private String chartUsername;
    private TextView usernameTextView;
    HashMap<String, HashMap<String, Integer>> datesWithMoods = new HashMap<>();
    private CheckBox happyCheckBox, sadCheckBox, calmCheckBox, anxiousCheckBox, excitedCheckBox, boredCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_mood_chart, frameLayout);
        moodChart = findViewById(R.id.mood_linechart);
        usernameTextView = findViewById(R.id.username_textview);
        happyCheckBox = findViewById(R.id.happy_checkbox);
        sadCheckBox = findViewById(R.id.sad_checkbox);
        calmCheckBox = findViewById(R.id.calm_checkbox);
        anxiousCheckBox = findViewById(R.id.anxious_checkbox);
        excitedCheckBox = findViewById(R.id.excited_checkbox);
        boredCheckBox = findViewById(R.id.bored_checkbox);
        initCheckBoxes();

        realtimeDatabase = FirebaseDatabase.getInstance().getReference();
        username = getIntent().getStringExtra("USERNAME");
        chartUsername = getIntent().getStringExtra("MOODCHARTUSER");
        usernameTextView.setText(chartUsername + "'s Moods");
        initMoodChartHandler();
    }

    /**
     * start up Mood chart handler
     */
    private void initMoodChartHandler() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                initMoodChart();
            }
        });
    }

    /**
     * initMoodChart() initialize mood chart and pull from database
     */
    private void initMoodChart() {
        Description desc = new Description();
        desc.setText("Moods");
        desc.setTextSize(20);
        moodChart.setDescription(desc);
        XAxis xAxis = moodChart.getXAxis();
        // Get user moods from database
        realtimeDatabase.child("users").child(chartUsername).child("moods").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                DataSnapshot snapshot = task.getResult();
                // Get list of dates in realtime database
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    HashMap<String, Integer> internalMoodMap = new HashMap<>();
                    for (DataSnapshot valueSnapshot : eventSnapshot.getChildren()) {
                        //populate mood map with moods from database
                        internalMoodMap.put(valueSnapshot.getKey(), Integer.parseInt(valueSnapshot.getValue().toString()));
                    }
                    datesWithMoods.put(eventSnapshot.getKey(), internalMoodMap);
                }
                setData();
            }

        });

        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
                long millis = (long) value * 1000L;
                return mFormat.format(new Date(millis));
            }
        });

        moodChart.getDescription().setEnabled(true);
        moodChart.setTouchEnabled(true);  // enable touch gestures
        moodChart.setDragEnabled(true);  // enable scaling and dragging
        moodChart.setScaleEnabled(true);
        moodChart.getXAxis().setEnabled(true);
        moodChart.getAxisLeft().setEnabled(true);
        moodChart.getAxisRight().setEnabled(true);
        moodChart.getAxisLeft().setDrawLabels(true);

        moodChart.getAxisRight().setDrawLabels(true);
        moodChart.getXAxis().setDrawLabels(true);
        moodChart.getLegend().setEnabled(true);
        moodChart.setPinchZoom(true);   // if disabled, scaling can be done on x- and y-axis separately
        YAxis yAxis = moodChart.getAxisLeft();
        yAxis.setDrawZeroLine(true);
        Legend l = moodChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);  // modify the legend
    }

    /**
     * getMoodDataSet() formats data and adds to mood array
     * @param mood
     * @return
     */
    private List<Entry> getMoodDataSet(String mood) {
        ArrayList<Entry> moodData = new ArrayList<>();
        for (String date : datesWithMoods.keySet()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            formatter = formatter.withLocale(Locale.getDefault());
            LocalDate newDate = LocalDate.parse(date, formatter);
            ZoneId zoneId = ZoneId.systemDefault();
            long epoch = newDate.atStartOfDay(zoneId).toEpochSecond();
            moodData.add(new Entry(epoch, datesWithMoods.get(date).get(mood)));
        }
        return moodData;
    }

    /**
     * setData() sets fields for user chart data
     */
    private void setData() {
        Comparator<Entry> comparator = new Comparator<Entry>() {
            @Override
            public int compare(Entry o1, Entry o2) {
                return Float.compare(o1.getX(), o2.getX());
            }
        };

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

        if (happyCheckBox.isChecked()) {
            List<Entry> happyValues = getMoodDataSet("happy");
            happyValues.sort(comparator);
            LineDataSet happyDataSet = new LineDataSet(happyValues, "Happy");
            happyDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            happyDataSet.setDrawFilled(true);
            happyDataSet.setFillColor(Color.GREEN);
            happyDataSet.setDrawCircles(true);
            happyDataSet.setCircleRadius(4);
            happyDataSet.setDrawValues(false);
            happyDataSet.setLineWidth(3);
            happyDataSet.setColor(Color.GREEN);
            happyDataSet.setCircleColor(Color.GREEN);
            dataSets.add(happyDataSet);
        }

        if (sadCheckBox.isChecked()) {
            List<Entry> sadValues = getMoodDataSet("sad");
            sadValues.sort(comparator);
            LineDataSet sadDataSet = new LineDataSet(sadValues, "Sad");
            sadDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            sadDataSet.setDrawFilled(true);
            sadDataSet.setFillColor(Color.GRAY);
            sadDataSet.setDrawCircles(true);
            sadDataSet.setCircleRadius(4);
            sadDataSet.setDrawValues(false);
            sadDataSet.setLineWidth(3);
            sadDataSet.setColor(Color.GRAY);
            sadDataSet.setCircleColor(Color.GRAY);
            dataSets.add(sadDataSet);
        }

        if (calmCheckBox.isChecked()) {
            List<Entry> calmValues = getMoodDataSet("calm");
            calmValues.sort(comparator);
            LineDataSet calmDataSet = new LineDataSet(calmValues, "Calm");
            calmDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            calmDataSet.setDrawFilled(true);
            calmDataSet.setFillColor(Color.BLUE);
            calmDataSet.setDrawCircles(true);
            calmDataSet.setCircleRadius(4);
            calmDataSet.setDrawValues(false);
            calmDataSet.setLineWidth(3);
            calmDataSet.setColor(Color.BLUE);
            calmDataSet.setCircleColor(Color.BLUE);
            dataSets.add(calmDataSet);
        }

        if (anxiousCheckBox.isChecked()) {
            List<Entry> anxiousValues = getMoodDataSet("anxious");
            anxiousValues.sort(comparator);
            LineDataSet anxiousDataSet = new LineDataSet(anxiousValues, "Anxious");
            anxiousDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            anxiousDataSet.setDrawFilled(true);
            anxiousDataSet.setFillColor(Color.RED);
            anxiousDataSet.setDrawCircles(true);
            anxiousDataSet.setCircleRadius(4);
            anxiousDataSet.setDrawValues(false);
            anxiousDataSet.setLineWidth(3);
            anxiousDataSet.setColor(Color.RED);
            anxiousDataSet.setCircleColor(Color.RED);
            dataSets.add(anxiousDataSet);
        }

        if (excitedCheckBox.isChecked()) {
            List<Entry> excitedValues = getMoodDataSet("excited");
            excitedValues.sort(comparator);
            LineDataSet excitedDataSet = new LineDataSet(excitedValues, "Excited");
            excitedDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            excitedDataSet.setDrawFilled(true);
            excitedDataSet.setFillColor(Color.YELLOW);
            excitedDataSet.setDrawCircles(true);
            excitedDataSet.setCircleRadius(4);
            excitedDataSet.setDrawValues(false);
            excitedDataSet.setLineWidth(3);
            excitedDataSet.setColor(Color.YELLOW);
            excitedDataSet.setCircleColor(Color.YELLOW);
            dataSets.add(excitedDataSet);
        }

        if (boredCheckBox.isChecked()) {
            List<Entry> boredValues = getMoodDataSet("bored");
            boredValues.sort(comparator);
            LineDataSet boredDataSet = new LineDataSet(boredValues, "Bored");
            boredDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            boredDataSet.setDrawFilled(true);
            boredDataSet.setFillColor(Color.DKGRAY);
            boredDataSet.setDrawCircles(true);
            boredDataSet.setCircleRadius(4);
            boredDataSet.setDrawValues(false);
            boredDataSet.setLineWidth(3);
            boredDataSet.setColor(Color.DKGRAY);
            boredDataSet.setCircleColor(Color.DKGRAY);
            dataSets.add(boredDataSet);
        }
        LineData data = new LineData(dataSets);
        moodChart.setData(data);
        moodChart.invalidate();
    }

    /**
     * setDataHandler() initializes handler
     */
    private void setDataHandler() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                setData();
            }
        });
    }

    /**
     * initCheckBoxes() initializes check boxes for users for graphs
     */
    private void initCheckBoxes() {
        happyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setDataHandler();
            }
        });
        sadCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setDataHandler();
            }
        });
        calmCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setDataHandler();
            }
        });
        anxiousCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setDataHandler();
            }
        });
        excitedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setDataHandler();
            }
        });
        boredCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setDataHandler();
            }
        });
    }
}