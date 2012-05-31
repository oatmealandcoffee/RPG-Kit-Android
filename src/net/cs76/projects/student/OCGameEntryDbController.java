/**
 * Philip Regan
 * http://www.oatmealandcoffee.com
 * 
 * 
 * OCGameEntryDbController
 * 
 * 
 * 
 */
package net.cs76.projects.student;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

/**
 * @author philipr
 *
 */
public class OCGameEntryDbController extends OCDbController {

	/**
	 * @param context
	 */
	public OCGameEntryDbController(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Pull the games from the DB. This is effectively a JOIN but there isn't a 
	 * graceful way to handle going through the results given the table's lean 
	 * construction
	 * 
	 * @param artist
	 * @return a list of Records
	 */
	public List<OCGameEntry> getGames() {
		
		if ( !openDatabase( this.context ) ) {
			return null;
		}
		
		List<OCGameEntry> gameEntries = new ArrayList<OCGameEntry>();
		
		// get the list of game ids.
		String[] targetGameColumns = new String[]{ COL_UNI_ID };		
		Cursor gameIds = db.query(TBL_GAMES, targetGameColumns, null, null, null, null, null);
		
		// iterating through the ids, get the stories for that game and create 
		// entries for the view
		if ( gameIds.moveToFirst() ) {
			do {
				// get the current game id
				int gameId = gameIds.getInt( gameIds.getColumnIndex( COL_UNI_ID ) );
				
				// get the stories that match the game id and type
				
				// build the target columns
				String[] targetStoryColumns = new String[]{ COL_STY_STORY, COL_STY_CONTEXT, COL_UNI_PARENT_ID, COL_UNI_PARENT_TYPE };
				Cursor gameStories = db.query(TBL_STORIES, targetStoryColumns, null, null, null, null, null);
				
				// now we iterate through the story results and create game entries
				if ( gameStories.moveToFirst() ) {
					
					// buffers for content
					String gameTitle = null;
					String gameAbout = null;
					
					// pair story context to record
					do {
						String storyContext = gameStories.getString( gameStories.getColumnIndexOrThrow( COL_STY_CONTEXT ) );
						int storyParentId = gameStories.getInt( gameStories.getColumnIndexOrThrow( COL_UNI_PARENT_ID ) );
						String storyParentType = gameStories.getString( gameStories.getColumnIndexOrThrow( COL_UNI_PARENT_TYPE ) );
						
						// we want only those stories which match a the game's id and type
						if ( ( storyParentId == gameId ) && ( storyParentType.contentEquals( TYP_GAME ) ) ) {
							
							if ( storyContext.contentEquals( CTX_TITLE )) {
								gameTitle = gameStories.getString( gameStories.getColumnIndexOrThrow( COL_STY_STORY ) );
							}
							
							if ( storyContext.contentEquals( CTX_ABOUT )) {
								gameAbout = gameStories.getString( gameStories.getColumnIndexOrThrow( COL_STY_STORY ) );
							}
							
							// we have the id, title, and about so we make a record and move onward
							if ( gameTitle != null && gameAbout != null ) {
								gameEntries.add( gameEntryFromCursor( gameTitle, gameAbout, gameId ) );
								// reset the buffers
								gameTitle = null;
								gameAbout = null;
							}
							
						}
												
					} while ( gameStories.moveToNext() );
					
				}

				if (gameStories != null && !gameStories.isClosed()) {
					gameStories.close();
				}
				
			} while ( gameIds.moveToNext() );
			
		}
				
		if (gameIds != null && !gameIds.isClosed()) {
			gameIds.close();
		}
		
		closeDatabase();
		
		// whew!
		return gameEntries;
	}
	
	/**
	 * Helper method to manage creation of GameEntry
	 * @param title
	 * @param about
	 * @param id
	 * @return OCGameEntry
	 */
	private OCGameEntry gameEntryFromCursor(String title, String about, int id) {
		return new OCGameEntry(title, about, id);
	}
	


}
