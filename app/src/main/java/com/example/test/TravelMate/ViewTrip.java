package com.example.test.TravelMate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Angeline Cheah on 1/23/2015.
 */

public class ViewTrip extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_trip);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle extras = getIntent().getExtras();
        long ID = extras.getLong("trip_ID");

        TripDB db = new TripDB(getBaseContext()); //retrieve trip's details
        Bundle tripDetails = new Bundle();
        db.open();
        Cursor c = db.getOneTrip(ID);
        c.moveToFirst(); //loop through cursor
        tripDetails.putLong("trip_ID", c.getLong(0)); //place data into arguments
        tripDetails.putString("startDate", c.getString(2));
        tripDetails.putString("endDate", c.getString(3));
        tripDetails.putString("destination", c.getString(4));
        tripDetails.putString("transport", c.getString(5));
        c.close();
        db.close();

        //set Fragment class Arguments
        ViewTripFragment viewTripFrag = new ViewTripFragment();
        if(savedInstanceState == null) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.container, viewTripFrag);
            transaction.commit();
        }
        viewTripFrag.setArguments(tripDetails);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_trip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ViewTripFragment extends Fragment implements View.OnClickListener {

        DatePickerDialog editStartDatePickerDialog;
        DatePickerDialog editEndDatePickerDialog;
        private EditText startDateOutput;
        private EditText endDateOutput;
        private EditText destinationOutput;
        private TextView transportOutput;
        private Button vItinBtn;
        private Button vChklistBtn;
        private Button vJournalBtn;
        private Button editUpdateBtn;
        private SimpleDateFormat dateFormatter;
        private Bundle viewDetails;

        public ViewTripFragment() {

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

            viewDetails = this.getArguments(); //get arguments passed

            View tripView = inflater.inflate(R.layout.fragment_view_trip, container, false); //inflate layout
            startDateOutput = (EditText) tripView.findViewById(R.id.startDateOutput);
            endDateOutput = (EditText) tripView.findViewById(R.id.endDateOutput);
            destinationOutput = (EditText) tripView.findViewById(R.id.destinationOutput);
            transportOutput = (TextView) tripView.findViewById(R.id.transportOutput);
            vItinBtn = (Button) tripView.findViewById(R.id.viewItin);
            vChklistBtn = (Button) tripView.findViewById(R.id.viewCheckList);
            vJournalBtn = (Button) tripView.findViewById(R.id.viewJournal);
            editUpdateBtn = (Button) tripView.findViewById(R.id.editUpdate);

            dateFormatter = new SimpleDateFormat("dd-MM-yyyy"); //format date

            editUpdateBtn.setTag(1);
            vItinBtn.setOnClickListener(this);
            vChklistBtn.setOnClickListener(this);
            vJournalBtn.setOnClickListener(this);

            startDateOutput.setText(viewDetails.getString("startDate"));
            endDateOutput.setText(viewDetails.getString("endDate"));
            destinationOutput.setText(viewDetails.getString("destination"));
            transportOutput.setText(viewDetails.getString("transport"));

            //edit or button trip's details
            editUpdateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int status = (Integer)v.getTag();
                    if (status == 1) {
                        editUpdateBtn.setText("Update");
                        editUpdateBtn.setTag(2);
                        startDateOutput.setFocusableInTouchMode(true);
                        startDateOutput.setCursorVisible(false);
                        startDateOutput.setInputType(InputType.TYPE_NULL);
                        endDateOutput.setFocusableInTouchMode(true);
                        endDateOutput.setCursorVisible(false);
                        endDateOutput.setInputType(InputType.TYPE_NULL);
                        destinationOutput.setFocusableInTouchMode(true);
                        transportOutput.setClickable(true);
                        transportOutput.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                editTransport();
                            }
                        });
                        editDates(); //edit trip's dates

                    } else if (status == 2) {

                        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");

                        try {
                            Date startDateObj = dateFormatter.parse(startDateOutput.getText().toString().trim());
                            Date endDateObj = dateFormatter.parse(endDateOutput.getText().toString().trim());

                            //check for empty inputs
                            if (destinationOutput.getText().toString().trim().length() == 0) {
                                Toast.makeText(getActivity(), "Input field(s) must not be empty!", Toast.LENGTH_SHORT).show();

                            } else if (endDateObj.before(startDateObj))  {
                                Toast.makeText(getActivity(), "End date must be after Start date", Toast.LENGTH_SHORT).show();

                            }else {

                                editUpdateBtn.setText("Edit");
                                editUpdateBtn.setTag(1);
                                startDateOutput.setFocusable(false);
                                startDateOutput.setCursorVisible(false);
                                startDateOutput.setInputType(InputType.TYPE_NULL);
                                endDateOutput.setFocusable(false);
                                endDateOutput.setCursorVisible(false);
                                endDateOutput.setInputType(InputType.TYPE_NULL);
                                destinationOutput.setFocusable(false);
                                transportOutput.setClickable(false);
                                update(); //update trip's details
                            }
                        }catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            return tripView;
        }

        private void editDates() { //edit start and end dates
            startDateOutput.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startDateOutput.requestFocus(1);
                    Calendar newCalendar = Calendar.getInstance();
                    editStartDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            Calendar newDate = Calendar.getInstance();
                            newDate.set(year, monthOfYear, dayOfMonth);
                            startDateOutput.setText(dateFormatter.format(newDate.getTime()));
                        }
                    }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
                    editStartDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                    editStartDatePickerDialog.show();
                }
            });

            endDateOutput.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    endDateOutput.requestFocus(1);
                    Calendar newCalendar = Calendar.getInstance();
                    editEndDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            Calendar newDate = Calendar.getInstance();
                            newDate.set(year, monthOfYear, dayOfMonth);
                            endDateOutput.setText(dateFormatter.format(newDate.getTime()));
                        }
                    }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
                    editEndDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                    editEndDatePickerDialog.show();
                }
            });
        }

        private void editTransport() { //edit transportation mode
            final String [] transportArray = getResources().getStringArray(R.array.transportType_array);

            final AlertDialog.Builder dBox = new AlertDialog.Builder(getActivity());
            dBox.setTitle("Choose a transport");
            dBox.setItems(transportArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int id) {
                    String selTransport = transportArray[id].toString();
                    transportOutput.setText(selTransport);
                }
            });
            dBox.show();
        }

        private void update() { //update trip's details

                viewDetails = this.getArguments();
                Long id = viewDetails.getLong("trip_ID");
                String startDate = startDateOutput.getText().toString();
                String endDate = endDateOutput.getText().toString();
                String destination = destinationOutput.getText().toString();
                String transport = transportOutput.getText().toString();

                TripDB db = new TripDB(getActivity());
                db.open();
                db.updateTripDetails(id, startDate, endDate, destination, transport);
                Toast.makeText(getActivity(), "Trip has been updated", Toast.LENGTH_SHORT).show();
                db.close();
        }

        @Override
        public void onClick(View v) { //set listener to buttons
            Bundle viewDetails = this.getArguments();
            final Long id = viewDetails.getLong("trip_ID");

            TripDB db = new TripDB(getActivity());
            Cursor c1, c2;

           switch (v.getId()) { //detect buttons
                case R.id.viewItin:
                    Intent viewItinList = new Intent (getActivity(), ViewItinList.class);
                    viewItinList.putExtra("trip_ID", id);
                    getActivity().startActivity(viewItinList);
                    break;

                case R.id.viewCheckList:
                    ArrayList<Integer> itemIdList = new ArrayList<Integer>();
                    db.open();
                    c1 = db.getDefaultItems();
                    c2 = db.getChecklistItems(id);
                    if (c2.getCount() < 1) {
                        while(c1.moveToNext()) {
                            itemIdList.add(c1.getInt(0)); //add the item
                        }
                        for (int item : itemIdList) {
                            db.saveDefaultChecklistItem(item, id);
                        }
                    }

                    Intent viewChecklist = new Intent (getActivity(), ViewChecklist.class);
                    viewChecklist.putExtra("trip_ID", id);
                    getActivity().startActivity(viewChecklist);
                    break;

                case R.id.viewJournal:
                    Intent journalList = new Intent (getActivity(), ViewJournalList.class);
                    journalList.putExtra("trip_ID", id);
                    getActivity().startActivity(journalList);
                    break;
            }
        }
    }
}