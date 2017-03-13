package com.infocam;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;




public class ProfileActivity extends BaseActivity {
    Button requestsButton;
    Button friendsButton;
    Button myGallery;
    ImageView img;
    Bitmap bmp;
    int b;

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

                createBackStack(new Intent(getApplicationContext(), MyFriendsActivity.class));
            }});
        myGallery = (Button) findViewById(R.id.galleryButton);
        myGallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), MyGalleryActivity.class);
                startActivityForResult(myIntent, 0);
                finish();
            }});

        img = (ImageView) findViewById(R.id.imageview_profile);
        new GetImage().execute();

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

        //Set nav drawer selected to second item in list
        mNavigationView.getMenu().getItem(3).setChecked(true);

    }


    private void createBackStack(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntentWithParentStack(intent);
            builder.startActivities();
        } else {
            startActivity(intent);
            finish();
        }
    }


    private class GetImage extends AsyncTask<String,String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... args) {
            //holds byte representation of the image
            UserFunctions userFunction = new UserFunctions();
            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            HashMap<String,String> user = new HashMap<String, String>();
            user = db.getUserDetails();
            String fromID = user.get("uid");
            String username = user.get("username");
            JSONObject json;

            String namenew = fromID + "_profil";
            json = userFunction.getProfilePic(namenew);
            Log.v("getprofilepicture 2", json.toString());


            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            try {
                Log.v("getprofilepicture", json.toString());
                String image = json.getString("image");
                String nopic = json.getString("nopic");
                // nopic=0 resim var nopic=1 resim yok
                if(nopic.equals("0")){
                    byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    img.setImageBitmap(decodedByte);
                }

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


