package com.example.test.TravelMate;

import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import java.util.Date;

/**
 * Created by Angeline Cheah on 1/23/2015.
 */

public class ViewJournal extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_journal);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle extras = getIntent().getExtras();
        long ID = extras.getLong("journalID");

        Bundle journalID = new Bundle();
        journalID.putLong("journalID", ID);

        ViewJournalFragment viewJournalFrag = new ViewJournalFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, viewJournalFrag)
                    .commit();
        }
        viewJournalFrag.setArguments(journalID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_journal, menu);
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
    public static class ViewJournalFragment extends Fragment {

        private TextView journalDate;
        private EditText journalText;
        private Button attachBtn;
        private Button imageBtn;
        private Button videoBtn;
        private Button saveJournalBtn;
        private Bundle getJournalID;

        private String dir_path;
        private static final int ATTACH_REQUEST_CODE = 1;
        private static final int CAPTURE_REQUEST_CODE = 2;
        private static final int RECORD_REQUEST_CODE = 3;
        private static final int SELECT_PICTURE = 1;

        private File travelMateDir;
        private String imgName;
        private String vidName;
        private FileOutputStream output;
        private String timeStamp;
        private LinearLayout imgDiv;

        private Bitmap bmp;
        private ArrayList<String> myList = new ArrayList<String>();

        GridView imageGridView;

        public ViewJournalFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            getJournalID = this.getArguments();
            final long ID = getJournalID.getLong("journalID");

            View rootView = inflater.inflate(R.layout.fragment_add_view_journal, container, false);
            imgDiv = (LinearLayout) rootView.findViewById(R.id.imgDiv);
            journalDate = (TextView) rootView.findViewById(R.id.journalDateInput);
            journalText = (EditText) rootView.findViewById(R.id.journalInput);
            attachBtn = (Button) rootView.findViewById(R.id.attachFilesBtn);
            imageBtn = (Button) rootView.findViewById(R.id.imageBtn);
            videoBtn = (Button) rootView.findViewById(R.id.videoBtn);
            saveJournalBtn = (Button) rootView.findViewById(R.id.journalSaveBtn);
            imageGridView = (GridView) imgDiv.findViewById(R.id.imageGrid);
            imageGridView = (GridView) rootView.findViewById(R.id.imageGrid);

            saveJournalBtn.setText("Edit");
            saveJournalBtn.setTag(1);
            journalText.setFocusableInTouchMode(false);
            attachBtn.setEnabled(false);
            imageBtn.setEnabled(false);
            videoBtn.setEnabled(false);
            imageGridView.setEnabled(false);

            //create necessary variables for storing media files
            dir_path = "TravelMate";
            travelMateDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), dir_path);
            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imgName = (timeStamp + ".jpg");
            vidName = (timeStamp + ".mp4");
            output = null;
            final String vidType = ".mp4";
            final String imgType = ".jpg";

            displayJournalDetails(ID); //retrieve journal details
            imageGridView.setAdapter(new ImageAdapter(getActivity(), myList));

            //edit and update journal
            saveJournalBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int status = (Integer)v.getTag();
                    if (status == 1) {
                        saveJournalBtn.setText("Update");
                        saveJournalBtn.setTag(2);
                        journalText.setFocusableInTouchMode(true);
                        journalText.requestFocus();
                        attachBtn.setEnabled(true);
                        imageBtn.setEnabled(true);
                        videoBtn.setEnabled(true);
                        imageGridView.setEnabled(true);
                    } else if (status == 2) {
                        saveJournalBtn.setText("Edit");
                        saveJournalBtn.setTag(1);
                        journalText.clearFocus();
                        journalText.setFocusable(false);
                        attachBtn.setEnabled(false);
                        imageBtn.setEnabled(false);
                        videoBtn.setEnabled(false);
                        imageGridView.setEnabled(false);
                        update(ID);
                        hideSoftKeyboard(getActivity(), v);
                    }
                }
            });

            //open media file viewing intent
            imageGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    String imgPath = myList.get(position);
                    if (imgPath.contains(imgType)) {
                        intent.setDataAndType(Uri.parse("file://" + myList.get(position)),  "image/*");
                        startActivity(intent);
                    } else if (imgPath.contains(vidType)) {
                        intent.setDataAndType(Uri.parse("file://" + myList.get(position)),  "video/*");
                        startActivity(intent);
                    }
                }
            });

            //delete attached media files from journal
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

            //attach image to journal
            attachBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, ATTACH_REQUEST_CODE);
                }
            });

            //invoke camera intent to capture images
            imageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkDir(); //check if app folder exist
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAPTURE_REQUEST_CODE);
                }
            });

            //invoke video intent to record video
            videoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkDir(); //check if app folder exist
                    File videoPath = new File (vidName);
                    Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoPath));
                    startActivityForResult(cameraIntent, RECORD_REQUEST_CODE);
                }
            });

            return rootView;
        }

        public static void hideSoftKeyboard (Activity getActivity, View view) { //hide virtual keyboard after update
            InputMethodManager imm = (InputMethodManager)getActivity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isAcceptingText()) {
                imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
            }
        }

        private void displayJournalDetails(long ID) { //retrieve and display all journal details

            TripDB db = new TripDB(getActivity());
            db.open();
            Cursor c = db.viewJournal(ID); //cast data to cursor
            c.moveToFirst(); //loop cursor
            journalDate.setText(c.getString(0));
            journalText.setText(c.getString(1));
            journalText.setSelection(c.getString(1).length());

            try { //catch if json object is null
                JSONObject jsonObj = new JSONObject(c.getString(2).toString());
                JSONArray jsonArr = jsonObj.getJSONArray("imagePathArray"); //get JSON array stored in DB
                for (int i = 0; i < jsonArr.length(); i++) { //loop JSON array and add strings into array
                    myList.add(jsonArr.getString(i));
                }
            } catch ( Exception e) {
                e.printStackTrace();
            }
            c.close();
            db.close();
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        private void checkDir() { //to check for directory
            dir_path = "TravelMate";
            travelMateDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), dir_path);

            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                if (!travelMateDir.exists() && (!travelMateDir.isDirectory())) {
                    travelMateDir.mkdir();
                }
            } else {
                Toast.makeText(getActivity(), "No storage available", Toast.LENGTH_SHORT).show();
            }
        }

        public void update(long ID) { //update journal

            String journalTextInput = journalText.getText().toString(); //get inputs
            String getJournalDate = journalDate.getText().toString();
            String imagePathList = "";

            if (myList.size() > 0) { //if arraylist not empty, create JSON Object
                JSONObject json = new JSONObject();
                try {
                    json.put("imagePathArray", new JSONArray(myList)); //convert arraylist to JSON array
                } catch (Exception e) {
                    e.printStackTrace();
                }
                imagePathList = json.toString(); //convert JSON to string
            }

            TripDB db = new TripDB(getActivity()); //update trip's journal
            db.open();
            db.updateJournal(ID, journalTextInput, getJournalDate, imagePathList); //saving trip details
            Toast.makeText(getActivity(), "Journal is successfully updated", Toast.LENGTH_SHORT).show();
            db.close();
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case 1: {
                    if (resultCode == RESULT_OK) {
                        Uri imgChosenUri = data.getData(); //get data from intent
                        String[] filePathColumn = {MediaStore.Images.Media.DATA}; //store image data in string array
                        Cursor cursor = getActivity().getContentResolver().query(imgChosenUri, filePathColumn, null, null, null); //query for image
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);//get index of a the specific image
                        String picturePath = cursor.getString(columnIndex); //retrieve file path from cursor using index
                        cursor.close();

                        myList.add(picturePath); //add file path to arraylist
                        ((BaseAdapter) imageGridView.getAdapter()).notifyDataSetChanged(); //notify adapter

                        // Refresh Gridview Image Thumbnails
                        imageGridView.invalidateViews();
                    }
                    break;
                }
                case 2: {
                    if (resultCode == RESULT_OK) {
                        try {
                            Uri imgUri = data.getData(); //get data from intent
                            bmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imgUri); //retrieve bitmap from Uri
                            dir_path = "TravelMate";
                            travelMateDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), dir_path); //create file path
                            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); //get current date
                            imgName = (timeStamp + ".jpg");
                            output = null;
                            File imagePath = new File(travelMateDir, imgName); //concatenate to create full image path

                            try {
                                output = new FileOutputStream(imagePath);
                                ExifInterface exif=new ExifInterface(imagePath.toString()); //rotate image if wrong orientation
                                if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")){
                                    bmp= rotate(bmp, 90);
                                }
                                bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
                                output.flush();
                                output.close();
                                MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bmp, imgName, imgName); //insert into gallery

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE); //start scanner to update gallery
                            Uri contentUri = Uri.fromFile(imagePath);
                            mediaScanIntent.setData(contentUri);
                            getActivity().sendBroadcast(mediaScanIntent);

                            // Add Image Path To List
                            String mCurrentPhotoPath = imagePath.getPath();
                            myList.add( mCurrentPhotoPath);
                            ((BaseAdapter) imageGridView.getAdapter()).notifyDataSetChanged();

                            // Refresh Gridview Image Thumbnails
                            imageGridView.invalidateViews();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
                case 3: {
                    if (resultCode == RESULT_OK) {
                        try {
                            Uri videoUri = data.getData(); //get data from intent
                            File vidPath = new File(travelMateDir, vidName);

                            try {
                                InputStream in = getActivity().getContentResolver().openInputStream(videoUri);
                                try {
                                    vidPath.setWritable(true, false); //process video
                                    OutputStream outputStream = new FileOutputStream(vidPath); //store video
                                    byte buffer[] = new byte[1024];
                                    int length = 0;

                                    while((length=in.read(buffer)) > 0) {
                                        outputStream.write(buffer,0,length);
                                    }
                                    outputStream.close();
                                    in.close();

                                }catch (IOException e) {
                                    e.printStackTrace();
                                }

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE); //start scanner to update gallery
                            Uri contentUri = Uri.fromFile(vidPath);
                            mediaScanIntent.setData(contentUri);
                            getActivity().sendBroadcast(mediaScanIntent);

                            // add video path to list
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

        private Bitmap rotate(Bitmap bmp, int degree) { //rotate bitmap ir orientation is wrong
            int w = bmp.getWidth();
            int h = bmp.getHeight();

            Matrix mtx = new Matrix();
            mtx.postRotate(degree);

            return Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);
        }

        public class ImageAdapter extends BaseAdapter { //prepare and display media files
            private Context mContext;
            private ArrayList<String> filePathList;

            public ImageAdapter(Context c, ArrayList<String> fileList) {
                mContext = c;
                filePathList = fileList; //retrieved arraylist passed from parameter
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

        private class LoadImages extends AsyncTask <Object,Void,Bitmap> {

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
//                        imgView.setImageBitmap(bmp);
                    } else if (path.contains(vidType)) { //check if its a video path
                        bmp = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);
//                        imgView.setImageBitmap(bmp);
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

        private int getSquareCropDimensionForBitmap(Bitmap bitmap) { //get dimensions from bitmap
            int dimension;
            //If the bitmap is wider than it is height
            //use the height as the square crop dimension
            if (bitmap.getWidth() >= bitmap.getHeight())
            {
                dimension = bitmap.getHeight();
            }
            //If the bitmap is taller than it is width
            //use the width as the square crop dimension
            else
            {
                dimension = bitmap.getWidth();
            }
            return dimension;
        }


    }
}