/**
 * Philip Regan
 * http://www.oatmealandcoffee.com
 * 
 * 
 * RPGKitActivity
 * 
 * Main activity that is used for only the most preliminary app prep, if any at
 * all
 */

package net.cs76.projects.student;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class RPGKitActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /* INITIAL APPLICATION PREP and INTRO SCREENS (IF ANY) */

        goToHomeScreen();
        
    }
    
    public void onResume() {
    	super.onResume();
    	goToHomeScreen();
    }
    
    private void goToHomeScreen() {
    	// push to game selection
        Intent gameSelection = new Intent( getBaseContext(), OCGameSelectionActivity.class );
        startActivity( gameSelection );
    }
}