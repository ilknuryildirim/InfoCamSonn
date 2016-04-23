package com.infocam;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;




public class ProfileActivity extends BaseActivity {
    Button requestsButton;
    Button friendsButton;
    Button myGallery;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        requestsButton = (Button) findViewById(R.id.requestButton);
        requestsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), RequestActivity.class);
                startActivityForResult(myIntent, 0);
                finish();
            }
        });

        friendsButton = (Button) findViewById(R.id.friendsButton);
        friendsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), MyFriendsActivity.class);
                startActivityForResult(myIntent, 0);
                finish();
            }});
        myGallery = (Button) findViewById(R.id.galleryButton);
        myGallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), MyGalleryActivity.class);
                startActivityForResult(myIntent, 0);
                finish();
            }});

        img = (ImageView) findViewById(R.id.imageview_profile);
        new UploadImage().execute();

        /**
         * Hashmap to load data from the Sqlite database
         **/
        HashMap user = new HashMap<String, String>();
        user = db.getUserDetails();

        TextView login = (TextView) findViewById(R.id.textview_username);
        login.setText("Welcome  " + user.get("fname") + " " + user.get("lname"));

        TextView email = (TextView) findViewById(R.id.textview_email);
        email.setText("Email: " + user.get("email"));
        /*final TextView bio = (TextView) findViewById(R.id.profileBio);
        bio.setText(user.get("bio"));*/

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "ProfileActivity", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });




        //Set nav drawer selected to second item in list
        mNavigationView.getMenu().getItem(2).setChecked(true);

    }



    /** HIDE TOOLBAR **/
//    @Override
//    protected boolean useToolbar() {
//        return false;
//    }



    /** HIDE hamburger menu **/
//    @Override
//    protected boolean useDrawerToggle() {
//        return false;
//    }


    private class UploadImage extends AsyncTask<String,String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... args) {
            //holds byte representation of the image
            UserFunctions userFunction = new UserFunctions();
            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            HashMap<String,String> user = new HashMap<String, String>();
            user = db.getUserDetails();
            String username = user.get("username");
            JSONObject json;
            String namenew = username + "_profil";
            json = userFunction.getProfilePic(namenew);

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            try {
                JSONArray jsonarray = json.getJSONArray("images");
                JSONObject jsonobject = jsonarray.getJSONObject(0);
                String image = jsonobject.getString("image");

                byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                img.setImageBitmap(decodedByte);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

}





/** HIDE TOOLBAR **/
//    @Override
//    protected boolean useToolbar() {
//        return false;
//    }



/** HIDE hamburger menu **/
//    @Override
//    protected boolean useDrawerToggle() {
//        return false;
//    }


