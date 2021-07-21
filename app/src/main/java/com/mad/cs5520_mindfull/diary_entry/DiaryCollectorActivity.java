package com.mad.cs5520_mindfull.diary_entry;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.cs5520_mindfull.DrawerClass;
import com.mad.cs5520_mindfull.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DiaryCollectorActivity extends DrawerClass implements PopUpWindow.popUpListener, AdapterView.OnItemSelectedListener {
    private final static String NUM_ITEMS = "NUM_ITEMS";
    private final static String KEY_OF_INSTANCE = "KEY_OF_INSTANCE";
    private DatabaseReference realtimeDatabase;


    ArrayList<DiaryItem> itemList = new ArrayList<>();
    private RecyclerView recyclerView;
    private rAdapter rAdapter;
    private RecyclerView.LayoutManager rLayoutManager;
    private FloatingActionButton addItemButton;
    private String title;
    private String username;

    /**
     * onCreate() creates database instance and initializes username
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realtimeDatabase = FirebaseDatabase.getInstance().getReference();
        username = getIntent().getStringExtra("USERNAME");
        getLayoutInflater().inflate(R.layout.activity_diary_item_collector, frameLayout);

        init(savedInstanceState);

        Spinner spinner = (Spinner) findViewById(R.id.sort_by_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_by_dropdown, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        addItemButton = findViewById(R.id.addItemButton);

        if (savedInstanceState != null) {
            title = savedInstanceState.getString("titleEditTextView");
        }

        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = 0;
                openPopUpWindow();
            }
        });

        //Specify what action a specific gesture performs, in this case swiping right or left deletes the entry
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Toast.makeText(DiaryCollectorActivity.this, "Deleted an entry", Toast.LENGTH_SHORT).show();
                int position = viewHolder.getLayoutPosition();
                itemList.remove(position);
                rAdapter.notifyItemRemoved(position);

            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void initHandler(Bundle savedInstanceState) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                init(savedInstanceState);
            }
        });
    }

    /**
     * init() initialize data. Works by clearing local itemList then repopulating
     * This is necessary to work properly
     * @param savedInstanceState
     */
    private void init(Bundle savedInstanceState) {
        initialItemData(savedInstanceState);
        createRecyclerView();
        realtimeDatabase.child("users").child(username).child("diary-entries").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { //might cause error here
                // Clearing original list
                itemList.clear();

                // Notifying data change
                rAdapter.notifyDataSetChanged();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String title = child.child("title").getValue().toString();
                    String date = child.child("date").getValue().toString();
                    String content = child.child("content").getValue().toString();
                    addDiaryItem(title, content, date);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**
     * initialItemData() helper to prevent double loading of data
     * @param savedInstanceState
     */
    private void initialItemData(Bundle savedInstanceState) {
        // Not the first time to open this Activity
        if (savedInstanceState != null && savedInstanceState.containsKey(NUM_ITEMS)) {
            if (itemList == null || itemList.size() == 0) {
                int size = savedInstanceState.getInt(NUM_ITEMS);
                // Retrieve keys we stored in the instance
                for (int i = 0; i < size; i++) {
                    String dTextTitle = savedInstanceState.getString(KEY_OF_INSTANCE + i + "0");
                    String dTextParagraph = savedInstanceState.getString(KEY_OF_INSTANCE + i + "1");
                    String dDate = savedInstanceState.getString(KEY_OF_INSTANCE + i + "2");
                    boolean isChecked = savedInstanceState.getBoolean(KEY_OF_INSTANCE + i + "3");

                    // We need to make sure names such as "XXX(checked)" will not duplicate
                    // Use a tricky way to solve this problem, not the best though
                    if (isChecked) {
                        dTextTitle = dTextTitle.substring(0, dTextTitle.lastIndexOf("("));
                    }
                    DiaryItem diaryItem = new DiaryItem(dTextTitle, dTextParagraph, dDate);

                    itemList.add(diaryItem);
                }
            }
        }
    }

    /**
     * createRecyclerView() initializes recycler view
     */
    private void createRecyclerView() {
        rLayoutManager = new LinearLayoutManager(this);
        recyclerView = findViewById(R.id.recycler_view);
        rAdapter = new rAdapter(itemList);
        ItemClickListener itemClickListener = new ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                itemList.get(position).onItemClick(position);
                rAdapter.notifyItemChanged(position);
            }

            @Override
            public String setText(int position, String text) {
                return itemList.get(position).setText(position, text);
            }

        };
        rAdapter.setOnClickListener(itemClickListener);
        recyclerView.setAdapter(rAdapter);

        recyclerView.setLayoutManager(rLayoutManager);
    }


    /**
     * addDiaryItem() adds diary entry to list
     * @param title
     * @param content
     * @param date
     */
    public void addDiaryItem(String title, String content, String date) {
        int position = itemList.size();
        DiaryItem item3 = new DiaryItem(title, content, date);
        itemList.add(item3);
        rAdapter.notifyItemInserted(position);
    }

    /**
     * openPopUpWindow() opens popup dialog for diary entry
     */
    private void openPopUpWindow() {
        PopUpWindow dialog = new PopUpWindow();
        dialog.show(getSupportFragmentManager(), "example");
        Bundle args = new Bundle();
        args.putString("username", username);
        dialog.setArguments(args);
    }

    /**
     * onItemSelected() listener for user clicks
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        // Creating empty copy list
        ArrayList<DiaryItem> copyList = new ArrayList<>();
        // Populating items from original into copy list
        for (int i = 0; i < itemList.size(); i++) {
            copyList.add(itemList.get(i));
        }
        // Checking if dropdown equals date
        if (text.equals("Date")) {
            // Sorting list items by date
            Collections.sort(copyList, new Comparator<DiaryItem>() {
                public int compare(DiaryItem di1, DiaryItem di2) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-YYYY");
                    Date d1 = null;
                    Date d2 = null;
                    try {
                        d1 = sdf.parse(di1.getdDate());
                        d2 = sdf.parse(di2.getdDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return d1.compareTo(d2);
                }
            });
        }
        // Clearing original list
        itemList.clear();

        // Repopulating original list with sorted items from copy list
        for (DiaryItem item : copyList) {
            itemList.add(position, item);
        }
        // Notifying data change
        rAdapter.notifyDataSetChanged();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
