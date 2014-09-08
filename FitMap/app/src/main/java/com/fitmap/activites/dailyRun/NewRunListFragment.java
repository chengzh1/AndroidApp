package com.fitmap.activites.dailyRun;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fitmap.R;
import com.fitmap.utils.NewRunDataContract.NewRunSchema;
import com.fitmap.utils.NewRunDbHelper;

/**
 * Fragment to list all Daily Run Item
 */
public class NewRunListFragment extends ListFragment{
    OnNewRunItemSelectedListener mCallback;
    OnLongPressListener mLongPress;
    private NewRunDbHelper mDbHelper;
    private CustomCursorAdapter dataAdapter;
    private Cursor cursor;

    private ProgressDialog pDialog;

    /**
     * Called when an item is selected
     */
    public interface OnNewRunItemSelectedListener {
        public void onNewRunItemSelected(int position, String startTime, double startL, double startB,
                                         double endL, double endB, int isEnd);
    }

    public interface  OnLongPressListener {
        public void onLongPress(int position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize database
        mDbHelper = new NewRunDbHelper(getActivity());
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> a, View v,
                                           int position, long id) {
                mLongPress.onLongPress(position);
                return true;
            }
        });

        // Give some text to display if there is no data.
        setEmptyText("No Daily Run Item");
        getData();
    }

    @Override
    public void onStart() {
        super.onStart();
        // When in two-pane layout, set the listview to highlight the selected list item
        // (We do this during onStart because at the point the listview is available.)
        if (getFragmentManager().findFragmentById(R.id.newrunItem_fragment) != null) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnNewRunItemSelectedListener) activity;
            mLongPress = (OnLongPressListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Notify the parent activity of selected item
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            cursor.move(position);
            String startTime = cursor.getString(cursor.getColumnIndex(NewRunSchema.StartTime));
            double startL = cursor.getDouble(cursor.getColumnIndex(NewRunSchema.StartL));
            double startB = cursor.getDouble(cursor.getColumnIndex(NewRunSchema.StartB));
            double endL = cursor.getDouble(cursor.getColumnIndex(NewRunSchema.EndL));
            double endB = cursor.getDouble(cursor.getColumnIndex(NewRunSchema.EndB));
            int isEnd = cursor.getInt(cursor.getColumnIndex(NewRunSchema.isEnd));

            mCallback.onNewRunItemSelected(position, startTime, startL, startB, endL, endB, isEnd);
        }
        // Set the item as checked to be highlighted when in two-pane layout
        getListView().setItemChecked(position, true);
    }

    /**
     * Get Date from dataBase
     */
    public void getData(){
        new LoadEntry().execute();
    }

    /**
     * Loading Date in AsyncTask
     */
    class LoadEntry extends AsyncTask<String, String, String> {
        private SQLiteDatabase db;

        public LoadEntry(){
            db = mDbHelper.getReadableDatabase();
        }

        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage(Html.fromHtml("<b>Loading Data...</b>"));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            cursor = db.query(
                    NewRunSchema.TABLE_NAME,  			    // The table to query
                    NewRunSchema.projection,                // The columns to return
                    null,                                	// The columns for the WHERE clause
                    null,                            		// The values for the WHERE clause
                    null,                                   // don't group the rows
                    null,                                   // don't filter by row groups
                    NewRunSchema.sortOrder                  // The sort order
            );
            if (cursor == null)
                return null;
            dataAdapter = new CustomCursorAdapter(getActivity(), cursor, 0);
            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            setListAdapter(dataAdapter);
            pDialog.dismiss();
        }
    }

    /**
     * Custom Cursor Adapter
     */
    public class CustomCursorAdapter extends CursorAdapter {

        private LayoutInflater mInflater;

        public CustomCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView startTextView = (TextView) view.findViewById(R.id.new_run_list_start_time);
            TextView alarmTextView = (TextView) view.findViewById(R.id.new_run_list_alarm);
            TextView completeTextView = (TextView) view.findViewById(R.id.new_run_list_complete);
            startTextView.setText(cursor.getString(cursor.getColumnIndex(NewRunSchema.StartTime)));
            alarmTextView.setText(cursor.getString(cursor.getColumnIndex(NewRunSchema.Alarm)));
            int ifComplete = cursor.getInt(cursor.getColumnIndex(NewRunSchema.isEnd));
            if (ifComplete == 1){
                completeTextView.setText("Completed");
                completeTextView.setTextColor(Color.LTGRAY);
            }else {
                completeTextView.setText("Not Start");
                completeTextView.setTextColor(Color.BLACK);
            }

        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(R.layout.new_run_list, parent, false);
        }

    }

}