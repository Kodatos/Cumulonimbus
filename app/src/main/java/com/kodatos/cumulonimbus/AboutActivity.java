package com.kodatos.cumulonimbus;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.kodatos.cumulonimbus.databinding.ActivityAboutBinding;

import de.psdev.licensesdialog.LicensesDialogFragment;

public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_about);
        setSupportActionBar(mBinding.toolbarAbout);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Window window = getWindow();

        Slide transition = new Slide();
        transition.setDuration(200);
        transition.setSlideEdge(Gravity.RIGHT);
        window.setEnterTransition(transition);
        window.setExitTransition(transition);
        window.setReturnTransition(transition);
        window.setReenterTransition(transition);

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        LicensesDialogFragment licensesFragment = new LicensesDialogFragment.Builder(this)
                .setNotices(R.raw.notices)
                .setIncludeOwnLicense(true).build();
        mBinding.licensesLayout.setOnClickListener(v -> licensesFragment.show(getSupportFragmentManager(), null));

    }
}
