package com.example.test.TravelMate;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Angeline Cheah on 1/21/2015.
 */

public class Itineraries extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itineraries); //set container to place fragment

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        
        
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ItinerariesFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_itineraries, menu);
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
    public static class ItinerariesFragment extends Fragment {

        private ListView itinListView;
        private TextView emptyList;
        private Cursor mCursor;
        private SimpleCursorAdapter adapter;

        public ItinerariesFragment() {
        }

        //attach fragment to activity host
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_itineraries, container, false);
            itinListView = (ListView) rootView.findViewById(R.id.itinList);
            emptyList = (TextView) rootView.findViewById(R.id.emptyList);

            String[] from = new String[]{"tripName", "itinDate"}; //retrieve data from Cursor and store in an array
            final int[] to = new int[]{android.R.id.text1, android.R.id.text2}; //display data retrieved to text views

            //prepare layout for data retrieve from cursor to be displayed in the UI components
            adapter = new SimpleCursorAdapter(getActivity(),android.R.layout.simple_list_item_2, null,from,to,0);
            adapter.notifyDataSetChanged();

            itinListView.setEmptyView(emptyList); // show TextView if cursor is empty
            itinListView.setAdapter(adapter); //set adapter to list in order to populate

            //set event listener to list view
            itinListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent viewItin = new Intent (getActivity(), ViewItin.class);
                    viewItin.putExtra("itinListID", id);
                    getActivity().startActivity(viewItin);
                }
            });

            itinListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, final long id) {
                    AlertDialog.Builder dBox = new AlertDialog.Builder(getActivity());
                    dBox.setTitle("Confirm?");
                    dBox.setMessage("Delete itinerary?");
                    dBox.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            TripDB db = new TripDB (getActivity());
                            db.deleteItinerary(id);
                            Toast.makeText(getActivity(), "Itinerary has been deleted!", Toast.LENGTH_LONG).show();
                            onResume();
                        }
                    });
                    dBox.setNegativeButton("Cancel", null);
                    dBox.show();
                    return true;
                }
            });
            return rootView; //return view that has been inflated with its containing UI
        }

        //retrieve data from database again when host activity is resumed
        public void onResume() {
            super.onResume();
            TripDB db = new TripDB(getActivity());
            db.open();
            mCursor = db.getAllItineraries();
            adapter.changeCursor(mCursor);
            db.close();
        }
    }
}