package com.example.test.TravelMate;

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
 * Created by Angeline Cheah on 1/21/2015.
 */

public class ViewJournalList extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list); //set container to place fragment

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle extras = getIntent().getExtras(); // retrieve data passed from another intent
        long ID = extras.getLong("trip_ID"); //place data retrieve in a variable

        Bundle tripID = new Bundle(); // pass data to another intent
        tripID.putLong("tripID", ID);

        JournalListFragment journalListFrag = new JournalListFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, (journalListFrag))
                    .commit();
        }
        journalListFrag.setArguments(tripID); //setting data into fragment
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_journal_list, menu);
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
    public static class JournalListFragment extends Fragment {

        private ListView journalListView;
        private TextView empty_JournalList;
        private SimpleCursorAdapter journalListAdapter;
        private Bundle getTripID;
        private Button addJournalBtn;

        public JournalListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            getTripID = this.getArguments(); //retrieve data passed from the host activity
            final long ID = getTripID.getLong("tripID"); //assigned data to a variable

            View rootView = inflater.inflate(R.layout.fragment_view_journalist, container, false);
            addJournalBtn = (Button) rootView.findViewById(R.id.addJournalBtn);
            journalListView = (ListView) rootView.findViewById(R.id.journalList);
            empty_JournalList = (TextView) rootView.findViewById(R.id.empty_JournalList);

            String [] values = new String[] {"journalDate"}; //retrieve data from Cursor and store in an array
            int [] to = new int[] {android.R.id.text1}; //display data retrieved to text views

            //prepare layout for data retrieve from cursor to be displayed in the UI components
            journalListAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, values, to, 0);

            journalListView.setEmptyView(empty_JournalList); // show TextView if cursor is empty
            journalListView.setAdapter(journalListAdapter); //set adapter to list in order to populate

           //set event listener to button
            addJournalBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent addJournal = new Intent (getActivity(), AddJournal.class); //instantiate a class intent
                    addJournal.putExtra("trip_ID", ID); //set data to be pass to the intent
                    getActivity().startActivity(addJournal); //start intent

                }
            });

            //set event listener to list view
            journalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent viewJournal = new Intent (getActivity(), ViewJournal.class);
                    viewJournal.putExtra("journalID", id);
                    getActivity().startActivity(viewJournal);
                }
            });

            //to delete journal from list
            journalListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, final long id) {
                    AlertDialog.Builder dBox = new AlertDialog.Builder(getActivity());
                    dBox.setTitle("Delete journal?");
                    dBox.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            TripDB db = new TripDB (getActivity());
                            db.deleteJournal(id);
                            Toast.makeText(getActivity(), "Journal has been deleted!", Toast.LENGTH_LONG).show();
                            onResume();
                        }
                    });
                    dBox.setNegativeButton("Cancel", null);
                    dBox.show();
                    return true;
                }
            });
            return rootView;
        }

        public void onResume() {
            super.onResume();
            getTripID = this.getArguments();
            Long ID = getTripID.getLong("tripID");
            TripDB db = new TripDB(getActivity());
            db.open();
            Cursor c = db.getJournals(ID);
            journalListAdapter.changeCursor(c);
            db.close();
        }
    }
}
