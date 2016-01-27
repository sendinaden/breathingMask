package com.sendinaden.app1.customViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sendinaden.app1.R;

/**
 * Created by ejalaa on 06/11/15.
 * A FeedbackCard is a container holding a seekbar and couple of string (good/bad)
 */
public class FeedbackCard extends CardView {

    private String title, good, bad;
    private int fd_max;

    public FeedbackCard(Context context) {
        super(context);
        init(context);
    }

    public FeedbackCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttributes(context, attrs);
        init(context);
    }

    public FeedbackCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttributes(context, attrs);
        init(context);
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FeedbackCard);
        title = a.getString(R.styleable.FeedbackCard_name);
        good = a.getString(R.styleable.FeedbackCard_good);
        bad = a.getString(R.styleable.FeedbackCard_bad);
        fd_max = a.getInt(R.styleable.FeedbackCard_max, 0);
        a.recycle();
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.card_feedback, null);
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
//        layoutParams.setMargins(8, 8, 8, 8);
//        layout.setLayoutParams(layoutParams);
        layout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));

        TextView titleView = (TextView) layout.findViewById(R.id.feedback_title);
        titleView.setText(title);

        TextView badView = (TextView) layout.findViewById(R.id.feedback_bad);
        badView.setText(bad);

        TextView goodView = (TextView) layout.findViewById(R.id.feedback_good);
        goodView.setText(good);

        SeekBar seekBar = (SeekBar) layout.findViewById(R.id.feedback_seekbar);
        seekBar.setMax(fd_max);
//        seekBar.setProgress(fd_max / 2);

        this.addView(layout);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGood() {
        return good;
    }

    public void setGood(String good) {
        this.good = good;
    }

    public String getBad() {
        return bad;
    }

    public void setBad(String bad) {
        this.bad = bad;
    }

    public int getFd_max() {
        return fd_max;
    }

    public void setFd_max(int fd_max) {
        this.fd_max = fd_max;
    }
}
