/**
 * Philip Regan
 * http://www.oatmealandcoffee.com
 * 
 * 
 * OCPlayerInformationEditActivity
 * 
 * The screen where editing of the player for a game takes place
 * 
 */
package net.cs76.projects.student;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author philipr
 *
 */
public class OCPlayerInformationEditActivity extends OCCoreActivity implements OnClickListener {
	
	/* interface elements */
	
	private TextView edittextPlayerTitle = null; // oc_table_row_player_name_edittext
	private TextView edittextPlayerAbout = null; // oc_table_row_player_about_edittext
	private Button buttonPlayerEquipment = null;// oc_button_player_equipment
	private Button saveButton = null; // oc_button_player_info_save
	private Button cancelButton = null; // oc_button_player_info_cancel
	
	/* player information */
	
	private int gameId = -1;
	private int playerId = -1;

	private String playerTitle = "";
	private String playerAbout = "";
		
	/* database */
	
	private OCPlayerInformationController pic = null;

	/**
	 * Auto-generated constructor stub
	 */
	public OCPlayerInformationEditActivity() {
		// Auto-generated constructor stub
	}
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //initActivity();
        
	}
	
	/**
	 * class event that we will use to ensure we populate with the latest content
	 * every time
	 */
	public void onResume() {
		super.onResume();
		
		initActivity();
	}
	
	/**
	 * Populates the content for the activity
	 */
	private void initActivity() {

	     // bind the activity controller to its view
	    setContentView( R.layout.oc_player_information_edit );
	    gameId = intent.getIntExtra( INTENT_GAME_ID , INTENT_GAME_ID_DEFAULT );
	    
	    // bind the interface elements to the activity
	    edittextPlayerTitle = (TextView) findViewById( R.id.oc_edittext_player_name );
	    edittextPlayerAbout = (TextView) findViewById( R.id.oc_edittext_player_about );
	    buttonPlayerEquipment = (Button) findViewById( R.id.oc_button_player_equipment );
	    
	    /* prep the cancel and save buttons */
	    // bind the buttons to the class
	    saveButton = (Button) findViewById( R.id.oc_button_player_info_save );
	    cancelButton = (Button) findViewById( R.id.oc_button_player_info_cancel );
	    
	    // set the class as the listener to the buttons to intercept clicks
	    saveButton.setOnClickListener( this );
	    cancelButton.setOnClickListener( this );
	    buttonPlayerEquipment.setOnClickListener( this );
	    
	    // set their respective roles (actions) as a tag for identification later
	    saveButton.setTag( BUTTON_ROLE_SAVE );
	    cancelButton.setTag( BUTTON_ROLE_CANCEL );
	    
	    // get the game information
	    pic = new OCPlayerInformationController( getBaseContext(), gameId );
	    
	    if ( !pic.retrievePlayer() ) {
	    	// something bad happened
	    	showMessage("Player data could not be retrieved");
	    	return;
	    }
	    
	    // push the found data to the interface
	    edittextPlayerTitle.setText( playerTitle );
	    edittextPlayerAbout.setText( playerAbout );
	    
	    // set the title so the user has confirmation
	    setTitle( intent.getCharSequenceExtra( INTENT_GAME_TITLE ) + ": Player Information");
	}
	

	public void onClick(View v) {
		switch ( v.getId() ) {
		case R.id.oc_button_player_equipment:
			// go get some equipment
			break;
		case R.id.oc_button_player_info_save:
			saveEdits();
			break;
		case R.id.oc_button_player_info_cancel:
			cancelEditing();
			break;
		}
		
	}
	
	/**
	 * Glue code between the parent activity and the db helper class
	 */
	public void saveEdits() {
		if ( !saveEditsWithError() ) {
			showMessage("The player information could not be saved.");
		}
	}
	
	/**
	 * Moves the user back to the previous activity in the stack without saving any
	 * changes without ceremony
	 */
	public void cancelEditing() {
		finish();
	}
	
	/**
	 * Glue code between the interface/activity and the db controller
	 * returns true if all goes right. Returns false on any error
	 * @return
	 */
	private boolean saveEditsWithError() {
		// capture the content in the edittext fields
		playerTitle = edittextPlayerTitle.getText().toString();
		playerAbout = edittextPlayerAbout.getText().toString();
		
		// save to the db
		return pic.updatePlayer( playerTitle, playerAbout );
	}
	
	/**
	 * Private class to manage CRUD of player information
	 * @author philipr
	 *
	 */
	private class OCPlayerInformationController extends OCDbController {
		int mGameId;
		
		/**
		 * Class constructor that accepts the context and the game id for CRUD
		 * @param context
		 * @param gameId
		 */
		public OCPlayerInformationController(Context context, int gameId) {
			super(context);
			
			this.mGameId = gameId;
		}
		
		/**
		 * Retrieves the player's data from the DB to be applied to the interface. 
		 * NB: This should do this as a join, but doing it separately works for now
		 * @return boolean returns false on any error, and details of error reported in
		 * LogCat
		 */
		public boolean retrievePlayer() {
			
			if ( !openDatabase(context) ) {
				Log.w("OC", "OCPlayerInformationEditActivity.OCPlayerInformationController.retrievePlayer() could not open database.");
				return false;
			}
			
			/* Get the target player */
			
			// we are after the id here so we know which stories to CRUD
			String[] targetPlayerColumns = new String[]{ COL_UNI_ID };
			String targetWherePlayerParams = COL_UNI_PARENT_ID + " = " + mGameId + " AND " + COL_UNI_PARENT_TYPE + " = '" + TYP_GAME + "'";
			Cursor playerInformation = db.query ( TBL_PLAYERS, targetPlayerColumns, targetWherePlayerParams, null, null, null, null );
			
			if ( playerInformation.moveToFirst() ) {
				do {
					playerId = playerInformation.getInt( playerInformation.getColumnIndexOrThrow( COL_UNI_ID ) );
				} while ( playerInformation.moveToNext() );
			} else {
				Log.w("OC", "OCPlayerInformationEditActivity.OCPlayerInformationController.retrievePlayer() could not retrieve the player's data");
				return false;
			}
			
			// close the cursor
			if (playerInformation != null && !playerInformation.isClosed()) {
				playerInformation.close();
			}
			
			/* Get the stories */
			
			// build the target parameters
			String[] targetStoryColumns = new String[]{ COL_STY_STORY, COL_STY_CONTEXT };
			String targetWhereParams = COL_UNI_PARENT_ID + " = " + playerId + " AND " + COL_UNI_PARENT_TYPE + " = '" + TYP_PLAYER + "'";
			
			// submit the query
			Cursor playerStories = db.query( TBL_STORIES, targetStoryColumns, targetWhereParams, null, null, null, null);
			
			// now we iterate through the story results and create game entries
			if ( playerStories.moveToFirst() ) {
				do {
					String storyContext = playerStories.getString( playerStories.getColumnIndexOrThrow( COL_STY_CONTEXT ) );
					// push the strings to the parent class per context
					if ( storyContext.contentEquals( CTX_TITLE )) {
						playerTitle = playerStories.getString( playerStories.getColumnIndexOrThrow( COL_STY_STORY ) );
					} else if ( storyContext.contentEquals( CTX_ABOUT )) {
						playerAbout = playerStories.getString( playerStories.getColumnIndexOrThrow( COL_STY_STORY ) );
					}
																	
				} while ( playerStories.moveToNext() );
			} else {
				Log.w("OC", "OCGameInformationEditActivity.OCGameInformationController.retrievePlayer() could not retrieve the game's stories");
				return false;
			}
			// close the cursor
			if (playerStories != null && !playerStories.isClosed()) {
				playerStories.close();
			}
			
			/* get the equipment */
			
			// TODO get the equipment
			
			closeDatabase();
			
			return true;
		}
		
		/**
		 * Updates the game's data from the parent class to the DB.
		 * @param title
		 * @param intro
		 * @return boolean Returns false on any error, and reports errors to LogCat
		 */
		public boolean updatePlayer(String title, String intro) {
			
			if ( !openDatabase(context) ) {
				Log.w("OC", "OCGameInformationEditActivity.OCGameInformationController.updatePlayer() could not open database.");
				return false;
			}
			
			/* Player Stories */
			
			// updateStoryIntoDatabase(String story, String context, int parentID, String parentType, SQLiteDatabase database)
			long titleResult = updateStoryIntoDatabase(title, CTX_TITLE, playerId, TYP_PLAYER, db);
			long introResult = updateStoryIntoDatabase(intro, CTX_ABOUT, playerId, TYP_PLAYER, db);
			if ( titleResult == -1 || introResult == -1 ) {
				Log.w("OC", "OCPlayerInformationActivity.updateStoryIntoDatabase() title and/or intro could not be saved");
				return false;
			}
			
			closeDatabase();
			return true;
		}
		
	}

}
