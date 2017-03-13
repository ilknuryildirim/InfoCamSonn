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
import android.view.LayoutInflater;
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

/**as
 * Created by ilknuryildirim on 14/03/16.
 */


public class BuildingListActivity extends BaseActivity {
    ViewHolder mainViewholder;

    private ArrayList<String> data = new ArrayList<String>();
    private ArrayList<String> buildingIDs = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();

    private ArrayList<Bitmap> images = new ArrayList<Bitmap>();
    HashMap<String,String> buildings = new HashMap<String, String>();

    private ArrayAdapter<String> BuildingListAdapter;

    private static String KEY_SUCCESS = "success";

    private EditText searchBuilding;
    private ListView buildingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buildinglist);
        //Create a listview to show users
        buildingList=(ListView) findViewById(R.id.listview_building);
        //searchBuilding 	= 	(EditText) findViewById(R.id.searchBuilding);
        // get the user list


        BuildingListAdapter = new MyListAdaper(this, R.layout.list_item_buildings, buildingIDs);
        buildingList.setAdapter(BuildingListAdapter);
        buildingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                createBackStack(new Intent(getApplicationContext(), BuildingInfoActivity.class), position);
                //Toast.makeText(BuildingListActivity.this, "List item was clicked at " + position, Toast.LENGTH_SHORT).show();
            }
        });
        NetAsync("GetBuildings", "a");

        //Set nav drawer selected to second item in list
        mNavigationView.getMenu().getItem(5).setChecked(true);

    }
    /**
     * Enables back navigation for activities that are launched from the NavBar. See
     * {@code AndroidManifest.xml} to find out the parent activity names for each activity.
     * @param intent
     */
    private void createBackStack(Intent intent, int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntentWithParentStack(intent);
            builder.startActivities();
            intent.putExtra("url", urls.get(position));
            builder.startActivities();
        } else {
            intent.putExtra("url", urls.get(position));
            startActivity(intent);
            finish();
        }
    }
    private class MyListAdaper extends ArrayAdapter<String> {
        private int layout;
        //private List<String> mObjects;
        private List<String> mIDs;
        private MyListAdaper(Context context, int resource, List<String> IDs) {
            super(context, resource);
            mIDs = IDs;
            layout = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            mainViewholder = null;
            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.list_item_building);
                viewHolder.title = (TextView) convertView.findViewById(R.id.list_item_text_building);
                convertView.setTag(viewHolder);
            }
            mainViewholder = (ViewHolder) convertView.getTag();

            mainViewholder.title.setText(getItem(position));
            if(images.get(position)!=null)
                mainViewholder.thumbnail.setImageBitmap(images.get(position));


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

    private class NetCheck extends AsyncTask<String,String,Boolean>
    {

        private ProgressDialog nDialog;
        String myTask;
        String buildingID;
        public NetCheck (String task, String id){
            myTask = task;
            buildingID = id;
        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(BuildingListActivity.this);
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

                if ((nDialog != null) && nDialog.isShowing()) {
                    nDialog.dismiss();
                }
                if(myTask.equals("GetBuildings")){
                    new GetBuildings().execute();
                }

            }
            else{

                if ((nDialog != null) && nDialog.isShowing()) {
                    nDialog.dismiss();
                }
                //loginErrorMsg.setText("Error in Network Connection");
            }
        }
    }
    /**
     * Async Task to get and send data to My Sql database through JSON respone.
     **/
    private class GetBuildings extends AsyncTask<String, String, JSONObject> {


        private ProgressDialog pDialog;

        String username,password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(BuildingListActivity.this);
            pDialog.setTitle("Contacting Servers");
            // pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();

            JSONObject json = userFunction.getAllBuildings();


            Log.v("BUILDING TAG", json.toString());


            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);

                    BuildingListAdapter.clear();

                    if ((pDialog != null) && pDialog.isShowing()) {
                        pDialog.dismiss();
                    }
                    JSONArray jsonarray = json.getJSONArray("buildings");
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        String bname = jsonobject.getString("title");
                        String rate = jsonobject.getString("rate");
                        String bid = jsonobject.getString("infoID");
                        String url = jsonobject.getString("url");
                        String tmp;
                        tmp = bname + " rate: " + rate;
                        data.add(tmp);
                        urls.add(url);
                        buildingIDs.add(bid);
                        buildings.put(tmp,bid);

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
                    BuildingListAdapter.addAll(data);



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
