package com.example.assignment_2;

import com.example.assignment_2.CalculateContract.CalculateResult;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.widget.TextView;

public class DisplayMessageActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_display_message);
		  
        Intent intent = getIntent();
		CalculateDbHelper mDbHelper = new CalculateDbHelper(getApplicationContext());
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		String[] projection = {
			CalculateResult._ID,
			CalculateResult.COLUMN_NAME_C1,
			CalculateResult.COLUMN_NAME_C2
		};

		// How you want the results sorted in the resulting Cursor
		String sortOrder = "_id" + " DESC";

		Cursor cursor = db.query(
			CalculateResult.TABLE_NAME,  			// The table to query
		    projection,                             // The columns to return
		    null,                                	// The columns for the WHERE clause
		    null,                            		// The values for the WHERE clause
		    null,                                   // don't group the rows
		    null,                                   // don't filter by row groups
		    sortOrder                               // The sort order
		);
		
		cursor.moveToFirst();
		String total = cursor.getString(
		    cursor.getColumnIndex(CalculateResult.COLUMN_NAME_C1)
		);
		String month_total = cursor.getString(
			cursor.getColumnIndex(CalculateResult.COLUMN_NAME_C2)
		);
        
		String month = intent.getStringExtra("month");
        String year = intent.getStringExtra("year");
        
        TextView totalMonthTextView = (TextView) findViewById(R.id.total_month);
        totalMonthTextView.setText("Total Monthly Payment: $" + month_total);
        
        TextView totalTermTextView = (TextView) findViewById(R.id.total_term);
        totalTermTextView.setText("Total Payment: $" + total);
        
        TextView dateTextView = (TextView) findViewById(R.id.date);
        dateTextView.setText("Pay off date: " + month + " " + year);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_message, menu);
		return true;
	}
}
