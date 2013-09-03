package cs.man.ac.uk.tavernamobile.io;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import cs.man.ac.uk.tavernamobile.R;
import cs.man.ac.uk.tavernamobile.utils.MessageHelper;
import cs.man.ac.uk.tavernamobile.utils.SystemStatesChecker;

public class InputsList extends Activity{
	
	// for context access within this class
	private InputsList currentActivity;

	// data used to display
	private ArrayList<Map<String, String>> listData = new ArrayList<Map<String, String>>();
	private ArrayList<String> inputNames;
	private String workflowtitle;
	
	public final static String EXTRA_FILE_PATH = "file_path";
	private final int REQUEST_PICK_FILE = 1;
	private InputsListAdaptor resultListAdapter;
	
	// collection to store all user inputs.
	// this is data needed for the run
	public HashMap<String, Object> userInputs = new HashMap<String, Object>();
	// store the name of the input that user is 
	// currently selecting the input file for
	private String currentInputName;
	private int inputsListSelectedIndex;

	private int Activity_Starter_Code;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inputs);
		
		// UI components
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		
		final Button run = (Button) findViewById(R.id.runButton);
		Button cancel = (Button) findViewById(R.id.cancelButton);
		TextView title = (TextView) findViewById(R.id.input_wfTitle);
		TextView number = (TextView) findViewById(R.id.input_wfNumber);
		ListView resultList = (ListView) findViewById(R.id.inputList);

		// get data
		if (savedInstanceState != null) {
			inputNames = savedInstanceState.getStringArrayList("inputNames");
			workflowtitle =  savedInstanceState.getString("workflow_title");
			Activity_Starter_Code = savedInstanceState.getInt("activity_starter");
		}
		else{
			// get data passed in
			Bundle extras = getIntent().getExtras();
			workflowtitle = extras.getString("workflow_title");
			inputNames = extras.getStringArrayList("input_names");
			Activity_Starter_Code = extras.getInt("activity_starter"); 
		}
		if(inputNames != null && inputNames.size() > 0){
			preparingListData(inputNames);	
		}

		// data setup
		title.setText(workflowtitle);
		if (inputNames != null && inputNames.size() > 1){
			number.setText("This workflow has "+ inputNames.size() + " inputs : ");
		}
		else if(inputNames != null && inputNames.size() == 1){
			number.setText("This workflow has "+ inputNames.size() + " input : ");
		}else{
			number.setText("This workflow has no input ");
		}

		LayoutInflater myInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		resultListAdapter = new InputsListAdaptor(myInflater, listData);
		resultList.setAdapter(resultListAdapter);

		currentActivity = this; // for access of this activity inside OnClickListner
		run.setOnClickListener(new android.view.View.OnClickListener() {

			public void onClick(android.view.View v) {
				SystemStatesChecker sysChecker = new SystemStatesChecker(currentActivity);
				if (!(sysChecker.isNetworkConnected())){
					return;
				}

				if(inputNames != null && inputNames.size() > 0){
					String unSetInputName = inputCheck(userInputs);
					if (unSetInputName != null){
						MessageHelper.showMessageDialog(currentActivity, "Please set input for \"" + unSetInputName+ "\"");
					}
					else{
						run.setEnabled(false);
						startTheRun();
					}
				}
				else{
					run.setEnabled(false);
					startTheRun();
				}				
			}

			private void startTheRun() {
				
				//go to monitor
				Intent goToMonitor = new Intent(currentActivity, RunMonitorScreen.class);
				Bundle extras = new Bundle();
				extras.putString("workflow_title", workflowtitle);
				if(inputNames != null && inputNames.size() > 0){
					extras.putSerializable("userInputs", userInputs);
				}
				extras.putInt("activity_starter", Activity_Starter_Code);
				extras.putString("command", "RunWorkflow");
				goToMonitor.putExtras(extras);
				currentActivity.startActivity(goToMonitor);
			}
		});

		cancel.setOnClickListener(new android.view.View.OnClickListener() {

			public void onClick(android.view.View v) {
				currentActivity.finish();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putStringArrayList("inputNames", inputNames);
		savedInstanceState.putString("workflow_title", workflowtitle);
		savedInstanceState.putInt("activity_starter", Activity_Starter_Code);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		inputNames = savedInstanceState.getStringArrayList("inputNames");
		workflowtitle =  savedInstanceState.getString("workflow_title");
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class InputsListAdaptor extends BaseAdapter {

		private LayoutInflater myInflater;
		private ArrayList<Map<String, String>> data;

		private TextView inputNameView;

		public InputsListAdaptor(LayoutInflater layoutInflater, ArrayList<Map<String, String>> listData)
		{
			myInflater = layoutInflater;
			data = listData;
		}

		public int getCount() {
			return data.size();
		}

		public Object getItem(int position) {
			return data.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			final int selectedInputIndex = position;
			if (convertView == null)
			{
				convertView = myInflater.inflate(R.layout.inputs_single_row, null);
			}

			inputNameView = (TextView) convertView.findViewById(R.id.inputName);
			TextView fileName = (TextView) convertView.findViewById(R.id.selectedFileName);

			Map<String, String> nameFilePair = (Map<String, String>) getItem(position);
			String inputName = nameFilePair.keySet().iterator().next();
			inputNameView.setText(inputName);
			String selfileName = nameFilePair.get(inputName);
			if (selfileName != null){
				fileName.setText(selfileName);
			}

			EditText inputValue = (EditText) convertView.findViewById(R.id.inputValueText);
			inputValue.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener()
			{
				public void onFocusChange(View v, boolean hasFocus)
				{
					if (!hasFocus){
						final EditText inputText = (EditText) v;
						String textInput = inputText.getText().toString();
						userInputs.put(inputNames.get(selectedInputIndex), textInput);
					}
				}
			});

			Button fileSelectButton = (Button) convertView.findViewById(R.id.fileSelectButton);
			fileSelectButton.setOnClickListener(new android.view.View.OnClickListener(){

				public void onClick(View v) {
					currentInputName = inputNames.get(selectedInputIndex);
					inputsListSelectedIndex = selectedInputIndex;
					Intent intent = new Intent(currentActivity, FilePickerActivity.class);
					startActivityForResult(intent, REQUEST_PICK_FILE);
				}
			});

			return convertView;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		File selectedFile = null;
		if(resultCode == RESULT_OK) {
			switch(requestCode) {
			case REQUEST_PICK_FILE:
				if(data.hasExtra(FilePickerActivity.EXTRA_FILE_PATH)) {
					// Get the file URI
					Uri fileUri = Uri.fromFile(new File(data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH)));
					java.net.URI fileURI = null;
					try {
						fileURI = new java.net.URI(fileUri.toString());
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
					selectedFile = new File(fileURI);
				}
			}
		}
		if(selectedFile != null){
			userInputs.put(currentInputName, selectedFile);
			String fileName = data.getStringExtra("selectedFileName");

			HashMap<String, String> newInputNameSelFilePair = new HashMap<String, String>();
			newInputNameSelFilePair.put(currentInputName, fileName);
			listData.set(inputsListSelectedIndex, newInputNameSelFilePair);
			resultListAdapter.notifyDataSetChanged();
		}		
	}

	private void preparingListData(ArrayList<String> inputNames){
		for(String i : inputNames){
			HashMap<String, String> pair = new HashMap<String, String>();
			pair.put(i, null);
			// data neede for run
			userInputs.put(i, null);
			// data used to display
			listData.add(pair);
		}
	}

	private String inputCheck(Map<String, Object> userInputs){
		String unSetInputName = null;
		if (userInputs != null){
			Iterator<Entry<String, Object>> it = userInputs.entrySet().iterator();
			search:
				while(it.hasNext()){
					Map.Entry<String, Object> pair = it.next();
					if(pair.getValue() == null){
						unSetInputName = pair.getKey();
						break search;
					}
				}
		}

		return unSetInputName;
	}
}