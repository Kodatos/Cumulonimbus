package com.kodatos.cumulonimbus;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.kodatos.cumulonimbus.databinding.GraphsActivityLayoutBinding;
import com.kodatos.cumulonimbus.uihelper.XAxisTimeFormatter;
import com.kodatos.cumulonimbus.utils.KeyConstants;

import java.util.ArrayList;
import java.util.List;

public class GraphsActivity extends AppCompatActivity {

    private GraphsActivityLayoutBinding mBinding;
    private int[] temperatures;
    private int[] winds;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.graphs_activity_layout);
        setSupportActionBar(mBinding.toolbarGraphsActivity);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        int backgroundColor = Color.parseColor("#00695C");
        getWindow().setStatusBarColor(backgroundColor);
        getWindow().setNavigationBarColor(backgroundColor);
        temperatures = getIntent().getIntArrayExtra("temperature_chart_data");
        winds = getIntent().getIntArrayExtra("wind_chart_data");
        createCharts();

    }

    private void createCharts() {
        List<Entry> temperatureData = new ArrayList<>();
        List<Entry> windData = new ArrayList<>();
        int temperaturesLength = temperatures.length;
        for (int i = 0; i < temperaturesLength; i++) {
            temperatureData.add(new Entry(i, temperatures[i]));
            windData.add(new Entry(i, winds[i]));
        }

        boolean metric = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_metrics_key), true);
        String temperatureLabel = "Temperature (\u00B0" + (metric ? "C" : "F") + ")";
        String windLabel = "Wind (" + (metric ? "km/h" : "mi/h") + ")";
        LineDataSet temperatureChartDataSet = new LineDataSet(temperatureData, temperatureLabel);
        temperatureChartDataSet.setColor(ContextCompat.getColor(this, R.color.apparent_temperature_color));
        temperatureChartDataSet.setFillColor(ContextCompat.getColor(this, R.color.apparent_temperature_color));

        LineDataSet windChartDataSet = new LineDataSet(windData, windLabel);
        windChartDataSet.setColor(ContextCompat.getColor(this, R.color.wind_info_color));
        windChartDataSet.setFillColor(ContextCompat.getColor(this, R.color.wind_info_color));

        for (LineDataSet dataSet : new LineDataSet[]{temperatureChartDataSet, windChartDataSet}) {
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawValues(false);
            dataSet.setDrawCircles(false);
            dataSet.setDrawFilled(true);
            dataSet.setFillAlpha(200);
            dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataSet.setHighlightEnabled(false);
        }

        XAxisTimeFormatter formatter = new XAxisTimeFormatter(PreferenceManager.getDefaultSharedPreferences(this)
                .getLong(KeyConstants.LAST_UPDATE_DATE_KEY, 0));

        for (LineChart chart : new LineChart[]{mBinding.temperatureChart, mBinding.windChart}) {
            chart.getXAxis().setValueFormatter(formatter);
            chart.getXAxis().setGranularity(1);
            chart.getXAxis().setTextSize(12f);
            chart.getXAxis().setTypeface(ResourcesCompat.getFont(this, R.font.open_sans));
            chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            chart.getXAxis().setDrawGridLines(false);
            //chart.getAxisLeft().setDrawGridLines(false);
            chart.getAxisLeft().setTextSize(12f);
            chart.getAxisLeft().enableGridDashedLine(10f, 10f, 0);
            chart.getAxisLeft().setDrawAxisLine(false);
            chart.setDrawGridBackground(false);
            chart.getDescription().setEnabled(false);
            chart.getAxisRight().setEnabled(false);
            chart.getLegend().setTextSize(16f);
            chart.getLegend().setTypeface(ResourcesCompat.getFont(this, R.font.open_sans));
            chart.setTouchEnabled(false);
        }

        mBinding.temperatureChart.setData(new LineData(temperatureChartDataSet));
        mBinding.windChart.setData(new LineData(windChartDataSet));
        mBinding.temperatureChart.invalidate();
        mBinding.windChart.invalidate();
    }
}
