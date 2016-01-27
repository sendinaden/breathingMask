package com.sendinaden.app1.customViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sendinaden.app1.R;


/**
 * Created by ejalaa on 05/11/15.
 * An activity card (extends CardView) is a holder for a type of activity
 */
public class ActivityCategory extends CardView {

    private int bg_color;
    private String titleText;
    private int img_src;
    private float img_size;

    public ActivityCategory(Context context) {
        super(context);
        init(context);
    }

    public ActivityCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttributes(context, attrs);
        init(context);
    }

    public ActivityCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttributes(context, attrs);
        init(context);
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActivityCategory);
        this.bg_color = a.getColor(R.styleable.ActivityCategory_bg_color, 0);
        this.titleText = a.getString(R.styleable.ActivityCategory_titleText);
        this.img_src = a.getResourceId(R.styleable.ActivityCategory_img_src, 0);
        this.img_size = a.getFloat(R.styleable.ActivityCategory_img_size, 0);
        a.recycle();
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.card_activity_category, null);

        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));

        ImageView img = (ImageView) layout.findViewById(R.id.act_img);
        img.setImageResource(img_src);
        img.setScaleX(img_size);
        img.setScaleY(img_size);

        TextView title = (TextView) layout.findViewById(R.id.act_title);
        title.setText(titleText);

        FrameLayout frame = (FrameLayout) layout.findViewById(R.id.act_img_frame);
        frame.setBackground(getResources().getDrawable(R.drawable.top_round_img));
        ((GradientDrawable) frame.getBackground()).setColor(bg_color);

        this.setPreventCornerOverlap(false);
        this.addView(layout);
    }

    public int getBg_color() {
        return bg_color;
    }

    public void setBg_color(int bg_color) {
        this.bg_color = bg_color;
    }

    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public int getImg_src() {
        return img_src;
    }

    public void setImg_src(int img_src) {
        this.img_src = img_src;
    }
}
