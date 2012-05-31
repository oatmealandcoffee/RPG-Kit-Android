/**
 * Philip Regan
 * http://www.oatmealandcoffee.com
 * 
 * 
 * OCGameObjectSelectionActivity
 * 
 * Generic, non-abstract class that is used to facilitate the creation of selection 
 * activities for various game objects. This is a simplified version of the original 
 * OCGameSelectionActivity.
 * 
 * Actions are determined by the previous activity, and the action determines what
 * happens each time the user presses a button.
 * 
 * Data required for object retrieval:
 * 	* target type
 * 	* parent id
 * 	* parent type
 * 
 * Data required for object editing
 * 	* target type
 * 	* intended edit
 * 		* update properties
 * 		* update parent
 * 		* delete
 * 
 * Pairs with (ListView)oc_game_object_selection and (TableView)oc_game_object_list_item layouts.
 * 
 * This class provides the List adapter, the SQLiteHelper and dbController. The subclass
 * provides the actual SQL statements and intents needed for CRUD, selection, and 
 * activity transitions
 * 
 */
package net.cs76.projects.student;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author philipr
 *
 */
public class OCGameObjectSelectionActivity extends ListActivity implements OnClickListener {
	
	/* 
	 * Activity Objects
	 * 
	 * These values are pulled from OCCoreActivity, but it would probably be better
	 * if these were placed into an interface or category that the two classes
	 * can implement and share rather than duplicating
	 */
	
	protected static String INTENT_GAME_ID = "game_id";
	protected static String INTENT_GAME_TITLE = "game_title";
	
	protected static String INTENT_EDIT_TARGET_COMPONENT = "target_component";
	protected static String INTENT_EDIT_TARGET_PARENT = "target_parent";
	protected static String INTENT_EDIT_PARENT_ID = "parent_id";
	
	// used in list selection activity
	protected static String INTENT_EDIT_INTENTION = "intended_edit";
	// things to do once an objects are selected and saved; HUMAN READABLE since
	// they are co-opted for button labels
	protected static String INTENDED_EDIT_LINK = "Link";
	protected static String INTENDED_EDIT_UNLINK = "Unlink";
	protected static String INTENDED_EDIT_UPDATE = "Edit";
	protected static String INTENDED_EDIT_DELETE = "Delete";
	
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
	
	protected static int INTENT_GAME_ID_DEFAULT = -1;
	
	protected Intent intent;
	
	/* target object parameters */
	
	protected String targetType = "";
	protected String parentType = "";
	protected int parentId = INTENT_GAME_ID_DEFAULT;
	protected String targetEdit = "";
	
	/* database */
	
	OCGameObjectSelectionController gameObjectSelectionController = null;
	
	/* interface */
	
	private ListView listView = null;
	private List<OCGameObjectPayload> gameObjectPayloads = null;
	private OCGameObjectAdapter gameObjectAdapter = null;

	/**
	 * Auto-generated constructor stub
	 */
	public OCGameObjectSelectionActivity() {
		// Auto-generated constructor stub
	}
	
	/**
	 * Required per framework
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
// get the intent
        
        intent = getIntent();
        
        // get the required information from the intent
        
        targetType = intent.getStringExtra( INTENT_EDIT_TARGET_COMPONENT );
        parentId = intent.getIntExtra( INTENT_GAME_ID, INTENT_GAME_ID_DEFAULT );
        parentType = intent.getStringExtra( INTENT_EDIT_TARGET_PARENT );
        targetEdit = intent.getStringExtra( INTENT_EDIT_INTENTION );
        
        // bind the interface to the class
        setContentView( R.layout.oc_game_object_selection );
                
     	// get the desired objects and their content
        
        // set up the db controller
     	gameObjectSelectionController = new OCGameObjectSelectionController( this, targetType, parentType, parentId, targetEdit );
     	// get an array of the found objects
     	gameObjectPayloads = gameObjectSelectionController.getGameObjects();
     	// create and bind an adapter to the class
     	gameObjectAdapter = new OCGameObjectAdapter(this, R.layout.oc_game_object_list_item, gameObjectPayloads, this);

     	// get the list view and bind the adapter to it
     	listView = getListView();
     	listView.setAdapter( this.gameObjectAdapter );
        
        // set the title for user confirmation of the chosen task
        setTitle( intent.getCharSequenceExtra( INTENT_GAME_TITLE ) + ": Select " + targetType );
	}
	
	/**
	 * Private class that acts as connecter between class and database
	 * 
	 * Target object parameters for CURD include the following
	 * String targetType;
	 * String parentType;
	 * int parentId;
	 * String targetEdit;
	 * @author philipr
	 *
	 */
	private class OCGameObjectSelectionController extends OCDbController {
		
