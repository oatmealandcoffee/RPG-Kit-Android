/**
 * Philip Regan
 * http://www.oatmealandcoffee.com
 * 
 * 
 * OCGameEngine
 * 
 * This is where the fate of the player is decided relative to the Accomplishments
 * in a Location. 
 * 
 * for each task
 * * get task qualifications
 * * get player info
 * * qualify player
 * * calculate attempt
 * * apply result
 * next task
 */
package net.cs76.projects.student;

import android.content.Context;

/**
 * @author philipr
 *
 */
public class OCGameEngine extends OCDbController {

	/**
	 * Auto-generated constructor stub
	 * @param context
	 */
	public OCGameEngine(Context context) {
		super(context);
		// Auto-generated constructor stub
	}
	
	/**
	 * The root method for all game play.
	 * @param playerId The player
	 * @param taskIds The tasks the player is to attempt to complete in full
	 */
	public boolean doTask(int playerId, int[] taskIds) {
		
		boolean playerWon = true;
		// TODO See header comments for workflow
		return playerWon;
	}

}
