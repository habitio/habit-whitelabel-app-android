package com.muzzley.app.cards;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.muzzley.model.cards.RangeStyle;
import com.muzzley.model.cards.Text;

import java.util.List;

import timber.log.Timber;

/**
 * Created by caan on 27-09-2015.
 */
public class TextContainer extends TextView implements Container<Text> {

//    static Typeface typeface;
    public TextContainer(Context context) {
        super(context);
//        init();
    }

//    private void init() {
//        setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//        int px = Utils.px(getContext(), 20);
//        setPadding(px, px/2, px, px);
//        setGravity(Gravity.CENTER);
//
//    }

    public TextContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
//        init();
    }

    public TextContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        init();
    }


    @Override
    public void setContainerData(Text text) {
        if (text == null) {
            setVisibility(GONE);
            return;
        } else {
            setVisibility(VISIBLE);
        }

        setTextColor(Color.parseColor("#" + CardContainer.from(this).card.colors.main.text));

        SpannableString sp = new SpannableString(text.content);
        float ts = getTextSize();
//        Timber.d("padding , text size= " + ts);

        if (text.contentStyles != null) {

            List<Double> margin = text.contentStyles.margin;
            //            context.getResources().getDisplayMetrics().
//            Timber.d("padding , margin size= " + margin.size());
            int top, right, bottom, left;
            top = right = bottom = left = 0;
            switch (margin.size()) {
                case 1:
                    top = right = bottom = left = (int) (ts * margin.get(0));
                    break;
                case 2:
                    top = bottom = (int) (ts * margin.get(0));
                    left = right = (int) (ts * margin.get(1));
                    break;
                case 4:
                    top = (int) (ts * margin.get(0));
                    right = (int) (ts * margin.get(1));
                    bottom = (int) (ts * margin.get(2));
                    left = (int) (ts * margin.get(3));
                    break;
            }
//            Timber.d("padding margins = %s,%s,%s,%s ", top, right, bottom, left);
            setPadding(left, top, right, bottom);
        }
        if (text.rangeStyles != null) {
            for (RangeStyle rs : text.rangeStyles) {
                int start = Math.max(rs.range.get(0),0);
                int end = Math.min(rs.range.get(1),sp.length()-1);

                if (rs.bold) {
                    sp.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
                }
                if (rs.italic) {
                    sp.setSpan(new StyleSpan(Typeface.ITALIC), start, end, 0);
                }
                if (rs.underline) {
                    sp.setSpan(new UnderlineSpan(), start, end, 0);
                }
                if (rs.fontSize > 0) {
                    sp.setSpan(new RelativeSizeSpan((float) rs.fontSize), start, end, 0);
                }
                if (rs.color != null) {
                    sp.setSpan(new ForegroundColorSpan(Color.parseColor("#" + rs.color)), start, end, 0);
                }

            }
        }
        setText(sp);
    }

}
