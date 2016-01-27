package com.sendinaden.app1.fragments;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sendinaden.app1.MainActivity;
import com.sendinaden.app1.R;
import com.sendinaden.app1.models.CategoryInformation;

/**
 * The fragment that shows all the card that corresponds to a category
 */
public class ChooseActivityFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "CHOOSE_ACTIVITY_FRAGMENT_TAG";
    public static final String CATEGORIES_PREFERENCE = "com.sendinaden.app1.CATEGORIES_PREFERENCE";
    public static final String CATEGORIES_LIST = "com.sendinaden.app1.CATEGORIES_LIST";
    private static final int[] categories = {
            R.id.YogaCategory, R.id.RunningCategory, R.id.RelaxCategory,
            R.id.CommuteCategory, R.id.SleepCategory, R.id.WorkCategory};
    private static final int[] categoriesColor = {
            R.color.ColorYoga, R.color.ColorRunning, R.color.ColorRelax,
            R.color.ColorCommute, R.color.ColorSleep, R.color.ColorWork};
    private static final int[] categoriesColorDark = {
            R.color.ColorYogaDark, R.color.ColorRunningDark, R.color.ColorRelaxDark,
            R.color.ColorCommuteDark, R.color.ColorSleepDark, R.color.ColorWorkDark};
    private static final int[] categoriesName = {
            R.string.yoga, R.string.running, R.string.relax,
            R.string.commute, R.string.sleep, R.string.work
    };
    private OnFragmentInteractionListener mListener;

    public ChooseActivityFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ChooseActivityFragment.
     */
    public static ChooseActivityFragment newInstance() {
        ChooseActivityFragment fragment = new ChooseActivityFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_activity_choosing, container, false);
        for (int category : categories) {
            CardView cardView = (CardView) view.findViewById(category);
            if (cardView != null) {
                cardView.setOnClickListener(this);
            }
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < categories.length; i++) {
            int category = categories[i];
            int color = categoriesColor[i];
            int colorDark = categoriesColorDark[i];
            if (category == v.getId()) {
                CategoryInformation cat = loadCategoryFromPosition(i);
                mListener.categoryChosen(cat);
//                mListener.categoryChosen(categoriesName[i], color, colorDark);
                return;
            }
        }
    }

    /**
     *
     * @param pos the position of the category
     * @return the category object that contains all the information of the category
     */
    private CategoryInformation loadCategoryFromPosition(int pos) {
        // TODO: 20/11/15 change when we find a way to save them somehow AND when possible to add categories
        int[] sparklineNames = new int[4];
        int[] sparklineTypes = new int[4];
        double amp, period;
        switch (categoriesName[pos]) {
            case R.string.yoga:
                sparklineNames[0] = R.string.depth;
                sparklineTypes[0] = 0;
                sparklineNames[1] = R.string.bpm;
                sparklineTypes[1] = 0;
                sparklineNames[2] = R.string.balance;
                sparklineTypes[2] = 1;
                sparklineNames[3] = R.string.dominance;
                sparklineTypes[3] = 1;
                amp = 50;
                period = 50;
                break;
            case R.string.sleep:
                sparklineNames[0] = R.string.depth;
                sparklineTypes[0] = 0;
                sparklineNames[1] = R.string.bpm;
                sparklineTypes[1] = 0;
                sparklineNames[2] = R.string.balance;
                sparklineTypes[2] = 1;
                sparklineNames[3] = R.string.dominance;
                sparklineTypes[3] = 1;
                amp = 50;
                period = 50;
                break;
            case R.string.running:
                sparklineNames[0] = R.string.depth;
                sparklineTypes[0] = 0;
                sparklineNames[1] = R.string.bpm;
                sparklineTypes[1] = 0;
                sparklineNames[2] = R.string.balance;
                sparklineTypes[2] = 1;
                sparklineNames[3] = R.string.dominance;
                sparklineTypes[3] = 1;
                amp = 100;
                period = 10;
                break;
            default:
                sparklineNames[0] = R.string.depth;
                sparklineTypes[0] = 0;
                sparklineNames[1] = R.string.bpm;
                sparklineTypes[1] = 0;
                sparklineNames[2] = R.string.balance;
                sparklineTypes[2] = 1;
                sparklineNames[3] = R.string.dominance;
                sparklineTypes[3] = 1;
                amp = 50;
                period = 50;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getContext().getColor(categoriesColor[0]);
            return new CategoryInformation(categoriesName[pos], getContext().getColor(categoriesColor[pos]),
                    getContext().getColor(categoriesColorDark[pos]), sparklineNames, sparklineTypes,
                    amp, period);
        } else {
            return new CategoryInformation(categoriesName[pos], getResources().getColor(categoriesColor[pos]),
                    getResources().getColor(categoriesColorDark[pos]), sparklineNames, sparklineTypes,
                    amp, period);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.ColorPrimaryDark));
        }
        // Set the action bar title and color
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Sendinaden");
        ((MainActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.ColorPrimary)));
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

        /**
         * Handles the click on the category card
         * @param categoryInformation object containing informations
         */
        void categoryChosen(CategoryInformation categoryInformation);

    }
}
