/**
 * Philip Regan
 * http://www.oatmealandcoffee.com
 * 
 * 
 * OCCoreActivity
 * 
 * An abstract class that contains all the strings needed for intents and basic
 * concepts to speed development. This is used as the basis for almost all of the 
 * custom Activities
 * 
 */
package net.cs76.projects.student;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

/**
 * @author philipr
 *
 */
public class OCCoreActivity extends Activity {
	
	/* INTENTS */
	
	protected static String INTENT_GAME_ID = "game_id";
	protected static String INTENT_GAME_TITLE = "game_title";
	
	protected static String INTENT_TASK_ID = "task_id";
	
	protected static String INTENT_EDIT_TARGET_COMPONENT = "target_component";
	protected static String INTENT_EDIT_TARGET_PARENT = "target_parent";
	protected static String INTENT_EDIT_PARENT_ID = "parent_id";
	
	// key used in list selection activity to tell it what action to commit when 
	// an object is selected
	protected static String INTENT_EDIT_INTENTION = "intended_edit";
	// things to do once objects are selected
	protected static String INTENDED_EDIT_LINK = "link_game_object";
	protected static String INTENDED_EDIT_UNLINK = "unlink_game_object";
	protected static String INTENDED_EDIT_UPDATE = "update_game_object";
	protected static String INTENDED_EDIT_DELETE = "delete_game_object";
	
	// tag strings to flag actions
	protected static String BUTTON_ROLE_SAVE = "save";
	protected static String BUTTON_ROLE_CANCEL = "cancel";
	
	/* CONTEXT: TYPES */
    
    protected static final String TYP_GAME = "game";
    protected static final String TYP_PLAYER = "player";
    protected static final String TYP_TASK = "task";
    protected static final String TYP_STORY = "story";
    protected static final String TYP_EQUIPMENT = "equipment";
    protected static final String TYP_WALLET = "wallet";
    protected static final String TYP_ATTRIBUTES = "attribute";
	// generic default with a variety of uses, not just for IDs
	protected static int INTENT_GAME_ID_DEFAULT = -1;
	
	protected Intent intent;
	
	/**
	 * Auto-generated constructor stub
	 */
	public OCCoreActivity() {
		// Auto-generated constructor stub
	}
	
	/**
	 * Required per framework
	 */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // get the intent so we know what data we are targeting
        intent = getIntent();
        
	}
	
	// stub
	public void onResume() {
		super.onResume();
		// do stuff
	}
	
	/**
	 * Helper method for showing messages onScreen using Toast.
	 * @param title
	 */
	public void showMessage(String title) {
		Toast t = Toast.makeText(this, title, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.BOTTOM, 0, 0);
		t.show();
	}

}
