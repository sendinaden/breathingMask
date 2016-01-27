package com.sendinaden.app1.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sendinaden.app1.R;
import com.sendinaden.app1.models.CategoryInformation;
import com.sendinaden.app1.models.SparklineModel;

import java.util.ArrayList;

import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by ejalaa on 18/09/15.
 * An adapter that contains the sparklines view
 */
public class SparklineListAdapter extends RecyclerView.Adapter<SparklineListAdapter.SparklineViewHolder> {

    private static final int LINE_TYPE = 0;
    private static final int BAR_TYPE = 1;
    private Context mainContext;
    private CategoryInformation categoryInformation;

    private ArrayList<SparklineModel> models;

    /**
     * This create the holder that contains the views
     */
    public SparklineListAdapter(Context mainContext, CategoryInformation categoryInformation) {
        this.mainContext = mainContext;
        this.categoryInformation = categoryInformation;
        models = new ArrayList<>();
        for (int type : categoryInformation.getSparklineTypes()) {
            ArrayList<Integer> dummyRawData = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                dummyRawData.add(0);
            }
            models.add(new SparklineModel(dummyRawData, type));
        }
    }

    /**
     * How to create the view
     *
     * @param parent
     * @param viewType
     * @return returns a sparkline view holder
     */
    @Override
    public SparklineListAdapter.SparklineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case LINE_TYPE:
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_sparkline_item, parent, false);
                SparklineViewHolder svh = new SparklineViewHolder(v, LINE_TYPE);
                return svh;
            case BAR_TYPE:
                View v1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_sparkline2_item, parent, false);
                SparklineViewHolder svh1 = new SparklineViewHolder(v1, BAR_TYPE);
                return svh1;
            default:
                return null;
        }
    }

    /**
     * How to populate each sparkline view holder
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(SparklineListAdapter.SparklineViewHolder holder, int position) {
        switch (holder.type) {
            case LINE_TYPE:
                holder.sparkline.setLineChartData((LineChartData) models.get(position).getChartData());
                break;
            case BAR_TYPE:
                holder.sparklineColumn.setColumnChartData((ColumnChartData) models.get(position).getChartData());
                break;
        }
        holder.sparklineName.setText(categoryInformation.getSparklineName(position));
        holder.sparklineCurrentValue.setText(String.valueOf(models.get(position).getLast()));
    }

    @Override
    public int getItemCount() {
        return categoryInformation.getSparklineNames().length;
    }

    @Override
    public int getItemViewType(int position) {
        return categoryInformation.getSparklineType(position);
    }

    /************************************************************************************************
     * Design of the charts
     ************************************************************************************************/

    private void setChartDesign(ColumnChartView columnChartView) {
        int last = columnChartView.getColumnChartData().getColumns().get(0)
                .getValues().size() - 1;
        columnChartView.getColumnChartData().getColumns().get(0)
                .getValues().get(last).setColor(mainContext.getResources().getColor(R.color.ColorGraphPrimaryDark));
        Viewport v = columnChartView.getMaximumViewport();
        v.bottom = -2;
        v.top = 2;
        columnChartView.setMaximumViewport(v);
        columnChartView.setCurrentViewport(v);
    }

    private void setChartDesign(LineChartView lineChartView, int top, int bottom) {

        lineChartView.getLineChartData().getLines().get(0)
                .setPointRadius(0)
                .setStrokeWidth(1)
                .setColor(mainContext.getResources().getColor(R.color.ColorGraphPrimary));
        lineChartView.getLineChartData().getLines().get(1)
                .setPointRadius(2)
                .setColor(mainContext.getResources().getColor(R.color.ColorGraphPrimaryDark));
        Viewport v = lineChartView.getMaximumViewport();
        v.bottom = bottom;
        v.top = top;
        lineChartView.setMaximumViewport(v);
        lineChartView.setCurrentViewport(v);
    }

    /************************************************************************************************
     * Setting the data of the charts
     ************************************************************************************************/

    public void addValueToGraph(String graphName, int y, int graphType) {
//        ArrayList<Object> p = findSparklinePackByName(graphName);
//        if (p.isEmpty()) {
//            SparklinePack added = new SparklinePack(graphName, graphType);
//            getSparklinePackList().add(added);
//            p.add(added);
//            p.add(getSparklinePackList().indexOf(added));
//        }
//        ((SparklinePack) p.get(0)).add((float) y);
//        notifyItemChanged((int) p.get(1));
    }

    public ArrayList<SparklineModel> getModels() {
        return models;
    }
    /************************************************************************************************
     * Views of the charts
     ************************************************************************************************/


    public static class SparklineViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        LineChartView sparkline;
        ColumnChartView sparklineColumn;
        int type;
        TextView sparklineName;
        TextView sparklineCurrentValue;

        public SparklineViewHolder(View itemView, int sparklineType) {
            super(itemView);
            switch (sparklineType) {
                case LINE_TYPE:
                    cardView = (CardView) itemView.findViewById(R.id.sparkline_cardview);
                    sparkline = ((LineChartView) itemView.findViewById(R.id.sparkline_chart));
                    type = 0;
                    sparklineName = ((TextView) itemView.findViewById(R.id.sparkline_name));
                    sparklineCurrentValue = ((TextView) itemView.findViewById(R.id.sparkline_recent_value));
                    break;
                case BAR_TYPE:
                    cardView = (CardView) itemView.findViewById(R.id.sparkline2_cardview);
                    sparklineColumn = ((ColumnChartView) itemView.findViewById(R.id.sparkline2_chart));
                    type = 1;
                    sparklineName = ((TextView) itemView.findViewById(R.id.sparkline2_name));
                    sparklineCurrentValue = ((TextView) itemView.findViewById(R.id.sparkline2_recent_value));
                    break;
            }
        }

    }


}
