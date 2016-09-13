package org.dolphin.secret.picker;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by yananh on 2016/2/25.
 */
public class SquareFrameLyoutLayout extends FrameLayout {
    public SquareFrameLyoutLayout(Context context) {
        super(context);
    }

    public SquareFrameLyoutLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareFrameLyoutLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SquareFrameLyoutLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childWidthSize = getMeasuredWidth();
        int childHeightSize = getMeasuredHeight();

        childHeightSize = childWidthSize;
        setMeasuredDimension(getMeasuredWidthAndState(), MeasureSpec.makeMeasureSpec(childHeightSize, MeasureSpec.EXACTLY));
    }


    protected void onMeasure1(int widthMeasureSpec, int heightMeasureSpec) {
        // For simple implementation, or internal size is always 0.
        // We depend on the container to specify the layout size of
        // our view. We can't really know what it is since we will be
        // adding and removing different arbitrary views and do not
        // want the layout to change as this happens.
//        Log.d("DDD", "width " + getDefaultSize(0, widthMeasureSpec));
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));

        // Children are just made to fill our space.
        int childWidthSize = getMeasuredWidth();
        int childHeightSize = getMeasuredHeight();
        //高度和宽度一样
        heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
