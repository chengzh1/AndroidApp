package com.fitmap.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * This Class helps display custom alert dialog.
 */
public class AlertDialogManager {

    /**
     * Display simple Alert Dialog
     * @param context  application context
     * @param title  alert dialog title
     * @param message  alert message
     * @param status  success/failure
     */
    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }
}
