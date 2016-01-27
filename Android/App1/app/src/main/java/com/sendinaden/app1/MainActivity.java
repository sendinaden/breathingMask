package com.sendinaden.app1;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sendinaden.app1.bluetooth.ArduinoInteraction;
import com.sendinaden.app1.bluetooth.BluetoothController;
import com.sendinaden.app1.dataManagement.BreathSession;
import com.sendinaden.app1.dataManagement.DataManager;
import com.sendinaden.app1.fragments.ChooseActivityFragment;
import com.sendinaden.app1.fragments.CurrentSessionFragment;
import com.sendinaden.app1.fragments.ExercisesFragment;
import com.sendinaden.app1.fragments.HistoryFragment;
import com.sendinaden.app1.models.CategoryInformation;
import com.sendinaden.app1.models.LiveChartModel;

public class MainActivity extends AppCompatActivity implements
        ChooseActivityFragment.OnFragmentInteractionListener,
        ExercisesFragment.OnFragmentInteractionListener,
        CurrentSessionFragment.OnFragmentInteractionListener,
        HistoryFragment.OnFragmentInteractionListener,
        BluetoothController.BluetoothInteraction {

    // Bluetooth variables
    BluetoothController btcontrol;
    private Context mainContext = this;
    private Handler mHandler = new Handler();
    private boolean onGoingSession = false;
    private boolean discussionOnGoing = false;
    private ArduinoInteraction arduino;
    private ProgressDialog connectingDialog;

    // UI Interface
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    // Processing
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TOOLBAR
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        openFragment(ChooseActivityFragment.newInstance(), false);

        // NAVIGATION DRAWER
        setUpNavigationDrawer();

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        // Recent app settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getTheme();
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
            int color = typedValue.data;

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.sendinaden_icon_white);
            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(null, bm, color);

            setTaskDescription(td);
            bm.recycle();

        }
        dataManager = new DataManager(savedInstanceState) {
            @Override
            protected void showFeedback(BreathSession currentBreathSession) {
                if (currentBreathSession.hasFeedback()) {
                    for (String feedback : currentBreathSession.getFeedbacks()) {
                        Toast.makeText(MainActivity.this, feedback, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            protected void updateSparklines(BreathSession currentBreathSession) {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame);
                if (currentFragment != null & currentFragment instanceof CurrentSessionFragment) {
                    ((CurrentSessionFragment) currentFragment).updateSparklines(currentBreathSession);
                }
            }

            @Override
            protected void notifyCalibrationDone() {
                connectingDialog.dismiss();
//                openFragment(CurrentSessionFragment.newInstance(categoryInfo), false);
                openFragment(CurrentSessionFragment.newInstance(dataManager.getCurrentSessionCategory()), false);
            }

            @Override
            protected void updateLiveGraph(LiveChartModel model) {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame);
                if (currentFragment != null & currentFragment instanceof CurrentSessionFragment) {
                    ((CurrentSessionFragment) currentFragment).updateLiveGraph(model);
                    ((CurrentSessionFragment) currentFragment).updateTime(getCurrentBreathSession().getStartTime());
                }
            }
        };

        // Initialize bluetooth classes
        btcontrol = new BluetoothController(mainContext, mHandler);
        arduino = new ArduinoInteraction(mainContext, mHandler, null) {
            @Override
            protected void onSerialReceived(String stringExtra) {
//                System.out.println(stringExtra);
                if (onGoingSession) {
                    dataManager.put(stringExtra);
//                    System.out.println(stringExtra);
                }
                if (discussionOnGoing) {
//                    System.out.println(stringExtra);
                    if (stringExtra.contains("A") || stringExtra.contains("B")) {
                        onGoingSession = true;
                        discussionOnGoing = false;
                        connectingDialog.setMessage("Calibrating...");
                    } else {
                        sendSerial("ConnectedX");
                    }
                }
            }

            @Override
            protected void onConnectionStateChange(connectionStateEnum mConnectionState) {
                switch (mConnectionState) {
                    case isConnecting:
                        connectingDialog.setMessage("Connecting...");
                        break;
                    case isConnected:
                        discussionOnGoing = true;
                        sendSerial("Hi");   // interrupts the sleeping mode if there is
                        sendSerial("ConnectedX");
                        connectingDialog.setMessage("Discussing...");
                        sendSerial("ConnectedX");
//                        onGoingSession = true;
//                        connectingDialog.setMessage("Calibrating...");
//                        connectingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//                        connectingDialog.setProgress(1);
//                        connectingDialog.setMax(100);
                        break;
                    case isToScan:
                        // also check if the session is over otherwise the dialog will be shown before
                        // moving to the next screen
                        if (onGoingSession & getSupportFragmentManager().findFragmentById(R.id.frame) instanceof CurrentSessionFragment) {
                            createConnectionLostDialog();
                        }
                        System.out.println("isToScan");
                        break;
                    case isScanning:
                        System.out.println("isScanning");
                        break;
                    case isNull:
                        System.out.println("isNull");
                        break;
                }
            }
        };

    }

    private void createConnectionLostDialog() {
        endSession();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Connection lost :(. \nSave session?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(mainContext, Session.class);
                TextView duration = (TextView) findViewById(R.id.duration);
                String message = duration.getText().toString();
                intent.putExtra("com.sendinaden.app1.DURATION", message);
                intent.putExtra("category info", dataManager.getCurrentSessionCategory());
                startActivity(intent);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openSessionFragment();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void setUpNavigationDrawer() {
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if (!item.isChecked()) {
                    item.setChecked(true);
                    //Closing drawer on item click
                    drawerLayout.closeDrawers();

                    //Check to see which item was being clicked and perform appropriate action
                    switch (item.getItemId()) {
                        case R.id.new_session:
                            Toast.makeText(getApplicationContext(), "New Session Selected", Toast.LENGTH_SHORT).show();
                            openSessionFragment();
                            return true;
                        case R.id.exercises:
                            Toast.makeText(getApplicationContext(), "Exercises selected", Toast.LENGTH_SHORT).show();
                            openFragment(ExercisesFragment.newInstance(), false);
                            return true;
                        case R.id.session_history:
                            openFragment(HistoryFragment.newInstance(), false);
                            Toast.makeText(getApplicationContext(), "Session History selected", Toast.LENGTH_SHORT).show();
                            return true;
                        default:
                            Toast.makeText(getApplicationContext(), "Something is Wrong", Toast.LENGTH_SHORT).show();
                            return true;

                    }
                } else {
                    return true;
                }
            }
        });
    }

    private void openSessionFragment() {
        if (onGoingSession) {
            openFragment(CurrentSessionFragment.newInstance(dataManager.getCurrentSessionCategory()), false);
        } else {
            openFragment(ChooseActivityFragment.newInstance(), false);
        }
    }

    private void openFragment(final Fragment fragment, boolean addToBackStack) {
        System.out.println("MainActivity.openFragment " + fragment.toString() + addToBackStack);
        if (addToBackStack) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            System.out.println("settings");
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void endSession(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Finish the session ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Create an intent for the session viewer with the needed informations from the session
                Intent intent = new Intent(mainContext, Session.class);
                TextView duration = (TextView) findViewById(R.id.duration);
                String message = duration.getText().toString();
                intent.putExtra("com.sendinaden.app1.DURATION", message);
                intent.putExtra("category info", dataManager.getCurrentSessionCategory());
                intent.putExtra("CompactedPressureData", dataManager.getCurrentBreathSession().extractCompactedPressureData());
                endSession();   // end the session and save all the variables
                startActivity(intent);  // start the activity with the intent
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    /**
     * Handle the process when the user select a new category
     * @param categoryInformation
     */
    @Override
    public void categoryChosen(CategoryInformation categoryInformation) {
        Log.d("USER INTERACTION", "---------------------------------------------------");
        dataManager.reset(categoryInformation);
        btcontrol.startScan();
        connectingDialog = new ProgressDialog(this);
        connectingDialog.setTitle("Connecting");
        connectingDialog.setMessage("Scanning");
        connectingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // If the connection dialog is cancelled
                Log.d("USER INTERACTION", "Connection Cancelled by user");
                btcontrol.stopScan();
                endSession();
            }
        });
        connectingDialog.show();
    }

    /**
     * Handling back button navigation
     */
    @Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.frame);
        // The session is ongoing and the the Fragment is shown
        if (f != null && f instanceof CurrentSessionFragment) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Cancel and exit the session ?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    endSession();
                    openSessionFragment();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Ending a session process
     */
    private void endSession() {
        onGoingSession = false;
        discussionOnGoing = false;
        // send multiple disconnecting message to the arduino to make sure it gets it even if on sleep
        for (int i=0; i<4; i++) {arduino.sendSerial("DisconnectedX");}
        // Then we disconnect from the arduino on the phone side
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                arduino.disconnect();
            }
        }, 200);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * Handling when the device is found
     * @param device
     */
    @Override
    public void onDeviceFound(BluetoothDevice device) {
        arduino.setDevice(device);
        arduino.connect();
    }

    /**
     * Handles the case when no device is found after a while of scanning
     */
    @Override
    public void noDeviceFound() {
        // Shows a dialog message for 1 second and closes
        connectingDialog.setMessage("No Device Found");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (connectingDialog.isShowing()) {
                    connectingDialog.dismiss();
                }
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        arduino.unregisterAndUnbind();
        super.onDestroy();
    }
}
