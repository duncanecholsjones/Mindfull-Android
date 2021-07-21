package com.mad.cs5520_mindfull;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad.cs5520_mindfull.diary_entry.DiaryCollectorActivity;

public class DrawerClass extends AppCompatActivity {

    private Toolbar topMenu;
    protected FrameLayout frameLayout;
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;
    protected String username;
    protected String moodChartUsername;
    private TextView profileName;
    private Button profileButton;
    private ImageView picture;
    private StorageReference storageReference;
    private DatabaseReference mDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        username = getIntent().getStringExtra("USERNAME");
        moodChartUsername = getIntent().getStringExtra("MOODCHARTUSER");

        Toolbar toolbar = (Toolbar) findViewById(R.id.topPanel);
        toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);
        actionbar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        frameLayout = (FrameLayout) findViewById(R.id.view_stub);

        dl = (DrawerLayout) findViewById(R.id.drawer_layout);
        t = new ActionBarDrawerToggle(this, dl, toolbar, R.string.Open, R.string.Close);

        dl.addDrawerListener(t);
        t.syncState();

        // Database logic
        storageReference = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        InitD();

        nv = (NavigationView) findViewById(R.id.navigation_view);

        // Header navigation logic
        View headerView = nv.getHeaderView(0);
        picture = headerView.findViewById(R.id.drawer_user_image_iv);
        profileName = headerView.findViewById(R.id.username);
        profileName.setText(username);
        profileButton = headerView.findViewById(R.id.btn_profile);

        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.chart:
                        Intent drawerIntent = new Intent(getApplicationContext(), MoodChartActivity.class);
                        drawerIntent.putExtra("USERNAME", username);
                        drawerIntent.putExtra("MOODCHARTUSER", username);
                        startActivity(drawerIntent);
                        break;

                    case R.id.diary:
                        Intent drawerIntent2 = new Intent(getApplicationContext(), DiaryCollectorActivity.class);
                        drawerIntent2.putExtra("USERNAME", username);
                        drawerIntent2.putExtra("MOODCHARTUSER", username);
                        startActivity(drawerIntent2);
                        break;
                    case R.id.friends:
                        Intent drawerIntent3 = new Intent(getApplicationContext(), FriendListActivity.class);
                        drawerIntent3.putExtra("USERNAME", username);
                        drawerIntent3.putExtra("MOODCHARTUSER", username);
                        startActivity(drawerIntent3);
                        break;
                    case R.id.logout:
                        Intent drawerIntent4 = new Intent(getApplicationContext(), MainActivity.class);
                        drawerIntent4.putExtra("USERNAME", username);
                        drawerIntent4.putExtra("MOODCHARTUSER", username);
                        startActivity(drawerIntent4);
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ProfileActivity.class);
                intent.putExtra("USERNAME", username);
                intent.putExtra("MOODCHARTUSER", username);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data.hasExtra("key1")) {
            }
        }
    }

    /**
     * onCreateOptionsMenu() creates inflater for menu
     * @param menu takes in a menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_menu_bar, menu);
        return true;
    }

    /**
     * onOptionsItemSelected() assign attribute to menu buttons from toolbar
     * @param item menu token
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.check_in:
                Intent drawerIntent = new Intent(getApplicationContext(), MoodTrackingActivityFirst.class);
                drawerIntent.putExtra("USERNAME", username);
                drawerIntent.putExtra("MOODCHARTUSER", username);
                startActivity(drawerIntent);
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * getProfilePicD() get profile picture from database and sets it to the xml item
     * @param pictureName mstring for the picture file
     */
    private void getProfilePicD(String pictureName) {
        StorageReference imgRef = storageReference.child("images").child(pictureName);
        imgRef.getBytes(1024 * 1024 * 5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                picture.setImageBitmap(bm);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });


    }

    /**
     * initd() initialize data. Works by clearing local itemList then repopulating
     * This is necessary to work properly
     */
    protected void InitD() {
        if (username != null) {
            mDatabase.child("users").child(username).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    DataSnapshot snapshot = task.getResult();
                    String picName = snapshot.child("picture").getValue().toString();
                    getProfilePicD(picName);
                }
            });
        }
    }
}
