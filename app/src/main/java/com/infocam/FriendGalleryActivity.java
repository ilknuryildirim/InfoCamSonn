package com.infocam;

        import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
        import android.content.Intent;
        import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class FriendGalleryActivity extends BaseActivity {

    String friendID;
    private static String KEY_SUCCESS = "success";
    public ArrayList<Image> data = new ArrayList<Image>();
    private ArrayAdapter<Bitmap> GalleryListAdapter;
    private SearchView searchImage;
    private GridView imagesList;
    public   ArrayList<Image> myIm = new ArrayList<Image>();
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    GridAdapter g;
    LinearLayout empty;
    ImageView expandedImageView ;
    private int j = 0;

    ImageView downloadedImage;
    private  GestureDetector detector;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendgallery);
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        Intent i = getIntent();
        friendID = i.getStringExtra("friendID");

        imagesList=(GridView) findViewById(R.id.imageview_fg);
        empty = (LinearLayout) findViewById(R.id.myempty);
        /*
         * Attach the empty view. The framework will show this view
         * when the ListView's adapter has no elements.
         */
        imagesList.setEmptyView(empty);

        searchImage    =  (SearchView) findViewById(R.id.searchImage_fg);

        NetAsync2();

        imagesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                j = position;
                zoomImageFromThumb(view, myIm.get(position).getBitmap());
            }
        });

        // Enabling Search Functionality
        searchImage.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                g.getFilter().filter(newText);
                return false;
            }

        });

        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);


    }
    private void zoomImageFromThumb(final View thumbView, Bitmap img){
        if(mCurrentAnimator != null){
            mCurrentAnimator.cancel();
        }
        expandedImageView = (ImageView) findViewById(R.id.expanded_image1);
        expandedImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (detector.onTouchEvent(event)) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        expandedImageView.setImageBitmap(img);

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container).getGlobalVisibleRect(finalBounds, globalOffset);

        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);


        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the
        // "center crop" technique. This prevents undesirable stretching during
        // the animation.
        // Also calculate the start scaling factor (the end scaling factor is
        // always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds
                .width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }


        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins,
        // it will position the zoomed-in view in the place of the thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations to the
        // top-left corner of
        // the zoomed-in view (the default is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties
        // (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set.play(
                ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y,
                        startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down to the
        // original bounds
        // and show the thumbnail instead of the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their
                // original values.
                AnimatorSet set = new AnimatorSet();
                set.play(
                        ObjectAnimator.ofFloat(expandedImageView, View.X,
                                startBounds.left))
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                                startBounds.top))
                        .with(ObjectAnimator.ofFloat(expandedImageView,
                                View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator.ofFloat(expandedImageView,
                                View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });


    }

    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                System.out.println("data size is is is" + myIm.size());
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

                    if(myIm.size()>j)
                    {
                        j++;

                        if(j < myIm.size())
                        {
                            expandedImageView.setImageBitmap(myIm.get(j).getBitmap());
                            return true;
                        }
                        else
                        {
                            j = 0;
                            expandedImageView.setImageBitmap(myIm.get(j).getBitmap());
                            return true;
                        }

                    }
                }
                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {


                    if(j>0)
                    {
                        j--;
                        expandedImageView.setImageBitmap(myIm.get(j).getBitmap());
                        return true;

                    }
                    else
                    {
                        j = myIm.size()-1;
                        expandedImageView.setImageBitmap(myIm.get(j).getBitmap());
                        return true;
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }




    private class NetCheck2 extends AsyncTask<String,String,Boolean>
    {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(FriendGalleryActivity.this);
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
                new DownloadImage().execute();
            }
            else{
                nDialog.dismiss();
                //loginErrorMsg.setText("Error in Network Connection");
            }
        }
    }


    private class DownloadImage extends AsyncTask<String,String, JSONObject>{
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(FriendGalleryActivity.this);
            pDialog.setTitle("Contacting Servers");
            // pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();

            JSONObject json = userFunction.getFriendPictures(friendID);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);
                    if(Integer.parseInt(res) == 1) {
                        // UserListAdapter.clear();
                        JSONArray jsonarray = json.getJSONArray("pictures");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            JSONObject jsonobject = jsonarray.getJSONObject(i);
                            String name = jsonobject.getString("name");
                            String image = jsonobject.getString("image");
                            String tag = jsonobject.getString("tag");

                            byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            data.add(new Image(tag, decodedByte));
                        }
                        g = new GridAdapter(FriendGalleryActivity.this, data);
                        imagesList.setAdapter(g);
                        detector = new GestureDetector(FriendGalleryActivity.this, new SwipeGestureDetector());
                    }
                    pDialog.dismiss();


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void NetAsync2(){
        new NetCheck2().execute();
    }

    private class GridAdapter extends BaseAdapter implements Filterable{

        private Context mContext;

        CustomFilter filter;

        private  ArrayList<Image> filterList = new ArrayList<Image>();


        public GridAdapter(Context c, ArrayList<Image> d) {
            mContext = c;
            myIm = d;
            filterList = d;

        }

        @Override
        public int getCount() {
            return myIm.size();
        }

        @Override
        public Image getItem(int position) {
            return myIm.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // TODO Auto-generated method stub
            GridViewHolder grid;
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {

                grid = new GridViewHolder();
                convertView = inflater.inflate(R.layout.list_gallery1, null);

                grid.imge1 = (ImageView)convertView.findViewById(R.id.list_image1);
                grid.tag1 = (TextView) convertView.findViewById(R.id.list_text1);
                grid.tag1.setText(myIm.get(position).getTag());
                grid.imge1.setImageBitmap(myIm.get(position).getBitmap());
            } else {
                grid = (GridViewHolder) convertView.getTag();
            }


            return convertView;
        }

        private  class GridViewHolder {
            TextView tag1;
            ImageView imge1;
        }


        @Override
        public android.widget.Filter getFilter() {
            if(filter == null){
                filter = new CustomFilter();
            }
            return filter;
        }

        //INNER CLASS
        private class CustomFilter extends android.widget.Filter{
            boolean a = true;
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if(constraint != null && constraint.length()>0){
                    constraint = constraint.toString().toUpperCase();
                    ArrayList<Image> fIm = new ArrayList<Image>();
                    for(int i = 0; i<filterList.size(); i++){
                        if(filterList.get(i).getTag().toUpperCase().contains(constraint)){
                            System.out.print("ADDED TAG IS" + filterList.get(i).getTag());
                            Image img1 = new Image(filterList.get(i).getTag(), filterList.get(i).getBitmap());
                            fIm.add(img1);
                        }
                    }
                    a = false;
                    results.count = fIm.size();
                    results.values = fIm;

                }else {
                    ArrayList<Image> fIm = new ArrayList<Image>();
                    for(int i = 0; i<filterList.size(); i++){
                        System.out.print("ADDED TAG IS" + filterList.get(i).getTag());
                        Image img1 = new Image(filterList.get(i).getTag(), filterList.get(i).getBitmap());
                        fIm.add(img1);
                    }
                    results.count = fIm.size();
                    results.values = fIm;

                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results){

                myIm = new ArrayList<Image>();
                myIm.addAll((ArrayList<Image>) results.values);
                g.notifyDataSetChanged();
                imagesList.setAdapter(g);



            }

        }

    }
}
