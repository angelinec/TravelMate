package com.example.test.TravelMate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Angeline Cheah on 1/23/2015.
 */

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private Cursor mCursor;
    private ListView mListView;
    private TextView emptyList;
    private CursorAdapter adapter;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MainScreenFragment newInstance(int sectionNumber) {
        MainScreenFragment fragment = new MainScreenFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MainScreenFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView) rootView.findViewById(R.id.tripList);
        emptyList = (TextView) rootView.findViewById(R.id.emptyList);

        String[] from = new String[]{"tripName"};
        final int[] to = new int[]{android.R.id.text1};

        //prepare layout for data retrieve from cursor to be displayed in the UI components
        adapter = new SimpleCursorAdapter(getActivity(),android.R.layout.simple_list_item_1, null,from,to,0);
        adapter.notifyDataSetChanged();

        mListView.setEmptyView(emptyList); // show TextView if cursor is empty
        mListView.setAdapter(adapter);

        //set event listener to list view
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent viewTrip = new Intent (getActivity(), ViewTrip.class);
                viewTrip.putExtra("trip_ID", id);
                getActivity().startActivity(viewTrip);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, final long id) {
                AlertDialog.Builder dBox = new AlertDialog.Builder(getActivity());
                dBox.setMessage("Delete trip?");
                dBox.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        TripDB db = new TripDB (getActivity());
                        db.deleteTrip(id);
                        Toast.makeText(getActivity(), "Trip has been deleted!", Toast.LENGTH_LONG).show();
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

    public void onActivityCreated (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void onResume() {
        super.onResume();
        TripDB db = new TripDB(getActivity());
        db.open();
        mCursor = db.getAllTrips();
        adapter.changeCursor(mCursor);
        db.close();
    }
}