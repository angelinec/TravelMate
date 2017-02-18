package com.example.test.TravelMate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Angeline Cheah on 1/21/2015.
 */

public class AddItinerary extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_itinerary); //set container to place fragment

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle extras = getIntent().getExtras(); // retrieve data passed from another intent
        long ID = extras.getLong("trip_ID");

        Bundle tripID = new Bundle(); // pass data to another intent
        tripID.putLong("tripID", ID);

        AddItineraryFragment addItinFrag = new AddItineraryFragment(); //instantiate fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction() //add fragment into container
                    .add(R.id.container, addItinFrag)
                    .commit();
        }
        addItinFrag.setArguments(tripID); //setting data into fragment
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_itinerary, menu);
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
    public static class AddItineraryFragment extends Fragment {

        private Bundle getTripID;
        private TextView itin_date;
        private TextView itin_date_set;
        private EditText new_time;
        private EditText new_activity;
        private TableLayout itin_table;
        private Button createNewItinBtn;


        public AddItineraryFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            //get data passed from the activity
            getTripID = this.getArguments();
            final Long ID = getTripID.getLong("tripID");

            //place xml layout file to container
            View rootView = inflater.inflate(R.layout.fragment_add_itinerary, container, false);

            //locate UI components in the layout xml file
            itin_date = (TextView) rootView.findViewById(R.id.itin_date);
            itin_date_set = (TextView) rootView.findViewById(R.id.itin_date_set);
            createNewItinBtn = (Button) rootView.findViewById(R.id.createItinBtn);

            //set event listener to button
            itin_date_set.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog.Builder dBox = new AlertDialog.Builder(getActivity());
                    List<String> datesList = getAllDates(ID); //place retrieved data into a List
                    final String[] datesArray = datesList.toArray(new String[datesList.size()]);
                    dBox.setItems(datesArray, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int id) {
                            String selDate = datesArray[id].toString();
                            itin_date_set.setText(selDate);
                            itin_date_set.setTextColor(Color.BLACK);
                        }
                    });
                    dBox.show();
                }
            });

                createNewItinBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (itin_date_set.getText().toString().trim().length() == 0) {
                            Toast.makeText(getActivity(), "Select a date to proceed!", Toast.LENGTH_SHORT).show();
                        } else {
                            String itin_dateSel = itin_date_set.getText().toString();
                            TripDB db = new TripDB(getActivity()); //create an instance of database class
                            db.open();
                            db.createItinerary(itin_dateSel, ID); //saving trip details
                            Toast.makeText(getActivity(), "Itinerary is successfully created!", Toast.LENGTH_SHORT).show();
                            db.close();
                            getActivity().finish();
                        }
                    }
                });
            return rootView;
        }

        private List<String> getAllDates(long ID) {
            TripDB db = new TripDB(getActivity());
            Cursor c = db.getOneTrip(ID); //place data retrieved into Cursor object
            String startDate = null;
            String endDate = null;

            while (c.moveToNext()) { //iterate to retrieve data in Cursor object
                startDate = c.getString(2);
                endDate = c.getString(3);
            }

            List<String> dates = getDates(startDate, endDate); //insert data retrieved into a List
            ArrayList<String> allDates = new ArrayList<String>();
            for (String date : dates) {
                allDates.add(date);
            }
            return allDates;
        }

        //method to retrieve dates in between start date and and end date of a trip
        private static List<String> getDates(String firstDate, String lastDate) {
            ArrayList<String> dates = new ArrayList<String>();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy"); //use to format date

            Date date1 = null;
            Date date2 = null;

            try {
                date1 = dateFormatter.parse(firstDate); //parse String date into Date object
                date2 = dateFormatter.parse(lastDate);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date2);

            while(!cal1.after(cal2)) { // iterate to check if start date is before end date
                dates.add(dateFormatter.format(cal1.getTime()));
                cal1.add(Calendar.DATE, 1);
            }
            return dates;
        }
    }
}