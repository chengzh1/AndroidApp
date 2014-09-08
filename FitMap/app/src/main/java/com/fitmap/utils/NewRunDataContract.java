package com.fitmap.utils;
import android.provider.BaseColumns;

/**
 * Contract for databse
 */
public class NewRunDataContract implements BaseColumns{
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public NewRunDataContract() { }

    /* Inner class that defines the table contents */
    public static abstract class NewRunSchema implements BaseColumns {
        public static final String TABLE_NAME = "NewRunItems";
        public static final String StartTime = "StartTime";
        public static final String StartL = "StartL";
        public static final String StartB = "StartB";
        public static final String EndL = "EndL";
        public static final String EndB = "EndB";
        public static final String Alarm = "Alarm";
        public static final String isEnd = "isEnd";

        public static final String sortOrder = NewRunSchema.StartTime + " desc";
        public static final String[] projection = {
                                                    _ID,
                                                    StartTime,
                                                    Alarm,
                                                    StartL,
                                                    StartB,
                                                    EndL,
                                                    EndB,
                                                    isEnd
                                                };
    }
}