package com.sendinaden.app1.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by ejalaa on 05/11/15.
 * This class is a container view for a sparkline
 * NOT USED
 */
public class SparklineView extends RelativeLayout {

    private View mValue;
    private ImageView mImage;

    public SparklineView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SparklineView(Context context) {
        this(context, null);
    }

    public void setValueColor(int color) {
        mValue.setBackgroundColor(color);
    }

    public void setImageVisible(boolean visible) {
        mImage.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
