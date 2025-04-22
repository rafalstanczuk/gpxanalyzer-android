package com.itservices.gpxanalyzer.utils.ui;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import androidx.annotation.NonNull;

/**
 * Utility class for creating styled text using {@link SpannableStringBuilder}.
 */
public class TextViewUtil {

    /**
     * Creates a {@link SpannableStringBuilder} by concatenating a prefix string (optional, regular style)
     * and a postfix string (mandatory, bold style).
     * <p>
     * Example usage: `getSpannableStringBuilder("Name: ", "John Doe")` would produce "Name: **John Doe**".
     *
     * @param prefixText  The text to appear before the bolded postfix. If null, it is omitted.
     * @param postFixText The text to appear at the end, formatted in bold. Must not be null.
     * @param space
     * @return A {@link SpannableStringBuilder} containing the combined text with the postfix styled bold.
     * @throws NullPointerException if {@code postFixText} is null.
     */
    @NonNull
    public static SpannableStringBuilder getSpannableStringBuilderWithBoldPostfix(String prefixText, String postFixText, String space) {
        SpannableStringBuilder textLine = new SpannableStringBuilder();

        if (prefixText != null) {
            SpannableString prefixTextSpannable = new SpannableString(prefixText);
            textLine.append(prefixTextSpannable);
        }

        if (space != null) {
            textLine.append(space);
        }

        SpannableString postFixTextSpannable = new SpannableString(postFixText);
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        postFixTextSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, postFixTextSpannable.length(), flag);

        textLine.append(postFixTextSpannable);

        return textLine;
    }
    
    /**
     * Creates a {@link SpannableStringBuilder} by concatenating a prefix string (mandatory, bold style)
     * and a postfix string (mandatory, regular style).
     *
     * Example usage: `getSpannableStringBuilderWithBoldPrefix("Name: ", "John Doe")` would produce "**Name:** John Doe".
     *
     * @param prefixText The text to appear at the beginning, formatted in bold. Must not be null.
     * @param postFixText The text to appear after the prefix. If null, it is omitted.
     * @return A {@link SpannableStringBuilder} containing the combined text with the prefix styled bold.
     * @throws NullPointerException if {@code prefixText} is null.
     */
    @NonNull
    public static SpannableStringBuilder getSpannableStringBuilderWithBoldPrefix(String prefixText, String postFixText, String space) {
        SpannableStringBuilder textLine = new SpannableStringBuilder();

        SpannableString prefixTextSpannable = new SpannableString(prefixText);
        int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
        prefixTextSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, prefixTextSpannable.length(), flag);
        textLine.append(prefixTextSpannable);

        if (space != null) {
            textLine.append(space);
        }

        if (postFixText != null) {
            SpannableString postFixTextSpannable = new SpannableString(postFixText);
            textLine.append(postFixTextSpannable);
        }

        return textLine;
    }
}
