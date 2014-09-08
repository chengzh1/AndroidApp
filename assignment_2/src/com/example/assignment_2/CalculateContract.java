package com.example.assignment_2;

import android.provider.BaseColumns;

public final class CalculateContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public CalculateContract() { }

    /* Inner class that defines the table contents */
    public static abstract class CalculateResult implements BaseColumns {
        public static final String TABLE_NAME = "Calculate_Result";
        public static final String COLUMN_NAME_ENTRY_ID = "id";
        public static final String COLUMN_NAME_C1 = "total";
        public static final String COLUMN_NAME_C2 = "month_total";
     
    }
}