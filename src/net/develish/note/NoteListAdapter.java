/*
 * Author: Kristoffer Pedersen
 * Mail: deifyed <Guess (hint: its an @)> gmail.com
 * 
 * License:
 * I take no responsibility what so ever of what you decide to do with this code.
 * You are free to use and/or modify it as you wish. 
 */

package net.develish.note;

import java.util.ArrayList;

import com.cognitiveadventures.note.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class NoteListAdapter extends SimpleCursorAdapter {
	
	private Cursor mCursor;
	private int titleIndex;
	private ArrayList<Long> deletePos;
	
	private LayoutInflater li;

	public NoteListAdapter(Context context, int layout, Cursor c, String from, int to) {
		
		super(context, layout, c, new String[] { from }, new int[] { to });
		
		li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mCursor = c;
		
		titleIndex = c.getColumnIndexOrThrow(from);
		
		deletePos = new ArrayList<Long>();
	}
	
	public ArrayList<Long> getSelectedItems() {
		
		return(deletePos);
	}
	
	public int getSelectedCount() {
		
		return(deletePos.size());
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if(mCursor.moveToPosition(position)) {
			
			ViewHolder vHolder;
			
			if(convertView == null) {
				
				convertView = li.inflate(R.layout.loadnotes_item, parent, false);
				
				vHolder = new ViewHolder();
				
				vHolder.noteTitleHolder = (TextView) convertView.findViewById(R.id.noteTitle);
				vHolder.noteCheckBoxHolder = (CheckBox) convertView.findViewById(R.id.noteCheckBox);
				
				vHolder.noteCheckBoxHolder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					
					public void onCheckedChanged(CompoundButton v, boolean isChecked) {
						
						if(isChecked)
							deletePos.add(((ViewHolder)((View) v.getParent()).getTag()).id);
						else
							deletePos.remove(((ViewHolder)((View) v.getParent()).getTag()).id);
					}
				});
				
				convertView.setTag(vHolder);
			}
			else {
				vHolder = (ViewHolder) convertView.getTag();
			}
			
			vHolder.id = mCursor.getLong(mCursor.getColumnIndex(SQLAdapter.KEY_ROWID));
			
			if(deletePos.contains(vHolder.id))
				vHolder.noteCheckBoxHolder.setChecked(true);
			
			vHolder.noteTitleHolder.setText(mCursor.getString(titleIndex));
		}
		
		return(convertView);
	}
	
	public static class ViewHolder {
		
		TextView noteTitleHolder;
		CheckBox noteCheckBoxHolder;
		long id;
	}

}
