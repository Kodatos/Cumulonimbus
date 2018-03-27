/*
 * MIT License
 *
 * Copyright (c) 2017 N Abhishek (aka Kodatos)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
