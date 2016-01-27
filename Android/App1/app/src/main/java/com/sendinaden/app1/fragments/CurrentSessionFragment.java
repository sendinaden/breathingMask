package com.sendinaden.app1.fragments;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sendinaden.app1.MainActivity;
import com.sendinaden.app1.R;
import com.sendinaden.app1.adapters.SparklineListAdapter;
import com.sendinaden.app1.dataManagement.BreathSession;
import com.sendinaden.app1.models.CategoryInformation;
import com.sendinaden.app1.models.LiveChartModel;

import java.text.SimpleDateFormat;

import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * This fragment shows the actual Current session with the graphs and sparklines
 */
public class CurrentSessionFragment extends Fragment {

    public static final String TAG = "CURRENT_SESSION_TAG";
    private static final int LINE_TYPE = 0;
    private static final int BAR_TYPE = 1;

    // Graphs
    LineChartView lineChartView;
    private CategoryInformation categoryInformation;
    private SparklineListAdapter sparklineListAdapter;
    private OnFragmentInteractionListener mListener;
    // Time
    private TextView durationTimeView;


    public CurrentSessionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CurrentSessionFragment.
     */
    public static CurrentSessionFragment newInstance(int[] info) {
        CurrentSessionFragment fragment = new CurrentSessionFragment();
        Bundle args = new Bundle();
        args.putIntArray("category info", info);
        fragment.setArguments(args);
        return fragment;
    }

    public static CurrentSessionFragment newInstance(CategoryInformation categoryInformation) {
        CurrentSessionFragment fragment = new CurrentSessionFragment();
        Bundle args = new Bundle();
        args.putParcelable("category info", categoryInformation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryInformation = getArguments().getParcelable("category info");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_current_session, container, false);
        // Graph
        lineChartView = (LineChartView) view.findViewById(R.id.current_session_livegraph);
        // Duration text view
        durationTimeView = (TextView) view.findViewById(R.id.duration);
        // Sparklines adapter and views
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recyclerSparklines);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        sparklineListAdapter = new SparklineListAdapter(getContext(), categoryInformation);
        rv.setAdapter(sparklineListAdapter);

        return view;
    }

//    private View SparklineView(int id)

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        categoryInformation = getArguments().getParcelable("category info");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(categoryInformation.getColor());
        }
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(categoryInformation.getName()));
        ((MainActivity) getActivity()).getSupportActionBar()
                .setBackgroundDrawable(new ColorDrawable(categoryInformation.getColor()));
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
     * Update the main graph and its view port
     * @param model the model containing the data to plot
     */
    public void updateLiveGraph(LiveChartModel model) {
        lineChartView.setLineChartData(model.getDataForLiveChart());
        Viewport v = lineChartView.getMaximumViewport();
        if (!model.isInvert()) {
            v.bottom = model.getBottom();
            v.top = model.getTop();
        } else {
            v.bottom = model.getTop();
            v.top = model.getBottom();
        }
        lineChartView.setMaximumViewport(v);
        lineChartView.setCurrentViewport(v);
    }

    /**
     * Update the 4 sparklines by getting the informations from the models
     * The models and therefore sparklines depend on the category, as every
     * category doesn't show the same information
     * @param currentBreathSession
     */
    public void updateSparklines(BreathSession currentBreathSession) {
        for (int i = 0; i < 4; i++) {
            sparklineListAdapter.getModels().get(i)
                    .updateRawData(currentBreathSession.getDataByName(
                            categoryInformation.getSparklineName(i)
                    ));
            sparklineListAdapter.notifyItemChanged(i);
        }
    }

    /**
     * update the time relatively to the start time
     * @param start start time of the session
     */
    public void updateTime(long start) {
        String difference = getDifference(start, System.currentTimeMillis());
        durationTimeView.setText(difference);
    }

    /**
     * Returns the difference between two time values given in milliseconds
     * @param time1 start time
     * @param time2 end time
     * @return Difference between start and end in a string format: HH:mm:ss
     */
    private String getDifference(long time1, long time2) {
        long difference = (time2 - time1) / 1000L;
        long hours = difference / 3600;
        difference %= 3600;
        long minutes = difference / 60;
        difference %= 60;
        long seconds = difference;

        return String.format("%d:%02d:%02d", hours, minutes, seconds);
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
    }

}