		 String mTargetType;
		 String mParentType;
		 int mParentId;
		 String mTargetEdit;
		 
		/**
		 * Auto-generated constructor stub
		 * @param context
		 */
		public OCGameObjectSelectionController(Context context, String tType, String pType, int pId, String tEdit ) {
			super(context);
			
			mTargetType = tType;
			mParentType = pType;
			mParentId = pId;
			mTargetEdit = tEdit;
			
		}
		
		/**
		 * Handles retrieving the game objects and their stories from the database
		 * @return
		 */
		public List<OCGameObjectPayload> getGameObjects() {
			// check to be sure we can actually open the db
			if ( !openDatabase( this.context ) ) {
				return null;
			}
			
			// prep the array list
			List<OCGameObjectPayload> gameObjects = new ArrayList<OCGameObjectPayload>();
			
			String targetTable = "";
			// convert the target type into a table
			if ( mTargetType.contains( (CharSequence) TYP_TASK ) ) {
				targetTable = TBL_TASKS;
			} else if ( mTargetType.contains( (CharSequence) TYP_EQUIPMENT ) ) {
				targetTable = TBL_EQUIPMENT;
			} else {
				targetTable = TBL_GAMES; // this won't return anything of value, but we need to put something here
				Log.w("OC", "OCGameObjectSelectionActivity.OCGameObjectSelectionController.getGameObjects(): mTargetType does not equate to an existing table inthe database. ");
			}
			
			// get the target objects that match the parent id and parent type
			String[] targetGameObjectColumns = new String[]{ COL_UNI_ID };
			String targetGameObjectWhereParams = COL_UNI_PARENT_ID + " = " + mParentId + " AND " + COL_UNI_PARENT_TYPE + " = '" + mParentType + "'";
			Cursor gameObjectIds = db.query(targetTable, targetGameObjectColumns, targetGameObjectWhereParams, null, null, null, null);
			
			if ( gameObjectIds.moveToFirst() ) {
								
				do {
					
					int targetId = gameObjectIds.getInt( gameObjectIds.getColumnIndexOrThrow( COL_UNI_ID ) );
					
					String[] targetStoryColumns = new String[]{ COL_UNI_ID, COL_STY_STORY, COL_STY_CONTEXT };
					String targetStoryWhereParams = COL_UNI_PARENT_ID + " = " + targetId + " AND " + COL_UNI_PARENT_TYPE + " = '" + mTargetType + "'";
					Cursor gameObjectStories = db.query(TBL_STORIES, targetStoryColumns, targetStoryWhereParams, null, null, null, null);
					
					// now we have some object ids, let's get their stories
					
					if ( gameObjectStories.moveToFirst() ) {
					
						// set up buffers to capture content
						String gameObjectTitle = null;
						String gameObjectAbout = null;
						
						do {
														
							String storyContext = gameObjectStories.getString( gameObjectStories.getColumnIndexOrThrow( COL_STY_CONTEXT ) );
							if ( storyContext.contentEquals( CTX_TITLE )) {
								gameObjectTitle = gameObjectStories.getString( gameObjectStories.getColumnIndexOrThrow( COL_STY_STORY ) );
							} else if ( storyContext.contentEquals( CTX_ABOUT )) {
								gameObjectAbout = gameObjectStories.getString( gameObjectStories.getColumnIndexOrThrow( COL_STY_STORY ) );
							}
							
							// now that we have the core information for a payload object, 
							// let's create a game object payload instance
							if ( gameObjectTitle != null && gameObjectAbout != null ) {
																
								OCGameObjectPayload gameObjectPayload = new OCGameObjectPayload();
								
								gameObjectPayload.gameObjectTitle = gameObjectTitle;
								gameObjectPayload.gameObjectAbout = gameObjectAbout;
								gameObjectPayload.gameObjectId = targetId;
								gameObjectPayload.gameObjectType = mTargetType;
								gameObjectPayload.intendedEdit = mTargetEdit;
								
								gameObjects.add( gameObjectPayload );
								
								// clear the buffers
								gameObjectTitle = null;
								gameObjectAbout = null;
								
							}
							
						} while ( gameObjectStories.moveToNext() );
						
						// close the story cursor
						if ( gameObjectStories != null && !gameObjectStories.isClosed() ) {
							gameObjectStories.close();
						}
					} else {
						// something bad happened with stories
						Log.w("OC", "OCGameObjectSelectionActivity.OCGameObjectSelectionController.getGameObjects(): Stories for " + mTargetType + "[" + targetId + "]->{" + mParentType + "} could not be retrieved");
					}
					
				} while ( gameObjectIds.moveToNext() );
			} else {
				// something bad happened with game objects
				Log.w("OC", "OCGameObjectSelectionActivity.OCGameObjectSelectionController.getGameObjects(): Objects for " + mParentType + "[" + mParentId + "]->{" + mTargetType + "} could not be retrieved");
			}
			// close the cursor
			if ( gameObjectIds != null && !gameObjectIds.isClosed() ) {
				gameObjectIds.close();
			}
			
			// close the database
			closeDatabase();
			
			// whew!
			return gameObjects;
			
		}
		
	}
	
