//MainActivity.java
//Managing flickr searches

package com.example.flickrsearch;

import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends ListActivity {
	//name of SharedPreferences XML file storing saved searches
	private static final String SEARCHES = "searches"; 
	
	private EditText queryEditText; // editText for Query
	private EditText tagEditText; // editText for Tags
	private SharedPreferences savedSearches; // tagged searches
	private ArrayList<String> tags; // tags list
	private ArrayAdapter<String> adapter; // binds tags
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		queryEditText = (EditText) findViewById(R.id.queryEditText);
	    tagEditText = (EditText) findViewById(R.id.tagEditText);
	      
	    // get the SharedPreferences containing the user's saved searches 
	    savedSearches = getSharedPreferences(SEARCHES, MODE_PRIVATE); 

	    // store the saved tags in an ArrayList and sort them
	    tags = new ArrayList<String>(savedSearches.getAll().keySet());
	    Collections.sort(tags, String.CASE_INSENSITIVE_ORDER); 
	      
	    // create ArrayAdapter to bind tags to the ListView
	    adapter = new ArrayAdapter<String>(this, R.layout.list_item, tags);
	    setListAdapter(adapter);

	    ImageButton saveButton = (ImageButton) findViewById(R.id.saveButton);
	    saveButton.setOnClickListener(saveButtonListener);
	    
	    // register listener that searches flickr when user touches a tag
	    getListView().setOnItemClickListener(itemClickListener);  
	      
	    // set listener for delete or edit on a search
	    getListView().setOnItemLongClickListener(itemLongClickListener);  
	}

	// save button listener
	public OnClickListener saveButtonListener = new OnClickListener(){
		@Override
		public void onClick(View v){
			if (queryEditText.getText().length() > 0 &&
					tagEditText.getText().length() > 0){
				addTaggedSearch(queryEditText.getText().toString(),
						tagEditText.getText().toString());
				queryEditText.setText(""); // clear query field
				tagEditText.setText(""); // clear tag field
				
				((InputMethodManager) getSystemService(
						Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
								tagEditText.getWindowToken(), 0);
			} // creates tag if both fields filled
			else {
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				// new AlertDialog Builder
				
				builder.setMessage(R.string.missingMessage);
				// set dialogue title and message
				
				builder.setPositiveButton(R.string.OK, null);
				// OK button dismissing dialog
				
				AlertDialog errorDialog = builder.create();
				errorDialog.show(); // display dialog
			} // need both to be filled
		}
	};
	
	private void addTaggedSearch(String query, String tag){
		SharedPreferences.Editor preferencesEditor = savedSearches.edit();
		preferencesEditor.putString(tag, query);
		preferencesEditor.apply();
		
		if(!tags.contains(tag)){
			tags.add(tag);
			Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
			adapter.notifyDataSetChanged();
		} // add tag if new and sorts before displaying
	}// add new search to save file
	
	OnItemClickListener itemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id){
			String tag = ((TextView) view).getText().toString(); // search tag
			String urlString = getString(R.string.searchURL)+
					Uri.encode(savedSearches.getString(tag, ""), "UTF-8"); // flickr + tag
			
			Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
			
			startActivity(webIntent); // launches web
		} // launch browser to display results
	};
	
	OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener(){
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id){
			final String tag = ((TextView) view).getText().toString(); // gets tag
			
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			
			builder.setTitle(getString(R.string.shareEditDeleteTitle,tag));
			
			builder.setItems(R.array.dialog_items,new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which){
					case 0: //share
						shareSearch(tag);
						break;
					case 1: // edit
						tagEditText.setText(tag);;
						queryEditText.setText(savedSearches.getString(tag, ""));
						break;
					case 2: // delete
						deleteSearchTag(tag);
						break;
					}
				}
			});
			
			builder.setNegativeButton(getString(R.string.cancel),
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int id){
					dialog.cancel();
				} // cancel button to cancel dialog
			});
			
			builder.create().show(); // display AlertDialog
			return true;	
		}
	};
	
	private void shareSearch(String tag){
		String urlString = getString(R.string.searchURL)+
				Uri.encode(savedSearches.getString(tag,""),"UTF-8");
		
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shareSubject));
		shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareMessage,urlString));
		shareIntent.setType("text/plain");
		
		startActivity(Intent.createChooser(shareIntent, getString(R.string.shareSearch)));
	} // choose app to share url and displays it
	
	private void deleteSearchTag(final String tag){
		AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);
		
		confirmBuilder.setMessage(getString(R.string.confirmMessage,tag));
		
		confirmBuilder.setNegativeButton(getString(R.string.cancel), 
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				dialog.cancel();
			}
		}); // cancel button
		
		confirmBuilder.setPositiveButton(getString(R.string.delete), 
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				tags.remove(tag);
				
				SharedPreferences.Editor preferencesEditor = savedSearches.edit();
				preferencesEditor.remove(tag);
				preferencesEditor.apply();
				
				adapter.notifyDataSetChanged();
			}
		}); // delete the tag and update
		
		confirmBuilder.create().show(); // display AlertDialog
	} // deletes search tag
}
