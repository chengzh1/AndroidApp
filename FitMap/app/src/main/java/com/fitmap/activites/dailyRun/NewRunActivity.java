package com.fitmap.activites.dailyRun;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.fitmap.R;
import com.fitmap.utils.NewRunDbHelper;
import com.fitmap.utils.TabsControl;
import com.fitmap.utils.TimeHelper;

/**
 * NewRunActivity: Activity for Daily Run Item
 */
public class NewRunActivity extends Activity implements NewRunListFragment.OnNewRunItemSelectedListener,
                                                        NewRunListFragment.OnLongPressListener,
                                                        AddNewRunItem.onSaveListener
{
    public String TAG = "Daily Run Activity";
    public static int itemPosition = -1;
    public static String itemStatTime;
    public static double itemStatL = 0, itemStartB = 0, itemEndL = 0, itemEndB = 0;
    public static int itemIsEnd;

    private NewRunListFragment newRunList;
    private NewRunItemFragment newRunItem;

    private NotificationManager nm;
    private AlarmManager am;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_run);

        nm = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        am = (AlarmManager)(getSystemService( Context.ALARM_SERVICE ));

        TabsControl.controlTabs(this);

        Log.w(TAG, "activity created");

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
           newRunList = new NewRunListFragment();

            // In case this activity was started with special instructions from an
            // intent, pass the Intent's extras to the fragment as arguments
            newRunList.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, newRunList).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_run, menu);

        //set backstack changed listener
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int stackHeight = getFragmentManager().getBackStackEntryCount();
                ActionBar bar = getActionBar();
                if (bar != null) {
                    // if we have something on the stack
                    if (stackHeight > 0) {
                        bar.setHomeButtonEnabled(true);
                        bar.setDisplayHomeAsUpEnabled(true);
                        MenuItem item = menu.findItem(R.id.new_run_add);
                        item.setVisible(false);
                    } else {
                        bar.setDisplayHomeAsUpEnabled(false);
                        bar.setHomeButtonEnabled(false);
                        MenuItem item = menu.findItem(R.id.new_run_add);
                        item.setVisible(true);
                    }
                }
            }
        });

        //Go to NewRunItemFragment if some item is selected
        if (itemPosition != -1)
            onNewRunItemSelected(itemPosition, itemStatTime, itemStatL,
                    itemStartB, itemEndL, itemEndB, itemIsEnd);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        if (id == R.id.new_run_add) {
            FragmentManager fm = getFragmentManager();
            AddNewRunItem addNewRunItem = new AddNewRunItem();
            addNewRunItem.show(fm, "Add New Run");
            return true;
        } else if (id == android.R.id.home) {
            if (newRunItem != null)
                newRunItem.clear();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * On selected one of the daily run item, listener for NewRunItem Fragment
     * @param position  selected item
     * @param startTime start time of run item
     * @param startL    start longitude of run item
     * @param startB    start latitude of run item
     * @param endL      end longitude of run item
     * @param endB      end latitude of run item
     * @param isEnd     if the item's goal is completed
     */
    @Override
    public void onNewRunItemSelected(int position, String startTime, double startL, double startB,
                                     double endL, double endB, int isEnd) {
        itemPosition = position;
        itemStatTime = startTime;
        itemStatL = startL;
        itemStartB = startB;
        itemEndL = endL;
        itemEndB = endB;
        itemIsEnd = isEnd;

        NewRunItemFragment itemFragment = (NewRunItemFragment)
                getFragmentManager().findFragmentById(R.id.newrunItem_fragment);

        if (itemFragment != null) {
            // in two-pane layout
            itemFragment.updateView(position, startTime, startL, startB, endL, endB, isEnd);

        } else {
            if (isEnd == 1){
                itemPosition = -1;
                itemStatTime = null;
                itemStatL = 0;
                itemStartB = 0;
                itemEndL = 0;
                itemEndB = 0;
                itemIsEnd = 0;

                Intent intent = new Intent(this, NewRunResult.class);
                intent.putExtra(NewRunResult.TAG, startTime);
                startActivity(intent);
                return;
            }
            newRunItem = new NewRunItemFragment();
            Bundle args = new Bundle();
            args.putInt(NewRunItemFragment.ARG_POSITION, position);
            args.putString(NewRunItemFragment.ARG_STARTTIME, startTime);
            args.putDouble(NewRunItemFragment.ARG_STARTL, startL);
            args.putDouble(NewRunItemFragment.ARG_STARTB, startB);
            args.putDouble(NewRunItemFragment.ARG_ENDL, endL);
            args.putDouble(NewRunItemFragment.ARG_ENDB, endB);
            args.putInt(NewRunItemFragment.ARG_ISEND, isEnd);

            newRunItem.setArguments(args);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, newRunItem);
            transaction.addToBackStack("head");

            //add animation
            transaction.setTransition(TRIM_MEMORY_RUNNING_LOW);

            // Commit the transaction
            transaction.commit();
        }
    }

    /**
     * refresh when switch between fragments
     * @param id alarm id
     * @param ifAlarm if the item should be alarmed
     * @param startTime the time item should be alarmed
     */
    @Override
    public void refresh(final int id, boolean ifAlarm, String startTime) {
        if (newRunList != null)
            newRunList.getData();
        if (! ifAlarm)
            return;

        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                Toast.makeText(c, "Alarm!", Toast.LENGTH_LONG).show();
                Intent notificationIntent = new Intent(c, NewRunActivity.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_HISTORY);
                PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                        0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                Resources res = c.getResources();
                Notification.Builder builder = new Notification.Builder(c);

                builder.setContentIntent(contentIntent)
                       .setSmallIcon(R.drawable.daily_run)
                       .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.run_logo))
                       .setTicker("FitMap Notification")
                       .setWhen(System.currentTimeMillis())
                       .setAutoCancel(true)
                       .setContentTitle("FitMap Notification ")
                       .setContentText("It's time to Run!");
                Notification n = builder.build();
                nm.notify(id,n);
            }
        };
        registerReceiver(br, new IntentFilter("com.fitmap"));
        PendingIntent pi = PendingIntent.getBroadcast(this, id, new Intent("com.fitmap"),0);

        am.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() +
                TimeHelper.remainTime(startTime), pi);
    }

    /**
     * When long pressed the item
     * @param position position of the item
     */
    @Override
    public void onLongPress(final int position) {
        //new alert dialog for deletion
        new AlertDialog.Builder(this)
                .setTitle("Delete Daily Run Entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        NewRunDbHelper mDbHelper = new NewRunDbHelper(getApplication());
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();
                        int id = mDbHelper.deleteByPosition(db, position);
                        if (id >= 0) {
                            //cancel alarm
                            nm.cancel(id);
                            PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), id,
                                                                          new Intent("com.fitmap"),0);
                            am.cancel(pi);
                            refresh(0, false, null);
                        }
                        db.close();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            //When press back button
            case KeyEvent.KEYCODE_BACK:
                if (newRunItem != null)
                    newRunItem.clear();
        }

        return super.onKeyDown(keycode, e);
    }
}
