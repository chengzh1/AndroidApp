package com.example.assignment_2;

import com.example.assignment_2.CalculateContract.CalculateResult;
import android.os.Bundle;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {
	/** Called when the activity is first created. */
    private static final String[] months={"Jan","Feb","Mar","Apr","May","Jun","Jul",
    										"Aug","Sep","Oct","Nov","Dec"};
    private static final Integer[] years = new Integer[31];
    private Spinner spinnerMonth;
    private Spinner spinnerYear;
    private ArrayAdapter<String> adapterMonth; 
    private ArrayAdapter<Integer> adapterYear;
    
	private CalculateDbHelper mDbHelper;
	
	private int selectedMonth;
	private int selectedYear;
	
    @Override 
    protected void onCreate(Bundle savedInstanceState) {
    
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final TextView v= (TextView) findViewById(R.id.help);
		  v.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					help(v);       
				}
			});
		
		//initialize database
		mDbHelper = new CalculateDbHelper(getApplicationContext());
		//initialize spinner for month and year selection
		for (int i = 0; i <= 30; i ++ ){
			years[i] = i + 2013;
		}
		
		spinnerMonth = (Spinner)findViewById(R.id.spinnerMonth);
		spinnerYear = (Spinner)findViewById(R.id.spinnerYear);    
	    spinnerMonth.setPrompt("Feb");
	    spinnerYear.setSelection(3);
	    adapterMonth = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,months);
	    adapterYear = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item,years);
	    
	   
	    adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    
	   
	    spinnerMonth.setAdapter(adapterMonth);
	    spinnerYear.setAdapter(adapterYear);
	    
		spinnerMonth.setSelection(2);
	    spinnerMonth.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
	    {
	        @Override
	        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	        	// TODO Auto-generated method stub
	        	if(arg2 == 0)
	        		selectedMonth = 11;
	        	else
	        		selectedMonth = arg2 -1;
	        	//设置显示当前选择的项
	        	arg0.setVisibility(View.VISIBLE);
	        	}
	        @Override
	        public void onNothingSelected(AdapterView<?> arg0) {
	        	// TODO Auto-generated method stub  
	        
	        }
	      }); 
	    spinnerYear.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
	    {
	        @Override
	        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	        	// TODO Auto-generated method stub
	        	selectedYear = years[arg2] + 30;
	        	//设置显示当前选择的项
	        	arg0.setVisibility(View.VISIBLE);
	        	}
	        @Override
	        public void onNothingSelected(AdapterView<?> arg0) {
	        	// TODO Auto-generated method stub                     
	        }
	      });  
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
/** Called when the user clicks the Send buton **/
	public void sendMessage(View view){
		Intent intent = new Intent(this, DisplayMessageActivity.class);
		
		EditText edit_purchase_price = (EditText) findViewById(R.id.edit_purchase_price);
		EditText edit_down_payment = (EditText) findViewById(R.id.edit_down_payment);
		EditText edit_mortgage_term = (EditText) findViewById(R.id.edit_mortgage_term);
		EditText edit_interest_rate = (EditText) findViewById(R.id.edit_interest_rate);
		EditText edit_property_taxe = (EditText) findViewById(R.id.edit_property_tax);
		EditText edit_property_insurance = (EditText) findViewById(R.id.edit_property_insurance);

		int purchase_price = Integer.valueOf(edit_purchase_price.getText().toString());
		int mortgage_term = Integer.valueOf(edit_mortgage_term.getText().toString());
		int down_payment = Integer.valueOf(edit_down_payment.getText().toString());
		float interest_rate = Float.valueOf(edit_interest_rate.getText().toString());
		int property_tax = Integer.valueOf(edit_property_taxe.getText().toString());
		int property_insurance = Integer.valueOf(edit_property_insurance.getText().toString());
	
		float month_rate = interest_rate / 12 / 100;
		float total_principal = (float)purchase_price * (1 - (float)down_payment / 100);
	
		float month_property_tax = ((float) property_tax) / 12;
		float month_property_insurance = ((float) property_insurance) / 12;
	
		float month_temp = (float) Math.pow(1 + month_rate,mortgage_term * 12 );
		float month_principal_interest = (total_principal * month_rate * month_temp) / (month_temp - 1);

		float month_total = month_principal_interest + month_property_tax + month_property_insurance;
		float total = month_total * mortgage_term * 12;
	
		intent.putExtra("month", months[selectedMonth]);
		if (selectedMonth == 11)
			selectedYear --;
		intent.putExtra("year", String.valueOf(selectedYear));
	
		// Gets the data repository in write mode
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// Create a new map of values, where column names are the keys
		String stringTotal = String.format("%,.2f", total);
		String stringMonthTotal = String.format("%,.2f", month_total);
		
		ContentValues values = new ContentValues();
		values.put(CalculateResult.COLUMN_NAME_ENTRY_ID, "2");
		values.put(CalculateResult.COLUMN_NAME_C1, stringTotal);
		values.put(CalculateResult.COLUMN_NAME_C2, stringMonthTotal);

		// Insert the new row, returning the primary key value of the new row
		long newRowId;
	
		newRowId = db.insert(
			 CalculateResult.TABLE_NAME,
	         null,
	         values); 
    
		startActivity(intent);    	
	}
	
	public void help(View view){
		DialogFragment helplink = new Helplink();
		helplink.show(getSupportFragmentManager(), "Help");
	}	
	
}
