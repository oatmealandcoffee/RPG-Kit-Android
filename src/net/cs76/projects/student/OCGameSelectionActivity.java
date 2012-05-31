/**
 * Philip Regan
 * http://www.oatmealandcoffee.com
 * 
 * 
 * OCGameSelectionActivity
 * 
 * Home screen where the user selects an action--edit or play--to a game
 * 
 * This a complex beast that requires getting the games from the DB, displaying in
 * a non-simple layout, and the directing the user to their chosen workflow.
 * 
 * This was the first Activity implemented and subsequent activities are more
 * streamlined.
 * 
 * Essentially, the way it works on startup is...
 * 
 * OCGameSelectionActivity.onCreate()
 * OCGameSelectionActivity.refreshView()
 * OCGameSelectionActivity.reloadEntries() 
 * OCGameSelectionActivity.getResults()
 * OCDbController.OCSqliteHelper.onCreate()
 * OCDbController.OCSqliteHelper.populateDefaults(1)
 * OCDbController.OCSqliteHelper.populateSampleGame(1)
 * OCDbController.OCSqliteHelper.populateDefaults(2)
 * OCDbController.OCSqliteHelper.populateSampleGame(2)
 * OCGameSelectionActivity.returnResults()
 * ...then we wait to intercept a click with OCGameSelectionActivity.onClick()
 */

package net.cs76.projects.student;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
 * ListView of all the games stored by the user including sample games
 * 
 * @author philipr
 *
 */
public class OCGameSelectionActivity extends ListActivity implements OnClickListener {

	private ProgressDialog progressDialog = null;
	private List<OCGameEntry> games = null;
	private GameEntryAdapter adapter;
	private OCGameEntryDbController gameEntryDbController;
	private Runnable showResults;
	
	/**
	 * @param savedInstanceState
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
        setContentView( R.layout.oc_game_selection );
        
        refreshView(); // initialization
	}
	
	/** OnClickListener
	 * This is for button clicks
	 */
	public void onClick(View v) {
		//Log.i("OC", "OCGameSelectionActivity.onClick()");
				
		// find out which button was pressed by examining the key
		
		Intent targetIntent = null;
		GamePayload gamePayload = null;
		
		if ( v.getTag( R.string.INTENT_KEY_PLAY ) != null ) {
			gamePayload = (GamePayload) v.getTag( R.string.INTENT_KEY_PLAY );
			targetIntent = new Intent( getBaseContext(), OCGamePlayLocationActivity.class );
		} else if ( v.getTag( R.string.INTENT_KEY_EDIT ) != null ) {
			gamePayload = (GamePayload) v.getTag( R.string.INTENT_KEY_EDIT );
			targetIntent = new Intent( getBaseContext(), OCGameComponentsEditActivity.class );
		}
		
		targetIntent.putExtra("game_id", gamePayload.gameId);
		targetIntent.putExtra("location_id", -1);
		targetIntent.putExtra("game_title", gamePayload.title);
		
        startActivity( targetIntent );
	}
	
	/**
	 * Root process for getting stored records and displaying them in the view
	 */
	public void refreshView(){
		//Log.i("OC", "OCGameSelectionActivity.refreshView()");
		// create an ArrayList of games and bind it to an Adapter
		games = new ArrayList<OCGameEntry>();
		this.adapter = new GameEntryAdapter(this, R.layout.oc_game_list_item, games, this);
		
		// create a ListView 
		ListView list = getListView();
		list.setAdapter( this.adapter );
		//list.setOnItemClickListener( (OnItemClickListener) this );
		
		// do the work of querying database in getResults in a Runnable/separate
		// thread
		showResults = new Runnable() {
			public void run() {
				getResults();
			}
		};
		
		reloadEntries(); // initialize	
	}
	
	/**
	 * Process for starting the OCGameEntryDbController and getting the results
	 * Pushes the results to the list adapter
	 */
	private void getResults() {
		//Log.i("OC", "OCGameSelectionActivity.getResults()");
		
		gameEntryDbController = new OCGameEntryDbController(getApplicationContext());
		try {
			games = gameEntryDbController.getGames(); 
		}
		catch (Exception e) {
			Log.w("OC", "OCGameSelectionActivity.getResults: " + e.toString() );
			showStringMessage(e.getMessage());
		}
		// the message to return to the UIThread when
		// results have been found and completed
		runOnUiThread(returnResults);
		
	}
	
