package net.hearnsoft.thwikicdsearchapi.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

import androidx.annotation.Nullable;

public class MarqueeTextView extends AppCompatTextView {
    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused(){
        return true;
    }
}
