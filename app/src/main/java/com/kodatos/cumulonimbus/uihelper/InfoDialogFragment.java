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

package com.kodatos.cumulonimbus.uihelper;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.databinding.InfoDialogLayoutBinding;

import java.util.Objects;

public class InfoDialogFragment extends DialogFragment {

    private static final String INFO_DIALOG_TITLE = "info_dialog_title";
    private static final String INFO_DIALOG_DESC = "info_dialog_desc";
    private static final String INFO_DIALOG_COLOR = "info_dialog_color";
    private static final String INFO_DIALOG_DRAWABLE = "info_dialog_drawable";

    private View.OnClickListener positiveTextClickListener = null;
    private String positiveText = null;

    public static InfoDialogFragment newInstance(String title, String description, int drawableID, int backgroundColor) {
        InfoDialogFragment fragment = new InfoDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(INFO_DIALOG_TITLE, title);
        bundle.putString(INFO_DIALOG_DESC, description);
        bundle.putInt(INFO_DIALOG_DRAWABLE, drawableID);
        bundle.putInt(INFO_DIALOG_COLOR, backgroundColor);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            dismiss();
            return null;
        }
        InfoDialogLayoutBinding mBinding = DataBindingUtil.inflate(inflater, R.layout.info_dialog_layout, container, false);
        Bundle arguments = Objects.requireNonNull(getArguments());
        mBinding.infoDialogTitle.setText(arguments.getString(INFO_DIALOG_TITLE));
        mBinding.infoDialogDescription.setText(arguments.getString(INFO_DIALOG_DESC));
        mBinding.infoDialogImageView.setImageResource(arguments.getInt(INFO_DIALOG_DRAWABLE));
        mBinding.infoDialogImageViewBackground.setBackgroundColor(arguments.getInt(INFO_DIALOG_COLOR));
        if(positiveText == null) {
            positiveText = getString(R.string.common_positive_text);
            mBinding.negativeActionText.setVisibility(View.GONE);
        }
        else{
            mBinding.negativeActionText.setVisibility(View.VISIBLE);
            mBinding.negativeActionText.setText(getString(R.string.common_negative_text));
        }
        mBinding.positiveActionText.setText(positiveText);
        mBinding.positiveActionText.setOnClickListener(v -> {
            //Perform provided action and dismiss the dialog as well
            if(positiveTextClickListener!=null)
                positiveTextClickListener.onClick(v);
            dismiss();
        });
        //Negative action is always dismiss the dialog
        mBinding.negativeActionText.setOnClickListener(v -> dismiss());
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getDialog().getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    //Set a custom positive action
    public void setPositiveAction(String text, View.OnClickListener positiveTextClickListener) {
        positiveText = text;
        this.positiveTextClickListener = positiveTextClickListener;
    }

}
