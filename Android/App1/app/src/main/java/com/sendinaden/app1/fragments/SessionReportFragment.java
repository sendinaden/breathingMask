package com.sendinaden.app1.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sendinaden.app1.R;
import com.sendinaden.app1.Session;

import java.util.ArrayList;
import java.util.Collections;

import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * This fragment shows the report of the session and display a summary
 * NOT COMPLETELY OVER YET
 */
public class SessionReportFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private int seekbarColor;

    public SessionReportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SessionReportFragment.
     */
    public static SessionReportFragment newInstance(int Color) {
        SessionReportFragment fragment = new SessionReportFragment();
        Bundle args = new Bundle();
        args.putInt("seekbarColor", Color);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            seekbarColor = getArguments().getInt("seekbarColor");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_session_report, container, false);

        // Get the duration from the previous screen and display it
        String message = getActivity().getIntent().getStringExtra("com.sendinaden.app1.DURATION");
        message = "Session #XX - " + message;
        ((TextView) view.findViewById(R.id.session_report_title)).setText(message);

        // Gets the array containing a sub-list of the breathing graph to show an overview
        // and put it in the graph
        ArrayList<Integer> compactPressureData = getActivity().getIntent().getIntegerArrayListExtra("CompactedPressureData");
        if (compactPressureData.size() > 0) {
            putDataInChart(view, compactPressureData);
        }
        return view;
    }

    /**
     * Adds the data received from the previous activity to the graph
     * @param view
     * @param compactPressureData
     */
    private void putDataInChart(View view, ArrayList<Integer> compactPressureData) {
        // improve make the lines already saved in the class and add the data to the lines, instead of recreating them.
        ArrayList<Line> lines = new ArrayList<>();

        // Pressure values
        ArrayList<PointValue> values = new ArrayList<>();
        for (int i = 0; i < compactPressureData.size(); i++) {
            values.add(new PointValue(i, compactPressureData.get(i)));
        }
        Line pressureLine = new Line(values);
//        pressureLine.setColor(Color.parseColor("#0fc0fc"));
        pressureLine.setColor(seekbarColor);
        pressureLine.setCubic(true);
        pressureLine.setStrokeWidth(1);
        pressureLine.setPointRadius(0);
        lines.add(pressureLine);
        LineChartData data = new LineChartData(lines);
        LineChartView lineChartView = (LineChartView) view.findViewById(R.id.reportLineChart);
        lineChartView.setLineChartData(data);

        Viewport v = lineChartView.getMaximumViewport();
        v.bottom = Collections.min(compactPressureData) - 10;
        v.top = Collections.max(compactPressureData) + 10;
        lineChartView.setMaximumViewport(v);
        lineChartView.setCurrentViewport(v);
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
        System.out.println("option item selected - Session Report");
        switch (item.getItemId()) {
            case R.id.action_done:
                // User chose the "Done" action, mark the current item
                // as a favorite...
                ((Session) getActivity()).openFragment(FeedbackFragment.newInstance(seekbarColor));
                return true;

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

    }

}
