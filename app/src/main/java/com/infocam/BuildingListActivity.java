package com.infocam;

import android.os.Bundle;

/**as
 * Created by ilknuryildirim on 14/03/16.
 */


public class BuildingListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buildinglist);



        //Set nav drawer selected to second item in list
        mNavigationView.getMenu().getItem(4).setChecked(true);

    }



    /** HIDE TOOLBAR **/
//    @Override
//    protected boolean useToolbar() {
//        return false;
//    }



    /** HIDE hamburger menu **/
//    @Override
//    protected boolean useDrawerToggle() {
//        return false;
//    }

}
