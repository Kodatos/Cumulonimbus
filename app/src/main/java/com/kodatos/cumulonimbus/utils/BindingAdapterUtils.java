package com.kodatos.cumulonimbus.utils;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

public class BindingAdapterUtils {

    @BindingAdapter("drawableResource")
    public static void setDrawableResource(ImageView imageView, int resource){
        imageView.setImageResource(resource);
    }
}
