package com.example.test.TravelMate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Angeline Cheah on 1/23/2015.
 */
public class TripDB {

    private SQLiteDatabase db;
    private DBOpenHelper dbHelper;
    private Context _context;

    public TripDB (Context context) {
        _context = context;
        dbHelper = new DBOpenHelper(context, "travelMateDB", null, 1);
        open();
    }

    public void open() {
        if(dbHelper == null) {
            dbHelper = new DBOpenHelper(_context, "travelMateDB", null, 1);
        }
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        db.close();
    }


    public void saveTrip(String tripName, String startDate, String endDate, String destination, String transport) {

        ContentValues addTrip  = new ContentValues();
        addTrip.put(dbHelper.TRIP_NAME,tripName);
        addTrip.put(dbHelper.START_DATE,startDate);
        addTrip.put(dbHelper.END_DATE,endDate);
        addTrip.put(dbHelper.DESTINATION,destination);
        addTrip.put(dbHelper.TRANSPORT_TYPE,transport);

        db.insert(dbHelper.TRIP_TABLE, null, addTrip);
    }

    public Cursor getOneTrip(long id) {

        String getOneTripSQL = ("SELECT * FROM trip WHERE trip_ID=" + id);
        return db.rawQuery(getOneTripSQL, null);
    }

    public Cursor getAllTrips() {

        String getAllTripsSQL = ("SELECT trip_ID as _id, tripName FROM trip");
        return db.rawQuery(getAllTripsSQL, null);
    }

    public void updateTripDetails(long id, String startDate, String endDate, String destination, String transport) {
        ContentValues updateTrip = new ContentValues();
        updateTrip.put(dbHelper.START_DATE, startDate);
        updateTrip.put(dbHelper.END_DATE, endDate);
        updateTrip.put(dbHelper.DESTINATION, destination);
        updateTrip.put(dbHelper.TRANSPORT_TYPE, transport);

        db.update(dbHelper.TRIP_TABLE, updateTrip, "trip_ID=" + id, null);
    }

    public void deleteTrip(long id) {

        db.execSQL("DELETE FROM " + dbHelper.TRIP_TABLE + " WHERE trip_ID=" + id);
    }

    public void saveDefaultChecklistItem(int item_ID, long id) {

        ContentValues saveDefaultChecklist = new ContentValues();
        saveDefaultChecklist.put(dbHelper.TRIP_ID, id);
        saveDefaultChecklist.put(dbHelper.ITEM_ID,item_ID);

        db.insert(dbHelper.CHECKLIST_TABLE, null, saveDefaultChecklist);
    }

    public void addNewItem(String item, long id) {
        ContentValues addItem = new ContentValues();
        addItem.put(dbHelper.ITEM_DESC, item);
        addItem.put(dbHelper.DEFAULT_ITEM, "1");

        db.insert(dbHelper.ITEMS_TABLE, null, addItem);
    }

    public void addItemChecklist (String item, long id) {
       Cursor c = getItemId(item);
       c.moveToFirst();
       int itemDesc = c.getInt(0);

       String addChecklistItem = ("INSERT INTO checklist (trip_ID, item_ID) VALUES (" + id + "," + itemDesc + ")");
       db.execSQL(addChecklistItem);
    }

    public Cursor getItemId (String item) {

        String getItemId = ("SELECT item_ID as _id FROM items WHERE itemDesc='" + item + "'" );
        return db.rawQuery(getItemId, null);
    }

    public void addChecklistItem (int itemID, long id) {
        ContentValues addListItem = new ContentValues();
        addListItem.put(dbHelper.TRIP_ID, id);
        addListItem.put(dbHelper.ITEM_ID, itemID);

        db.insert(dbHelper.CHECKLIST_TABLE, null, addListItem);
    }

    public Cursor getDefaultItems() {

        String getDefaultItems = ("SELECT item_ID as _id FROM items WHERE default_item= 0");
        return db.rawQuery(getDefaultItems, null);
    }

    public Cursor getAllItems() {

        String getAllItems = ("SELECT item_ID as _id, itemDesc FROM items");
        return db.rawQuery(getAllItems, null);
    }

    public void deleteItem (long id) {
        db.execSQL("DELETE FROM " + dbHelper.ITEMS_TABLE + " WHERE item_ID=" + id);
        db.execSQL("DELETE FROM " + dbHelper.CHECKLIST_TABLE + " WHERE item_ID=" + id);
    }

    public Cursor getChecklistItems(long id) {
        String getOneChecklist = ("SELECT list_ID as _id, itemDesc FROM items i INNER JOIN checklist c ON i.item_ID=c.item_ID WHERE c.trip_ID=" + id);
        return db.rawQuery(getOneChecklist, null);
    }

