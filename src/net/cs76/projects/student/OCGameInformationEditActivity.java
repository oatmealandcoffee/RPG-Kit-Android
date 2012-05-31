/**
 * Philip Regan
 * http://www.oatmealandcoffee.com
 * 
 * 
 * OCGameInformationEditActivity
 * 
 * The screen where editing of the global information for a game takes place. Retrieving, 
 * displaying, and saving information is easier here than in the game selection page
 * so everything is contained within this task.
 * 
 * NB: This class was set up and used as a template for editing the information of
 * other game components since it neatly binds an activity to a layout and DB controller
 * 
 */
package net.cs76.projects.student;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;

/**
 * @author philipr
 *
 */
public class OCGameInformationEditActivity extends OCCoreActivity implements OnClickListener {
	
	/* interface elements */
	
	private TextView edittextGameTitle = null;
	private TextView edittextGameIntro = null;
	private TextView edittextGameTimeUnit = null;
	private TextView edittextGameMoneyUnit = null;
	private Button saveButton = null;
	private Button cancelButton = null;
	
	/* game information */
	
	private int gameId = -1;

	private String gameTitle = "";
	private String gameIntro = "";
	private String gameTimeUnit = "";
	private String gameMoneyUnit = "";
	
	/* database */
	
	private OCGameInformationController gic = null;

	/**
	 * Auto-generated constructor stub
	 */
	public OCGameInformationEditActivity() {
		// Auto-generated constructor stub
	}
	
	/**
	 * onCreate, required by Activity class
	 */
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
	    setContentView( R.layout.oc_game_information_edit );
	    
	    // capture intent extras
	    setTitle( intent.getCharSequenceExtra( INTENT_GAME_TITLE ) + ": Game Information");
	    gameId = intent.getIntExtra( INTENT_GAME_ID , INTENT_GAME_ID_DEFAULT );
	    
	    // bind the interface elements to the activity
	    edittextGameTitle = (TextView) findViewById( R.id.oc_edittext_game_title );
	    edittextGameIntro = (TextView) findViewById( R.id.oc_edittext_game_intro );
	    edittextGameTimeUnit = (TextView) findViewById( R.id.oc_edittext_game_time_unit );
	    edittextGameMoneyUnit = (TextView) findViewById( R.id.oc_textview_game_money_unit );
	    
	    /* prep the cancel and save buttons */
	    
	    // bind the buttons to the class
	    saveButton = (Button) findViewById( R.id.oc_button_game_info_save );
	    cancelButton = (Button) findViewById( R.id.oc_button_game_info_cancel );
	    
	    // set the class as the listener to the buttons to intercept clicks
	    saveButton.setOnClickListener( this );
	    cancelButton.setOnClickListener( this );
	    
	    // set their respective roles (actions) as a tag for identification later
	    saveButton.setTag( BUTTON_ROLE_SAVE );
	    cancelButton.setTag( BUTTON_ROLE_CANCEL );
	    
	    // get the game information
	    gic = new OCGameInformationController( getBaseContext(), gameId );
	    
	    if ( !gic.retrieveGame() ) {
	    	// something bad happened
	    	showMessage("Game data could not be retrieved");
	    	return;
	    }
	    
	    // populate the interface with the game information
	    edittextGameTitle.setText(gameTitle);
	    edittextGameIntro.setText(gameIntro);
	    edittextGameTimeUnit.setText(gameTimeUnit);
	    edittextGameMoneyUnit.setText(gameMoneyUnit);
	    