	/**
	 * Private class that binds the game entries found to a list item for display 
	 * to the user
	 * @author philipr
	 *
	 */
	
	private class OCGameObjectAdapter extends ArrayAdapter<OCGameObjectPayload> {
		
		private List<OCGameObjectPayload> items;
		private OnClickListener clickListener;
		
		public OCGameObjectAdapter(Context context, int textViewResourceId, List<OCGameObjectPayload> items, OnClickListener cl) {
			
			super(context, textViewResourceId, items);
			
			this.items = items;
			this.clickListener = cl;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			// set up a buffer for a given position
			View v = convertView;
						
			if (v == null) {
				
				// inflate the interface
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.oc_game_object_list_item, null);
				
			} else {
				
				v = convertView;
				
			}
			
			// we have a view, now push the gameObjectPayload data to it
			if ( items.get( position ) != null ) {
				
				OCGameObjectPayload gameObjectPayload = items.get( position );
				
				// populate the interface with the game object's text
				TextView gameObjectTitleView = (TextView) v.findViewById( R.id.oc_textview_game_object_title );
				gameObjectTitleView.setText( gameObjectPayload.gameObjectTitle );
				
				TextView gameObjectAboutView = (TextView) v.findViewById( R.id.oc_textview_game_object_about );
				gameObjectAboutView.setText( gameObjectPayload.gameObjectAbout );
				
				Button gameObjectActionButton = (Button) v.findViewById( R.id.oc_button_game_object_action );
				gameObjectActionButton.setText( gameObjectPayload.intendedEdit );

				// set the game object payload to the button for use in onClick
				gameObjectActionButton.setTag( gameObjectPayload );
								
				// set the OnClickListener
				gameObjectActionButton.setOnClickListener( clickListener );				
			}
			return v;
		}
		
	}
	
	/**
	 * Capture the button clicks
	 */
	public void onClick(View v) {
		// get the gameObjectPayload so we can see what action on which object was intended.
		OCGameObjectPayload gameObjectPayload = (OCGameObjectPayload) v.getTag();
		
		// something got clicked we are not supporting at this point
		if ( gameObjectPayload == null ) {
			return;
		}
		
		Intent targetIntent = null;
		// switch by the class then switch by the edit.

		if ( gameObjectPayload.gameObjectType.contains( TYP_TASK ) ) {
			if ( gameObjectPayload.intendedEdit.contains( INTENDED_EDIT_UPDATE ) ) {
				// TODO push to the Task Edit Home
			} else {
				// do nothing for now, but there is link, unlink, and delete
			}
		}
		
		if ( gameObjectPayload.gameObjectType.contains( TYP_EQUIPMENT ) ) {
			if ( gameObjectPayload.intendedEdit.contains( INTENDED_EDIT_UPDATE ) ) {
				// TODO push to the Equipment Edit Home
			} else {
				// do nothing for now, but there is link, unlink, and delete
			}
		}
		
		if ( targetIntent != null ) {
			// throw in some extras as needed
			
			startActivity( targetIntent );
		}
	}
	
	static class OCGameObjectPayload {
		String gameObjectTitle;
		String gameObjectAbout;
		int gameObjectId;
		String gameObjectType;
		String intendedEdit;
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
