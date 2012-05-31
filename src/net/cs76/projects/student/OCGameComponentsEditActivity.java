/**
 * Philip Regan
 * http://www.oatmealandcoffee.com
 * 
 * 
 * OCGameComponentsEditActivity
 * 
 * The screen where all game editing begins. Lists all possible components and 
 * links to selection and editing workflows for them.
 * 
 */
package net.cs76.projects.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableRow;

/**
 * @author philipr
 *
 */
public class OCGameComponentsEditActivity extends OCCoreActivity implements OnClickListener {
	
	TableRow gameComponentRow;
	TableRow playerComponentRow;
	TableRow tasksComponentRow;
	TableRow equipmentComponentRow;
	
	/**
	 * Constructor
	 * Auto-generated constructor stub
	 */
	public OCGameComponentsEditActivity() {
		// Auto-generated constructor stub
	}
	
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
        setContentView( R.layout.oc_game_components_edit );
        
        // set the title so the user has confirmation
        setTitle( intent.getCharSequenceExtra( INTENT_GAME_TITLE ) + ": Game Components");
        
        // bind the interface elements to the class so we can listen for which
        // item was pressed
        
        gameComponentRow = (TableRow) this.findViewById( R.id.oc_table_row_game_component_game );
        gameComponentRow.setOnClickListener( (OnClickListener) this );
 
        playerComponentRow = (TableRow) this.findViewById( R.id.oc_table_row_game_component_player );
        playerComponentRow.setOnClickListener( (OnClickListener) this );
        
        tasksComponentRow = (TableRow) this.findViewById( R.id.oc_table_row_game_component_tasks );
        tasksComponentRow.setOnClickListener( (OnClickListener) this );
        
        equipmentComponentRow = (TableRow) this.findViewById( R.id.oc_table_row_game_component_equipment);
        equipmentComponentRow.setOnClickListener( (OnClickListener) this );
	}
	
	/**
	 * Capture the click on a given row
	 */
	public void onClick(View v) {
		
		Intent targetIntent = null;
		
		switch ( v.getId() ) {
		case R.id.oc_table_row_game_component_game:
			targetIntent = new Intent( getBaseContext(), OCGameInformationEditActivity.class );
			break;
		case R.id.oc_table_row_game_component_player:
			targetIntent = new Intent( getBaseContext(), OCPlayerInformationEditActivity.class );
			break;
		case R.id.oc_table_row_game_component_tasks: 
			// we have to select which object first, then edit it. So we go to the 
			// game object selection activity, and then tell that activity where
			// we want to go upon a click
			targetIntent = new Intent( getBaseContext(), OCTaskSelectionActivity.class );
			targetIntent.putExtra( INTENT_EDIT_TARGET_COMPONENT, TYP_TASK );
			targetIntent.putExtra( INTENT_EDIT_TARGET_PARENT, TYP_GAME );
			targetIntent.putExtra( INTENT_EDIT_INTENTION, INTENDED_EDIT_UPDATE );

			break;
		case R.id.oc_table_row_game_component_equipment: 
			// Same as tasks, we have to select the object first, then edit it
			targetIntent = new Intent( getBaseContext(), OCEquipmentSelectionActivity.class );
			targetIntent.putExtra( INTENT_EDIT_TARGET_COMPONENT, TYP_EQUIPMENT );
			targetIntent.putExtra( INTENT_EDIT_TARGET_PARENT, TYP_GAME );
			targetIntent.putExtra( INTENT_EDIT_INTENTION, INTENDED_EDIT_UPDATE );
			break;
		default: 
			// do nothing
			break;
		}
		// push the user to their selection
		if ( targetIntent != null ) {
			targetIntent.putExtra( "game_id", intent.getIntExtra( INTENT_GAME_ID, 1) );
			targetIntent.putExtra( INTENT_GAME_TITLE, intent.getCharSequenceExtra( INTENT_GAME_TITLE ) );
			startActivity( targetIntent );
		}
		
	}

}
