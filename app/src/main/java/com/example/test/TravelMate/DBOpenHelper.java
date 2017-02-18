package com.example.test.TravelMate;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Angeline Cheah on 1/21/2015.
 */
 public class DBOpenHelper extends SQLiteOpenHelper {

        //initialising all variables

        private static final String DB_NAME = "travelMateDB";
        private static final int DB_VERSION = 1;

        public static final String TRIP_TABLE = "trip";
        public static final String ITIN_TABLE = "itinerary";
        public static final String ITEMS_TABLE = "items";
        public static final String CHECKLIST_TABLE = "checklist";
        public static final String JOURNAL_TABLE = "journal";
        public static final String ITINLIST_TABLE = "itineraryList";

        public static final String TRIP_ID = "trip_ID";
        public static final String TRIP_NAME = "tripName";
        public static final String START_DATE = "startDate";
        public static final String END_DATE = "endDate";
        public static final String DESTINATION = "destination";
        public static final String TRANSPORT_TYPE = "transport_type";

        public static final String ITINLIST_ID = "itinList_ID";
        public static final String ITIN_DATE = "itinDate";

        public static final String ITIN_ID = "itin_ID";
        public static final String TODO = "todo";
        public static final String ITIN_TIME = "itinTime";

        public static final String ITEM_ID = "item_ID";
        public static final String ITEM_DESC= "itemDesc";
        public static final String DEFAULT_ITEM = "default_item";
        public static final String LIST_ID = "list_ID";
        public static final String JOURNAL_ID = "journal_ID";
        public static final String JOURNAL_ENTRY = "journalEntry";
        public static final String JOURNAL_DATE = "journalDate";
        public static final String IMG_PATH = "img_path" ;

        //create SQLite syntax
        public static final String CREATE_TRIP_TABLE = "CREATE TABLE " + TRIP_TABLE
                + "(" + TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TRIP_NAME + " TEXT NOT NULL, " + START_DATE + " DATE NOT NULL, "
                + END_DATE + " DATE NOT NULL, " + DESTINATION + " TEXT NOT NULL, "
                + TRANSPORT_TYPE + " TEXT NOT NULL " + ")";

        public static final String CREATE_ITINLIST_TABLE = "CREATE TABLE " + ITINLIST_TABLE
            + "(" + ITINLIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ITIN_DATE + " TEXT NOT NULL, " + TRIP_ID + " INTEGER, "
            + "FOREIGN KEY(" + TRIP_ID + ") REFERENCES " + TRIP_TABLE + "(trip_ID)" + ")";

        public static final String CREATE_ITIN_TABLE = "CREATE TABLE " + ITIN_TABLE
                + "(" + ITIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ITIN_TIME + " TEXT NOT NULL, "
                + TODO + " TEXT NOT NULL, " + ITINLIST_ID + " INTEGER, "
                + "FOREIGN KEY(" + ITINLIST_ID + ") REFERENCES " + ITINLIST_TABLE + "(itinList_ID)" + ")";

        public static final String CREATE_ITEMS_TABLE = "CREATE TABLE " + ITEMS_TABLE
                + "(" + ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ITEM_DESC + " TEXT NOT NULL, " + DEFAULT_ITEM + " INTEGER NOT NULL " + ")";

        public static final String CREATE_CHECKLIST_TABLE = "CREATE TABLE " + CHECKLIST_TABLE
                + "(" + LIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TRIP_ID + " INTEGER REFERENCES " + TRIP_TABLE + "(trip_ID), "
                + ITEM_ID + " INTEGER REFERENCES " + ITEMS_TABLE + "(item_ID)" + ")";

        public static final String CREATE_JOURNAL_TABLE = "CREATE TABLE " + JOURNAL_TABLE
                + "(" + JOURNAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + JOURNAL_ENTRY + " TEXT NOT NULL, "
                + JOURNAL_DATE + " DATE NOT NULL, " + IMG_PATH + " TEXT, " + TRIP_ID + " INTEGER, "
                + "FOREIGN KEY(" + TRIP_ID + ") REFERENCES " + TRIP_TABLE + "(trip_ID)" + ")";


        public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, DB_NAME, factory, DB_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase sqlDB) { //execute all SQL syntax

            String defaultItems = "INSERT INTO " + ITEMS_TABLE
                    + " (" + ITEM_DESC + ", " + DEFAULT_ITEM + ") VALUES ('Passport', '0'), ('Wallet/Purse', '0'), ('Charger', '0')";
            sqlDB.execSQL(CREATE_TRIP_TABLE);
            sqlDB.execSQL(CREATE_ITINLIST_TABLE);
            sqlDB.execSQL(CREATE_ITIN_TABLE);
            sqlDB.execSQL(CREATE_ITEMS_TABLE);
            sqlDB.execSQL(CREATE_CHECKLIST_TABLE);
            sqlDB.execSQL(CREATE_JOURNAL_TABLE);
            sqlDB.execSQL(defaultItems);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqlDB, int oldVersion, int newVersion) { //upgrade database upon releasing an update
        }
 }



