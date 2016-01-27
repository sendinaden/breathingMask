package com.sendinaden.app1.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sendinaden.app1.R;
import com.sendinaden.app1.Session;
import com.sendinaden.app1.customViews.FeedbackCard;

/**
 * This fragment is shown in the Session activity
 * It allows the user to rate and give feedback about the session
 */
public class FeedbackFragment extends Fragment {

    private static final int[] feedbackCards = {
            R.id.calmnessCard,
            R.id.comfortCard,
            R.id.energeticCard,
            R.id.overallCard
    };

    private int[] marks = new int[feedbackCards.length];
    private int totalMax;
    private TextView totalView;

    private OnFragmentInteractionListener mListener;
    private int seekBarColor;

    public FeedbackFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FeedbackFragment.
     */
    public static FeedbackFragment newInstance(int seekbarColor) {
        FeedbackFragment fragment = new FeedbackFragment();
        Bundle args = new Bundle();
        args.putInt("seekbarcolor", seekbarColor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((Session) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        ((Session) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getArguments() != null) {
            seekBarColor = getArguments().getInt("seekbarcolor");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);
        totalView = (TextView) view.findViewById(R.id.feedback_total_txt_view);
        FeedbackCard feedbackCard;
        SeekBar seekBar;
        for (int i = 0; i < feedbackCards.length; i++) {
            feedbackCard = (FeedbackCard) view.findViewById(feedbackCards[i]);
            seekBar = (SeekBar) feedbackCard.findViewById(R.id.feedback_seekbar);
            ;
            final int finalI = i;
//            seekBar.getThumb().setColorFilter(seekBarColor, PorterDuff.Mode.MULTIPLY);
//            seekBar.setProgressDrawable(new ColorDrawable(seekBarColor));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                /**
                 * Updates the total feedback everytime one the seekbar is changed
                 * @param seekBar
                 * @param progress
                 * @param fromUser
                 */
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    marks[finalI] = progress;
                    updateTotalFeedback();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            totalMax += seekBar.getMax();
        }
        updateTotalFeedback();
        return view;
    }

    /**
     * update the total feedback by suming the feedback
     */
    private void updateTotalFeedback() {
        System.out.println("update");
        int total = 0;
        for (int m : marks) {
            total += m;
        }
        String stotal = String.valueOf(total) + "/" + String.valueOf(totalMax);
        totalView.setText(stotal);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_session, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("option item selected - Session Feedback");
        switch (item.getItemId()) {
            case R.id.action_done:
                // User chose the "Done" action, mark the current item
                // as a favorite...
                System.out.println("HIHIHIHI");
                super.getActivity().onBackPressed();
                return true;

            case android.R.id.home:
                System.out.println("home");
                return super.onOptionsItemSelected(item);
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
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
        public void onFragmentInteraction(Uri uri);
    }

}
