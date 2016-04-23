package com.infocam;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    private static String KEY_SUCCESS = "success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendprofile);
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        galleryButton = (Button) findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Toast.makeText(FriendProfileActivity.this, "gallery button is clicked", Toast.LENGTH_SHORT).show();

            }
        });



        Intent i = getIntent();
        friendID = i.getStringExtra("friendID");

        NetAsync("GetFriend", friendID);

        /**
         * Hashmap to load data from the Sqlite database
         **/
        HashMap<String,String> user = new HashMap<String, String>();
        user = db.getUserDetails();

        name = (TextView) findViewById(R.id.textview_username);

        email = (TextView) findViewById(R.id.textview_email);
        /*final TextView bio = (TextView) findViewById(R.id.profileBio);
        bio.setText(user.get("bio"));*/




        //Set nav drawer selected to second item in list
        mNavigationView.getMenu().getItem(2).setChecked(true);

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
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);

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
