package com.example.test.TravelMate;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

/**
* Created by Angeline Cheah on 3/9/2015.
*/
public class ItemListDialog extends DialogFragment {

    private ListView allItemList;
    private EditText newItem;
    MyAdapter myAdapter;
    private Refresher refresh;

    Bundle getTripID;

    public static ItemListDialog newInstance(String title) {
        ItemListDialog dialogFrag = new ItemListDialog();
        return dialogFrag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        refresh = (Refresher) activity;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
       View dialogView = getActivity().getLayoutInflater().inflate(R.layout.fragment_item_dialog, null);

        allItemList = (ListView) dialogView.findViewById(R.id.checkList);
        newItem = (EditText) dialogView.findViewById(R.id.addItem);

        getTripID = this.getArguments();
        final Long ID = getTripID.getLong("tripID");

        retrieveItems(); //call method to retrieve method

        //set event listener
        allItemList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, final long id) {
                AlertDialog.Builder delBox = new AlertDialog.Builder(getActivity());
                delBox.setTitle("Delete item?");
                delBox.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TripDB db = new TripDB(getActivity());
                        db.deleteItem(id);
                        Toast.makeText(getActivity(), "Item has been deleted!", Toast.LENGTH_SHORT).show();
                        retrieveItems(); //retrieve updated items list
                        refresh.onRefresh(); //refresh checklist
                    }
                });
                delBox.setNegativeButton("Cancel", null);
                delBox.show();
                return true;
            }
        });

        //create dialog to contain list of all items
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Items to add:").setView(dialogView);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                TripDB db = new TripDB(getActivity());

                //check for empty inputs
                if (myAdapter.itemIdList.isEmpty() && (newItem.getText().toString().trim().length() == 0)) {
                    Toast.makeText(getActivity(), "Please select or input at least one item", Toast.LENGTH_LONG ).show();
                }
                else if (myAdapter.itemIdList.size() > 0 && (newItem.getText().toString().trim().length() != 0)) {
                    for (int newListId : myAdapter.itemIdList ) {
                        db.addChecklistItem(newListId, ID);
                    }
                    db.addNewItem(newItem.getText().toString(), ID);
                    db.addItemChecklist(newItem.getText().toString(), ID);
                }
                else if (myAdapter.itemIdList.size() > 0 && (newItem.getText().toString().trim().length() == 0)) {
                    for (int newListId : myAdapter.itemIdList ) {
                        db.addChecklistItem(newListId, ID);
                    }
                }
                else if (myAdapter.itemIdList.isEmpty() && (newItem.getText().toString().trim().length() != 0)) {
                    db.addNewItem(newItem.getText().toString(), ID);
                    db.addItemChecklist(newItem.getText().toString(), ID);
                }

                Toast.makeText(getActivity(), "Item(s)added!", Toast.LENGTH_SHORT).show();
                refresh.onRefresh(); //refresh list
            }
        });
        builder.setNegativeButton("Cancel", null);
        return builder.create();
    }

    //method to retrieve all items from database
    private void retrieveItems() {
        final TripDB db = new TripDB(getActivity());
        db.open();
        Cursor c = db.getAllItems();
        myAdapter = new MyAdapter(getActivity(), R.layout.fragment_item_dialog2, c, 0);
        allItemList.setAdapter(myAdapter);
    }

    //adapter class to initialise UI components and check for checked boxes
    private class MyAdapter extends ResourceCursorAdapter {

        private ArrayList<Integer> itemIdList;

        public MyAdapter(Context context, int layout, Cursor c, int flag) {
            super(context, layout, c, 0);
        }

        private class ViewHolder {
            TextView listItem;
            CheckBox listCb;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            itemIdList = new ArrayList<Integer>();
            final LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.fragment_item_dialog2, parent, false);

            final ViewHolder holder = new ViewHolder();
            holder.listItem = (TextView) v.findViewById(R.id.list_item);
            holder.listCb = (CheckBox) v.findViewById(R.id.check_item);

            v.setTag(holder);

            return v;
        }

        @Override
        public void bindView(View v, Context context, Cursor c) {

            final ViewHolder holder = (ViewHolder) v.getTag();
            holder.listItem.setText(c.getString(1));
            holder.listCb.setTag(c.getInt(0));
            final int itemID = (Integer) holder.listCb.getTag();

            holder.listCb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.listCb.isChecked()) {
                        itemIdList.add(itemID); //add itemID to array is checkbox is checked

                    }else {
                        Iterator<Integer> i = itemIdList.iterator(); //check if item ID exist in array
                        while (i.hasNext()) {
                            int id = i.next();
                            if (id == (itemID))
                                i.remove();
                        }
                    }
                }
            });
        }
    }
}