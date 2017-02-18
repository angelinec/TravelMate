package com.example.test.TravelMate;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Angeline Cheah on 1/21/2015.
 */

public class Sharing extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SharingFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sharing, menu);
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
    public static class SharingFragment extends Fragment {

        private TextView instruction;
        private Button shareBtn;
        private TextView countLabel;
        private TextView imageCount;
        private String[] FilePathStrings;
        private File[] listFile;
        File file;
        private String dir_path;
        ArrayList<Uri> imageUris = new ArrayList<Uri>();
        private TextView guide;
        private GridView imageGrid;
        private TextView emptyFolder;

        NetworkInfo activeNetworkInfo;
        ConnectivityManager connectivityManager;

        public SharingFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_sharing, container, false);

            instruction = (TextView) rootView.findViewById(R.id.guide);
            shareBtn = (Button) rootView.findViewById(R.id.shareBtn);
            countLabel = (TextView) rootView.findViewById(R.id.countLabel);
            imageCount = (TextView) rootView.findViewById(R.id.imageCount);
            guide = (TextView) rootView.findViewById(R.id.guide);
            emptyFolder = (TextView) rootView.findViewById(R.id.emptyFolder);
            imageGrid = (GridView) rootView.findViewById(R.id.imageGridView);

            dir_path = "TravelMate";

            // Check for SD Card
            if (!Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                Toast.makeText(getActivity(), "Error: Device storage not found", Toast.LENGTH_LONG)
                        .show();
            } else {
                // Locate the image folder in your SD Card
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), dir_path);
            }

            if (file.isDirectory()) {
                listFile = file.listFiles();
                // Create a String array for FilePathStrings
                FilePathStrings = new String[listFile.length];

                for (int i = 0; i < listFile.length; i++) {
                    // Get the path of the image file
                    FilePathStrings[i] = listFile[i].getAbsolutePath();
                }
            }

            imageGrid.setEmptyView(emptyFolder); //show message if no images
            imageGrid.setAdapter(new GridViewAdapter(getActivity(), FilePathStrings));

            //open view Intent
            imageGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + FilePathStrings[position]),  "image/*");
                    startActivity(intent);
                }
            });

            //delete image from queue
            imageGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Uri imagePath = Uri.parse("file://" + FilePathStrings[position]);

                    if (!imageUris.isEmpty()) { //if queue not empty, check for existing image
                        if (imageUris.contains(imagePath)) {
                            imageUris.remove(imagePath);
                            view.setBackgroundColor(Color.TRANSPARENT);
                            Toast.makeText(getActivity(), "Photo has been removed from queue", Toast.LENGTH_SHORT).show();

                        } else { //add image to queue
                            imageUris.add(imagePath);
                            view.setBackgroundColor(Color.parseColor("#00FF00"));
                            Toast.makeText(getActivity(), "Photo has been added to queue", Toast.LENGTH_SHORT).show();
                        }
                    } else { //add image if queue is empty
                        imageUris.add(imagePath);
                        view.setBackgroundColor(Color.parseColor("#00FF00"));
                        Toast.makeText(getActivity(), "Photo has been added to queue", Toast.LENGTH_SHORT).show();
                    }

                    String count = String.valueOf(imageUris.size());
                    imageCount.setText(count); //show image count

                    return true;
                }
            });

            //share image queue to SNS
            shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    checkNetworkAvailability(); //check if Internet is enabled

                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) { //if enabled
                        if (imageUris.isEmpty()) { //check if queue is empty
                            Toast.makeText(getActivity(), "Image queue is empty!", Toast.LENGTH_SHORT).show();

                        } else { //prepare intent to share queue to SNS
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                            shareIntent.setType("image/*");
                            startActivity(Intent.createChooser(shareIntent, "Share images to"));
                        }
                    }else {
                            alertInternetDialog(); //if Internet is disabled, inform user
                        }
                }
            });
            return rootView;
        }

        private boolean checkNetworkAvailability() {
            connectivityManager
                    = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

            return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        }

        private void alertInternetDialog() {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Internet connection is disabled. Enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); //open Settings intent
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            builder.show();
        }

        private class GridViewAdapter extends BaseAdapter { //prepare images to be loaded into GridView

            private String[] filepath;
            private Context mContext;

            public GridViewAdapter(Context c, String[] fpath) {
                filepath = fpath;
                mContext = c;
            }

            @Override
            public int getCount() {
                return filepath.length;
            }

            @Override
            public Object getItem(int position) {
                return position;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ImageView imageView;

                if(convertView == null){ //if null, instantiate view to hold image
                    imageView = new ImageView(mContext);
                    imageView.setLayoutParams(new GridView.LayoutParams(250,250));
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setPadding(5, 5, 5, 5);
                    imageView.setAdjustViewBounds(true);

                }else{ //cast to convertview to imageview
                    imageView = (ImageView) convertView;
                }

                imageView.setTag(filepath[position]); //set tag to each view for identification
                new LoadImages(imageView).execute(); //execute asynctask to load images

                return imageView; //return view to be displayed once done
            }
        }

        private class LoadImages extends AsyncTask<Object, Void, Bitmap> { //load images asynchronously

            private ImageView imgView;
            private String path;

            private LoadImages(ImageView imgView) { //retrieve view passed from parameter
                this.imgView = imgView;
                this.path = imgView.getTag().toString(); //get file path from tag
            }

            @Override
            protected Bitmap doInBackground(Object... params) {
                Bitmap bitmap, bmp = null;

                BitmapFactory.Options bmOptions = new BitmapFactory.Options(); //prepare to process image
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = 6;

                bitmap = BitmapFactory.decodeFile(path, bmOptions); //decode image into bitmap

                try { //catch null bitmap
                    int dimension = getSquareCropDimensionForBitmap(bitmap); //get equal dimensions
                    bmp = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);

                }catch (Exception e) {
                    e.printStackTrace();
                }

                return bmp;
            }

            @Override
            protected void onPostExecute(Bitmap result) {

                // path is not same, meaning that this image view is handled by some other async task.
                //don't do anything and return.
                if (!imgView.getTag().toString().equals(path)) {
                    return;
                }

                if(result != null && imgView != null){ //if bitmap return & imageview not null
                    imgView.setVisibility(View.VISIBLE);
                    imgView.setImageBitmap(result);

                }else{
                    imgView.setVisibility(View.GONE); //hide image view
                }
            }
        }

        private int getSquareCropDimensionForBitmap(Bitmap bitmap) {

            int dimension;
            //If the bitmap is wider than it is height then use the height as the square crop dimension
            if (bitmap.getWidth() >= bitmap.getHeight())    {
                dimension = bitmap.getHeight();
            }
            //If the bitmap is taller than it is width use the width as the square crop dimension
            else    {
                dimension = bitmap.getWidth();
            }
            return dimension;
        }
    }
}