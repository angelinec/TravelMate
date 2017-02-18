 package com.example.test.TravelMate;

 import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

 /**
  * Created by Angeline Cheah on 1/23/2015.
  */

 interface Refresher { //refresher class to refresh checklist
     void onRefresh();
 }

 public class ViewChecklist extends ActionBarActivity implements Refresher {

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_view_checklist);

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

         ViewChecklistFragment viewChecklistFrag = new ViewChecklistFragment();
         if (savedInstanceState == null) {
             getSupportFragmentManager().beginTransaction()
                     .add(R.id.container, viewChecklistFrag, "ViewChecklistFrag")
                     .commit();
         }
         viewChecklistFrag.setArguments(tripID);
     }

     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.menu_view_chklist, menu);
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

     @Override
     public void onRefresh() { //refresh checklist when item is added/deleted from items dialog list
         ViewChecklistFragment reViewChecklist = (ViewChecklistFragment)
                 getSupportFragmentManager().findFragmentByTag("ViewChecklistFrag");
         reViewChecklist.updateChecklist();
     }

     /**
      * A placeholder fragment containing a simple view.
      */
     public static class ViewChecklistFragment extends Fragment {

         private ListView checkList;
         private TextView empty_checkList;
         private ImageButton addItemBtn;
         private CursorAdapter itemAdapter;

         Bundle getTripID;

         public ViewChecklistFragment() {
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
             final Long ID = getTripID.getLong("tripID"); //get Trip ID from parameter

             View checkListView = inflater.inflate(R.layout.fragment_view_checklist, container, false);
             checkList = (ListView) checkListView.findViewById(R.id.checkList);
             empty_checkList = (TextView) checkListView.findViewById(R.id.empty_checkList);
             addItemBtn = (ImageButton) checkListView.findViewById(R.id.add_Item_Btn);

             String[] items = new String[]{"itemDesc"};
             int[] to = new int[]{R.id.list_item};

             //prepare layout for data retrieve from cursor to be displayed in the UI components
            itemAdapter = new SimpleCursorAdapter(getActivity(), R.layout.fragment_view_checklist_2, null, items, to, 0);

             checkList.setEmptyView(empty_checkList); // show TextView if cursor is empty
             checkList.setAdapter(itemAdapter);

             //delete item from chacklist
             checkList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                 @Override
                 public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, final long id) {
                     AlertDialog.Builder delBox = new AlertDialog.Builder(getActivity());
                     delBox.setTitle("Delete item?");
                     delBox.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                             TripDB db = new TripDB(getActivity());
                             db.deleteChecklistItem(id);
                             onResume();
                             Toast.makeText(getActivity(), "Item has been deleted!", Toast.LENGTH_LONG).show();
                         }
                     });
                     delBox.setNegativeButton("Cancel", null);
                     delBox.show();

                     return true;
                 }
             });

             //to add new item to checklist
             addItemBtn.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     Bundle tripID = new Bundle();
                     tripID.putLong("tripID", ID);
                     //instantiate items dialog
                     DialogFragment newFragment = ItemListDialog.newInstance(
                             "Add Item");
                     newFragment.setArguments(tripID);
                     newFragment.show(getActivity().getFragmentManager(), "dialog");
                }
             });

             return checkListView;
         }

         public void retrieveChecklist(long ID) { //get all checklist for this trip and display
             TripDB db = new TripDB(getActivity());
             db.open();
             Cursor c = db.getChecklistItems(ID);
             itemAdapter.changeCursor(c);
             db.close();
         }

         @Override
         public void onActivityCreated(Bundle savedInstanceState) {
             super.onActivityCreated(savedInstanceState);
         }

         public void onResume() { //resume this activity
             super.onResume();
             getTripID = this.getArguments();
             Long ID = getTripID.getLong("tripID");
             retrieveChecklist(ID);
         }

         public void updateChecklist() { //called from onRefresh to update items
             getTripID = this.getArguments();
             Long ID = getTripID.getLong("tripID");
             retrieveChecklist(ID);
         }
     }
 }