	    // now we wait for edits
	}
	
	public void onPause() {
		super.onPause();
		//gic.closeDatabase();
	}
	
	/**
	 * Glue code between the interface/activity and the db controller
	 * returns true if all goes right. Returns false on any error
	 * @return
	 */
	private boolean saveEditsWithError() {
		// capture the content in the edittext fields
		gameTitle = edittextGameTitle.getText().toString();
		gameIntro = edittextGameIntro.getText().toString();
		gameTimeUnit = edittextGameTimeUnit.getText().toString();
		gameMoneyUnit = edittextGameMoneyUnit.getText().toString();
		
		// save to the db
		return gic.updateGame( gameTitle, gameIntro, gameTimeUnit, gameMoneyUnit );
	}
	
	/**
	 * Private class to manage CRUD of game information
	 * @author philipr
	 *
	 */
	private class OCGameInformationController extends OCDbController {
		int mGameId;
		
		/**
		 * Class constructor that accepts the context and the game id for CRUD
		 * @param context
		 * @param gameId
		 */
		public OCGameInformationController(Context context, int gameId) {
			super(context);
			
			this.mGameId = gameId;
		}
		
		/**
		 * Retrieves the game's data from the DB to be applied to the interface. 
		 * NB: This should do this as a join, but doing it separately works for now
		 * @return boolean returns false on any error, and details of error reported in
		 * LogCat
		 */
		public boolean retrieveGame() {
			
			if ( !openDatabase(context) ) {
				Log.w("OC", "OCGameInformationEditActivity.OCGameInformationController.retrieveGame() could not open database.");
				return false;
			}
		
			/* Get the stories */
			
			// build the target parameters
			String[] targetStoryColumns = new String[]{ COL_STY_STORY, COL_STY_CONTEXT };
			String targetWhereParams = COL_UNI_PARENT_ID + " = " + mGameId + " AND " + COL_UNI_PARENT_TYPE + " = '" + TYP_GAME + "'";
			// android.database.sqlite.SQLiteException: no such column: game: , 
			// while compiling: SELECT story, context FROM stories WHERE parent_id = 1 AND parent_type = game

			// submit the query
			Cursor gameStories = db.query( TBL_STORIES, targetStoryColumns, targetWhereParams, null, null, null, null);
			
			// now we iterate through the story results and create game entries
			if ( gameStories.moveToFirst() ) {
				do {
					String storyContext = gameStories.getString( gameStories.getColumnIndexOrThrow( COL_STY_CONTEXT ) );
					// push the strings to the parent class per context
					if ( storyContext.contentEquals( CTX_TITLE )) {
						gameTitle = gameStories.getString( gameStories.getColumnIndexOrThrow( COL_STY_STORY ) );
					} else if ( storyContext.contentEquals( CTX_ABOUT )) {
						gameIntro = gameStories.getString( gameStories.getColumnIndexOrThrow( COL_STY_STORY ) );
					}
																	
				} while ( gameStories.moveToNext() );
			} else {
				Log.w("OC", "OCGameInformationEditActivity.OCGameInformationController.retrieveGame() could not retrieve the game's stories");
				return false;
			}
			// close the cursor
			if (gameStories != null && !gameStories.isClosed()) {
				gameStories.close();
			}
			
			/* get the information */
			
			String[] targetInformationColumns = new String[]{ COL_GAM_TIME_UNIT, COL_GAM_MONEY_UNIT };
			String targetInformationParams = COL_UNI_ID + " = " + mGameId;
			Cursor gameInfos = db.query( TBL_GAMES, targetInformationColumns, targetInformationParams, null, null, null, null);
			
			// we should only get one back from the games table, but we run through 
			// multiples if only to keep with the design pattern
			if ( gameInfos.moveToFirst() ) {
				do {
					gameTimeUnit = gameInfos.getString( gameInfos.getColumnIndexOrThrow( COL_GAM_TIME_UNIT ) );
					gameMoneyUnit = gameInfos.getString( gameInfos.getColumnIndexOrThrow( COL_GAM_MONEY_UNIT ) );
					
				} while ( gameInfos.moveToNext() );
			} else {
				Log.w("OC", "OCGameInformationEditActivity.OCGameInformationController.retrieveGame() could not retrieve the game's information");
				return false;
			}
			// close the cursor
			if (gameInfos != null && !gameInfos.isClosed()) {
				gameInfos.close();
			}
			
			closeDatabase();
			
			return true;
		}
		
		/**
		 * Updates the game's data from the parent class to the DB.
		 * @param title
		 * @param intro
		 * @param timeUnit
		 * @param moneyUnit 
		 * @return boolean Returns false on any error, and reports errors to LogCat
		 */
		public boolean updateGame(String title, String intro, String timeUnit, String moneyUnit) {
			
			if ( !openDatabase(context) ) {
				Log.w("OC", "OCGameInformationEditActivity.OCGameInformationController.updateGame() could not open database.");
				return false;
			}
			
			/* Game Units */
			// collect the vals
			ContentValues gameVals = new ContentValues();
			gameVals.put(COL_GAM_TIME_UNIT, gameTimeUnit);
			gameVals.put(COL_GAM_MONEY_UNIT, gameMoneyUnit);
			// collect the WHERE
			String targetInformationParams = COL_UNI_ID + " = " + mGameId;
			// perform the UPDATE
			int gameInfoResult = db.update(TBL_GAMES, gameVals, targetInformationParams, null);
			// check the result
			if ( gameInfoResult <= 0 ) {
				Log.w("OC", "OCDbController: gameInfoResult = -1, aborting game saving");
				return false;
			}
			
			/* Game Stories */
			
			// updateStoryIntoDatabase(String story, String context, int parentID, String parentType, SQLiteDatabase database)
			long titleResult = updateStoryIntoDatabase(gameTitle, CTX_TITLE, gameId, TYP_GAME, db);
			long introResult = updateStoryIntoDatabase(gameIntro, CTX_ABOUT, gameId, TYP_GAME, db);
			if ( titleResult == -1 || introResult == -1 ) {
				Log.w("OC", "OCGameInformationActivity.updateStoryIntoDatabase() title and/or intro could not be saved");
				return false;
			}
			
			closeDatabase();
			return true;
		}
		
	}

	/**
	 * Capture the button clicks to commit to an action
	 */
	public void onClick(View v) {
		switch ( v.getId() ) {
		case R.id.oc_button_game_info_save:
			saveEdits();
			break;
		case R.id.oc_button_game_info_cancel:
			cancelEditing();
			break;
		}
	}
	
	/**
	 * Glue code between the parent activity and the db helper class
	 */
	public void saveEdits() {
		if ( !saveEditsWithError() ) {
			showMessage("The game information could not be saved.");
		}
	}
	
	/**
	 * Moves the user back to the previous activity in the stack without saving any
	 * changes without ceremony
	 */
	public void cancelEditing() {
		finish();
	}

}
