/*
 * Author: Kristoffer Pedersen
 * Mail: deifyed <Guess (hint: its an @)> gmail.com
 * 
 * License:
 * I take no responsibility what so ever of what you decide to do with this code.
 * You are free to use and/or modify it as you wish. 
 * 
 * TODO:
 * 	* Batch delete
 */

package net.develish.note;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Simple list adapter used to fill the load notes listview.
 * Reason for the custom list adapter is because I'm planning on implementing 
 * a batch delete function.
 * 
 * @author Kristoffer Pedersen
 *
 */
public class NoteListAdapter extends SimpleCursorAdapter {
	
	private Cursor mCursor;
	private int titleIndex;
	
	private LayoutInflater li;

	public NoteListAdapter(Context context, int layout, Cursor c, String from, int to) {
		
		super(context, layout, c, new String[] { from }, new int[] { to });
		
		li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mCursor = c;
		
		titleIndex = c.getColumnIndexOrThrow(from);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if(mCursor.moveToPosition(position)) {
			
			ViewHolder vHolder;
			
			if(convertView == null) {
				
				convertView = li.inflate(R.layout.loadnotes_item, parent, false);
				
				vHolder = new ViewHolder();
				
				vHolder.noteTitleHolder = (TextView) convertView.findViewById(R.id.noteTitle);
				
				convertView.setTag(vHolder);
			}
			else {
				vHolder = (ViewHolder) convertView.getTag();
			}
			
			vHolder.id = mCursor.getLong(mCursor.getColumnIndex(SQLAdapter.KEY_ROWID));
			
			vHolder.noteTitleHolder.setText(mCursor.getString(titleIndex));
		}
		
		return(convertView);
	}
	
	public static class ViewHolder {
		
		TextView noteTitleHolder;
		long id;
	}

}
