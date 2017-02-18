package com.example.test.TravelMate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Angeline Cheah on 1/21/2015.
 */

public class AddJournal extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journal); //set container to place fragment

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

        AddJournalFragment2 addJournalFrag2 = new AddJournalFragment2(); //instantiate fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction() //add fragment into container
                    .add(R.id.container, addJournalFrag2)
                    .commit();
        }
        addJournalFrag2.setArguments(tripID); //setting data into fragment
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_journal, menu);
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
    public static class AddJournalFragment2 extends Fragment {

        private Bundle getTripID;
        private TextView journalDateInput;
        private EditText journalText;
        private Button attachBtn;
        private Button cameraBtn;
        private Button videoBtn;
        private Button saveJournalBtn;
        private LinearLayout imgDiv;
        private GridView imageGridView;
        private File travelMateDir;
        private String imgName;
        private String vidName;
        private FileOutputStream output;
        private String timeStamp;
        private String dir_path;

        private Bitmap bmp;
        private ArrayList<String> myList = new ArrayList<String>();

        private static final int SELECT_IMAGE = 1;
        private static final int CAMERA_REQUEST = 2;
        private static final int CAPTURE_AUDIO = 3;

        public AddJournalFragment2() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            getTripID = this.getArguments(); //retrieve data passed from the host activity
            final Long ID = getTripID.getLong("tripID"); //assigned data to a variable

            //place xml layout file to container
            View journalView = inflater.inflate(R.layout.fragment_add_view_journal, container, false);

            //locate UI components in the layout xml file
            imgDiv = (LinearLayout) journalView.findViewById(R.id.imgDiv);
            journalDateInput = (TextView) journalView.findViewById(R.id.journalDateInput);
            journalText = (EditText) journalView.findViewById(R.id.journalInput);
            attachBtn = (Button) journalView.findViewById(R.id.attachFilesBtn);
            cameraBtn = (Button) journalView.findViewById(R.id.imageBtn);
            videoBtn = (Button) journalView.findViewById(R.id.videoBtn);
            saveJournalBtn = (Button) journalView.findViewById(R.id.journalSaveBtn);
            imageGridView = (GridView) imgDiv.findViewById(R.id.imageGrid);

            //set adapter to Grid View
            imageGridView.setAdapter(new ImageAdapter(getActivity(), myList));

            //assign values to variables to be used to create file path
            dir_path = "TravelMate";
            travelMateDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), dir_path);
            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imgName = (timeStamp + ".jpg");
            vidName = (timeStamp + ".mp4");


            //set event listener to button
            journalDateInput.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog.Builder dBox = new AlertDialog.Builder(getActivity());
                    List<String> datesList = getAllDates(ID);
                    final String[] datesArray = datesList.toArray(new String[datesList.size()]);
                    dBox.setTitle("Select a date");
                    dBox.setItems(datesArray, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int id) {
                            String selDate = datesArray[id].toString();
                            journalDateInput.setText(selDate);
                        }
                    });
                    dBox.show();
                }
            });

            //set event listener to Grid View
            imageGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent intent = new Intent(); //instantiate intent
                    intent.setAction(Intent.ACTION_VIEW); //set action for intent
                    intent.setDataAndType(Uri.parse("file://" + myList.get(position)), "image/*"); //passing image path to intent
                    startActivity(intent); //start intent
                }
            });


            imageGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {
                    AlertDialog.Builder delBox = new AlertDialog.Builder(getActivity());
                    delBox.setTitle("Remove photo from journal?");
                    delBox.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            myList.remove(position);
                            ((BaseAdapter) imageGridView.getAdapter()).notifyDataSetChanged();
                            Toast.makeText(getActivity(), "Photo has been removed from journal", Toast.LENGTH_SHORT).show();
                        }
                    });
                    delBox.setNegativeButton("Cancel", null);
                    delBox.show();
                    return true;
                }
            });

            //set event listener to button
            attachBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setType("image/*"); //set type of files to display to only images
                    intent.setAction(Intent.ACTION_GET_CONTENT);//set action to retrieve content
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE); //start intent with a request code
                }
            });

            cameraBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkDir(); //check if app folder has been created
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); //set action to open camera intent
                    startActivityForResult(cameraIntent, CAMERA_REQUEST); //start intent with a request code
                }
            });

            videoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkDir();//check if app folder has been created
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE); //set action to open video intent
                    startActivityForResult(intent, CAPTURE_AUDIO); //start intent with a request code
                }
            });

            saveJournalBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (journalDateInput.getText().toString().trim().length() == 0) {
                        Toast.makeText(getActivity(), "Select a date!", Toast.LENGTH_SHORT).show();
                    } else {
                        String journalTextInput = journalText.getText().toString(); //retrieve test input
                        String journalDate = journalDateInput.getText().toString();
                        String imagePathList = ""; //initialise variable
                        if (myList.size() > 0) { //check if array list is not empty
                            JSONObject json = new JSONObject(); //instantiate JSONObject
                            try {
                                json.put("imagePathArray", new JSONArray(myList)); //convert arraylist to jsonArray
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            imagePathList = json.toString(); // convert json object to string value
                        }

                        TripDB db = new TripDB(getActivity()); //create an instance of database class
                        db.open();
                        db.saveJournal(ID, journalTextInput, journalDate, imagePathList); //saving trip details
                        Toast.makeText(getActivity(), "Journal is added", Toast.LENGTH_SHORT).show();
                        db.close();
                        getActivity().finish();
                    }
                }
            });

            return journalView;
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) { //identify request code
                case 1: {
                    if (resultCode == RESULT_OK) { //check if result code is OK
                        Uri imgUri = data.getData(); //retrieve Uri data from intent

                        String[] filePathColumn = {MediaStore.Images.Media.DATA}; //place data to string array
                        Cursor cursor = getActivity().getContentResolver().query(imgUri, filePathColumn, null, null, null); //place array into cursor
                        cursor.moveToFirst(); //iterate cursor to retrieve data
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex); //retrieve image path
                        cursor.close();

                        myList.add(picturePath); //add image path to arraylist
                        ((BaseAdapter) imageGridView.getAdapter()).notifyDataSetChanged();

                        // Refresh Gridview Image Thumbnails
                        imageGridView.invalidateViews();
                    }
                    break;
                }
                case 2: {
                    if (resultCode == RESULT_OK) { //check if result code is OK
                        try {
                            Uri imageUri = data.getData(); //retrieve Uri data from intent
                            bmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri); //retrieve and auto auto rotate bitmap
                            dir_path = "TravelMate";
                            travelMateDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), dir_path); //create file path
                            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            imgName = (timeStamp + ".jpg");
                            output = null;
                            File imagePath = new File(travelMateDir, imgName); //concatenate file name

                            try {
                                output = new FileOutputStream(imagePath);
                                ExifInterface exif = new ExifInterface(imagePath.toString()); //instantiate exif object
                                if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")) { //retrieve exif data and compare
                                    bmp = rotate(bmp, 90); //rotate image if comparison returns true
                                }
                                bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
                                output.flush();
                                output.close();
                                MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bmp, imgName, imgName); //insert bitmap image t path

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE); //trigger media scanner
                            Uri contentUri = Uri.fromFile(imagePath);
                            mediaScanIntent.setData(contentUri); //set Uri to scan for
                            getActivity().sendBroadcast(mediaScanIntent); //scan folder

                            // Add Image Path To List
                            String mCurrentPhotoPath = imagePath.getPath();
                            myList.add(mCurrentPhotoPath);
                            ((BaseAdapter) imageGridView.getAdapter()).notifyDataSetChanged();

                            // Refresh Gridview Image Thumbnails
                            imageGridView.invalidateViews();

                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                case 3: {
                    if (resultCode == RESULT_OK) { //check if result code is OK
                        try {
                            Uri videoUri = data.getData(); //retrieve Uri data from intent
                            dir_path = "TravelMate";
                            travelMateDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), dir_path);
                            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            vidName = (timeStamp + ".mp4");
                            File vidPath = new File(travelMateDir, vidName);

                            try {
                                InputStream in = getActivity().getContentResolver().openInputStream(videoUri);
                                try {
                                    vidPath.setWritable(true, false); //allow video path to be writable
                                    OutputStream outputStream = new FileOutputStream(vidPath);//output video path
                                    byte buffer[] = new byte[1024];
                                    int length = 0;

                                    while((length=in.read(buffer)) > 0) {
                                        outputStream.write(buffer,0,length);
                                    }
                                    outputStream.close();
                                    in.close();

                                }catch (IOException e) {
                                    Log.d("Error message: ", "error in creating a file"); //error message
                                    e.printStackTrace();
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE); //trigger media scanner
                            Uri contentUri = Uri.fromFile(vidPath);
                            mediaScanIntent.setData(contentUri); //set Uri to scan for
                            getActivity().sendBroadcast(mediaScanIntent);

                            String mCurrentVidPath = vidPath.getPath();
                            myList.add( mCurrentVidPath);
                            ((BaseAdapter) imageGridView.getAdapter()).notifyDataSetChanged();

                            // Refresh Gridview Image Thumbnails
                            imageGridView.invalidateViews();

                        } catch ( Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
            }
        }

        //check if folder directory exist
        private void checkDir() {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) { //check if storage exist
                dir_path = "TravelMate";
                travelMateDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), dir_path);

                if (!travelMateDir.exists() && (!travelMateDir.isDirectory())) {
                    travelMateDir.mkdir();
                }
            } else {
                Toast.makeText(getActivity(), "Unable to locate device storage", Toast.LENGTH_SHORT).show();
            }
        }

        //this method is to rotate bitmap
        private Bitmap rotate(Bitmap bmp, int degree) {

            int w = bmp.getWidth(); //get bitmap width
            int h = bmp.getHeight(); //get bitmap height

            Matrix mtx = new Matrix();
            mtx.postRotate(degree);

            return Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true); //create bitmap based on rotation
        }

        //this class prepares bitmaps and image views to be displayed in Grid View
        public class ImageAdapter extends BaseAdapter {
            private Context mContext;
            private ArrayList<String> filePathList;

            public ImageAdapter(Context c, ArrayList<String> fileList) {
                mContext = c;
                filePathList = fileList;
            }

            public int getCount() {

                return (filePathList == null) ? 0 : filePathList.size();
            }

            public Object getItem(int position) {
                return null;
            }

            public long getItemId(int position) {
                return 0;
            }

            public View getView(int position, View convertView, ViewGroup parent) { //create new view for each image
                ImageView imageView;

                if (convertView == null) { //instantiate view if null
                    imageView = new ImageView(mContext);
                    imageView.setLayoutParams(new GridView.LayoutParams(250, 250)); //sets h and w of view
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY); //scale image using fill
                    imageView.setPadding(5, 5, 5, 5);
                    imageView.setAdjustViewBounds(true); //preserve aspect ration of image

                } else {
                    imageView = (ImageView) convertView;
                }

                imageView.setTag(filePathList.get(position));
                new LoadImages(imageView).execute();

                return imageView;
            }
        }

        private class LoadImages extends AsyncTask<Object,Void,Bitmap> {
            private ImageView imgView;
            private String path;
            String vidType = ".mp4";
            String imgType = ".jpg";

            public LoadImages(ImageView imgView) {
                this.imgView = imgView;
                this.path = imgView.getTag().toString();
            }

            @Override
            protected Bitmap doInBackground(Object... params) {
                Bitmap imgBitmap, bmp = null;

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = 6; //set bitmap options for decoding

                try {
                    if (path.contains(imgType)) { //check if its an image path
                        imgBitmap = BitmapFactory.decodeFile(path, bmOptions);
                        int dimension = getSquareCropDimensionForBitmap(imgBitmap);
                        bmp = ThumbnailUtils.extractThumbnail(imgBitmap, dimension, dimension);

                    } else if (path.contains(vidType)) { //check if its a video path
                        bmp = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return bmp;
            }

            @Override
            protected void onPostExecute(Bitmap result) {

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
            //If the bitmap is wider than it is tall use the height as the square crop dimension
            if (bitmap.getWidth() >= bitmap.getHeight())
            {
                dimension = bitmap.getHeight();
            }
            //If the bitmap is taller than it is wide use the width as the square crop dimension
            else
            {
                dimension = bitmap.getWidth();
            }
            return dimension;
        }

        private List<String> getAllDates(long ID) { //get all dates of this trip
            TripDB db = new TripDB(getActivity());
            Cursor c = db.getOneTrip(ID); //place data retrieved into Cursor object
            String startDate = null;
            String endDate = null;
            while (c.moveToNext()) { //iterate to retrieve data in Cursor object
                startDate = c.getString(2);
                endDate = c.getString(3);
            }
            List<String> dates = getDates(startDate, endDate); //insert data retrieved into a List
            ArrayList<String> allDates = new ArrayList<String>();
            for (String date : dates) {
                allDates.add(date);
            }
            return allDates;
        }

        //method to retrieve dates in between start date and and end date of a trip
        private static List<String> getDates(String firstDate, String lastDate) {
            ArrayList<String> dates = new ArrayList<String>();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy"); //use to format date

            Date date1 = null;
            Date date2 = null;

            try {
                date1 = dateFormatter.parse(firstDate); //parse String date into Date object
                date2 = dateFormatter.parse(lastDate);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date1);

            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date2);

            while(!cal1.after(cal2)) { // iterate to check if start date is before end date
                dates.add(dateFormatter.format(cal1.getTime()));
                cal1.add(Calendar.DATE, 1);
            }
            return dates;
        }
    }
}

