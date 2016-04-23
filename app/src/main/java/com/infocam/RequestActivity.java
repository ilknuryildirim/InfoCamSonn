package com.infocam;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RequestActivity extends BaseActivity {

    public ArrayList<String> dataR = new ArrayList<String>();
    private ArrayList<String> userIDs = new ArrayList<>();
    HashMap<String,String> friends = new HashMap<String, String>();
    private ArrayAdapter<String> UserListAdapter;
    private static String KEY_SUCCESS = "success";
    private ListView usersList;

    LinearLayout empty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        //Create a listview to show users
        usersList=(ListView) findViewById(R.id.listview_request);

            empty = (LinearLayout) findViewById(R.id.myempty);
        /*
         * Attach the empty view. The framework will show this view
         * when the ListView's adapter has no elements.
         */
            usersList.setEmptyView(empty);
            // continue adding adapters and data to the list

            //usersList=new ListView(this);
            UserListAdapter = new MyListAdapter(this, R.layout.list_item_request, userIDs);
            usersList.setAdapter(UserListAdapter);
            usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(RequestActivity.this, "List item was clicked at " + position, Toast.LENGTH_SHORT).show();
                }
            });

        NetAsync("GetRequests", "a");

        //setContentView(usersList);

    }


    private class MyListAdapter extends ArrayAdapter<String> {
        private int layout;
        private List<String> mObjects;
        private List<String> mIDs;
        private MyListAdapter(Context context, int resource,List<String> IDs) {
            super(context, resource);
            mIDs = IDs;
            layout = resource;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder2 mainViewholder = null;

            if(convertView == null) {

                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewHolder2 viewHolder = new ViewHolder2();
                viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.list_item_thumbnail_req);
                viewHolder.title = (TextView) convertView.findViewById(R.id.list_item_text_req);
                viewHolder.buttonA = (Button) convertView.findViewById(R.id.acceptButton);
                viewHolder.buttonR = (Button) convertView.findViewById(R.id.rejectButton);
                convertView.setTag(viewHolder);
            }
            mainViewholder = (ViewHolder2) convertView.getTag();
            // friends deki user listesinde userların yanında bulunan add friend butonu
            mainViewholder.buttonA.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NetAsync("AcceptFriend", friends.get(v.getTag()));
                    NetAsync("GetRequests", "a");

                }
            });
            // friends deki user listesinde userların yanında bulunan reject butonu

                mainViewholder.buttonR.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NetAsync("RejectFriend", friends.get(v.getTag()));
                        NetAsync("GetRequests", "a");
                    }
                });


            mainViewholder.title.setText(getItem(position));
            mainViewholder.buttonA.setTag(getItem(position));
            mainViewholder.buttonR.setTag(getItem(position));
            return convertView;
        }
    }
    public class ViewHolder2 {

        ImageView thumbnail;
        TextView title;
        Button buttonA;
        Button buttonR;
    }

    /**
     * Async Task to check whether internet connection is working.
     **/

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
            nDialog = new ProgressDialog(RequestActivity.this);
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
                if(myTask.equals("GetRequests")){
                    new ProcessGetRequest().execute();
                } else if(myTask.equals("AcceptFriend")){
                    new AcceptFriend(friendID).execute();
                } else if(myTask.equals("RejectFriend")){
                    new RejectFriend(friendID).execute();
                }

            }
            else{
                nDialog.dismiss();
                //loginErrorMsg.setText("Error in Network Connection");
            }
        }
    }

    /**
     * Async Task to get and send data to My Sql database through JSON respone.
     **/
    private class ProcessGetRequest extends AsyncTask<String, String, JSONObject> {


        private ProgressDialog pDialog;

        String username,password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(RequestActivity.this);
            pDialog.setTitle("Contacting Servers");
            // pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();
            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            HashMap<String,String> user = new HashMap<String, String>();
            user = db.getUserDetails();
            String userID = user.get("uid");

            JSONObject json = userFunction.getRequests(userID);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);

                    UserListAdapter.clear();
                    pDialog.dismiss();

                    JSONArray jsonarray = json.getJSONArray("users");
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        String firstname = jsonobject.getString("firstname");
                        String lastname = jsonobject.getString("lastname");
                        String uid = jsonobject.getString("uid");

                        String tmp;
                        tmp = firstname + " " + lastname;
                        dataR.add(tmp);
                        userIDs.add(uid);
                        friends.put(tmp, uid);
                    }

                    UserListAdapter.addAll(dataR);


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class AcceptFriend extends AsyncTask<String, String, JSONObject> {


        private ProgressDialog pDialog;
        String sender;

        public AcceptFriend(String fid){
            sender = fid;
        }

        String username,password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(RequestActivity.this);
            pDialog.setTitle("Contacting Servers");
            // pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();
            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            HashMap<String,String> user = new HashMap<String, String>();
            user = db.getUserDetails();
            String owner = user.get("uid");
            JSONObject json = userFunction.acceptFriend(owner, sender);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);

                    pDialog.dismiss();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class RejectFriend extends AsyncTask<String, String, JSONObject> {


        private ProgressDialog pDialog;
        String sender;

        public RejectFriend(String fid){
            sender = fid;
        }

        String username,password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(RequestActivity.this);
            pDialog.setTitle("Contacting Servers");
            // pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();
            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            HashMap<String,String> user = new HashMap<String, String>();
            user = db.getUserDetails();
            String owner = user.get("uid");
            JSONObject json = userFunction.rejectFriend(owner, sender);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {
                    String res = json.getString(KEY_SUCCESS);
                    pDialog.dismiss();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
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