	/**
	 * creates a new thread for the SQLite3 database queries
	 */
	private void reloadEntries() {
		//Log.i("OC", "OCGameSelectionActivity.reloadEntries()");
		
		adapter.clear();
		Thread thread = new Thread(null, showResults, "OCGameEntriesBackgroundThread");
		thread.start();
		progressDialog = ProgressDialog.show(this, "Please wait...", "Retrieving data ...", true);
	}

	/** 
	 * Runnable that returns results to UI thread - if no results are found, then a 
	 * message is given to the user
	 */
	private Runnable returnResults = new Runnable() {
		
		public void run() {
			//Log.i("OC", "OCGameSelectionActivity.returnResults()");
			if (games != null && games.size() > 0) {
				adapter.notifyDataSetChanged();
				for (int i = 0; i < games.size(); i++)
					adapter.add(games.get(i));
			} else {
				showStringMessage("No games were found.");
			}
			progressDialog.dismiss();
			adapter.notifyDataSetChanged();
		}
	};

	
	/**
	 * Private class that binds the game entries found to a list item
	 * @author philipr
	 *
	 */
	private class GameEntryAdapter extends ArrayAdapter<OCGameEntry> {
		private List<OCGameEntry> items;
		private OnClickListener clickListener;

		public GameEntryAdapter(Context context, int textViewResourceId, List<OCGameEntry> items, OnClickListener cl) {
			
			super(context, textViewResourceId, items);
			
			//Log.i("OC", "GameEntryAdapter.GameEntryAdapter()");

			this.items = items;
			this.clickListener = cl;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//Log.i("OC", "GameEntryAdapter.getView()");
			// ViewHolder keeps references to children views avoiding 
			// findViewById() calls on each row.
			
			GamePayload gamePayload;
            // useful for displaying a large number of complex views
			
			View v = convertView;
			if (v == null) {
				
				// inflate the interface
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.oc_game_list_item, null);
				
				// Create the ViewHolder and bind the data to it - note this is only done
				// when the view is null
				
				// set the views to the holder
				gamePayload = new GamePayload();
				
				gamePayload.titleTextView = (TextView) v.findViewById( R.id.oc_game_select_list_item_title );
				gamePayload.introTextView = (TextView) v.findViewById( R.id.oc_game_select_list_item_intro );
				gamePayload.playButton = (Button) v.findViewById( R.id.oc_game_select_list_item_play );
				gamePayload.editButton = (Button) v.findViewById( R.id.oc_game_select_list_item_edit );
				
			}
			else {
				// get holder back - fast access to TextViews and Button
				gamePayload = (GamePayload) v.getTag();
			}

			final OCGameEntry gameEntry = items.get(position);
			
			if (gameEntry != null) {
				// push the entry data to the view
				
				gamePayload.titleTextView.setText( "" + gameEntry.title );
				gamePayload.introTextView.setText( "" + gameEntry.intro );
				gamePayload.title = gameEntry.title;
				gamePayload.intro = gameEntry.intro;
				gamePayload.gameId = gameEntry.id; // the db index of the game
				gamePayload.playButton.setOnClickListener( clickListener );
				gamePayload.editButton.setOnClickListener( clickListener );
				
				v.setTag( gamePayload );
				gamePayload.playButton.setTag( R.string.INTENT_KEY_PLAY, gamePayload );
				gamePayload.editButton.setTag( R.string.INTENT_KEY_EDIT, gamePayload );
				
			}

			return v;
		}
		
	}
	
	/**
	 * Container class to help manage views
	 * @author philipr
	 *
	 */
	static class GamePayload {
				
		TextView titleTextView;
		TextView introTextView;
		String title;
		String intro;
		Button playButton;
		Button editButton;
		int gameId;
	}

	/**
	 * Displays a message to the user via Toast, intended for quick, un-actionable 
	 * messages
	 * @param title
	 */
	public void showMessage(TextView title) {
		Toast t = Toast.makeText(this, title.toString(), Toast.LENGTH_SHORT);
		t.setGravity(Gravity.BOTTOM, 0, 0);
		t.show();
	}
	
	/**
	 * Displays a message to the user via Toast, intended for quick, un-actionable 
	 * messages
	 * @param title
	 */
	public void showStringMessage(String title) {
		Toast t = Toast.makeText(this, title, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.BOTTOM, 0, 0);
		t.show();
	}

}
