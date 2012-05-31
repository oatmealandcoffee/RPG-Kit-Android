/**
 * Philip Regan
 * http://www.oatmealandcoffee.com
 * 
 * 
 * OCDbController
 * 
 * Game storage is entirely SQLite-driven. There are different tables for different 
 * game objects, but they have many commonalities that need to be shared.
 * 
 * This controller class is intended to be abstract, each different type of activity 
 * will have their own private controller for database interaction. Contained in 
 * here is the table management primarily so we don't have to duplicate it. The 
 * subclasses handle the CRUD methods as required by its paired activity.
 * 
 * Opening and closing the database happens at the controller level on an as-needed
 * basis. This is more granular than handling opening and closing in the parent
 * activity's life-cycle, but it also ensures we don't get IllegalStateExceptions
 * due to the DB being opened but not closed between user actions
 * 
 */

package net.cs76.projects.student;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class OCDbController {
	
	/**
	 * DATABASE CONSTANTS
	 * 
	 * NB: Columns need to be universally unique so we avoid collisions and/or ambiguity 
	 * during joins.
	 */
	
	protected static final int DATABASE_VERSION = 1;
    
    /* DATABASE */
    
	protected static final String DB_NAME = "ocrpgkit.db";
    
    /* TABLES */
    
    protected static final String TBL_GAMES = "games";
    protected static final String TBL_PLAYERS = "players";
    protected static final String TBL_TASKS = "tasks";
    protected static final String TBL_STORIES = "stories";
    protected static final String TBL_EQUIPMENT = "equipment";
    protected static final String TBL_WALLETS = "wallets";
    protected static final String TBL_ATTRIBUTES = "attributes";
    
    /* COLUMNS: UNIVERSAL */
    
    protected static final String COL_UNI_ID = "_id";
    protected static final String COL_UNI_PARENT_ID = "parent_id";
    protected static final String COL_UNI_PARENT_TYPE = "parent_type";
    
    /* COLUMNS: GAME */
    
    protected static final String COL_GAM_PLAYER = "player";
    protected static final String COL_GAM_HOME = "home";
    protected static final String COL_GAM_TIME_UNIT = "time_unit";
    protected static final String COL_GAM_TIME_UNITS_PASSED = "time_units_passed";
    protected static final String COL_GAM_MONEY_UNIT = "money_unit";
    
    /* COLUMNS: PLAYERS */
    
    protected static final String COL_PLY_ELO = "elo";
    protected static final String COL_PLY_LOCATION = "location";
    
    /* COLUMNS: TASKS */
    
    protected static final String COL_TSK_TYPE = "task_type";
    protected static final String COL_TSK_LINKED = "tasks_linked";
    
    /* COLUMNS: STORIES */
    
    protected static final String COL_STY_STORY = "story";
    protected static final String COL_STY_CONTEXT = "context";
    protected static final String COL_STY_GAME_ID = "game_id";
    
    /* COLUMNS: EQUIPMENT */
    
    protected static final String COL_EQP_PURCHASE_VALUE = "purchase_value";
    protected static final String COL_EQP_SALE_VALUE = "sale_value";
    
    /* COLUMNS: WALLETS */
    
    protected static final String COL_WLT_AMOUNT = "amount";
    
    /* COLUMNS: ATTRIBUTES */
    
    protected static final String COL_ATR_VALUE = "value";
    protected static final String COL_ATR_CONTEXT = "context";
    
    /* CONTEXTS: ATTRIBUTES, WALLETS */
    
    protected static final String CTX_REWARD = "reward";
    protected static final String CTX_PENALTY = "penalty";
    protected static final String CTX_STAT = "stat";
    
    /* CONTEXTS: STORIES */
    
    protected static final String CTX_TITLE = "title";
    protected static final String CTX_ABOUT = "about";
    protected static final String CTX_ACTION = "action";
    protected static final String CTX_WIN_CURRENT = "win_current";
    protected static final String CTX_WIN_PAST = "win_past";
    protected static final String CTX_LOSE_CURRENT = "lose_current";
    protected static final String CTX_LOSE_PAST = "lose_past";
    
    /* CONTEXT: TYPES */
    
    protected static final String TYP_GAME = "game";
    protected static final String TYP_PLAYER = "player";
    protected static final String TYP_TASK = "task";
    protected static final String TYP_STORY = "story";
    protected static final String TYP_EQUIPMENT = "equipment";
    protected static final String TYP_WALLET = "wallet";
    protected static final String TYP_ATTRIBUTES = "attribute";
    
    /* SAMPLE: MARKERS */
    private static final int MARKER_RACING = 1; // auto racing but without those pesky graphics
    private static final int MARKER_OFFICE = 2; // every day living, just to show anything can be done
    //private static final int MARKER_FANTASY = 3; // warmed-over, fantasy buffoonery
    //private static final int MARKER_SCIFI = 4; // homage to the comfortable hokiness that is classic sci-fi
    
	
	// needed objects for all classes
    protected Context context;
    protected SQLiteDatabase db;
    protected SQLiteOpenHelper dbHelper;
	
	/**
	 * DATABASE TABLE CREATION STRINGS
	 */
	
	private static final String GAME_TABLE_CREATE =
			"create table " + TBL_GAMES + " (" + 
			COL_UNI_ID + " integer primary key autoincrement, " + 
			COL_UNI_PARENT_ID + " integer, " +
			COL_UNI_PARENT_TYPE + " text, " +
			COL_GAM_PLAYER + " integer, " +
			COL_GAM_HOME + " integer, "  +
			COL_GAM_MONEY_UNIT + " text, " + 
			COL_GAM_TIME_UNIT + " text, " +
			COL_GAM_TIME_UNITS_PASSED + " integer);";
	
	private static final String PLAYER_TABLE_CREATE =
			"create table " + TBL_PLAYERS + " (" + 
			COL_UNI_ID + " integer primary key autoincrement, " + 
			COL_UNI_PARENT_ID + " integer, " +
			COL_UNI_PARENT_TYPE + " text, " +
			COL_PLY_ELO + " integer, " +
			COL_PLY_LOCATION + " integer);";
	
	private static final String TASK_TABLE_CREATE =
			"create table " + TBL_TASKS + " (" + 
			COL_UNI_ID + " integer primary key autoincrement, " + 
			COL_UNI_PARENT_ID + " integer, " +
			COL_UNI_PARENT_TYPE + " text, " +
			COL_TSK_TYPE + " text, " + 
			COL_TSK_LINKED + " text);";
	
	private static final String STORY_TABLE_CREATE =
			"create table " + TBL_STORIES + " (" + 
			COL_UNI_ID + " integer primary key autoincrement, " + 
			COL_UNI_PARENT_ID + " integer, " +
			COL_UNI_PARENT_TYPE + " text, " +
			COL_STY_STORY + " text, " +
			COL_STY_CONTEXT + " text);";
	
	private static final String EQUIPMENT_TABLE_CREATE =
			"create table " + TBL_EQUIPMENT + " (" + 
			COL_UNI_ID + " integer primary key autoincrement, " + 
			COL_UNI_PARENT_ID + " integer, " +
			COL_UNI_PARENT_TYPE + " text, " +
			COL_EQP_PURCHASE_VALUE + " integer, " +
			COL_EQP_SALE_VALUE + " integer);";
	
	private static final String ATTRIBUTE_TABLE_CREATE =
			"create table " + TBL_ATTRIBUTES + " (" + 
			COL_UNI_ID + " integer primary key autoincrement, " + 
			COL_UNI_PARENT_ID + " integer, " +
			COL_UNI_PARENT_TYPE + " text, " +
			COL_ATR_VALUE + " integer, " +
			COL_ATR_CONTEXT + " text);";
	
	private static final String WALLET_TABLE_CREATE = 
			"create table " + TBL_WALLETS + " (" + 
			COL_UNI_ID + " integer primary key autoincrement, " + 
			COL_UNI_PARENT_ID + " integer, " +
			COL_UNI_PARENT_TYPE + " text, " +
			COL_ATR_VALUE + " integer, " +
			COL_ATR_CONTEXT + " text);";
	
	/**
	 * Class constructor
	 * @param context
	 */
	public OCDbController(Context context) {
		//Log.i("OC", "OCDbController.OCDbController()");
		this.context = context;
		
		/* 
		 * While testing and debugging, nuke the database from orbit.
		 * "It's the only way to be sure." -- Ripley
		 * With great power comes great responsibility
		 */
		/*
		context.deleteDatabase( DB_NAME );
		*/
		
		// Okay. Let's get this show on the road.
		openDatabase(this.context);
		
		// close the DB to avoid any memory leak and IllegalStateException errors
		closeDatabase();
		
	}
	
	public boolean openDatabase(Context context) {
		
		// create the db and tables with the helper class
		dbHelper = new OCSqliteHelper(context); 
		
		// bind the db to this class
		this.db = dbHelper.getWritableDatabase();  

		if ( this.db == null ) {
			Log.w("OC", "OCDbController.openDatabase() could not getWritableDatabase. Aborting db opening");
			return false;
		}
		
		return true;
	}
	
	public void closeDatabase() {
		dbHelper.close();
	}
	
	/** 
	 * SQLiteOpenHelper extension to manage database creation and upgrading
	 * @author philipr
	 *
	 */
	private static class OCSqliteHelper extends SQLiteOpenHelper {
        
		/**
		 * Class constructor
		 * @param context
		 */
		public OCSqliteHelper(Context context) {
			// instantiate a SQLiteOpenHelper by passing it
			// the context, the database's name, a CursorFactory
			// (null by default), and the database version.			
			super(context, DB_NAME, null, DATABASE_VERSION);
		}
		
		/**
		 * called by the parent class when a DB doesn't exist. Sample game creation
		 * is called from here so we be sure that everything works properly and the
		 * user has something to do each time they start up the application.
		 * @param database
		 */
		@Override
		public void onCreate(SQLiteDatabase database) {
			//Log.i("OC", "OCDbController.OCSqliteHelper.onCreate()");
			
			database.execSQL(GAME_TABLE_CREATE);
			database.execSQL(PLAYER_TABLE_CREATE);
			database.execSQL(TASK_TABLE_CREATE);
			database.execSQL(STORY_TABLE_CREATE);
			database.execSQL(EQUIPMENT_TABLE_CREATE);
			database.execSQL(ATTRIBUTE_TABLE_CREATE);
			database.execSQL(WALLET_TABLE_CREATE);
			
			// create multiple samples to ensure we are parsing relevant data properly
			populateDefaults(database, MARKER_RACING);
			populateDefaults(database, MARKER_OFFICE);
			
		}
        
		/**
		 * called by the parent when a DB needs to be upgraded
		 * @param db
		 * @param oldVersion
		 * @param newVersion
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(OCSqliteHelper.class.getName(),
                  "Upgrading database from version " + oldVersion + " to "
                  + newVersion + ", which will destroy all old data");
			
			/* cache data */		
			
			db.execSQL("DROP TABLE IF EXISTS " + TBL_GAMES);
			db.execSQL("DROP TABLE IF EXISTS " + TBL_PLAYERS);
			db.execSQL("DROP TABLE IF EXISTS " + TBL_TASKS);
			db.execSQL("DROP TABLE IF EXISTS " + TBL_STORIES);
			db.execSQL("DROP TABLE IF EXISTS " + TBL_EQUIPMENT);
			db.execSQL("DROP TABLE IF EXISTS " + TBL_ATTRIBUTES);
			db.execSQL("DROP TABLE IF EXISTS " + TBL_WALLETS);
			
			onCreate(db);
			
			/* migrate data */
		}
		
		/**
		 * Method to help manage error-checking and database errors if any.
		 * @param database
		 * @param marker
		 */
		private void populateDefaults(SQLiteDatabase database, int marker) {
			//Log.i("OC", "OCDbController.OCSqliteHelper.populateDefaults()");
			
			if ( !populateSampleGame(database, marker) ) {
				Log.w("OC", "OCDbController: populateSampleGame == false, sample population aborted");
			}
			
		}
		
		private boolean populateSampleGame(SQLiteDatabase database, int marker) {
			//Log.i("OC", "OCDbController.OCSqliteHelper.populateSampleGame()");
			
			/* CREATE THE GAME */
			
			ContentValues gameVals = new ContentValues();
			
			switch ( marker ) {
			case MARKER_RACING:
				gameVals.put(COL_GAM_TIME_UNIT, "day");
				gameVals.put(COL_GAM_MONEY_UNIT, "credit");
				break;
			case MARKER_OFFICE:
				gameVals.put(COL_GAM_TIME_UNIT, "hour");
				gameVals.put(COL_GAM_MONEY_UNIT, "dollar");
				break;
			}
			gameVals.put(COL_GAM_TIME_UNITS_PASSED, 0);
			
			// gameID is the parent id for later objects. The parent id is part of 
			// a given content's "address" to know which content to CRUD
			long gameId = database.insert(TBL_GAMES, null, gameVals);
			
			if ( gameId == -1 ) {
				Log.w("OC", "OCDbController: gameId = -1, aborting sample population");
				return false;
			}
			
			// create the game stories
			long titleId = -1;
			long aboutId = -1;
			
			switch ( marker ) {
			case MARKER_RACING:
				titleId = insertStoryIntoDatabase("A Day at the Races", CTX_TITLE, (int)gameId, TYP_GAME, database);
				aboutId = insertStoryIntoDatabase("Like Gran Turismo, but without the hassle of actually driving the car.", CTX_ABOUT, (int)gameId, TYP_GAME, database);
				break;
			case MARKER_OFFICE:
				titleId = insertStoryIntoDatabase("Back At The Office", CTX_TITLE, (int)gameId, TYP_GAME, database);
				aboutId = insertStoryIntoDatabase("Meanwhile, in the real world, this week is your turn in the Office Donut Club.", CTX_ABOUT, (int)gameId, TYP_GAME, database);
				break;
			}
			
			if ( titleId == -1 || aboutId == -1 ) {
				Log.w("OC", "OCDbController: sample game stories could not be inserted, aborting sample population");
				return false;
			}
			
			/* CREATE SOME LOCATIONS (GAME-LEVEL, SIBLING TASKS) AND ACCOMPLISHMENTS (TASK_LEVEL, CHILD TASKS) */
			
			// establish some markers for easy grabbing of content on actionable objects
			int lastLoc = 4;
			// store the ids for the locations to manage linking (linking is hard)
			long[] locationIds = new long[ lastLoc ];
			
			final int homeIndex = 0;
			final int storeIndex = 1;
			final int basicIndex = 2;
			final int bossIndex = 3;
			
			for ( int loc = 0 ; loc < lastLoc ; loc ++) {
				
				// create a location
				ContentValues locationValues = new ContentValues();
				locationValues.put(COL_UNI_PARENT_ID, (int)gameId);
				locationValues.put(COL_UNI_PARENT_TYPE, TYP_GAME);
				long locationId = database.insert(TBL_TASKS, null, locationValues);
				
				if ( locationId == -1 ) {
					Log.w("OC", "OCDbController: locationId" + loc + " = -1, aborting sample population");
					return false;
				}
				
				// save the index for linking
				locationIds[loc] = locationId;
				
				// record the first location created as the player's home and set it in the game
				if ( loc == homeIndex ) {					
					ContentValues homeVals = new ContentValues();
					homeVals.put(COL_GAM_HOME, locationId);
					String targetGameParams = COL_UNI_ID + " = " + gameId;
					int homeInfoResult = database.update(TBL_GAMES, homeVals, targetGameParams, null);
					if ( homeInfoResult <= 0 ) {
						Log.w("OC", "OCDbController.populateSampleGame(): home id not set for game");
					}
					
				}
				
				// insert the core location stories and then build and associate 
				// an accomplishment for each where needed
				long locTitleId = -1;
				long locAboutId = -1;
				long accTitleId = -1; // we don't have a use for this yet
				long accAboutId = -1; // same as above
				
				ContentValues accomplishmentVals = new ContentValues();
				accomplishmentVals.put( COL_UNI_PARENT_ID, (int)locationId );
				accomplishmentVals.put( COL_UNI_PARENT_TYPE, TYP_TASK );
				long accomplishmentId = database.insert( TBL_TASKS, null, accomplishmentVals );
				
				switch ( marker ) {
				case MARKER_RACING:
					switch ( loc ) {
					case 0:
						// home task
						locTitleId = insertStoryIntoDatabase("Moe's Garage", CTX_TITLE, (int)locationId, TYP_TASK, database);
						locAboutId = insertStoryIntoDatabase("Home of the world famous 3STG Racing Team.", CTX_ABOUT, (int)locationId, TYP_TASK, database);
						// home tasks do nothing except pass time to apply benefits and select other locations to go to
						break;
					case 1:
						// store task
						locTitleId = insertStoryIntoDatabase("Larry's Dealership and Parts", CTX_TITLE, (int)locationId, TYP_TASK, database);
						locAboutId = insertStoryIntoDatabase("Everything needed to race all the time.", CTX_ABOUT, (int)locationId, TYP_TASK, database);
						// stores get equipment to purchase handled in the game play location activity
						break;
					case 2:
						// basic task
						locTitleId = insertStoryIntoDatabase("R Classic", CTX_TITLE, (int)locationId, TYP_TASK, database);
						locAboutId = insertStoryIntoDatabase("Drive against the best (and the worst) in R-Series cars. Sponsered by Bud Lite.", CTX_ABOUT, (int)locationId, TYP_TASK, database);
						// accomplishment 
						accTitleId = insertStoryIntoDatabase("Road course, 2.3 miles, 100 laps", CTX_TITLE, (int)accomplishmentId, TYP_TASK, database);
						accAboutId = insertStoryIntoDatabase("1st place prize: Alfa Romeo GT 3.2 V6 24V '04", CTX_ABOUT, (int)accomplishmentId, TYP_TASK, database);
						break;
					case 3:
						// boss task
						locTitleId = insertStoryIntoDatabase("GT Classic", CTX_TITLE, (int)locationId, TYP_TASK, database);
						locAboutId = insertStoryIntoDatabase("Only the best get to drive GT Supercars, and only the best make it into the GT Classic. Sponsered by Audi.", CTX_ABOUT, (int)locationId, TYP_TASK, database);
						// accomplishment 
						accTitleId = insertStoryIntoDatabase("Super speedway, 3.1 miles, 200 laps", CTX_TITLE, (int)accomplishmentId, TYP_TASK, database);
						accAboutId = insertStoryIntoDatabase("1st place prize: $25,000", CTX_ABOUT, (int)accomplishmentId, TYP_TASK, database);
						break;
					default: 
						// task
						locTitleId = insertStoryIntoDatabase("Race " + marker + " " + loc + " Circuit", CTX_TITLE, (int)locationId, TYP_TASK, database);
						locAboutId = insertStoryIntoDatabase("Race " + marker + " " + loc + " is all about speed and adrenaline (if you were actually driving the car).", CTX_ABOUT, (int)locationId, TYP_TASK, database);
						// accomplishment
						accTitleId = insertStoryIntoDatabase("Oval track, 1.5 miles, 50", CTX_TITLE, (int)accomplishmentId, TYP_TASK, database);
						accAboutId = insertStoryIntoDatabase("1st place prize: $5,000", CTX_ABOUT, (int)accomplishmentId, TYP_TASK, database);
					}
					break;
				case MARKER_OFFICE:
					switch ( loc ) {
					case 0:
						// home task
						locTitleId = insertStoryIntoDatabase("Home Sweet Home", CTX_TITLE, (int)locationId, TYP_TASK, database);
						locAboutId = insertStoryIntoDatabase("The car is in the garage warming up for the commute.", CTX_ABOUT, (int)locationId, TYP_TASK, database);
						break;
					case 1:
						// store task
						locTitleId = insertStoryIntoDatabase("The Donut Store", CTX_TITLE, (int)locationId, TYP_TASK, database);
						locAboutId = insertStoryIntoDatabase("America's favorite all-day, everyday stop for coffee and baked goods. Lovingly baked in their central processing facility in Newark, New Jersey.", CTX_ABOUT, (int)locationId, TYP_TASK, database);
						break;
					case 2:
						// basic task
						locTitleId = insertStoryIntoDatabase("Le Donument", CTX_TITLE, (int)locationId, TYP_TASK, database);
						locAboutId = insertStoryIntoDatabase("Purveyors of luxury donuts. Handcrafted by specially-trained chefs with hoity European accents. Try their new, groundbreaking drinkÑGazŽifiŽe CafŽÑperfectly paired with their Le DŽmodŽ Donette.", CTX_ABOUT, (int)locationId, TYP_TASK, database);
						// accomplishment
						accTitleId = insertStoryIntoDatabase("Treize ˆ la Douzaine VariŽtŽ Raffle", CTX_TITLE, (int)accomplishmentId, TYP_TASK, database);
						accAboutId = insertStoryIntoDatabase("Win 1 Treize ˆ la Douzaine VariŽtŽ, valued at $500. Free entry with purchase of large GazŽifiŽe CafŽ (now only $50)", CTX_ABOUT, (int)accomplishmentId, TYP_TASK, database);
						break;
					case 3:
						// boss task
						locTitleId = insertStoryIntoDatabase("Innotech", CTX_TITLE, (int)locationId, TYP_TASK, database);
						locAboutId = insertStoryIntoDatabase("If you don't have the donuts, then don't bother showing up.", CTX_ABOUT, (int)locationId, TYP_TASK, database);
						// accomplishment
						accTitleId = insertStoryIntoDatabase("TPS Reports and Tetris", CTX_TITLE, (int)accomplishmentId, TYP_TASK, database);
						accAboutId = insertStoryIntoDatabase("These TPS reports really intrude on Tetris time. Do NOT forget the cover sheet as clearly stated in the memo.", CTX_ABOUT, (int)accomplishmentId, TYP_TASK, database);
						break;
					default: 
						locTitleId = insertStoryIntoDatabase("Chotchkie's Location #" + marker + "-" + loc , CTX_TITLE, (int)locationId, TYP_TASK, database);
						locAboutId = insertStoryIntoDatabase("All you ever really want is the coffee because you think GazŽifiŽe CafŽ is just wrong.", CTX_ABOUT, (int)accomplishmentId, TYP_TASK, database);
					}
					break;
				}
				
				if ( locTitleId == -1 || locAboutId == -1 ) {
					Log.w("OC", "OCDbController: sample location stories could not be inserted, aborting sample population");
					return false;
				}
				
			}
			
			/* RECORD LOCATION LINKS */
			
			/* 
			 * Linking is hard.
			 * 
			 * We have to run through all of the locations again now that we have
			 * all of their locations so that we can add in the sample links so 
			 * that locations are linked as so...
			 * 
			 *   1
			 *  /|\
			 * 0 | 3
			 *  \|/
			 *   2
			 */
			
			for ( int loc = 0 ; loc < lastLoc ; loc++ ) {
				
				String links = "";
				String delim = ",";
				
				switch ( loc ) {
				case homeIndex:
					links = locationIds[storeIndex] + delim + locationIds[basicIndex]; // "1,2"
					break;
				case storeIndex:
					links = locationIds[homeIndex] + delim + locationIds[basicIndex] + delim + locationIds[bossIndex]; // "0,2,3"
					break;
				case basicIndex:
					links = locationIds[homeIndex] + delim + locationIds[storeIndex] + delim + locationIds[bossIndex]; // "0,1,3"
					break;
				case bossIndex:
					links = locationIds[storeIndex] + delim + locationIds[basicIndex]; // "1,2"
					break;
				default:
					links = locationIds[homeIndex] + ""; // some unlinked locs for testing later
					break;
				}
			
				// update the task with the links
				
				ContentValues linkVals = new ContentValues();
				linkVals.put( COL_TSK_LINKED, links );
				String whereClause = COL_UNI_ID + " = " + locationIds[loc];
				int affectedRows = database.update(TBL_TASKS, linkVals, whereClause, null);
				
				if ( affectedRows <= 0 ) {
					Log.w("OC", "OCDbController: affectedRows <= 0, locations could not be linked, aborting sample population");
					return false;
				}
			
			}
			

			/* CREATE THE PLAYER */
			
			// now that we have a destination location within the game, we insert 
			// the player
			ContentValues playerVals = new ContentValues();
			// we use the game id for the player's parent id
			playerVals.put(COL_UNI_PARENT_ID, (int)gameId);
			playerVals.put(COL_UNI_PARENT_TYPE, TYP_GAME);
			playerVals.put(COL_PLY_ELO, 900);
			playerVals.put(COL_PLY_LOCATION, locationIds[homeIndex]);
			long playerID = database.insert(TBL_PLAYERS, null, playerVals);
			
			if ( playerID == -1 ) {
				Log.w("OC", "OCDbController: playerID = -1, aborting sample population");
				return false;
			}
			
			// create the player stories
			
			long playerNameId = -1;
			long playerAboutId = -1;
			
			switch ( marker ) {
			case MARKER_RACING:
				playerNameId = insertStoryIntoDatabase("The Stig's Virtual Cousin", CTX_TITLE, (int)playerID, TYP_PLAYER, database);
				playerAboutId = insertStoryIntoDatabase("Asking \"Who is The Stig?\" is a question of massive metaphysical implications.", CTX_ABOUT, (int)playerID, TYP_PLAYER, database);
				break;
			case MARKER_OFFICE:
				playerNameId = insertStoryIntoDatabase("Tyler Durden", CTX_TITLE, (int)playerID, TYP_PLAYER, database);
				playerAboutId = insertStoryIntoDatabase("Use soap.", CTX_ABOUT, (int)playerID, TYP_PLAYER, database);
				break;
			}
						
			if ( playerNameId == -1 || playerAboutId == -1 ) {
				Log.w("OC", "OCDbController: sample player stories could not be populated, aborting sample population");
				return false;
			}
			
			/* TODO CREATE THE ANCILLARY OBJECTS */
			
			// create a couple widgets for equipment, one for the player and one 
			// for a location
			
			// create a wallet for the player
			
			// populate some attributes for the player
			// populate some attributes for some tasks
			// populate some attributes for some equipment
			
			return true;
		}
        
	}
	
	/**
	 * Helper method to manage code for inserting stories for a given parent
	 * @param title
	 * @param context
	 * @param parentID
	 * @param parentType
	 * @param database
	 * @return
	 */
	private static long insertStoryIntoDatabase(String story, String context, int parentID, String parentType, SQLiteDatabase database) {
		ContentValues storyVals = new ContentValues();
		storyVals.put(COL_UNI_PARENT_ID, parentID);
		storyVals.put(COL_UNI_PARENT_TYPE, parentType);
		storyVals.put(COL_STY_STORY, story);
		storyVals.put(COL_STY_CONTEXT, context);
		return database.insert(TBL_STORIES, null, storyVals);
	}
	
	/**
	 * Helper method to manage code for updating stories for a given parent. This 
	 * does NOT manage the opening and closing of the database since multiple calls
	 * can be made from one calling method
	 * @param story
	 * @param context
	 * @param parentID
	 * @param parentType
	 * @param database
	 * @return
	 */
	protected static long updateStoryIntoDatabase(String story, String context, int parentID, String parentType, SQLiteDatabase database) {
		ContentValues storyVals = new ContentValues();
		storyVals.put(COL_STY_STORY, story);
		String whereClause = 	COL_UNI_PARENT_ID + " = " + parentID + " AND " + 
								COL_UNI_PARENT_TYPE + " = '" + parentType + "'" + " AND " + 
								COL_STY_CONTEXT + " = '" + context + "'";
		return database.update(TBL_STORIES, storyVals, whereClause, null);
	}
	
	/**
	 * checks to be sure a long can be safely cast to an int
	 * return an int if it can be casted and throws an error if not
	 * NB: I think this ended up not being used in the application as the longs and
	 * ints we are dealing with wouldn't have conversion issues. Are we ever really
	 * going to have 2,147,483,647 or more indexes in a table?
	 * @param l
	 * @return
	 */
	public static int safeLongToInt(long l) {
	    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
	        throw new IllegalArgumentException
	            (l + " cannot be cast to int without changing its value.");
	    }
	    return (int) l;
	}
	
}
