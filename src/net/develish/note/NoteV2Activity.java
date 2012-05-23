/*

 * Author: Kristoffer Pedersen
 * Mail: deifyed <Guess (hint: its an @)> gmail.com
 * 
 * License:
 * I take no responsibility what so ever of what you decide to do with this code.
 * You are free to use and/or modify it as you wish. 
 */
 /* 
 * TODO:
 *  * Title label onClick to rename
 *  * Settings
 *  	* Font settings
 *  	* Holo dark choice?
 *  * Undo
 *  * Sync SyncAdapter <-- Not in settings
 *  
 *  Text:
 *  	Add auto-indent OG options (Lese currentLine og telle indent n�r bruker trykker enter)
 *  	Add auto-bullet-list OG options (Lese f�rste character etter whitespace og evt legge til "* " n�r bruker trykker enter
 */

package net.develish.note;

import java.io.File;
import java.io.FileWriter;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Main activity. Stands for the edit note functionality.
 * @author Kristoffer Pedersen
 *
 */
public class NoteV2Activity extends Activity {
	
	private final boolean debug = false;
	
	private final String PREFS_NAME = "NotePref";
	
	private final String PREFS_TITLESTRING = "strTitle";
	private final String PREFS_BODYSTRING = "strBody";
	private final String PREFS_NIDLONG = "longNid";
	
	public static final int REQUEST_OPEN = 1;
	
	private long nId = -1;
	
	private EditText body;
	
	private SQLAdapter sql;
	
	/*
	 * OVERRIDES
	 */
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        
        setContentView(R.layout.main);

        body = (EditText) findViewById(R.id.txtBody);
        
