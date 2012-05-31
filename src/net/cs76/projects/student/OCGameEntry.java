package net.cs76.projects.student;
/**
 * Philip Regan
 * http://www.oatmealandcoffee.com
 * 
 * 
 * OCGameEntry
 * 
 * Payload object for user games to aid in display in various views
 */

/**
 * @author philipr
 *
 */
public class OCGameEntry {

	public String title;
	public String intro;
	public int id;
	
	public OCGameEntry (String newTitle, String newIntro, int newId) {
		title = newTitle;
		intro = newIntro;
		id = newId;
	}
	
}
