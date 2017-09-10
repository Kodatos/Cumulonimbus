package com.kodatos.cumulonimbus.utils;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

/*
    A utility class that contain custom attribute setters for the Data Binding Library. Kept in a separate
    class for cleanliness
 */
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
    public static void setintegerTint(ImageView imageView, int color){
        imageView.getDrawable().setTint(color);
    }
}
