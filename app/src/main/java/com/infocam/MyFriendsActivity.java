package com.infocam;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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


public class MyFriendsActivity extends BaseActivity {

    private ArrayList<String> data = new ArrayList<String>();
    private ArrayList<String> userIDs = new ArrayList<>();
    HashMap<String,String> friends = new HashMap<String, String>();


    private ArrayList<Bitmap> images = new ArrayList<Bitmap>();

    private ArrayAdapter<String> UserListAdapter;

    private static String KEY_SUCCESS = "success";

    private EditText searchFriend;
    private ListView usersList;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myfriends);

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        HashMap<String,String> user = new HashMap<String, String>();
        user = db.getUserDetails();
        userID = user.get("uid");


        //Create a listview to show users
        usersList=(ListView) findViewById(R.id.listview_myf);
        searchFriend 	= 	(EditText) findViewById(R.id.searchFriend);
        // get the user list


        UserListAdapter = new MyListAdaper(this, R.layout.list_item_myfriends, data, userIDs);
        usersList.setAdapter(UserListAdapter);
        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent myIntent = new Intent(view.getContext(), FriendProfileActivity.class);
                Log.d("fidasd",friends.get(view.getTag()));
                myIntent.putExtra("friendID", friends.get(view.getTag()));
                startActivityForResult(myIntent, 0);
                //startActivity(myIntent);
                finish();
            }
        });

        NetAsync2("GetMyFriends", "a");

        // Enabling Search Functionality
        searchFriend.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                MyFriendsActivity.this.UserListAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub


            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    private class MyListAdaper extends ArrayAdapter<String> {
        private int layout;
        private List<String> mObjects;
        private List<String> mIDs;
        private MyListAdaper(Context context, int resource, List<String> objects, List<String> IDs) {
            super(context, resource, objects);
            mObjects = objects;
            mIDs = IDs;
            layout = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder mainViewholder = null;
            convertView = null;
            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.list_item_thumbnail_myfriends);
                viewHolder.title = (TextView) convertView.findViewById(R.id.list_item_text_myfriends);
                convertView.setTag(viewHolder);
            }
            mainViewholder = (ViewHolder) convertView.getTag();
            mainViewholder.title.setText(getItem(position));

            if(images.get(position)!=null)
                mainViewholder.thumbnail.setImageBitmap(images.get(position));
            convertView.setTag(getItem(position));
            return convertView;
        }
    }
    public class ViewHolder {

        ImageView thumbnail;
        TextView title;
    }
    /**
     * Async Task to check whether internet connection is working.
     **/

    private class NetCheck2 extends AsyncTask<String,String,Boolean>
    {

        private ProgressDialog nDialog;
        String myTask;
        String friendID;
        public NetCheck2 (String task, String id){
            myTask = task;
            friendID = id;
        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(MyFriendsActivity.this);
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
                if(myTask.equals("GetMyFriends")){
                    new ProcessGetFriends().execute();
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
    private class ProcessGetFriends extends AsyncTask<String, String, JSONObject> {


        private ProgressDialog pDialog;

        String username,password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MyFriendsActivity.this);
            pDialog.setTitle("Contacting Servers");
            // pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();
            JSONObject json = userFunction.getMyFriends(userID);
            System.out.println("my json = " + json.toString());
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);
                    if(Integer.parseInt(res) == 1) {
                        UserListAdapter.clear();

                        JSONArray jsonarray = json.getJSONArray("users");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            JSONObject jsonobject = jsonarray.getJSONObject(i);
                            String firstname = jsonobject.getString("firstname");
                            String lastname = jsonobject.getString("lastname");
                            String uid = jsonobject.getString("uid");

                            String tmp;
                            tmp = firstname + " " + lastname;
                            data.add(tmp);
                            userIDs.add(uid);
                            friends.put(tmp, uid);
                            String name = jsonobject.getString("name");

                            String hasimage = jsonobject.getString("hasimage");
                            if(Integer.parseInt(hasimage)==1){
                                String image = jsonobject.getString("image");
                                byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                images.add(i,decodedByte);

                            }else {
                                images.add(i,null);
                            }
                        }
                    }
                    pDialog.dismiss();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public void NetAsync2(String task, String id){
        new NetCheck2(task,id).execute();
    }

    private class UploadImage extends AsyncTask<String,String, JSONObject> {
        ImageView img;
        String fID;
        public UploadImage(ImageView image, String friendsID){
            img = image;
            fID = friendsID;

        }
        @Override
        protected JSONObject doInBackground(String... args) {
            //holds byte representation of the image
            UserFunctions userFunction = new UserFunctions();
            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            HashMap<String,String> user = new HashMap<String, String>();
            user = db.getUserDetails();
            JSONObject json;
            String namenew = fID + "_profil";
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
