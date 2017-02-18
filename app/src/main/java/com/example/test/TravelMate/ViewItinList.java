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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Angeline Cheah on 1/23/2015.
 */

public class ViewItinList extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_itinlist);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle extras = getIntent().getExtras();
        long ID = extras.getLong("trip_ID");

        Bundle tripID = new Bundle();
        tripID.putLong("tripID", ID);

        ViewItinListFragment viewItinListFrag = new ViewItinListFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, viewItinListFrag)
                    .commit();
        }
        viewItinListFrag.setArguments(tripID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_itin, menu);
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
    public static class ViewItinListFragment extends Fragment {

        private SimpleCursorAdapter adapter;
        private TextView emptyItinList;
        private ListView viewItinListView;
        private Cursor mCursor;
        private Bundle getTripID;
        private Button createItinBtn;

        public ViewItinListFragment() {
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
            View itinView = inflater.inflate(R.layout.fragment_view_itinlist, container, false);
            viewItinListView = (ListView) itinView.findViewById(R.id.itinList);
            emptyItinList = (TextView) itinView.findViewById(R.id.emptyItin);
            createItinBtn = (Button) itinView.findViewById(R.id.createItinBtn);

            getTripID = this.getArguments();
            final Long ID = getTripID.getLong("tripID");
            
            String[] from = new String[]{"itinDate"};

            final int[] to = new int[]{android.R.id.text1};
            adapter = new SimpleCursorAdapter(getActivity(),android.R.layout.simple_list_item_1, null,from,to,0);
            adapter.notifyDataSetChanged();

            viewItinListView.setEmptyView(emptyItinList); // show TextView if cursor is empty
            viewItinListView.setAdapter(adapter);

            //view an itinerary
            viewItinListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent viewItin = new Intent (getActivity(), ViewItin.class);
                    viewItin.putExtra("itinListID", id);
                    getActivity().startActivity(viewItin);
                }
            });

            //delete an itinerary
            viewItinListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, final long id) {
                    AlertDialog.Builder dBox = new AlertDialog.Builder(getActivity());
                    dBox.setTitle("Confirm?");
                    dBox.setMessage("Are you sure you want to delete this itinerary?");
                    dBox.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

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

            //create new itinerary
            createItinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Long ID = getTripID.getLong("tripID");
                    Intent addItin = new Intent (getActivity(), AddItinerary.class);
                    addItin.putExtra("trip_ID", ID);
                    getActivity().startActivity(addItin);
                }
            });

            return itinView;
        }
        
        public void onResume() {
            super.onResume();
            getTripID = this.getArguments();
            Long ID = getTripID.getLong("tripID");
            TripDB db = new TripDB(getActivity());
            db.open();
            mCursor = db.getTripItineraryList(ID);
            adapter.changeCursor(mCursor);
            db.close();
        }
    }
}