        sql = new SQLAdapter(this);
        
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setIcon(R.drawable.homeiconunsaved);
    }
    
    /**
     * Called when an activity regains focus.
     */
    @Override
    protected void onResume() {
    	
    	if(nId == -1)
    		tmpLoad();

    	super.onResume();
    }
    
    /**
     * Called when an activity lose focus.
     */
    @Override
    protected void onPause() {
    	
    	tmpSave();
    	
    	if(nId != -1)
    		updateNote();
    	
    	super.onPause();
    }

    /** Creates the action bar options menu **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_action_bar, menu);
    	
    	return(true);
    }
    
    /** Enables Action Bar buttons' actions **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch(item.getItemId()) {
    		case android.R.id.home:
    			renameNote();
    			return(true);
    		case R.id.btnNew:
    			newNote();
    			return(true);
    		case R.id.btnLoad:
    			startActivityForResult(new Intent(this, OpenNoteActivity.class), REQUEST_OPEN);
    			return(true);
    		case R.id.btnExport:
    			export();
    			return(true);
    		case R.id.btnSettings:
    			startActivity(new Intent(this, SettingsActivity.class));
    			return(true);
    		case R.id.btnAbout:
    			about();
    			return(true);
			default:
				return super.onOptionsItemSelected(item);
    	}
    }
    
    /**
     * Listener which fires when an activity started by startActivityForResult() finishes.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	Bundle extras = data.getExtras();
    	
    	if(resultCode == Activity.RESULT_OK)
	    	switch(requestCode) {
	    	
	    		case REQUEST_OPEN:
        			getActionBar().setTitle(extras.getString(SQLAdapter.KEY_TITLE));
        			body.setText(extras.getString(SQLAdapter.KEY_BODY));
        			nId = extras.getLong(SQLAdapter.KEY_ROWID);
        			body.setSelection(body.length());
        			
        	    	getActionBar().setIcon(R.drawable.homeicon);
        	    	
        	    	break;
	    	}
    	else if (resultCode == Activity.RESULT_CANCELED)
    		switch(requestCode) {
    		
    			case REQUEST_OPEN:
    				if(!noteExists(nId)) {
    					
    					nId = -1;
    					
    					tmpSave();
    				}
    				break;
    		}
    }
    
    /*
     * FUNCTIONALITY
     */
    /**
     * Resets the UI and variables for a new note.
     */
    private void newNote() {
    	
    	if(nId != -1)
    		if(noteExists(nId)) 
    			updateNote();
    	
		reset();
    }
    
    /**
     * Gives the user a dialog to rename and save their note.
     */
    private void renameNote() {
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	if(nId == -1) {
    		
			builder.setTitle(R.string.renameTitleUnsaved);
			builder.setMessage(R.string.renameMessageUnsaved);
    	}
    	else {
    		
			builder.setTitle(R.string.renameTitle);
			builder.setMessage(R.string.renameMessage);
    	}
    	
    	final EditText txtTitleInput = new EditText(builder.getContext());

    	txtTitleInput.setSingleLine();
    	
    	txtTitleInput.setFilters(new InputFilter[] { new InputFilter.LengthFilter(15) });
    	
    	if(nId == -1)
    		txtTitleInput.setText((body.length() > 15) ? body.getText().toString().substring(0, 15) : body.getText().toString().substring(0, body.length()));
    	else
    		txtTitleInput.setText(getActionBar().getTitle());
    	
    	txtTitleInput.setSelection(txtTitleInput.length());
    		
    	builder.setView(txtTitleInput);
    	
    	builder.setPositiveButton((nId == -1) ? R.string.renameSaveUnsaved : R.string.renameSave, new Dialog.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				
				String tmp = txtTitleInput.getText().toString().trim();
				
				if(tmp.length() != 0) {
					
					getActionBar().setTitle(tmp);
					
					if(nId != -1)
						updateNote();
					else
						saveNote();
					
					getActionBar().setIcon(R.drawable.homeicon);
    			}
				
				else
					Toast.makeText(getApplicationContext(), R.string.renameErrorEmptyTitle, Toast.LENGTH_SHORT).show();
			}
    		
    	});
    	builder.setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); } 
			
    	});
    	
    	builder.show();
    }
    
    /**
     * Calles the About dialog.
     */
    private void about() {
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setIcon(R.drawable.launcher);
    	builder.setTitle(getString(R.string.about) + " " + getString(R.string.app_name));
    	
    	builder.setMessage(R.string.aboutMessage);
    	
    	builder.show();
    }
    
    /**
     * Saves note to application memory or database.
     */
    private void saveNote() {
    	
    	String title = getActionBar().getTitle().toString();
    	
    	if(nId == -1 && !title.equals("Note")) {
    		
    		sql.open();
    		
    		nId = sql.createNote(title, body.getText().toString());
    		
    		sql.close();
    	}
    	
    	debugmsg("saveNote");
    }
    
    private void tmpSave() {
    	
    	SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, 0).edit();
    	editor.putLong(PREFS_NIDLONG, nId);
    	editor.putString(PREFS_TITLESTRING, getActionBar().getTitle().toString());
    	editor.putString(PREFS_BODYSTRING, body.getText().toString());
    	editor.commit();
    	
    	debugmsg("tmpsave");
    }
    
    private void updateNote() {
    	
    	sql.open();
    	
    	sql.updateNote(nId, getActionBar().getTitle().toString(), body.getText().toString());
    	
    	sql.close();
    	
    	debugmsg("updateNote");
    }
    
    /**
     * Loads note from SharedPreferences.
     */
    private void tmpLoad() {
    	
    	nId = getSharedPreferences(PREFS_NAME, 0).getLong(PREFS_NIDLONG, -1);
    	getActionBar().setTitle(getSharedPreferences(PREFS_NAME, 0).getString(PREFS_TITLESTRING, getString(R.string.app_name)));
    	body.setText(getSharedPreferences(PREFS_NAME, 0).getString(PREFS_BODYSTRING, ""));
    	body.setSelection(body.length());
    	
    	if(nId == -1)
    		getActionBar().setIcon(R.drawable.homeiconunsaved);
    	else
    		getActionBar().setIcon(R.drawable.homeicon);
    	
    	debugmsg("tmpLoad");
    }
    
    /**
     * Used to export notes to SD-card.
     */
    private void export() {
    	export(getActionBar().getTitle().toString(), body.getText().toString());
    }
    
    /**
     * Main export magic
     * @param The title of the file.
     * @param The content of the file.
     */
    private void export(String title, String body) {
    	
    	try {
    		
    		File exportFile = new File(Environment.getExternalStorageDirectory(), title);
    		
    		FileWriter writer = new FileWriter(exportFile);
    		writer.append(body);
    		writer.flush();
    		writer.close();
    		
    		Toast.makeText(this, R.string.exportSuccess, Toast.LENGTH_SHORT).show();
    	}
    	catch(Exception e) {
    		Toast.makeText(this, R.string.exportError, Toast.LENGTH_SHORT).show();
    	}
    }
    
    /*
     * SUPPORT FUNCTIONS/METHODS
     */
    
    /**
     * Checks if a note id currently exist in the database.
     * @param id to check for existance
     * @return True if note id exists
     */
    private boolean noteExists(Long id) {
    	
    	sql.open();
    	
    	Cursor c = sql.fetchNote(id);
    	
    	if(c.getCount() != 0) {
    		
    		sql.close();
    		
    		return(true);
    	}
    	else {
    		
    		sql.close();
    		
    		return(false);
    	}
    }
    /**
     * Resets UI and nId.
     */
    private void reset() {
    	
		getActionBar().setTitle(getString(R.string.app_name));
		body.setText("");
		nId = -1;
        getActionBar().setIcon(R.drawable.homeiconunsaved);
    }

    /**
     * Prints out debug information when debug flag is True
     * @param msg Message to print
     */
    private void debugmsg(String msg) {
    	
    	if(debug)    	
    		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}