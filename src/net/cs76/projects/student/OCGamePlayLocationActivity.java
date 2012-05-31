/**
 * Philip Regan
 * http://www.oatmealandcoffee.com
 * 
 * 
 * OCGamePlayLocationActivity
 * 
 * Main screen for playing a game.
 * 
 * This is the closest a task actually gets to a class that holds all of the relevant 
 * data. We could have loaded all of tasks as class objects with strong coupling, 
 * but that would have wreaked havoc on memory usage. This activity gets passed a 
 * task id and pulls data from the tables as required so that we are only using what 
 * we need.
 * 
 * Game play is cyclical:
 * (1) Current Location information is displayed to the Player
 * (2) If the Player is at Home, then they can either
 * 		(2.1) Select a Location
 * 			(2.1.1) Get the information to the selected Location
 * 			(2.1.2) Go to (1)
 * 		(2.2) Pass time (which generates income, heals, or any assortment of changes 
 *            so desired)
 * 			(2.2.1) Get the information for Home
 * 			(2.2.2) Go to (1)
 * (3) If the Player is not at Home, then they can either
 * 		(3.1) Select a Location
 * 			(3.1.1) Get the information to the selected Location
 * 			(3.1.2) Go to (1)
 * 		(3.2) Do the Accomplishments in the Location, if available
 * 			(3.2.1) Go to the GamePlayEngine to perform the Accomplishments and apply
 *                  changes to the Player
 *          (3.2.2) Go to (1)
 *   
 * Roadmap
 * X Bind actionable interface elements
 * X Private DBController to get required information
 * 		X Location Stories
 * 		X Location Accomplishments, child tasks (if any)
 * 		X Location Linked Locations, sibling tasks
 * 		X Player
 * X Populate Accomplishments
 * X Populate Linked Locations
 * X Perform Accomplishments (GamePlayEngine)
 * X Go to Linked Location
 * * Subclass Home
 * * Subclass Store
 */
package net.cs76.projects.student;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

/**
 * @author philipr
 *
 */
public class OCGamePlayLocationActivity extends OCCoreActivity implements OnClickListener {
	
	/* CONTEXTS: STORIES */
    
    protected static final String CTX_TITLE = "title";
    protected static final String CTX_ABOUT = "about";
    protected static final String CTX_ACTION = "action";
    protected static final String CTX_WIN_CURRENT = "win_current";
    protected static final String CTX_WIN_PAST = "win_past";
    protected static final String CTX_LOSE_CURRENT = "lose_current";
    protected static final String CTX_LOSE_PAST = "lose_past";

	/* ACTIONABLE INTERFACE ELEMENTS */
	
    protected TextView textViewTitle; // oc_textview_gplocation_title
    protected TextView textViewIntro; // oc_textview_gplocation_intro
    protected TableLayout tableAccomplishments; // oc_tablelayout_gplocation_accomplishments
    protected Button buttonDoAccomplishments; // oc_button_gplocation_do_accomplishments
    protected TableLayout tableLinkedLocations; // oc_tablelayout_gplocation_linked_locations
	
    protected static final int BUTTON_LINKED_LOCATION_ID = 2744526; // arbitrary number
	
	/* GAME INFORMATION */
	
    protected int playerId = -1;
    
	protected int gameId = -1;
	protected int locationId = -1;
	
	protected boolean atHome = false;
	protected boolean inStore = false;
	
	protected String gameTitle = "";
	protected String locationTitle = "";
	protected String locationIntro = "";
	protected int[] locationAccomplishments;
	protected int[] locationLinkedLocations;
	
	/* DATABASE */
	
	protected OCLocationDbController locationDbController;
	protected OCGameEngine gameEngine;
	
