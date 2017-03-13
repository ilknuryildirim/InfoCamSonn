package com.infocam;
/*
 * Copyright (C) 2010- Peer internet solutions
 *
 * This file is part of infocam.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.format.DateFormat;
import android.util.FloatMath;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;


/**
 * This class is the main application which uses the other classes for different
 * functionalities.
 * It sets up the camera screen and the augmented screen which is in front of the
 * camera screen.
 * It also handles the main sensor events, touch events and location events.
 */
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.infocam.data.DataHandler;
import com.infocam.data.DataSourceList;
import com.infocam.lib.gui.PaintScreen;
import com.infocam.lib.marker.Marker;
import com.infocam.lib.render.Matrix;


public class MixView extends Activity implements SensorEventListener, View.OnTouchListener {
    public Camera.PictureCallback mPicture;
    private CameraSurface camScreen;
    private AugmentedView augScreen;
    public static Camera camera;
    private boolean isInited;
    private static PaintScreen dWindow;
    private static DataView dataView;
    private boolean fError;

    //----------
    private MixViewDataHolder mixViewData;

    // TAG for logging
    public static final String TAG = "InfoCam";

    // why use Memory to save a state? MixContext? activity lifecycle?
    //private static MixView CONTEXT;

    /* string to name & access the preference file in the internal storage */
    public static final String PREFS_NAME = "MyPrefsFileForMenuItems";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Log.v("yeni1", "requestTen sonra");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //MixView.CONTEXT = this;
        /*FloatingActionButton captureButton = (FloatingActionButton) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        camScreen.camera.takePicture(null, null, mPicture);
                    }
                }
        );*/
        try {

            handleIntent(getIntent());

            final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            getMixViewData().setmWakeLock(pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag"));

            killOnError();

            FloatingActionButton captureButton = (FloatingActionButton) findViewById(R.id.fab);
            captureButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // get an image from the camera
                            camera.takePicture(null, null, mPicture);
                        }
                    }
            );
            maintainCamera(camera);
            maintainAugmentR();
            maintainZoomBar();

            if (!isInited) {
                //getMixViewData().setMixContext(new MixContext(this));
                //getMixViewData().getMixContext().setDownloadManager(new DownloadManager(mixViewData.getMixContext()));
                setdWindow(new PaintScreen());
                setDataView(new DataView(getMixViewData().getMixContext()));

				/* set the radius in data view to the last selected by the user */
                setZoomLevel();
                isInited = true;
            }

			/*Get the preference file PREFS_NAME stored in the internal memory of the phone*/
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

			/*check if the application is launched for the first time*/
            if (settings.getBoolean("firstAccess", false) == false) {
                firstAccess(settings);

            }

        } catch (Exception ex) {
            Log.v("ex1", "message");
            doError(ex);
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /*public void takeScreenshot() {
        Date now = new Date();
        DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);

            // this is the important code :)
            // Without it the view will have a dimension of 0,0 and the bitmap will
            // be null
            ViewGroup v1 = (ViewGroup) this.findViewById(id).getRootView();
            v1.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            v1.layout(0, 0, v1.getMeasuredWidth(), v1.getMeasuredHeight());

            v1.buildDrawingCache(true);
            v1.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            if(v1 == null)
                Log.v("aaaaaaaaaa", "v1 nulll : ");
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            if (bitmap != null) {
                int rgbColor = bitmap.getPixel(200, 100);
                Log.v("pixell", "color : " + rgbColor);
            }
            v1.setDrawingCacheEnabled(false);

        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }*/

    public MixViewDataHolder getMixViewData() {
        if (mixViewData == null) {
            // TODO: VERY important, only one!
            mixViewData = new MixViewDataHolder(new MixContext(this));
        }
        return mixViewData;
    }


    @Override
    protected void onPause() {
        super.onPause();
        camScreen.releaseCam();
        try {
            this.getMixViewData().getmWakeLock().release();

            try {
                getMixViewData().getSensorMgr().unregisterListener(this,
                        getMixViewData().getSensorGrav());
                getMixViewData().getSensorMgr().unregisterListener(this,
                        getMixViewData().getSensorMag());
                getMixViewData().setSensorMgr(null);

                getMixViewData().getMixContext().getLocationFinder().switchOff();
                getMixViewData().getMixContext().getDownloadManager().switchOff();

                if (getDataView() != null) {
                    getDataView().cancelRefreshTimer();
                }
            } catch (Exception ignore) {
            }

            if (fError) {
                finish();
            }
        } catch (Exception ex) {
            Log.v("ex2", "message");
            doError(ex);
        }
    }

    /**
     * {@inheritDoc}
     * InfoCam - Receives results from other launched activities
     * Base on the result returned, it either refreshes screen or not.
     * Default value for refreshing is false
     */
    protected void onActivityResult(final int requestCode,
                                    final int resultCode, Intent data) {
        Log.d(TAG + " WorkFlow", "MixView - onActivityResult Called");
        // check if the returned is request to refresh screen (setting might be
        // changed)
        try {
            if (data.getBooleanExtra("RefreshScreen", false)) {
                Log.d(TAG + " WorkFlow",
                        "MixView - Received Refresh Screen Request .. about to refresh");
                repaint();
                refreshDownload();
            }

        } catch (Exception ex) {
            // do nothing do to mix of return results.
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();

        try {
            this.getMixViewData().getmWakeLock().acquire();

            killOnError();
            getMixViewData().getMixContext().doResume(this);

            repaint();
            getDataView().doStart();
            getDataView().clearEvents();

            getMixViewData().getMixContext().getDataSourceManager().refreshDataSources();

            float angleX, angleY;

            int marker_orientation = -90;

            int rotation = Compatibility.getRotation(this);

            // display text from left to right and keep it horizontal
            angleX = (float) Math.toRadians(marker_orientation);
            getMixViewData().getM1().set(1f, 0f, 0f, 0f,
                    (float) Math.cos((int) Math.ceil(angleX)),
                    (float) -Math.sin((int) Math.ceil(angleX)), 0f,
                    (float) Math.sin((int) Math.ceil(angleX)),
                    (float) Math.cos((int) Math.ceil(angleX)));
            angleX = (float) Math.toRadians(marker_orientation);
            angleY = (float) Math.toRadians(marker_orientation);
            if (rotation == 1) {
                getMixViewData().getM2().set(1f, 0f, 0f, 0f,
                        (float) Math.cos((int) Math.ceil(angleX)),
                        (float) -Math.sin((int) Math.ceil(angleX)), 0f,
                        (float) Math.sin((int) Math.ceil(angleX)),
                        (float) Math.cos((int) Math.ceil(angleX)));
                getMixViewData().getM3().set((float) Math.cos((int) Math.ceil(angleY)), 0f,
                        (float) Math.sin((int) Math.ceil(angleY)), 0f, 1f, 0f,
                        (float) -Math.sin((int) Math.ceil(angleY)), 0f,
                        (float) Math.cos((int) Math.ceil(angleY)));
            } else {
                getMixViewData().getM2().set((float) Math.cos((int) Math.ceil(angleX)), 0f,
                        (float) Math.sin((int) Math.ceil(angleX)), 0f, 1f, 0f,
                        (float) -Math.sin((int) Math.ceil(angleX)), 0f,
                        (float) Math.cos((int) Math.ceil(angleX)));
                getMixViewData().getM3().set(1f, 0f, 0f, 0f,
                        (float) Math.cos((int) Math.ceil(angleY)),
                        (float) -Math.sin((int) Math.ceil(angleY)), 0f,
                        (float) Math.sin((int) Math.ceil(angleY)),
                        (float) Math.cos((int) Math.ceil(angleY)));

            }

            getMixViewData().getM4().toIdentity();

            for (int i = 0; i < getMixViewData().getHistR().length; i++) {
                getMixViewData().getHistR()[i] = new Matrix();
            }

            getMixViewData()
                    .setSensorMgr((SensorManager) getSystemService(SENSOR_SERVICE));

            getMixViewData().setSensors(getMixViewData().getSensorMgr().getSensorList(
                    Sensor.TYPE_ACCELEROMETER));
            if (getMixViewData().getSensors().size() > 0) {
                getMixViewData().setSensorGrav(getMixViewData().getSensors().get(0));
            }

            getMixViewData().setSensors(getMixViewData().getSensorMgr().getSensorList(
                    Sensor.TYPE_MAGNETIC_FIELD));
            if (getMixViewData().getSensors().size() > 0) {
                getMixViewData().setSensorMag(getMixViewData().getSensors().get(0));
            }

            getMixViewData().getSensorMgr().registerListener(this,
                    getMixViewData().getSensorGrav(), SENSOR_DELAY_GAME);
            getMixViewData().getSensorMgr().registerListener(this,
                    getMixViewData().getSensorMag(), SENSOR_DELAY_GAME);
            GeomagneticField gmf = null;
            try {
                gmf = getMixViewData().getMixContext().getLocationFinder().getGeomagneticField();
                angleY = (float) Math.toRadians(-gmf.getDeclination());
                getMixViewData().getM4().set((float) Math.cos((int) Math.ceil(angleY)), 0f,
                        (float) Math.sin((int) Math.ceil(angleY)), 0f, 1f, 0f,
                        (float) -Math.sin((int) Math.ceil(angleY)), 0f,
                        (float) Math.cos((int) Math.ceil(angleY)));

            } catch (Exception ex) {
                Log.d("infocam", "GPS Initialize Error", ex);
            }
            getMixViewData().getMixContext().getDownloadManager().switchOn();
            Log.v("taaaaaaaaag", getMixViewData().getMixContext().getDownloadManager().toString());
            getMixViewData().getMixContext().getLocationFinder().switchOn();
        } catch (Exception ex) {
            Log.v("ex5", "message");
            doError(ex);
            try {
                if (getMixViewData().getSensorMgr() != null) {
                    getMixViewData().getSensorMgr().unregisterListener(this,
                            getMixViewData().getSensorGrav());
                    getMixViewData().getSensorMgr().unregisterListener(this,
                            getMixViewData().getSensorMag());
                    getMixViewData().setSensorMgr(null);
                }

                if (getMixViewData().getMixContext() != null) {
                    getMixViewData().getMixContext().getLocationFinder().switchOff();
                    getMixViewData().getMixContext().getDownloadManager().switchOff();
                }
            } catch (Exception ignore) {
            }
        }

        Log.d("---", "resume");
        if (getDataView().isFrozen() && getMixViewData().getSearchNotificationTxt() == null) {
            getMixViewData().setSearchNotificationTxt(new TextView(this));
            getMixViewData().getSearchNotificationTxt().setWidth(
                    getdWindow().getWidth());
            getMixViewData().getSearchNotificationTxt().setPadding(10, 2, 0, 0);
            getMixViewData().getSearchNotificationTxt().setText(
                    "" + " "
                            + DataSourceList.getDataSourcesStringList());
            ;
            getMixViewData().getSearchNotificationTxt().setBackgroundColor(
                    Color.DKGRAY);
            getMixViewData().getSearchNotificationTxt().setTextColor(Color.WHITE);

            getMixViewData().getSearchNotificationTxt().setOnTouchListener(this);
            addContentView(getMixViewData().getSearchNotificationTxt(),
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
        } else if (!getDataView().isFrozen()
                && getMixViewData().getSearchNotificationTxt() != null) {
            getMixViewData().getSearchNotificationTxt().setVisibility(View.GONE);
            getMixViewData().setSearchNotificationTxt(null);
        }
    }

    /**
     * {@inheritDoc}
     * Customize Activity after switching back to it.
     * Currently it maintain and ensures view creation.
     */
    protected void onRestart() {
        super.onRestart();
        maintainCamera(camera);

        maintainAugmentR();
        maintainZoomBar();

    }

	/* ********* Operators ***********/

    public void repaint() {
        //clear stored data
        getDataView().clearEvents();
        setDataView(null); //It's smelly code, but enforce garbage collector
        //to release data.
        setDataView(new DataView(mixViewData.getMixContext()));
        setdWindow(new PaintScreen());
        //setZoomLevel(); //@TODO Caller has to set the zoom. This function repaints only.
    }

    /**
     * Checks camScreen, if it does not exist, it creates one.
     */
    private void maintainCamera(Camera camera) {
        if (camScreen == null) {
            camScreen = new CameraSurface(this, camera);
        }
        setContentView(camScreen);
    }

    /**
     * Checks augScreen, if it does not exist, it creates one.
     */
    private void maintainAugmentR() {
        if (augScreen == null) {
            augScreen = new AugmentedView(this);
        }
        addContentView(augScreen, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /**
     * Creates a zoom bar and adds it to view.
     */
    private void maintainZoomBar() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        FrameLayout frameLayout = createZoomBar(settings);
        addContentView(frameLayout, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM));
    }

    /**
     * Refreshes Download
     * TODO refresh downloads
     */
    private void refreshDownload() {
//		try {
//			if (getMixViewData().getDownloadThread() != null){
//				if (!getMixViewData().getDownloadThread().isInterrupted()){
//					getMixViewData().getDownloadThread().interrupt();
//					getMixViewData().getMixContext().getDownloadManager().restart();
//				}
//			}else { //if no download thread found
//				getMixViewData().setDownloadThread(new Thread(getMixViewData()
//						.getMixContext().getDownloadManager()));
//				//@TODO Syncronize DownloadManager, call Start instead of run.
//				mixViewData.getMixContext().getDownloadManager().run();
//			}
//		}catch (Exception ex){
//		}
    }

    public void refresh() {
        dataView.refresh();
    }

    public void setErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Eroor");
        builder.setCancelable(true);

		/*Retry*/
        builder.setPositiveButton("", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                fError = false;
                //TODO improve
                try {
                    maintainCamera(camera);
                    maintainAugmentR();
                    repaint();
                    setZoomLevel();
                } catch (Exception ex) {
                    //Don't call doError, it will be a recursive call.
                    //doError(ex);
                }
            }
        });
        /*Open settings*/
        builder.setNeutralButton("", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent1 = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivityForResult(intent1, 42);
            }
        });
		/*Close application*/
        builder.setNegativeButton("", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.exit(0); //wouldn't be better to use finish (to stop the app normally?)
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public float calcZoomLevel() {

        int myZoomLevel = getMixViewData().getMyZoomBar().getProgress();
        float myout = 5;

        if (myZoomLevel <= 26) {
            myout = myZoomLevel / 25f;
        } else if (25 < myZoomLevel && myZoomLevel < 50) {
            myout = (1 + (myZoomLevel - 25)) * 0.38f;
        } else if (25 == myZoomLevel) {
            myout = 1;
        } else if (50 == myZoomLevel) {
            myout = 10;
        } else if (50 < myZoomLevel && myZoomLevel < 75) {
            myout = (10 + (myZoomLevel - 50)) * 0.83f;
        } else {
            myout = (30 + (myZoomLevel - 75) * 2f);
        }


        return myout;
    }

    /**
     * Handle First time users. It display license agreement and store user's
     * acceptance.
     *
     * @param settings
     */
    private void firstAccess(SharedPreferences settings) {
        /*SharedPreferences.Editor editor = settings.edit();
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(getString(""));
        builder1.setNegativeButton("close_button"),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert1 = builder1.create();
        alert1.setTitle(getString(R.string.license_title));
        alert1.show();
        editor.putBoolean("firstAccess", true);

        // value for maximum POI for each selected OSM URL to be active by
        // default is 5
        editor.putInt("osmMaxObject", 5);
        editor.commit();

        // add the default datasources to the preferences file
        DataSourceStorage.getInstance().fillDefaultDataSources();*/
    }

    /**
     * Create zoom bar and returns FrameLayout. FrameLayout is created to be
     * hidden and not added to view, Caller needs to add the frameLayout to
     * view, and enable visibility when needed.
     * <p/>
     * param SharedOreference settings where setting is stored
     * return FrameLayout Hidden Zoom Bar
     */
    private FrameLayout createZoomBar(SharedPreferences settings) {
        getMixViewData().setMyZoomBar(new SeekBar(this));
        getMixViewData().getMyZoomBar().setMax(100);
        getMixViewData().getMyZoomBar()
                .setProgress(settings.getInt("zoomLevel", 65));
        getMixViewData().getMyZoomBar().setOnSeekBarChangeListener(myZoomBarOnSeekBarChangeListener);
        getMixViewData().getMyZoomBar().setVisibility(View.INVISIBLE);

        FrameLayout frameLayout = new FrameLayout(this);

        frameLayout.setMinimumWidth(3000);
        frameLayout.addView(getMixViewData().getMyZoomBar());
        frameLayout.setPadding(10, 0, 10, 10);
        return frameLayout;
    }

	/* ********* Operator - Menu ******/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int base = Menu.FIRST;

		/* define the first */
        /*MenuItem item1 =                menu.add(base, base, base,
                getString(R.string.menu_item_1));*/

        getMenuInflater().inflate(R.menu.activity_base_drawer, menu);
        /*MenuItem item2 = menu.add(base, base + 1, base + 1,
                getString(R.string.menu_item_2));
        MenuItem item3 = menu.add(base, base + 2, base + 2,
                getString(R.string.menu_item_3));
        MenuItem item4 = menu.add(base, base + 3, base + 3,
                getString(R.string.menu_item_4));
        MenuItem item5 = menu.add(base, base + 4, base + 4,
                getString(R.string.menu_item_5));
        MenuItem item6 = menu.add(base, base + 5, base + 5,
                getString(R.string.menu_item_6));
        MenuItem item7 = menu.add(base, base + 6, base + 6,
                getString(R.string.menu_item_7));*/

        // assign icons to the menu items
        /*item1.setIcon(drawable.icon_datasource);
        item2.setIcon(android.R.drawable.ic_menu_view);
        item3.setIcon(android.R.drawable.ic_menu_mapmode);
        item4.setIcon(android.R.drawable.ic_menu_zoom);
        item5.setIcon(android.R.drawable.ic_menu_search);
        item6.setIcon(android.R.drawable.ic_menu_info_details);
        item7.setIcon(android.R.drawable.ic_menu_share);*/

        return true;
    }

    public void createBackStack(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntentWithParentStack(intent);
            builder.startActivities();
        } else {
            startActivity(intent);
            finish();
        }
    }

    public void logout() {
        UserFunctions logout = new UserFunctions();
        logout.logoutUser(getApplicationContext());
        Intent login = new Intent(getApplicationContext(), Login.class);
        login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(login);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_photo:
                onPause();
                createBackStack(new Intent(this, CameraActivity.class));

                break;
            case R.id.nav_main:
                createBackStack(new Intent(this, MixView.class));
                break;

            case R.id.nav_friends:
                createBackStack(new Intent(this, FriendsActivity.class));
                break;

            case R.id.nav_profile:
                createBackStack(new Intent(this, ProfileActivity.class));
                break;

            case R.id.nav_gallery:
                createBackStack(new Intent(this, GalleryActivity.class));
                break;

            case R.id.nav_buildinglist:
                createBackStack(new Intent(this, BuildingListActivity.class));
                break;

            case R.id.nav_settings:
                createBackStack(new Intent(this, SettingActivity.class));
                break;

            case R.id.nav_logout:
                logout();
                break;
        }

        //closeNavDrawer();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_out_left);

        return true;
    }

	/* ******** Operators - Sensors ****** */

    private SeekBar.OnSeekBarChangeListener myZoomBarOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        Toast t;

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            float myout = calcZoomLevel();

            getMixViewData().setZoomLevel(String.valueOf(myout));
            getMixViewData().setZoomProgress(getMixViewData().getMyZoomBar()
                    .getProgress());

            t.setText("Radius: " + String.valueOf(myout));
            t.show();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            Context ctx = seekBar.getContext();
            t = Toast.makeText(ctx, "Radius: ", Toast.LENGTH_LONG);
            // zoomChanging= true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
			/* store the zoom range of the zoom bar selected by the user */
            editor.putInt("zoomLevel", getMixViewData().getMyZoomBar().getProgress());
            editor.commit();
            getMixViewData().getMyZoomBar().setVisibility(View.INVISIBLE);
            // zoomChanging= false;

            getMixViewData().getMyZoomBar().getProgress();

            t.cancel();
            //repaint after zoom level changed.
            repaint();
            setZoomLevel();
        }

    };


    public void onSensorChanged(SensorEvent evt) {
        try {

            if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                getMixViewData().getGrav()[0] = evt.values[0];
                getMixViewData().getGrav()[1] = evt.values[1];
                getMixViewData().getGrav()[2] = evt.values[2];

                augScreen.postInvalidate();
            } else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                getMixViewData().getMag()[0] = evt.values[0];
                getMixViewData().getMag()[1] = evt.values[1];
                getMixViewData().getMag()[2] = evt.values[2];

                augScreen.postInvalidate();
            }

            SensorManager.getRotationMatrix(getMixViewData().getRTmp(),
                    getMixViewData().getI(), getMixViewData().getGrav(),
                    getMixViewData().getMag());

            int rotation = Compatibility.getRotation(this);

            if (rotation == 1) {
                SensorManager.remapCoordinateSystem(getMixViewData().getRTmp(),
                        SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z,
                        getMixViewData().getRot());
            } else {
                SensorManager.remapCoordinateSystem(getMixViewData().getRTmp(),
                        SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z,
                        getMixViewData().getRot());
            }
            getMixViewData().getTempR().set(getMixViewData().getRot()[0],
                    getMixViewData().getRot()[1], getMixViewData().getRot()[2],
                    getMixViewData().getRot()[3], getMixViewData().getRot()[4],
                    getMixViewData().getRot()[5], getMixViewData().getRot()[6],
                    getMixViewData().getRot()[7], getMixViewData().getRot()[8]);

            getMixViewData().getFinalR().toIdentity();
            getMixViewData().getFinalR().prod(getMixViewData().getM4());
            getMixViewData().getFinalR().prod(getMixViewData().getM1());
            getMixViewData().getFinalR().prod(getMixViewData().getTempR());
            getMixViewData().getFinalR().prod(getMixViewData().getM3());
            getMixViewData().getFinalR().prod(getMixViewData().getM2());
            getMixViewData().getFinalR().invert();

            getMixViewData().getHistR()[getMixViewData().getrHistIdx()].set(getMixViewData()
                    .getFinalR());
            getMixViewData().setrHistIdx(getMixViewData().getrHistIdx() + 1);
            if (getMixViewData().getrHistIdx() >= getMixViewData().getHistR().length)
                getMixViewData().setrHistIdx(0);

            getMixViewData().getSmoothR().set(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
            for (int i = 0; i < getMixViewData().getHistR().length; i++) {
                getMixViewData().getSmoothR().add(getMixViewData().getHistR()[i]);
            }
            getMixViewData().getSmoothR().mult(
                    1 / (float) getMixViewData().getHistR().length);

            getMixViewData().getMixContext().updateSmoothRotation(getMixViewData().getSmoothR());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        try {
            killOnError();

            float xPress = me.getX();
            float yPress = me.getY();
            if (me.getAction() == MotionEvent.ACTION_UP) {
                getDataView().clickEvent(xPress, yPress);
            }//TODO add gesture events (low)

            return true;
        } catch (Exception ex) {
            // doError(ex);
            ex.printStackTrace();
            return super.onTouchEvent(me);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            killOnError();

            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (getDataView().isDetailsView()) {
                    getDataView().keyEvent(keyCode);
                    getDataView().setDetailsView(false);
                    return true;
                } else {
                    //TODO handle keyback to finish app correctly
                    return super.onKeyDown(keyCode, event);
                }
            } else if (keyCode == KeyEvent.KEYCODE_MENU) {
                DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
            } else {
                getDataView().keyEvent(keyCode);
                return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return super.onKeyDown(keyCode, event);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
                && accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
                && getMixViewData().getCompassErrorDisplayed() == 0) {
            for (int i = 0; i < 2; i++) {
                Toast.makeText(getMixViewData().getMixContext(),
                        "Compass data unreliable. Please recalibrate compass.",
                        Toast.LENGTH_LONG).show();
            }
            getMixViewData().setCompassErrorDisplayed(getMixViewData()
                    .getCompassErrorDisplayed() + 1);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        getDataView().setFrozen(false);
        if (getMixViewData().getSearchNotificationTxt() != null) {
            getMixViewData().getSearchNotificationTxt().setVisibility(View.GONE);
            getMixViewData().setSearchNotificationTxt(null);
        }
        return false;
    }


	/* ************ Handlers *************/

    public void doError(Exception ex1) {
        if (!fError) {
            fError = true;

            setErrorDialog();

            ex1.printStackTrace();

        }

        try {
            augScreen.invalidate();
        } catch (Exception ignore) {
        }
    }

    public void killOnError() throws Exception {
        /*if (fError)
            throw new Exception();*/
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMixSearch(query);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void doMixSearch(String query) {
        DataHandler jLayer = getDataView().getDataHandler();
        if (!getDataView().isFrozen()) {
            MixListView.originalMarkerList = jLayer.getMarkerList();
            //MixMap.originalMarkerList = jLayer.getMarkerList();
        }

        ArrayList<Marker> searchResults = new ArrayList<Marker>();
        //Log.d("SEARCH-------------------0", "" + query);
        if (jLayer.getMarkerCount() > 0) {
            for (int i = 0; i < jLayer.getMarkerCount(); i++) {
                Marker ma = jLayer.getMarker(i);
                if (ma.getTitle().toLowerCase().indexOf(query.toLowerCase()) != -1) {
                    searchResults.add(ma);
					/* the website for the corresponding title */
                }
            }
        }
        if (searchResults.size() > 0) {
            getDataView().setFrozen(true);
            jLayer.setMarkerList(searchResults);
        }
           /* Toast.makeText(this,
                    getString(R.string.search_failed_notification),
                    Toast.LENGTH_LONG).show();*/
    }

	/* ******* Getter and Setters ********** */

    public boolean isZoombarVisible() {
        return getMixViewData().getMyZoomBar() != null
                && getMixViewData().getMyZoomBar().getVisibility() == View.VISIBLE;
    }

    public String getZoomLevel() {
        return getMixViewData().getZoomLevel();
    }

    /**
     * @return the dWindow
     */
    static PaintScreen getdWindow() {
        return dWindow;
    }


    /**
     * @param dWindow the dWindow to set
     */
    static void setdWindow(PaintScreen dWindow) {
        MixView.dWindow = dWindow;
    }


    /**
     * @return the dataView
     */
    static DataView getDataView() {
        return dataView;
    }

    /**
     * @param dataView the dataView to set
     */
    static void setDataView(DataView dataView) {
        MixView.dataView = dataView;
    }


    public int getZoomProgress() {
        return getMixViewData().getZoomProgress();
    }

    private void setZoomLevel() {
        float myout = calcZoomLevel();

        getDataView().setRadius(myout);
        //caller has the to control of zoombar visibility, not setzoom
        //mixViewData.getMyZoomBar().setVisibility(View.INVISIBLE);
        mixViewData.setZoomLevel(String.valueOf(myout));
        //setZoomLevel, caller has to call refreash download if needed.
//		mixViewData.setDownloadThread(new Thread(mixViewData.getMixContext().getDownloadManager()));
//		mixViewData.getDownloadThread().start();


        getMixViewData().getMixContext().getDownloadManager().switchOn();

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "MixView Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.infocam/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "MixView Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.infocam/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}


/**
 * @author daniele
 *
 */
class CameraSurface extends SurfaceView implements SurfaceHolder.Callback {
    MixView app;
    SurfaceHolder holder;
    Camera camera;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    public void setCamera(Camera camera){
        this.camera = camera;
    }
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.v("tag", "onpictaken called");
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d("TAG", "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("TAG", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("TAG", "Error accessing file: " + e.getMessage());
            }
        }
    };
    public void releaseCam(){
        if(camera != null)
            camera.release();
    }
    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            Log.d("phototake", "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("phototake", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("phototake", "Error accessing file: " + e.getMessage());
        }
    }
    public static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Infocam");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("Infocam", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            Log.v("tag", "image saved!!!!!!!!!!!!!!!!!!!!");
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    CameraSurface(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        try {
            app = (MixView) context;

            holder = getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        } catch (Exception ex) {

        }
    }

    public void surfaceCreated(SurfaceHolder holder) {

        try {

            if (camera != null) {
                /*camera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data,Camera cam)
                    {
                        Camera.Size previewSize = cam.getParameters().getPreviewSize();
                        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21,previewSize.width,previewSize.height, null);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        yuvImage.compressToJpeg(new Rect(0,0,previewSize.width,previewSize.height),80,baos);
                        byte[] jdata = baos.toByteArray();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(jdata,0,jdata.length);
                        int rgbColor = bitmap.getPixel(200, 100);
                        Log.v("pixell", "color : " + rgbColor);
                    }
                });*/
                try {

                    camera.stopPreview();
                } catch (Exception ignore) {
                }
                try {

                    camera.release();
                } catch (Exception ignore) {
                }
                camera = null;
            }

            camera = Camera.open();
            camera.setPreviewDisplay(holder);
        } catch (Exception ex) {
            try {
                if (camera != null) {
                    try {
                        camera.stopPreview();
                    } catch (Exception ignore) {
                    }
                    try {
                        camera.release();
                    } catch (Exception ignore) {
                    }
                    camera = null;
                }
            } catch (Exception ignore) {

            }
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            if (camera != null) {

                try {
                    camera.stopPreview();
                } catch (Exception ignore) {
                }
                try {
                    camera.release();
                } catch (Exception ignore) {
                }
                camera = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        try {
            Camera.Parameters parameters = camera.getParameters();

            try {

                List<Camera.Size> supportedSizes = null;
                // On older devices (<1.6) the following will fail
                // the camera will work nevertheless
                supportedSizes = Compatibility.getSupportedPreviewSizes(parameters);

                // preview form factor
                float ff = (float) w / h;
                Log.d("InfoCam", "Screen res: w:" + w + " h:" + h
                        + " aspect ratio:" + ff);

                // holder for the best form factor and size
                float bff = 0;
                int bestw = 0;
                int besth = 0;
                Iterator<Camera.Size> itr = supportedSizes.iterator();

                // we look for the best preview size, it has to be the closest
                // to the
                // screen form factor, and be less wide than the screen itself
                // the latter requirement is because the HTC Hero with update
                // 2.1 will
                // report camera preview sizes larger than the screen, and it
                // will fail
                // to initialize the camera
                // other devices could work with previews larger than the screen
                // though
                while (itr.hasNext()) {
                    Camera.Size element = itr.next();
                    // current form factor
                    float cff = (float) element.width / element.height;
                    // check if the current element is a candidate to replace
                    // the best match so far
                    // current form factor should be closer to the bff
                    // preview width should be less than screen width
                    // preview width should be more than current bestw
                    // this combination will ensure that the highest resolution
                    // will win
                    Log.d("InfoCam", "Candidate camera element: w:"
                            + element.width + " h:" + element.height
                            + " aspect ratio:" + cff);
                    if ((ff - cff <= ff - bff) && (element.width <= w)
                            && (element.width >= bestw)) {
                        bff = cff;
                        bestw = element.width;
                        besth = element.height;
                    }
                }
                Log.d("InfoCam", "Chosen camera element: w:" + bestw + " h:"
                        + besth + " aspect ratio:" + bff);
                // Some Samsung phones will end up with bestw and besth = 0
                // because their minimum preview size is bigger then the screen
                // size.
                // In this case, we use the default values: 480x320
                if ((bestw == 0) || (besth == 0)) {
                    Log.d("InfoCam", "Using default camera parameters!");
                    bestw = 480;
                    besth = 320;
                }
                parameters.setPreviewSize(bestw, besth);
            } catch (Exception ex) {
                parameters.setPreviewSize(480, 320);
            }

            camera.setParameters(parameters);
            camera.startPreview();
            /*camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera cam) {
                    Camera.Size previewSize = cam.getParameters().getPreviewSize();
                    YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    yuvImage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
                    byte[] jdata = baos.toByteArray();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
                    int rgbColor = bitmap.getPixel(200, 300);
                    Log.v("pixell", "color : " + rgbColor);
                }
            });*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

class AugmentedView extends View {
    MixView app;
    int xSearch = 200;
    int ySearch = 10;
    int searchObjWidth = 0;
    int searchObjHeight = 0;

    Paint zoomPaint = new Paint();

    public AugmentedView(Context context) {
        super(context);

        try {
            app = (MixView) context;

            app.killOnError();
        } catch (Exception ex) {
            app.doError(ex);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            // if (app.fError) {
            //
            // Paint errPaint = new Paint();
            // errPaint.setColor(Color.RED);
            // errPaint.setTextSize(16);
            //
            // /*Draws the Error code*/
            // canvas.drawText("ERROR: ", 10, 20, errPaint);
            // canvas.drawText("" + app.fErrorTxt, 10, 40, errPaint);
            //
            // return;
            // }

            //app.killOnError();

            MixView.getdWindow().setWidth(canvas.getWidth());
            MixView.getdWindow().setHeight(canvas.getHeight());

            MixView.getdWindow().setCanvas(canvas);

            if (!MixView.getDataView().isInited()) {
                MixView.getDataView().init(MixView.getdWindow().getWidth(), MixView.getdWindow().getHeight());
            }
            if (app.isZoombarVisible()) {
                Log.v("ex3", "zoom level");
                zoomPaint.setColor(Color.WHITE);
                zoomPaint.setTextSize(14);
                String startKM, endKM;
                endKM = "80km";
                startKM = "0km";
				/*
				 * if(MixListView.getDataSource().equals("Twitter")){ startKM =
				 * "1km"; }
				 */
                canvas.drawText(startKM, canvas.getWidth() / 100 * 4,
                        canvas.getHeight() / 100 * 85, zoomPaint);
                canvas.drawText(endKM, canvas.getWidth() / 100 * 99 + 25,
                        canvas.getHeight() / 100 * 85, zoomPaint);

                int height = canvas.getHeight() / 100 * 85;
                int zoomProgress = app.getZoomProgress();
                if (zoomProgress > 92 || zoomProgress < 6) {
                    height = canvas.getHeight() / 100 * 80;
                }
                canvas.drawText(app.getZoomLevel(), (canvas.getWidth()) / 100
                        * zoomProgress + 20, height, zoomPaint);
            }
            //Log.v("ex3", "messagedan once");
            MixView.getDataView().draw(MixView.getdWindow());
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.v("ex3", "message");
            app.doError(ex);
        }
    }
}

/**
 * Internal class that holds Mixview field Data.
 *
 * @author A B
 */
class MixViewDataHolder {
    private final MixContext mixContext;
    private float[] RTmp;
    private float[] Rot;
    private float[] I;
    private float[] grav;
    private float[] mag;
    private SensorManager sensorMgr;
    private List<Sensor> sensors;
    private Sensor sensorGrav;
    private Sensor sensorMag;
    private int rHistIdx;
    private Matrix tempR;
    private Matrix finalR;
    private Matrix smoothR;
    private Matrix[] histR;
    private Matrix m1;
    private Matrix m2;
    private Matrix m3;
    private Matrix m4;
    private SeekBar myZoomBar;
    private PowerManager.WakeLock mWakeLock;
    private int compassErrorDisplayed;
    private String zoomLevel;
    private int zoomProgress;
    private TextView searchNotificationTxt;

    public MixViewDataHolder(MixContext mixContext) {
        this.mixContext=mixContext;
        this.RTmp = new float[9];
        this.Rot = new float[9];
        this.I = new float[9];
        this.grav = new float[3];
        this.mag = new float[3];
        this.rHistIdx = 0;
        this.tempR = new Matrix();
        this.finalR = new Matrix();
        this.smoothR = new Matrix();
        this.histR = new Matrix[60];
        this.m1 = new Matrix();
        this.m2 = new Matrix();
        this.m3 = new Matrix();
        this.m4 = new Matrix();
        this.compassErrorDisplayed = 0;
    }

    /* ******* Getter and Setters ********** */
    public MixContext getMixContext() {
        return mixContext;
    }

    public float[] getRTmp() {
        return RTmp;
    }

    public void setRTmp(float[] rTmp) {
        RTmp = rTmp;
    }

    public float[] getRot() {
        return Rot;
    }

    public void setRot(float[] rot) {
        Rot = rot;
    }

    public float[] getI() {
        return I;
    }

    public void setI(float[] i) {
        I = i;
    }

    public float[] getGrav() {
        return grav;
    }

    public void setGrav(float[] grav) {
        this.grav = grav;
    }

    public float[] getMag() {
        return mag;
    }

    public void setMag(float[] mag) {
        this.mag = mag;
    }

    public SensorManager getSensorMgr() {
        return sensorMgr;
    }

    public void setSensorMgr(SensorManager sensorMgr) {
        this.sensorMgr = sensorMgr;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    public Sensor getSensorGrav() {
        return sensorGrav;
    }

    public void setSensorGrav(Sensor sensorGrav) {
        this.sensorGrav = sensorGrav;
    }

    public Sensor getSensorMag() {
        return sensorMag;
    }

    public void setSensorMag(Sensor sensorMag) {
        this.sensorMag = sensorMag;
    }

    public int getrHistIdx() {
        return rHistIdx;
    }

    public void setrHistIdx(int rHistIdx) {
        this.rHistIdx = rHistIdx;
    }

    public Matrix getTempR() {
        return tempR;
    }

    public void setTempR(Matrix tempR) {
        this.tempR = tempR;
    }

    public Matrix getFinalR() {
        return finalR;
    }

    public void setFinalR(Matrix finalR) {
        this.finalR = finalR;
    }

    public Matrix getSmoothR() {
        return smoothR;
    }

    public void setSmoothR(Matrix smoothR) {
        this.smoothR = smoothR;
    }

    public Matrix[] getHistR() {
        return histR;
    }

    public void setHistR(Matrix[] histR) {
        this.histR = histR;
    }

    public Matrix getM1() {
        return m1;
    }

    public void setM1(Matrix m1) {
        this.m1 = m1;
    }

    public Matrix getM2() {
        return m2;
    }

    public void setM2(Matrix m2) {
        this.m2 = m2;
    }

    public Matrix getM3() {
        return m3;
    }

    public void setM3(Matrix m3) {
        this.m3 = m3;
    }

    public Matrix getM4() {
        return m4;
    }

    public void setM4(Matrix m4) {
        this.m4 = m4;
    }

    public SeekBar getMyZoomBar() {
        return myZoomBar;
    }

    public void setMyZoomBar(SeekBar myZoomBar) {
        this.myZoomBar = myZoomBar;
    }

    public PowerManager.WakeLock getmWakeLock() {
        return mWakeLock;
    }

    public void setmWakeLock(PowerManager.WakeLock mWakeLock) {
        this.mWakeLock = mWakeLock;
    }

    public int getCompassErrorDisplayed() {
        return compassErrorDisplayed;
    }

    public void setCompassErrorDisplayed(int compassErrorDisplayed) {
        this.compassErrorDisplayed = compassErrorDisplayed;
    }

    public String getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(String zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public int getZoomProgress() {
        return zoomProgress;
    }

    public void setZoomProgress(int zoomProgress) {
        this.zoomProgress = zoomProgress;
    }

    public TextView getSearchNotificationTxt() {
        return searchNotificationTxt;
    }

    public void setSearchNotificationTxt(TextView searchNotificationTxt) {
        this.searchNotificationTxt = searchNotificationTxt;
    }
}
