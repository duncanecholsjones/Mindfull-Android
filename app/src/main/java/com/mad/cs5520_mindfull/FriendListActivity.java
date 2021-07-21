package com.mad.cs5520_mindfull;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class FriendListActivity extends DrawerClass {

    private DatabaseReference realtimeDatabase;
    private ArrayList<FriendCard> friendCardList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RviewAdapter rviewAdapter;
    private RecyclerView.LayoutManager rLayoutManager;
    private String username = null;
    private String chartUsername = null;
    private static boolean initNeedsCompleted;
    private StorageReference storageReference;

    /**
     * onCreate(). Initialize friends list, handler, and recyclerView.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_friend_list, frameLayout);
        username = getIntent().getStringExtra("USERNAME");
        chartUsername = getIntent().getStringExtra("MOODCHARTUSER");
        initNeedsCompleted = getIntent().getBooleanExtra("NEEDSINIT", true);
        initListHandler();
        storageReference = FirebaseStorage.getInstance().getReference();
        initRecyclerView();
    }
    /**
     * initListHandler(). Initialize handler
     *
     */
    private void initListHandler() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                initList();
            }
        });
    }

    /**
     * initList(). Initialize friends list
     *
     */
    protected void initList() {
        // Get firebase reference
        realtimeDatabase = FirebaseDatabase.getInstance().getReference();
        // Get username the user passed in from MainActivity
        realtimeDatabase.child("users").child(username).child("friends").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                DataSnapshot snapshot = task.getResult();
                // Get list of other users in realtime database
                if (initNeedsCompleted) {
                    int i = 0;
                    for (DataSnapshot child : snapshot.getChildren()) {
                        try {
                            // Add cards to UI
                            String key =  child.getKey();
                            addItem(i, child.getKey(),  child.getValue().toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        i++;
                    }
                    initNeedsCompleted = false;
                }
            }
        });
    }


    /**
     * initRecyclerView. Create recycler view that will hold our UserCards.
     */
    private void initRecyclerView() {
        rLayoutManager = new LinearLayoutManager(this);
        recyclerView = findViewById(R.id.recycler_user_view);
        recyclerView.setHasFixedSize(true);
        rviewAdapter = new RviewAdapter(friendCardList, getApplicationContext());
        FriendClickListener itemClickListener = new FriendClickListener() {
            @Override
            public void onUserClick(int position) {
                rviewAdapter.notifyItemChanged(position);
                goToFriendMoodChart(friendCardList.get(position).getUsername());
            }
        };
        // Set adapter and layout manager
        rviewAdapter.setOnItemClickListener(itemClickListener);
        recyclerView.setAdapter(rviewAdapter);
        recyclerView.setLayoutManager(rLayoutManager);
    }

    /**
     * addItem() add friend card to list
     *
     * @param position
     * @param username
     * @throws Exception
     */
    private void addItem(Integer position, String username, String image) throws Exception {
        friendCardList.add(position, new FriendCard(username, image));
        rviewAdapter.notifyItemInserted(position);
    }

    /**
     * goToFriendMoodChart(). Go to chat activity and set extras.
     *
     * @param chartUsername
     */
    private void goToFriendMoodChart(String chartUsername) {
        Intent intent = new Intent(this, MoodChartActivity.class);
        intent.putExtra("USERNAME", username);
        intent.putExtra("MOODCHARTUSER", chartUsername);
        startActivity(intent);
    }
}