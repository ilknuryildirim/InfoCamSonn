package com.infocam;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;


public class FriendProfileActivity extends BaseActivity {

    String friendID;
    TextView name;
    TextView email;
    Button galleryButton;
    ImageView img;

    private static String KEY_SUCCESS = "success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendprofile);
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());




        img = (ImageView) findViewById(R.id.imageview_profile);

        Intent i = getIntent();
        friendID = i.getStringExtra("friendID");


        NetAsync("GetFriend", friendID);
        galleryButton = (Button) findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(), FriendGalleryActivity.class);
                myIntent.putExtra("friendID", friendID);
                startActivityForResult(myIntent, 0);
                //startActivity(myIntent);
                finish();
            }
        });
        /**
         * Hashmap to load data from the Sqlite database
         **/
        HashMap<String,String> user = new HashMap<String, String>();
        user = db.getUserDetails();

        name = (TextView) findViewById(R.id.textview_username);

        email = (TextView) findViewById(R.id.textview_email);



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


    private class NetCheck extends AsyncTask<String,String,Boolean>
    {

        private ProgressDialog nDialog;
        String myTask;
        String friendID;
        public NetCheck (String task, String id){
            myTask = task;
            friendID = id;
        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(FriendProfileActivity.this);
            nDialog.setTitle("Checking Network");
            nDialog.setMessage("Loading..");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();
        }
        /**
         * Gets current device state and checks for working internet connection by trying Google.
         **/
        @Override
        protected Boolean doInBackground(String... args){

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(3000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        return true;
                    }
                } catch (MalformedURLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return false;

        }
        @Override
        protected void onPostExecute(Boolean th){

            if(th == true){
                nDialog.dismiss();
                if(myTask.equals("GetFriend")){
                    new GetFriend().execute();
                }

            }
            else{
                nDialog.dismiss();
                //loginErrorMsg.setText("Error in Network Connection");
            }
        }
    }
    private class GetFriend extends AsyncTask<String, String, JSONObject> {


        private ProgressDialog pDialog;

        String username,password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(FriendProfileActivity.this);
            pDialog.setTitle("Contacting Servers");
            // pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();
//            Toast.makeText(FriendProfileActivity.this, "friendid = " + friendID, Toast.LENGTH_SHORT).show();
            System.out.println("FRIEND ID = " + friendID);
            JSONObject json = userFunction.getFriend(friendID);
            Log.v("FRIENDjson",json.toString());

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);
                    if(Integer.parseInt(res)==1) {
                        pDialog.dismiss();

                        JSONObject jsonobject = json.getJSONObject("user");

                        String firstname = jsonobject.getString("fname");
                        String lastname = jsonobject.getString("lname");
                        String uid = jsonobject.getString("uid");
                        String tmp;
                        tmp = firstname + " " + lastname;
                        String emailA = jsonobject.getString("email");
                        name.setText(tmp);
                        email.setText("Email: " + emailA);

                        String image = json.getString("image");

                        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                        img.setImageBitmap(decodedByte);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void NetAsync(String task, String id){
        new NetCheck(task,id).execute();
    }

}
