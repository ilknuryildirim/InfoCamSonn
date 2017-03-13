package com.infocam;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.TaskStackBuilder;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by ilknuryildirim on 14/03/16.
 */


public class SettingActivity extends BaseActivity {
    Button changepas;
    final int RESULT_LOAD_IMAGE = 1;
    Bitmap bmp;
    ImageView  imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        changepas = (Button) findViewById(R.id.btchangepass);

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        /**
         * Hashmap to load data from the Sqlite database
         **/
        HashMap<String,String> user = new HashMap<String, String>();
        user = db.getUserDetails();


        /**
         * Change Password Activity Started
         **/
        changepas.setOnClickListener(new View.OnClickListener(){
            public void onClick(View arg0){

                createBackStack(new Intent(getApplicationContext(), ChangePassword.class));
            }

        });

        ImageView buttonLoadImage = (ImageView) findViewById(R.id.buttonLoadPicture);
        imageView = (ImageView) findViewById(R.id.imgView);
        new UploadImage(2).execute();
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });




        //Set nav drawer selected to second item in list
        mNavigationView.getMenu().getItem(6).setChecked(true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();



            bmp = null;
            try {
                bmp = getBitmapFromUri(selectedImage);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            imageView.setImageBitmap(bmp);
            new UploadImage(1).execute();

        }


    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
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
    private class UploadImage extends AsyncTask<String,String, JSONObject> {
        int b;
        UploadImage(int a){
            b = a;

        }


        @Override
        protected JSONObject doInBackground(String... args) {
            //holds byte representation of the image
            UserFunctions userFunction = new UserFunctions();
            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            HashMap<String,String> user = new HashMap<String, String>();
            user = db.getUserDetails();
            String fromID = user.get("uid");
            JSONObject json;
            if(b == 1) {
                String namenew = fromID + "_profil";
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream);
                String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
                json = userFunction.updateProfilePicture(namenew, encodedImage, fromID);
            }
            else{
                String namenew = fromID + "_profil";
                json = userFunction.getProfilePic(namenew);


            }
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            if(b == 1) {
                Toast.makeText(getApplicationContext(), "IMAGE UPLOADED SUCCESSFULLY", Toast.LENGTH_LONG).show();
            }else{
                try {

                    String image = json.getString("image");

                    byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imageView.setImageBitmap(decodedByte);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }


}
