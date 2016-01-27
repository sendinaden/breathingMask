package com.sendinaden.app1;

import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.sendinaden.app1.fragments.FeedbackFragment;
import com.sendinaden.app1.fragments.SessionReportFragment;
import com.sendinaden.app1.models.CategoryInformation;

/**
 * THe session activity is the one that sum up the previous (or selected in history) session
 * It handles the informations from the session and displays it for the user
 */
public class Session extends AppCompatActivity implements
        SessionReportFragment.OnFragmentInteractionListener,
        FeedbackFragment.OnFragmentInteractionListener {

    CategoryInformation categoryInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        // Set the STATUS BAR COLOR
        categoryInformation = getIntent().getParcelableExtra("category info");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(categoryInformation.getColorDark());
        }
        // TOOLBAR
        Toolbar myToolbar = (Toolbar) findViewById(R.id.report_toolbar);
        setSupportActionBar(myToolbar);

        // SET ACTION BAR COLOR
        ColorDrawable c = new ColorDrawable(categoryInformation.getColor());
        getSupportActionBar().setBackgroundDrawable(c);
        openFragment(SessionReportFragment.newInstance(categoryInformation.getColorDark()));
    }

    public void openFragment(final Fragment fragment) {
        System.out.println("MainActivity.openFragment " + fragment.toString());
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.report_frame, fragment)
//                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
