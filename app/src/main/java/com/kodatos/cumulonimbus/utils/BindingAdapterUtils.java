package com.kodatos.cumulonimbus.utils;

import android.databinding.BindingAdapter;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;

import com.kodatos.cumulonimbus.R;

/*
    A utility class that contain custom attribute setters for the Data Binding Library. Kept in a separate
    class for cleanliness
 */
@SuppressWarnings("unused")
public class BindingAdapterUtils {

    /**
     * BindingAdapter to set a drawable from resource to an ImageView
     * @param imageView The ImageView to set drawable on
     * @param resource The resource id for the drawable
     */
    @BindingAdapter("drawableResource")
    public static void setDrawableResource(ImageView imageView, int resource){
        imageView.setImageResource(resource);
    }

    @BindingAdapter("integerTint")
    public static void setIntegerTint(ImageView imageView, int color) {
        imageView.getDrawable().setTint(color);
    }

    @BindingAdapter("locationTextAndIcon")
    public static void setLocationTextAndIcon(TextView textView, String locationAndIcon) {
        if (locationAndIcon == null)
            return;
        String[] spl = locationAndIcon.split("/");
        if (spl.length == 2 && Boolean.parseBoolean(spl[1])) {
            textView.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(textView.getContext(), R.drawable.ic_location_current), null, null, null);
        } else
            textView.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(textView.getContext(), R.drawable.ic_location_custom), null, null, null);
        textView.setText(spl[0]);
    }
}
