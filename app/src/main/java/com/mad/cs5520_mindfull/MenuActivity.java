package com.mad.cs5520_mindfull;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    /**
     * onCreateOptionsMenu() Starts up options menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu (Menu menu){

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.top_menu_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * onOptionsItemSelected()
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.check_in:
                message("item one clicked");
                Intent drawerIntent = new Intent(getApplicationContext(), MoodTrackingActivityFirst.class);
                startActivity(drawerIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * message() adds toast
     * @param message
     */
    private void message(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
