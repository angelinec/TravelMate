package com.example.test.TravelMate;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Angeline Cheah on 1/21/2015.
 */

public class AddNewTrip extends ActionBarActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_trip); //set container to place fragment

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        
        
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new AddNewTripFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_new_trip, menu);
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
    public static class AddNewTripFragment extends Fragment {

        //define UI components
        private TextView startDateInput;
        private TextView endDateInput;
        private EditText tripNameInput;
        private EditText destinationInput;
        private TextView transportInput;
        private Button saveTripBtn;
        private Button setStartDateBtn;
        private Button setEndDateBtn;
        private DatePickerDialog startDatePickerDialog;
        private DatePickerDialog endDatePickerDialog;
        private SimpleDateFormat dateFormatter;
        String[] transportArray;
        String selTransport;

        public AddNewTripFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            //place xml layout file to container
            View rootView = inflater.inflate(R.layout.fragment_add_new_trip, container, false);

            //initialise UI component
            startDateInput = (TextView) rootView.findViewById(R.id.startDateInput);
            endDateInput = (TextView) rootView.findViewById(R.id.endDateInput);
            tripNameInput = (EditText) rootView.findViewById(R.id.tripNameInput);
            destinationInput = (EditText) rootView.findViewById(R.id.destinationInput);
            transportInput = (TextView) rootView.findViewById(R.id.transportInput);
            saveTripBtn = (Button) rootView.findViewById(R.id.saveTrip);
            setStartDateBtn = (Button) rootView.findViewById(R.id.setStartDate);
            setEndDateBtn = (Button) rootView.findViewById(R.id.setEndDate);

            dateFormatter = new SimpleDateFormat("dd-MM-yyyy");

            //set event listener
            setStartDateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setBtn(setStartDateBtn); //call setBtn() method
                }
            });

            setEndDateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setBtn(setEndDateBtn);
                }
            });

            transportInput.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    transportPicker();
                }
            });

            saveTripBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setBtn(saveTripBtn);
                }
            });

            return rootView;
        }

        private void setDateTime() {
            Calendar newCalendar = Calendar.getInstance();

            startDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar startDate = Calendar.getInstance();
                    startDate.set(year, monthOfYear, dayOfMonth);
                    Log.d("startDate object :", startDate.getTime().toString());
                    Log.d("startDate string :", dateFormatter.format(startDate.getTime()));
                    startDateInput.setText(dateFormatter.format(startDate.getTime()));
                }
            },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

            endDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar endDate = Calendar.getInstance();
                    endDate.set(year, monthOfYear, dayOfMonth);
                    endDateInput.setText(dateFormatter.format(endDate.getTime()));
                }
            },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        }

        public void setBtn(Button btn) {
            setDateTime();
            if(btn == setStartDateBtn) {
                startDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); //set min date
                startDatePickerDialog.show();
            } else if(btn == setEndDateBtn) {
                endDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); //set min date
                endDatePickerDialog.show();
            }else if (btn == saveTripBtn) {
                saveTrip();
            }
        }

        //retrieve an array of transports and place it a dialog
        public void transportPicker() {
            transportArray = getResources().getStringArray(R.array.transportType_array);

            final AlertDialog.Builder dBox = new AlertDialog.Builder(getActivity());
            dBox.setTitle("Choose a transport");
            dBox.setItems(transportArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int id) {
                   selTransport = transportArray[id].toString();
                    transportInput.setText(selTransport);
                }
            });
            dBox.show();
        }

        //to save trip details into database
        public void saveTrip() {
            String startDateRes = getString(R.string.startDateInput);
            String endDateRes = getString(R.string.endDateInput);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");

            try {
                Date startDateObj = dateFormatter.parse(startDateInput.getText().toString().trim());
                Date endDateObj = dateFormatter.parse(endDateInput.getText().toString().trim());

                if ((tripNameInput.getText().toString().trim().length() == 0) || (destinationInput.getText().toString().trim().length() == 0)) {
                    Toast.makeText(getActivity(), "Input field(s) must not be empty", Toast.LENGTH_SHORT).show();

                } else if (startDateInput.getText().equals(startDateRes) || (endDateInput.getText().equals(endDateRes))) {
                    Toast.makeText(getActivity(), "Please enter the dates for your trip", Toast.LENGTH_SHORT).show();

                } else if (endDateObj.before(startDateObj)){
                    Toast.makeText(getActivity(), "End date must be after Start date", Toast.LENGTH_SHORT).show();

                } else {
                    TripDB db = new TripDB(getActivity());
                    String tripName = tripNameInput.getText().toString();
                    String startDate = startDateInput.getText().toString();
                    String endDate = endDateInput.getText().toString();
                    String destination = destinationInput.getText().toString();
                    String transport = transportInput.getText().toString();

                    db.open();
                    db.saveTrip(tripName, startDate, endDate, destination, transport); //saving trip details
                    Toast.makeText(getActivity(), "New Trip has been added", Toast.LENGTH_SHORT).show();
                    db.close();
                    getActivity().finish();
                }
            }catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}