	/**
	 * Auto-generated constructor stub
	 */
	public OCGamePlayLocationActivity() {
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
        setContentView( R.layout.oc_game_play_location );
        
        // set up the db controller
        locationDbController = new OCLocationDbController( this );
        
        // get the necessary information from the intent so we know what we are grabbing
        gameId = intent.getIntExtra( INTENT_GAME_ID , INTENT_GAME_ID_DEFAULT );
        locationId = intent.getIntExtra( INTENT_TASK_ID, INTENT_GAME_ID_DEFAULT );
        gameTitle = intent.getStringExtra( INTENT_GAME_TITLE );
        
        // we need to sort out whether this is the start of a game or if we have been
        // pushed to a different location
        
        if ( locationId == -1 ) {
        	// get the home index for the location and set it to the location id 
        	// for later use
        	
        	locationId = locationDbController.getHomeIdForGame( gameId );
        	atHome = true;
        	inStore = false;
        }
        
        // get the rest of the game information we need to play
        playerId = locationDbController.getPlayerIdForGame( gameId );
        
        // get the location data
        locationTitle = locationDbController.getStory( locationId, TYP_TASK, CTX_TITLE );
        locationIntro = locationDbController.getStory( locationId, TYP_TASK, CTX_ABOUT );
        locationAccomplishments = locationDbController.getAccomplishmentIds( locationId );
    	locationLinkedLocations = locationDbController.getLinkedLocationIds( locationId );
        
        // bind the actionable interface elements to the class
        textViewTitle = (TextView) findViewById( R.id.oc_textview_gplocation_title );
        textViewIntro = (TextView) findViewById( R.id.oc_textview_gplocation_intro );
        tableAccomplishments = (TableLayout) findViewById( R.id.oc_tablelayout_gplocation_accomplishments );
        buttonDoAccomplishments = (Button) findViewById( R.id.oc_button_gplocation_do_accomplishments );
        tableLinkedLocations = (TableLayout) findViewById( R.id.oc_tablelayout_gplocation_linked_locations );
        
        // push the location information to the interface
        textViewTitle.setText( locationTitle );
        textViewIntro.setText( locationIntro );
        
        // push the accomplishments to the interface
        if ( locationAccomplishments != null ) {
        	int lastAccomplishment = locationAccomplishments.length;
        	for ( int i = 0 ; i < lastAccomplishment ; i++ ) {
        		if ( locationAccomplishments[i] != -1 ) {
        			
        			// create a row, textview to place in the row, text to place in the textview
    	        	TableRow titleRow = new TableRow( this );
    	        	titleRow.setLayoutParams( new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT ) );
    	        	TextView titleTextView = new TextView( this );
    	        	titleTextView.setText( 	locationDbController.getStory( locationAccomplishments[i], TYP_TASK, CTX_TITLE ) );
    	        	titleTextView.setTextSize( 18f );
    	        	titleRow.addView( titleTextView );
    	        	
    	        	TableRow aboutRow = new TableRow( this );
    	        	aboutRow.setLayoutParams( new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT ) );
    	        	TextView aboutTextView = new TextView( this );
    	        	aboutTextView.setText( locationDbController.getStory( locationAccomplishments[i], TYP_TASK, CTX_ABOUT ) );
    	        	aboutRow.addView( aboutTextView );
    	        	
    	        	// add the rows to the accomplishment table
    	        	tableAccomplishments.addView( titleRow, new TableLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT ));
    	        	tableAccomplishments.addView( aboutRow, new TableLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT ));
        		}
        	}
        	
        	// set the accomplishments button to capture clicks here
            buttonDoAccomplishments.setOnClickListener( this );
            
        } else {
        	// hide the button since there is nothing else to do here
        	buttonDoAccomplishments.setVisibility( Button.INVISIBLE );
        }
        
        // push the linked locations to the interface
        if ( locationLinkedLocations != null ) {
	        int lastLinkedLocation = locationLinkedLocations.length;
	        for ( int i = 0 ; i < lastLinkedLocation ; i++ ) {
	        	if ( locationLinkedLocations[0] != -1 ) {
	        		// create a row
		        	TableRow row = new TableRow( this );
		        	row.setLayoutParams( new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT ) );
		            // add a button to the row
		        	Button button = new Button( this );
		        	// set the ID so we can identify it later
		        	button.setId( BUTTON_LINKED_LOCATION_ID );
		        	// set the title of the linked location to the button
		        	button.setText( locationDbController.getStory( locationLinkedLocations[i], TYP_TASK, CTX_TITLE) );
		            // set the linked location id as a tag to the button
		        	OCTaskPayload payload = new OCTaskPayload();
		        	payload.aId = locationLinkedLocations[i];
		        	button.setTag( payload );
		        	// set this class to capture the press
		        	button.setOnClickListener( this );
		            // add the button to the row
		        	row.addView( button );
		        	// add the row to the linked location table
		        	tableLinkedLocations.addView( row, new TableLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT ));
	        	}
	        }
        } else {
        	// something bad happened we should know about, but for now this just 
        	// forces an empty list
        }

        // start up the game engine
        gameEngine = new OCGameEngine( this );
        
        // set the title so the user has confirmation
        setTitle( gameTitle + ": " + locationTitle );
	}
	
	/**
	 * Captures button presses and initiates actions according to the button pressed
	 */
	public void onClick(View v) {
		
		switch ( v.getId() ) {
		
		case R.id.oc_button_gplocation_do_accomplishments:
			// let's play
			doTasks();
			break;
			
		case BUTTON_LINKED_LOCATION_ID:
			// let's play somewhere else
			Intent targetIntent = new Intent( getBaseContext(), OCGamePlayLocationActivity.class );
			targetIntent.putExtra( INTENT_GAME_ID, gameId );
			targetIntent.putExtra( INTENT_GAME_TITLE, gameTitle );
			// set the target location id to the intent
			OCTaskPayload payload = (OCTaskPayload) v.getTag();
			targetIntent.putExtra( INTENT_TASK_ID, payload.aId );
			// off to the next location
			startActivity( targetIntent );
			break;
			
		default:
			// this is something else we are not supporting yet but maybe in the future
			break;
		}
		
	}
	
	/**
	 * Manages interaction with the Game Engine when the user decides to try the
	 * Locations Accomplishments
	 */
	public void doTasks() {
		showMessage( "Roll 1d20 and don't forget your bonuses." );
		if ( gameEngine.doTask( playerId , locationAccomplishments ) ) {
			showMessage( "You won!" );
		} else {
			showMessage( "You lost!" );
		}
	}
	
	/**
	 * Private class that provides the link between this class and the databases
	 * @author philipr
	 *
	 */
	
	private class OCLocationDbController extends OCDbController {
		/**
		 * Auto-generated constructor stub
		 * @param context
		 */
		public OCLocationDbController(Context context) {
			super(context);
			// Auto-generated constructor stub
		}
		
		/**
		 * Convenience method for getting the home id for a given game.
		 * 
		 * @param gameId
		 * @return int
		 */
		public int getHomeIdForGame( int gameId ) {
			// variable names and patterns are as generic as possible for each of
			// implementation of other methods in this class, thus we use moveToNext()
			// even if we have only one result being returned.
			
			// set a default so we don't return nil or something completely unexpected
			int targetValue = -1;
			
			// open the db
			if ( !openDatabase(context) ) {
				Log.w("OC", "OCGamePlayLocationActivity.OCLocationDbController.getHomeIdForGame() could not open database.");
				closeDatabase();
				return targetValue;
			}
			
			// construct the query statement
			String[] targetColumns = new String[]{ COL_GAM_HOME };
			String targetWhere = COL_UNI_ID + " = " + gameId;
			// punch it
			Cursor cursor = db.query( TBL_GAMES, targetColumns, targetWhere, null, null, null, null);
			
			// check to see if we got anything
			if ( cursor.moveToFirst() ) {
				do {
					targetValue = cursor.getInt( cursor.getColumnIndexOrThrow( COL_GAM_HOME ) );
				} while ( cursor.moveToNext() );
			} else {
				Log.w("OC", "OCGamePlayLocationActivity.OCLocationDbController.getHomeIdForGame() could not retrieve the home id for game id" + gameId );
				closeDatabase();
				return targetValue;
			}
			// close the cursor
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			
			// close the db
			closeDatabase();
			
			// return whatever we found and let the caller sort it all out
			return targetValue;
		}
		
		/**
		 * Convenience method for getting the player id for a given game.
		 * 
		 * NB: This and getHomeIdForGame could probably be abstracted to something
		 * like getSingleValueFromTable( String targetColumn, int parentId, String parentType, String targetTable )
		 * @param gameId
		 * @return int
		 */
		public int getPlayerIdForGame( int gameId ) {
			int targetValue = -1;
			
			if ( !openDatabase(context) ) {
				Log.w("OC", "OCGamePlayLocationActivity.OCLocationDbController.getHomeIdForGame() could not open database.");
				closeDatabase();
				return targetValue;
			}

			String[] targetColumns = new String[]{ COL_UNI_ID };
			String targetWhere = 	COL_UNI_PARENT_ID + " = " + gameId + " AND " +
									COL_UNI_PARENT_TYPE + " = '" + TYP_GAME + "'";
			Cursor cursor = db.query( TBL_PLAYERS, targetColumns, targetWhere, null, null, null, null);
			
			if ( cursor.moveToFirst() ) {
				do {
					targetValue = cursor.getInt( cursor.getColumnIndexOrThrow( COL_UNI_ID ) );
				} while ( cursor.moveToNext() );
			} else {
				Log.w("OC", "OCGamePlayLocationActivity.OCLocationDbController.getHomeIdForGame() could not retrieve the player id for game id" + gameId );
				closeDatabase();
				return targetValue;
			}
			// close the cursor
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			
			closeDatabase();
			
			return targetValue;
		}
		
		/**
		 * Gets a story for a given context
		 * @param storyContext
		 * @return
		 */
		public String getStory( int parentId, String parentType, String storyContext ) {
			String targetValue = "";

			if ( !openDatabase(context) ) {
				Log.w("OC", "OCGamePlayLocationActivity.OCLocationDbController.getStory() could not open database.");
				closeDatabase();
				return targetValue;
			}
			
			String[] targetColumns = new String[]{ COL_STY_STORY };
			String targetWhere = 	COL_UNI_PARENT_ID + " = " + parentId + " AND " + 
									COL_UNI_PARENT_TYPE + " = '" + parentType + "'" + " AND " +
									COL_STY_CONTEXT + " = '" + storyContext + "'";
			Cursor cursor = db.query( TBL_STORIES, targetColumns, targetWhere, null, null, null, null);
			
			if ( cursor.moveToFirst() ) {
				do {
					targetValue = cursor.getString( cursor.getColumnIndexOrThrow( COL_STY_STORY ) );
				} while ( cursor.moveToNext() );
			} else {
				Log.w("OC", "OCGamePlayLocationActivity.OCLocationDbController.getStory() could not retrieve story");
				closeDatabase();
				return targetValue;
			}
			// close the cursor
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			
			closeDatabase();
			
			return targetValue;
		}
		
		/**
		 * Returns a list of tasks for accomplishments
		 * @return
		 */
		public int[] getAccomplishmentIds( int taskId ) {
			int targetValues[] = null;
			
			if ( !openDatabase(context) ) {
				Log.w("OC", "OCGamePlayLocationActivity.OCLocationDbController.getAccomplishmentIds() could not open database.");
				closeDatabase();
				return targetValues;
			}
			
			String[] targetColumns = new String[]{ COL_UNI_ID };
			String targetWhere = 	COL_UNI_PARENT_ID + " = " + taskId + " AND " +
									COL_UNI_PARENT_TYPE + " = '" + TYP_TASK + "'";
			Cursor cursor = db.query( TBL_TASKS, targetColumns, targetWhere, null, null, null, null);
			
			if ( cursor.moveToFirst() ) {
				
				targetValues = new int[ cursor.getCount() ];
				
				do {
					targetValues[ cursor.getPosition() ] = cursor.getInt( cursor.getColumnIndexOrThrow( COL_UNI_ID ) );
				} while ( cursor.moveToNext() );
			} else {
				Log.w("OC", "OCGamePlayLocationActivity.OCLocationDbController.getAccomplishmentIds() could not retrieve the player id for game id" + gameId );
				closeDatabase();
				return targetValues;
			}
			// close the cursor
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			
			closeDatabase();
			
			return targetValues;
		}
		
		/**
		 * Returns an array of all the locations that are linked to this one
		 * @param taskId
		 * @return
		 */
		
		public int[] getLinkedLocationIds( int taskId ) {
			
			/*
			 * First we have to get the string value for the linked locations, which 
			 * is a comma-delimited String array since SQLite does not respect arrays
			 * (as least that was what I gathered from the docs). Once we have the 
			 * string, we then convert the string into an array of ints.
			 */
			String targetValue = ""; // what is stored in the db
			int targetValues[] = null; // what is returned to the caller

			if ( !openDatabase(context) ) {
				Log.w("OC", "OCGamePlayLocationActivity.OCLocationDbController.getStory() could not open database.");
				closeDatabase();
				return targetValues;
			}
			
			String[] targetColumns = new String[]{ COL_TSK_LINKED };
			String targetWhere = 	COL_UNI_ID + " = " + taskId;
			Cursor cursor = db.query( TBL_TASKS, targetColumns, targetWhere, null, null, null, null);
			
			if ( cursor.moveToFirst() ) {
				do {
					targetValue = cursor.getString( cursor.getColumnIndexOrThrow( COL_TSK_LINKED ) );
				} while ( cursor.moveToNext() );
			} else {
				Log.w("OC", "OCGamePlayLocationActivity.OCLocationDbController.getLinkedLocationIds() could not retrieve linked locations for taskId " + taskId );
				closeDatabase();
				return targetValues;
			}
			// close the cursor
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			
			closeDatabase();
			
			if ( targetValue != null ) {
				// split the targetValue into an array with the delimiter
				String[] targetValueStrings = targetValue.split( "," );
				// get the length so we can initialize the array
				int ubound = targetValueStrings.length;
				targetValues = new int[ ubound ];
				// for each String, get the int value
				for ( int i = 0 ; i < ubound ; i++ ) {
					targetValues[i] = Integer.parseInt( targetValueStrings[i] );
				}
			} else {
				
				targetValues = new int[ 1 ];
				targetValues[0] = -1;
				
			}
			return targetValues;
		}
	}
	
	/**
	 * Private class use to help manage information for linked tasks
	 * @author philipr
	 *
	 */
	private class OCTaskPayload {
		int aId;
	}

}
