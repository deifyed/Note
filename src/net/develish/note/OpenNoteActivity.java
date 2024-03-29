/*
 * Author: Kristoffer Pedersen
 * Mail: deifyed <Guess (hint: its an @)> gmail.com
 * 
 * License:
 * I take no responsibility what so ever of what you decide to do with this code.
 * You are free to use and/or modify it as you wish. 
 */

package net.develish.note;


import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

/**
 * Class representing loading of notes functionality.
 * @author Kristoffer Pedersen
 *
 */
public class OpenNoteActivity extends ListActivity {
	
	private final int DELETE_KEY = 37;
	
	private SQLAdapter sql;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.loadnotelist);
        
        sql = new SQLAdapter(this);
        
        getActionBar().setHomeButtonEnabled(true);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        fillList();
        
        registerForContextMenu(getListView());
    }
    
    @Override
    public void onBackPressed() {
    	
    	setResult(Activity.RESULT_CANCELED, new Intent());
    	
    	this.finish();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	getMenuInflater().inflate(R.menu.open_action_bar, menu);
    	
    	return(true);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
    		ContextMenuInfo menuInfo) {
    	
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
    	menu.add(0, DELETE_KEY, 0, "Delete");
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	
    	switch(item.getItemId()) {
    	
    		case DELETE_KEY:
    			
    			sql.open();
    			
    			sql.deleteNote(((NoteListAdapter.ViewHolder) getListView().getChildAt(((AdapterContextMenuInfo) item.getMenuInfo()).position).getTag()).id);
    			
    			sql.close();
    			
    			fillList();
    			
    			return(true);
    	}
    	return super.onContextItemSelected(item);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

    	switch(item.getItemId()) {
    	
    		case android.R.id.home:
    			setResult(Activity.RESULT_CANCELED, new Intent());
    			this.finish();
    			return(true);
    		default:
    	    	return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	
    	sql.open();
    	
    	Cursor c = sql.fetchNote(id);
    	
    	sql.close();
    	
    	if(c.moveToFirst()) {
    		
        	Bundle extras = new Bundle();
        	
    		extras.putString(SQLAdapter.KEY_TITLE, c.getString(c.getColumnIndexOrThrow(SQLAdapter.KEY_TITLE)));
    		extras.putString(SQLAdapter.KEY_BODY, c.getString(c.getColumnIndexOrThrow(SQLAdapter.KEY_BODY)));
    		extras.putLong(SQLAdapter.KEY_ROWID, c.getLong(c.getColumnIndexOrThrow(SQLAdapter.KEY_ROWID)));
    		
    		this.setResult(Activity.RESULT_OK, new Intent().putExtras(extras));
    		
    		finish();
    	}
    	
    	super.onListItemClick(l, v, position, id);
    }
    
    /**
     * Populates the load notes list view
     */
    void fillList() {
    	
    	sql.open();
    	
    	Cursor c = sql.fetchAllNotes();
    	
    	NoteListAdapter adapter = new NoteListAdapter(this, R.layout.loadnotes_item, c, SQLAdapter.KEY_TITLE, 1);
    	
    	setListAdapter(adapter);
    	
    	sql.close();
    }
}
