package com.infocam;

import android.content.Context;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class UserFunctions {

    private JSONParser jsonParser;

    //URL of the PHP API/aaa
    private static String URL = "http://139.179.207.228/infocam_server/";
    //private static String URL = "http://192.168.2.229/infocam_server/";

    private static String login_tag = "login";
    private static String register_tag = "register";
    private static String forpass_tag = "forpass";
    private static String chgpass_tag = "chgpass";
    private static String getUsers = "gtusrs";
    private static String addFriend = "addFriend";
    private static String getRequests = "getRequests";
    private static String acceptFriend = "acceptFriend";
    private static String rejectFriend = "rejectFriend";
    private static String getMyFriends = "getMyFriends";
    private static String getFriend = "getFriend";
    private static String addPic = "addPic";
    private static String getAllPics = "getAllPics";
    private static String getMyPics = "getMyPics";
    private static String updateProfilePicture = "updateProfilePicture";
    private static String getProfilePic = "getProfilePic";
    private static String getAllBuildings = "getAllBuildings";
    private static String getBuildingInfo = "getBuildingInfo";
    private static String getBuildingPic = "getBuildingPic";
    private static String updateRating = "updateRating";
    private static String getBuildingPhotos = "getBuildingPhotos";
    private static String getRating = "getRating";
    private static String getFriendPictures = "getFriendPictures";
    private static String getComment = "getComment";
    private static String addComment = "addComment";


    // constructor
    public UserFunctions(){
        jsonParser = new JSONParser();

    }

    /**
     * Function to send friend request to other user
     **/
    public JSONObject addFriend(String from, String to){
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", addFriend));
        params.add(new BasicNameValuePair("fromUser", from));
        params.add(new BasicNameValuePair("toUser", to));
        JSONObject json = jsonParser.getJSONFromUrl(URL, params);
        return json;
    }
    /**
     * Function to accept friend request
     **/
    public JSONObject acceptFriend(String owner, String sender){
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", acceptFriend));
        params.add(new BasicNameValuePair("owner", owner));
        params.add(new BasicNameValuePair("sender", sender));
        JSONObject json = jsonParser.getJSONFromUrl(URL, params);
        return json;
    }
    /**
     * Function to reject friend request
     **/
    public JSONObject rejectFriend(String owner, String sender){
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", rejectFriend));
        params.add(new BasicNameValuePair("owner", owner));
        params.add(new BasicNameValuePair("sender", sender));
        JSONObject json = jsonParser.getJSONFromUrl(URL, params);
        return json;
    }
    /**
     *
     * Function to get REQUESTS
     **/
    public JSONObject getRequests(String userID){
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", getRequests));
        params.add(new BasicNameValuePair("userID", userID));
        JSONObject json = jsonParser.getJSONFromUrl(URL, params);
        return json;
    }
    /**
     * Function to get all users
     **/
    public JSONObject getAllUsers(String userID){


        Log.v("GETFRIENDSSSSSSSSS","I AM IN 4");
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", getUsers));
        params.add(new BasicNameValuePair("userID", userID));
        JSONObject json = jsonParser.getJSONFromUrl(URL, params);
        System.out.println("get users  :  " + json.toString());
        return json;
    }
    /**
     * Function to get user's friends
     **/
    public JSONObject getMyFriends(String userID){


        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", getMyFriends));
        params.add(new BasicNameValuePair("userID", userID));
        JSONObject json = jsonParser.getJSONFromUrl(URL, params);
        return json;
    }
    /**
     * Function to get user's friends
     **/
    public JSONObject getFriend(String friendID){


        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", getFriend));
        params.add(new BasicNameValuePair("friendID", friendID));
        JSONObject json = jsonParser.getJSONFromUrl(URL, params);
        return json;
    }
    /**
     * Function to Login
     **/
    public JSONObject loginUser(String username, String password){
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", login_tag));
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        JSONObject json = jsonParser.getJSONFromUrl(URL, params);
        return json;
    }
    /**
     * Function to change password
     **/
    public JSONObject chgPass(String newpas, String email){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", chgpass_tag));

        params.add(new BasicNameValuePair("newpas", newpas));
        params.add(new BasicNameValuePair("email", email));
        JSONObject json = jsonParser.getJSONFromUrl(URL, params);
        return json;
    }
    /**
     * Function to reset the password
     **/
    public JSONObject forPass(String forgotpassword){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", forpass_tag));
        params.add(new BasicNameValuePair("forgotpassword", forgotpassword));
        JSONObject json = jsonParser.getJSONFromUrl(URL, params);
        return json;
    }
    /**
     * Function to  Register
     **/
    public JSONObject registerUser(String fname, String lname, String email, String uname, String password){
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", register_tag));
        params.add(new BasicNameValuePair("fname", fname));
        params.add(new BasicNameValuePair("lname", lname));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("uname", uname));
        params.add(new BasicNameValuePair("password", password));
        JSONObject json = jsonParser.getJSONFromUrl(URL,params);

        System.out.println("registered json" + json.toString());
        return json;
    }
    public JSONObject addPic(String name, String image,String userId,String tagI){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag",addPic));
        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("image", image));
        params.add(new BasicNameValuePair("userID", userId));
        params.add(new BasicNameValuePair("tagI", tagI));


        JSONObject json = jsonParser.getJSONFromUrl(URL, params);
        System.out.println("addPic  :  " + json.toString());

        return json;
    }
    public JSONObject getAllPics(){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag",getAllPics));
        JSONObject json1 = jsonParser.getJSONFromUrl(URL, params);
        System.out.println("getAll  :  " + json1.toString());
        return json1;
    }
    public JSONObject getMyPics(String userID){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag",getMyPics));
        params.add(new BasicNameValuePair("userID", userID));

        JSONObject json = jsonParser.getJSONFromUrl(URL, params);
        System.out.println("getMy  :  " + json.toString());

        return json;
    }
    public JSONObject getFriendPictures(String friendID){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag",getFriendPictures));
        params.add(new BasicNameValuePair("friendID", friendID));

        JSONObject json = jsonParser.getJSONFromUrl(URL, params);

        return json;
    }
    public JSONObject getBuildingPhotos(){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", getBuildingPhotos));
        JSONObject json1 = jsonParser.getJSONFromUrl(URL, params);
        return json1;
    }

    public JSONObject updateProfilePicture(String name, String encodedImage ,String userID){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag",updateProfilePicture));
        params.add(new BasicNameValuePair("userID", userID));
        params.add(new BasicNameValuePair("encodedImage", encodedImage));
        params.add(new BasicNameValuePair("imagename", name));

        JSONObject json = jsonParser.getJSONFromUrl(URL, params);

        return json;
    }
    public JSONObject getProfilePic(String username){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag",getProfilePic));
        params.add(new BasicNameValuePair("UID", username));

        JSONObject json = jsonParser.getJSONFromUrl(URL, params);
        Log.v("getprofilepicture 3", json.toString());
        return json;

    }

    public JSONObject getAllBuildings(){
        JSONParser jsonParserr = new JSONParser();
        // Building Parameters

        Log.v("buildings tag", getAllBuildings);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", getAllBuildings));

        JSONObject json = jsonParserr.getJSONFromUrl(URL, params);

        Log.v("buildings json", jsonParserr.getJSONFromUrl(URL, params).toString());

        return json;
    }
    public JSONObject getBuildingPic(String title){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag",getBuildingPic));
        params.add(new BasicNameValuePair("title", title));

        JSONObject json = jsonParser.getJSONFromUrl(URL, params);

        return json;
    }

    public JSONObject getBuildingInfo(String url){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag",getBuildingInfo));
        params.add(new BasicNameValuePair("url", url));

        JSONObject json = jsonParser.getJSONFromUrl(URL, params);

        return json;
    }

    public JSONObject updateRating(float rate, String infoID, String userID){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag",updateRating));
        params.add(new BasicNameValuePair("rate", String.valueOf(rate)));
        params.add(new BasicNameValuePair("infoID", infoID));
        params.add(new BasicNameValuePair("userID", userID));

        JSONObject json = jsonParser.getJSONFromUrl(URL, params);

        return json;
    }

    public JSONObject getRating(String userID, String infoID){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag",getRating));
        params.add(new BasicNameValuePair("userID", userID));
        params.add(new BasicNameValuePair("infoID", infoID));

        JSONObject json = jsonParser.getJSONFromUrl(URL, params);

        return json;
    }

    public JSONObject addComment(String userID, String text, String infoID){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag",addComment));
        params.add(new BasicNameValuePair("userID", userID));
        params.add(new BasicNameValuePair("text", text));
        params.add(new BasicNameValuePair("infoID", infoID));

        JSONObject json = jsonParser.getJSONFromUrl(URL, params);

        return json;
    }
    public JSONObject getComment(String infoID){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag",getComment));
        params.add(new BasicNameValuePair("infoID", infoID));

        JSONObject json = jsonParser.getJSONFromUrl(URL, params);

        return json;
    }
    /**
     * Function to logout user
     * Resets the temporary data stored in SQLite Database
     * */
    public boolean logoutUser(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        db.resetTables();
        return true;
    }

}

