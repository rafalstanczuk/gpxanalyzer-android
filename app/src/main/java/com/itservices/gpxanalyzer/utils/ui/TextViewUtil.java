package com.itservices.gpxanalyzer.utils.ui;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import androidx.annotation.NonNull;

public class TextViewUtil {
    @NonNull
    public static SpannableStringBuilder getSpannableStringBuilder(String prefixText, String postFixText) {
        SpannableStringBuilder textLine = new SpannableStringBuilder();

        if (prefixText != null) {
            SpannableString prefixTextSpannable = new SpannableString(prefixText);
            textLine.append(prefixTextSpannable);
        }

        SpannableString postFixTextSpannable = new SpannableString(postFixText);
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        postFixTextSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, postFixTextSpannable.length(), flag);

        textLine.append(postFixTextSpannable);

        return textLine;
    }
}
