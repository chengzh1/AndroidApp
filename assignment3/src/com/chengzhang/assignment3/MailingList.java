package com.chengzhang.assignment3;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

//Send email Activity
public class MailingList extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mailing_list);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mailing_list, menu);
		return true;
	}
	//method to send email
	public void sendEmail(View view){
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		
		String subject = getTextContent(R.id.subject);
		String content = getTextContent(R.id.content);
		
		emailIntent.setType("message/rfc822");
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"wangleehom.com@gmail.com"}); // recipients
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, content);
		startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}
	//method to get text from EditText View
	private String getTextContent(int id){
		EditText editText = (EditText) findViewById(id);
		return editText.getText().toString();		
	}
}