    public void deleteChecklistItem(long id) {

        db.execSQL("DELETE FROM " + dbHelper.CHECKLIST_TABLE + " WHERE list_ID=" + id);
    }

    public void createItinerary(String itin_dateSel, Long id) {
        ContentValues createItin = new ContentValues();
        createItin.put(dbHelper.ITIN_DATE, itin_dateSel);
        createItin.put(dbHelper.TRIP_ID, id);

        db.insert(dbHelper.ITINLIST_TABLE, null, createItin);
    }

    public void addItinerary(String time, String todo, long id) {

        ContentValues addItin = new ContentValues();
        addItin.put(dbHelper.ITIN_TIME, time);
        addItin.put(dbHelper.TODO, todo);
        addItin.put(dbHelper.ITINLIST_ID, id);

        db.insert(dbHelper.ITIN_TABLE, null, addItin);
    }

    public Cursor getItinerary(long id) {
        String getItin = ("SELECT itin_ID as _id, itinTime, todo FROM itinerary WHERE itinList_ID="
                + id + " ORDER BY itinTime ASC");
        return db.rawQuery(getItin, null);
    }

    public Cursor getItineraryDate(long id) {
        String getItineraryDate = ("SELECT itinDate FROM itineraryList WHERE itinList_ID=" + id);
        return db.rawQuery(getItineraryDate, null);
    }

    public Cursor getTripItineraryList(long id) {
        String getTripItinList = ("SELECT itinList_ID as _id, itinDate FROM itineraryList WHERE trip_ID="
                + id + " ORDER BY itinDate DESC");
        return db.rawQuery(getTripItinList, null);
    }

    public Cursor getAllItineraries () {
        String getAllItin = ("SELECT i.itinList_ID as _id, i.itinDate, t.tripName " +
                "FROM itineraryList i INNER JOIN trip t ON i.trip_ID=t.trip_ID ORDER BY i.trip_ID DESC, i.itinDate DESC");
        return db.rawQuery(getAllItin, null);
    }

    public void updateItinerary(long rowID, String newTime, String newTodo) {

        ContentValues updateItin = new ContentValues();
        updateItin.put(dbHelper.ITIN_TIME, newTime);
        updateItin.put(dbHelper.TODO, newTodo);

        db.update(dbHelper.ITIN_TABLE, updateItin, "itin_ID=" + rowID, null);
    }

    public void deleteItinerary(long id) {
        db.execSQL("DELETE FROM " + dbHelper.ITINLIST_TABLE + " WHERE itinList_ID=" + id);
    }

    public void deleteSchedule(long id) {
        db.execSQL("DELETE FROM " + dbHelper.ITIN_TABLE + " WHERE itin_ID=" + id);
    }

    public Cursor getJournals(long id) {
        String getJournals = ("SELECT j.journal_ID as _id, j.journalEntry, j.journalDate, t.tripName " +
                "FROM journal j INNER JOIN trip t" + " ON j.trip_ID = t.trip_ID WHERE j.trip_ID=" +
                id +" ORDER BY j.trip_ID DESC, j.journalDate DESC ");
        return db.rawQuery(getJournals,null);
    }

    public Cursor getAllJournals(){
        String getAllJournals = ("SELECT j.journal_id as _id, j.journalEntry, j.journalDate, t.tripName " +
                "FROM journal j INNER JOIN trip t ON j.trip_ID=t.trip_ID ORDER BY j.journal_ID DESC " );
        return db.rawQuery(getAllJournals, null);
    }

    public void saveJournal (long id, String journalText, String journalDate, String imagePathList) {
        ContentValues saveJournal = new ContentValues();
        saveJournal.put(dbHelper.TRIP_ID, id);
        saveJournal.put(dbHelper.JOURNAL_ENTRY,journalText);
        saveJournal.put(dbHelper.JOURNAL_DATE,journalDate);
        saveJournal.put(dbHelper.IMG_PATH, imagePathList);

        db.insert(dbHelper.JOURNAL_TABLE, null, saveJournal);
    }

    public Cursor viewJournal(long id) {
        String viewJournal = ("SELECT journalDate, journalEntry, img_path FROM journal WHERE journal_ID=" + id);
        return db.rawQuery(viewJournal, null);

    }

    public void updateJournal(long id, String journalText, String journalDate, String imagePathList){
        ContentValues updateJournal = new ContentValues();
        updateJournal.put(dbHelper.JOURNAL_ENTRY, journalText);
        updateJournal.put(dbHelper.JOURNAL_DATE, journalDate);
        updateJournal.put(dbHelper.IMG_PATH, imagePathList);

        db.update(dbHelper.JOURNAL_TABLE, updateJournal, "journal_ID=" + id, null);
    }

    public void deleteJournal(long id) {
        db.execSQL("DELETE FROM journal WHERE journal_ID="+ id);
    }
}