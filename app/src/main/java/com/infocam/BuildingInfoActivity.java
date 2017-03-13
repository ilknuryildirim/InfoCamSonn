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
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by konuk on 23.4.2016.
 */
public class BuildingInfoActivity extends BaseActivity {
    private static String KEY_SUCCESS = "success";
    TextView title;
    TextView data;
    ImageButton buildImg;

    EditText comment;
    Button btnAddComment;

    private RatingBar ratingBar;
    private TextView txtRatingValue;
    private Button btnSubmit;

    int infoID;
    float ratingNew;
    float ratingOld;

    //String url = "tr.wikipedia.org/wiki/Do%C4%9Framac%C4%B1zade_Ali_Sami_Pa%C5%9Fa_Camii";
    String url="";


    private static final int SWIPE_MIN_DISTANCE = 0;
    private static final int SWIPE_THRESHOLD_VELOCITY = 0;

    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;

    private  GestureDetector detector;

    private int j = 0;
    ViewGroup p;
    LinearLayout commentList;
    private MyListAdaper CommentListAdapter;
    private ArrayList<String> datas = new ArrayList<String>();

    private ArrayList<String> comments = new ArrayList<String>();
    private ArrayList<String> users = new ArrayList<String>();
    private ArrayList<String> dates = new ArrayList<String>();
    /*public BuildingInfoActivity(String webUrl){
       url = webUrl;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_info);
        Intent i = getIntent();
        url = i.getStringExtra("url");
        if(url.contains("webpage")){
            url = url.substring(15);
        }


        commentList=(LinearLayout) findViewById(R.id.listview_comments);

        txtRatingValue = (TextView) findViewById(R.id.rateVal);

        comment = (EditText) findViewById(R.id.comment);
        btnAddComment = (Button) findViewById(R.id.btnAddComment);

        buildImg = (ImageButton) findViewById(R.id.img);

        data = (TextView) findViewById(R.id.data);
        //data.setMovementMethod(new ScrollingMovementMethod());

        title = (TextView) findViewById(R.id.title);

        new DownloadData().execute();
        new getRating().execute();

        p = (ViewGroup) commentList.getParent();

        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);



        btnAddComment.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (comment.getText().length() != 0) {
                    new AddComment(comment.getText().toString()).execute();
                    comment.setText("");
                    new getComm().execute();

                } else {
                    Toast.makeText(BuildingInfoActivity.this, "No comment to add", Toast.LENGTH_SHORT).show();
                }

            }

        });



    }

    private class MyListAdaper extends ArrayAdapter<String> {
        private int layout;
        private View view;
        //private List<String> mObjects;
        private List<String> mIDs;
        private MyListAdaper(Context context, int resource) {
            super(context, resource);
            layout = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder mainViewholder = null;
            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewHolder viewHolder = new ViewHolder();

                viewHolder.username = (TextView) convertView.findViewById(R.id.list_item_username);
                viewHolder.comment = (TextView) convertView.findViewById(R.id.list_item_comment);
                viewHolder.date = (TextView) convertView.findViewById(R.id.list_item_date);

                convertView.setTag(viewHolder);
            }
            mainViewholder = (ViewHolder) convertView.getTag();
            mainViewholder.comment.setText(comments.get(position));
            mainViewholder.username.setText(users.get(position));
            mainViewholder.date.setText(dates.get(position));
            view = convertView;
            return convertView;
        }

       /* public View getAdapterView(){
            return view;
        }

        public void addItems(String uname, String ctext, String cdate){
            username.setText(uname);
            mainViewholder.comment.setText(ctext);
            mainViewholder.date.setText(cdate);

        }*/


    }
    public class ViewHolder {

        TextView username;
        TextView comment;
        TextView date;
    }

    private class DownloadData extends AsyncTask<String,String, JSONObject> {
        private ProgressDialog pDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            pDialog = new ProgressDialog(BuildingInfoActivity.this);
            pDialog.setTitle("Contacting Servers");
            // pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();

            JSONObject json;
            json = userFunction.getBuildingInfo(url);
            //json = userFunction.getBuildingPic("tr.wikipedia.org/wiki/Bilkent_%C3%9Cniversitesi_K%C3%BCt%C3%BCphanesi");
            System.out.print("myJSON building is "+ json.toString());


            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);

                    // UserListAdapter.clear();
                    JSONArray jsonarray = json.getJSONArray("building");
                    JSONObject jsonobject = jsonarray.getJSONObject(0);
                    title.setText(jsonobject.getString("title"));
                    data.setText(jsonobject.getString("descript"));

                    txtRatingValue.setText("Rate: " + jsonobject.getString("rate"));

                    infoID = jsonobject.getInt("infoID");

                    ratingOld = Float.parseFloat(jsonobject.getString("rate"));

                    new DownloadImage(jsonobject.getString("title")).execute();

                    new getRating().execute();
                    new getComm().execute();

                    pDialog.dismiss();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }


    private class getRating extends AsyncTask<String,String, JSONObject> {
        private ProgressDialog pDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(BuildingInfoActivity.this);
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
            String fromID = user.get("uid");

            JSONObject json;
            json = userFunction.getRating(fromID,String.valueOf(infoID));
            //json = userFunction.getBuildingPic("tr.wikipedia.org/wiki/Bilkent_%C3%9Cniversitesi_K%C3%BCt%C3%BCphanesi");


            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);
                    String rated = json.getString("rated");
                    if(Integer.parseInt(rated)==0){

                        btnSubmit = (Button) findViewById(R.id.btnSubmit);

                        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
                        //btnSubmit.setVisibility(View.GONE);
                        //ratingBar.setFocusable(false);

                        //if rating value is changed,
                        //display the current rating value in the result (textview) automatically
                        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                            public void onRatingChanged(RatingBar ratingBar, float rating,
                                                        boolean fromUser) {

                                ratingNew = rating;

                            }
                        });


                        //if click on me, then display the current rating value.
                        btnSubmit.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {

                                Toast.makeText(BuildingInfoActivity.this,
                                        String.valueOf(ratingBar.getRating()),
                                        Toast.LENGTH_SHORT).show();

                                new UpdateRating(ratingNew).execute();
                                btnSubmit.setVisibility(View.GONE);
                                ratingBar.setFocusable(false);
                                ratingBar.setClickable(false);
                                ratingBar.setFocusableInTouchMode(false);
                            }

                        });
                    }else{
                        btnSubmit = (Button) findViewById(R.id.btnSubmit);
                        btnSubmit.setVisibility(View.GONE);
                    }

                    pDialog.dismiss();

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private class DownloadImage extends AsyncTask<String,String, JSONObject> {
        private ProgressDialog pDialog;
        String title;
        public DownloadImage(String tit){
            title = tit;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(BuildingInfoActivity.this);
            pDialog.setTitle("Contacting Servers");
            // pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();

            JSONObject json;
            title = title.replaceAll("ü", "u");
            title = title.replaceAll("ı", "i");
            title = title.replaceAll("ğ", "g");
            title = title.replaceAll("ş", "s");
            title = title.replaceAll("ç", "c");
            title = title.replaceAll("ö", "o");
            title = title.replaceAll("Ü", "U");

            title = title.replaceAll(" ","");
            json = userFunction.getBuildingPic(title);
            System.out.print("myJSON building new "+ json.toString());


            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);

                    // UserListAdapter.clear();
                    JSONArray jsonarray = json.getJSONArray("images");
                    JSONObject jsonobject = jsonarray.getJSONObject(0);
                    /*String image = jsonobject.getString("image");



                    byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    buildImg.setImageBitmap(decodedByte);
                    buildImg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            zoomImageFromThumb(buildImg, ((BitmapDrawable)buildImg.getDrawable()).getBitmap());
                        }
                    });
*/


                    String nopic = jsonobject.getString("nopic");
                    if(Integer.parseInt(nopic)==0){
                        String image = jsonobject.getString("image");



                        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                        buildImg.setImageBitmap(decodedByte);
                        buildImg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                zoomImageFromThumb(buildImg, ((BitmapDrawable) buildImg.getDrawable()).getBitmap());
                            }
                        });

                    }
                    pDialog.dismiss();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
    private class UpdateRating extends AsyncTask<String,String, JSONObject> {
        private ProgressDialog pDialog;
        float rating;

        public UpdateRating(float val){
            rating = val;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(BuildingInfoActivity.this);
            pDialog.setTitle("Contacting Servers");
            // pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();

            JSONObject json;
            float num = (float)(ratingNew + ratingOld)/2;


            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            HashMap<String,String> user = new HashMap<String, String>();
            user = db.getUserDetails();
            String fromID = user.get("uid");
            String a = String.format("%.2f", num);
            a = a.replaceAll(",",".");
            json = userFunction.updateRating(Float.parseFloat(a), String.valueOf(infoID), fromID);
            //json = userFunction.getBuildingPic("tr.wikipedia.org/wiki/Bilkent_%C3%9Cniversitesi_K%C3%BCt%C3%BCphanesi");
            System.out.print("myJSON rate is "+ json.toString());


            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);

                    // UserListAdapter.clear();

                    float num = (float)(ratingNew + ratingOld)/2;
                    txtRatingValue.setText("Rate: " + String.valueOf(String.format("%.2f", num)));


                    pDialog.dismiss();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private class AddComment extends AsyncTask<String,String, JSONObject> {
        private ProgressDialog pDialog;
        String com;

        public AddComment(String val){
            com = val;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(BuildingInfoActivity.this);
            pDialog.setTitle("Contacting Servers");
            // pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();

            JSONObject json;


            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            HashMap<String,String> user = new HashMap<String, String>();
            user = db.getUserDetails();
            String fromID = user.get("uid");
            json = userFunction.addComment(fromID, com, String.valueOf(infoID));

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);

                    if(Integer.parseInt(res) == 1){
                        Toast.makeText(BuildingInfoActivity.this, "Your comment is succesfully added", Toast.LENGTH_SHORT).show();
                    }

                    new DownloadData().execute();

                    pDialog.dismiss();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
    private class getComm extends AsyncTask<String,String, JSONObject> {
        private ProgressDialog pDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(BuildingInfoActivity.this);
            pDialog.setTitle("Contacting Servers");
            // pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();

            JSONObject json;


            json = userFunction.getComment(String.valueOf(infoID));

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {
                    RelativeLayout parent = new RelativeLayout(BuildingInfoActivity.this);
                    String res = json.getString(KEY_SUCCESS);
                    if(Integer.parseInt(res)==1) {
                        if (commentList.getChildCount() > 0)
                            commentList.removeAllViews();

                        JSONArray jsonarray = json.getJSONArray("comments");
                        for (int i = 0; i < jsonarray.length(); i++) {
                            JSONObject jsonobject = jsonarray.getJSONObject(i);
                            String username = jsonobject.getString("username");
                            String commentt = jsonobject.getString("ctext");
                            String date = jsonobject.getString("cdate");
                            String a = username + " - " + commentt + " - " + date;
                            datas.add(a);

                            View convertView = null;
                            ViewHolder mainViewholder = null;
                            if (convertView == null) {
                                LayoutInflater inflater = LayoutInflater.from(getBaseContext());
                                convertView = inflater.inflate(R.layout.list_item_comments, p, false);
                                ViewHolder viewHolder = new ViewHolder();

                                viewHolder.username = (TextView) convertView.findViewById(R.id.list_item_username);
                                viewHolder.comment = (TextView) convertView.findViewById(R.id.list_item_comment);
                                viewHolder.date = (TextView) convertView.findViewById(R.id.list_item_date);

                                convertView.setTag(viewHolder);
                            }
                            mainViewholder = (ViewHolder) convertView.getTag();
                            mainViewholder.comment.setText(commentt);
                            mainViewholder.username.setText(username);
                            mainViewholder.date.setText(date);

                            commentList.addView(convertView);
                            //CommentListAdapter.addItems(username, commentt, date);
                        }
                    }
                    pDialog.dismiss();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void zoomImageFromThumb(final View thumbView, Bitmap imageResId) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) findViewById(
                R.id.expanded_image3);
        expandedImageView.setImageBitmap(imageResId);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
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
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
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

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
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

}
