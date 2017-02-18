package com.example.test.TravelMate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Angeline Cheah on 1/23/2015.
 */

public class ViewItin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_itin);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle extras = getIntent().getExtras();
        long ID = extras.getLong("itinListID");

        Bundle itinListID = new Bundle();
        itinListID.putLong("itinListID", ID);

        ViewItineraryFragment viewItinFrag = new ViewItineraryFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, viewItinFrag)
                    .commit();
        }
        viewItinFrag.setArguments(itinListID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_itin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ViewItineraryFragment extends Fragment {

        TableLayout itin_table;
        TableRow itin_row;
        TextView itin_date;
        TextView itin_date_set;
        Bundle getTripID;
        private EditText newTimeInput;
        private EditText new_activity;
        private TextView itin_time;
        private EditText itin_todo;
        private SimpleDateFormat timeFormatter_12;
        private SimpleDateFormat timeFormatter_24;
        private Button addNewItinBtn;
        private Button editItinBtn;
        private Cursor c, c2;
        private KeyListener keyListener;

        public  ViewItineraryFragment() {
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            getTripID = this.getArguments();
            final Long ID = getTripID.getLong("itinListID");

            View itinView = inflater.inflate(R.layout.fragment_view_itin, container, false);
            itin_date = (TextView) itinView.findViewById(R.id.itin_date);
            itin_date_set = (TextView) itinView.findViewById(R.id.itin_date_set);
            newTimeInput = (EditText) itinView.findViewById(R.id.newItin_time);
            new_activity = (EditText) itinView.findViewById(R.id.new_Activity);
            itin_table = (TableLayout) itinView.findViewById(R.id.itin_table);
            addNewItinBtn = (Button) itinView.findViewById(R.id.addNewItin_Btn);
            editItinBtn = (Button) itinView.findViewById(R.id.editItnBtn);

            editItinBtn.setTag(1);
            timeFormatter_12 = new SimpleDateFormat("h:mm a"); //12-hour format date
            timeFormatter_24 = new SimpleDateFormat("HH:mm"); //24-hour format date

            retrieveItinerary(ID); //get all schedules for the trip

            //input time for new schedule
            newTimeInput.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            Calendar newTime = Calendar.getInstance();
                            newTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                            newTime.set(Calendar.MINUTE, selectedMinute);
                            String selectedTime = timeFormatter_12.format(newTime.getTime());
                            newTimeInput.setText(selectedTime);
                        }
                    }, hour, minute, false);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();
                }
            });

            //input activity for new schedule
            addNewItinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //check empty input
                    if (newTimeInput.getText().toString().trim().length() == 0 ||
                            new_activity.getText().toString().trim().length() == 0) {
                        Toast.makeText(getActivity(), "Time or activity must not be empty!", Toast.LENGTH_SHORT).show();
                    } else {
                        new PopulateItinerary(ID).execute(); //add new schedule using asynctask
                        Toast.makeText(getActivity(), "Time and activity have been successfully added!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //edit schedule
            editItinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int status = (Integer)view.getTag();
                    if (status == 1) {
                        enableViews();
                        editItinBtn.setText("Update");
                        editItinBtn.setTag(2);
                    }else if (status == 2) {
                        updateSchedule();
                    }
                }
            });
            return itinView;
        }

        private void enableViews() { //enable view for editing

            if (itin_table.getChildCount() > 0) { //if table has row(s)
                for (int i = 0; i < itin_table.getChildCount(); i++) { //loop row to get child
                    View rowView = itin_table.getChildAt(i);

                    if (rowView instanceof TableRow) {
                        TableRow row = (TableRow) rowView; //case rowview to row
                        for (int k = 0; k < row.getChildCount(); k++) {

                            if (k == 0) { //if index is 0
                                TextView time = (TextView) row.getChildAt(0);
                                time.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        final TextView edit_itin_time = (TextView) view;
                                        Calendar mcurrentTime = Calendar.getInstance();
                                        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                                        int minute = mcurrentTime.get(Calendar.MINUTE);
                                        TimePickerDialog mTimePicker;
                                        mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                                Calendar newTime = Calendar.getInstance();
                                                newTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                                                newTime.set(Calendar.MINUTE, selectedMinute);
                                                String selectedTime = timeFormatter_12.format(newTime.getTime());
                                                edit_itin_time.setText(timeFormatter_12.format(newTime.getTime()));
                                                Toast.makeText(getActivity(), "Time selected is: " + selectedTime, Toast.LENGTH_LONG).show();
                                            }
                                        }, hour, minute, false);//Yes: 24 hour time
                                        mTimePicker.setTitle("Select Time");
                                        mTimePicker.show();
                                    }
                                });
                            } else if (k == 1){ //if index is 1
                               final EditText todo = (EditText) row.getChildAt(1);
                                todo.setFocusableInTouchMode(true);
                                todo.setKeyListener(keyListener);
                            }
                        }
                    }
                }
            }
        }

        private void updateSchedule() { //update schedule after editing
            getTripID = this.getArguments();
            final Long ID = getTripID.getLong("itinListID");
            Date newTime24;
            String newTime = "";
            String newTodo = "";
            TextView time = null;
            EditText activity = null;

            if (itin_table.getChildCount() > 0) { //if table has row(s)
                outerloop:
                for (int i = 0; i < itin_table.getChildCount(); i++) {
                    View rowView = itin_table.getChildAt(i);

                    if (rowView instanceof TableRow) {
                        TableRow row = (TableRow) rowView;
                        for (int k = 0; k < row.getChildCount(); k++) {

                            if (k == 0) { //if index is 0
                                time = (TextView) row.getChildAt(0);

                                    try {
                                        newTime24 = timeFormatter_12.parse(time.getText().toString());
                                        newTime = timeFormatter_24.format(newTime24);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                            }else if (k == 1) { //if index is 1

                                activity = (EditText) row.getChildAt(1);
                                newTodo = activity.getText().toString();

                                if (newTodo.trim().length() == 0) {
                                    Toast.makeText(getActivity(), "Input is empty", Toast.LENGTH_SHORT).show();
                                    return;
                            }
                                long rowID = row.getId();
                                TripDB db = new TripDB(getActivity());
                                db.open();
                                db.updateItinerary(rowID, newTime, newTodo);
                                editItinBtn.setText("Edit");
                                editItinBtn.setTag(1);
                            }
                        }
                    }
                }
                itin_table.removeAllViews(); //remove existing table rows
                retrieveItinerary(ID); //retrieve and display updated schedule
                Toast.makeText(getActivity(), "Schedule has been updated", Toast.LENGTH_SHORT).show();
            }
        }

        public void retrieveItinerary (final long ID) { //get all schedule for this trip
            TripDB db = new TripDB(getActivity());
            db.open();
            c = db.getItinerary(ID); //cast data to cursor
            c2 = db.getItineraryDate(ID);
            c2.moveToFirst(); //loop cursor to retrieve data
            itin_date_set.setText(c2.getString(0));

            if (c.getCount() > 0) { //if cursor not empty, loop cursor to retrieve data
                itin_date_set.setText(c2.getString(0));
                while (c.moveToNext()) {
                    itin_row = new TableRow(getActivity()); //instantiate new row view
                    TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                    itin_row.setId(c.getInt(0));
                    itin_time = new TextView (getActivity()); //instantiate new child view
                    itin_todo = new EditText (getActivity());//instantiate new child view

                    keyListener = itin_todo.getKeyListener();

                    itin_time.setId(c.getInt(0));
                    itin_todo.setId(c.getInt(0));

                    itin_time.setFocusable(false);
                    itin_todo.setFocusable(false);
                    itin_todo.setKeyListener(null);

                    itin_time.setLayoutParams(params);
                    itin_todo.setLayoutParams(params);
                    itin_time.setTextAppearance(getActivity(), R.style.TextOutput);
                    itin_time.setPadding(80,0,0,0);

                    try {
                        String timeOutput24;
                        String timeOutput12;

                        timeOutput24 = c.getString(1);
                        Date timeOutput24Obj;
                        timeOutput24Obj = timeFormatter_24.parse(timeOutput24); //format date from
                        timeOutput12 = timeFormatter_12.format(timeOutput24Obj); //24 to 12 hour

                        itin_time.setText(timeOutput12); //display schedule
                        itin_todo.setText(c.getString(2));

                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    //add listener to delete schedule
                    itin_row.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            final long id = view.getId();
                            AlertDialog.Builder dBox = new AlertDialog.Builder(getActivity());
                            dBox.setMessage("Delete schedule?");
                            dBox.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int button) {
                                    TripDB db = new TripDB (getActivity());
                                    db.deleteSchedule(id);
                                    Toast.makeText(getActivity(), "Schedule has been deleted!", Toast.LENGTH_LONG).show();
                                    refreshItinerary(ID);
                                }
                            });
                            dBox.setNegativeButton("Cancel", null);
                            dBox.show();

                            return true;
                        }
                    });

                    itin_row.addView(itin_time);
                    itin_row.addView(itin_todo);
                    itin_table.addView(itin_row);
                }

            } else {
                //layout for if itinerary is empty
                itin_row = new TableRow(getActivity());
                itin_row.setGravity(Gravity.CENTER_HORIZONTAL);
                itin_row.setPadding(5,20,5,0);
                TextView empty_itin = new TextView(getActivity());
                empty_itin.setPadding(5,5,5,5);
                empty_itin.setText("Itinerary is empty! Please add activities!");
                itin_row.addView(empty_itin);
                itin_table.addView(itin_row);
            }
        }

        private void refreshItinerary(long ID) { //refresh itinerary once schedule is deleted
            TripDB db = new TripDB(getActivity());
            db.open();
            if (itin_table.getChildCount() > 0 ) {
                itin_table.removeAllViews();
            }
            retrieveItinerary(ID);
        }

        public class PopulateItinerary extends AsyncTask <Void, Void, String >{ //asynchronously add schedule

            String timeInput24;
            String newActivityInput;
            Date hour24;
            private long ID;

            public PopulateItinerary(long id) {
                ID = id;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... param) {

                try {
                    hour24 = timeFormatter_12.parse(newTimeInput.getText().toString()); //format 12 hour
                    timeInput24 = timeFormatter_24.format(hour24);                      //to 24 hour
                    Log.d("24-hour", "24-hour format: " + timeInput24);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                newActivityInput = new_activity.getText().toString();

                TripDB db = new TripDB(getActivity());
                db.open();
                db.addItinerary(timeInput24, newActivityInput, ID); // add new schedule into DB
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                if (itin_table.getChildCount() > 0 ) { //if row is more than 0, remove all
                    itin_table.removeAllViews();
                }
                retrieveItinerary(ID); //retrieve updated itinerary

                newTimeInput.setText("");
                new_activity.setText("");
            }
        }
    }
}
