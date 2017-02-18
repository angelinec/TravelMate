package com.example.test.TravelMate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by Angeline Cheah on 1/23/2015.
 */

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = getTitle();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(mTitle);
            setSupportActionBar(toolbar);
        }
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer); //id in activity_main.xml


        if(savedInstanceState == null) { //if null, place MainScreenFragment into container
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            MainScreenFragment mainFragment = new MainScreenFragment();
            transaction.add(R.id.container, mainFragment);
            transaction.commit();
        }

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout), toolbar); // id in activity_main.xml
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();

        switch(position){
            case 0: //Home
                fragmentManager.beginTransaction()
                        .replace(R.id.container, MainScreenFragment.newInstance(position + 1)) //container id in activity_main.xml
                        .commit();
                break;

            case 1: //Itineraries
                Intent itineraries = new Intent(this, Itineraries.class);
                startActivity(itineraries);
                break;

            case 2: //Journals
                Intent journals = new Intent(this, Journals.class);
                startActivity(journals);
                break;

            case 3: //Nearby Places
                Intent nearbyPlaces = new Intent(this, NearbyPlaces.class);
                startActivity(nearbyPlaces);
                break;

            case 4: //Sharing
                Intent sharing = new Intent(this, Sharing.class);
                startActivity(sharing);
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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
}

    /*public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.home);
                break;
            case 2:
                mTitle = getString(R.string.itin_Chklist);
                break;
            case 3:
                mTitle = getString(R.string.journal);
                break;
            case 4:
                mTitle = getString(R.string.places);
                break;
            case 5:
                mTitle = getString(R.string.share);
                break;
        }
    }*/