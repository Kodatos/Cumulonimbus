package com.kodatos.cumulonimbus;

import android.app.ActivityManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.kodatos.cumulonimbus.databinding.GraphsActivityLayoutBinding;
import com.kodatos.cumulonimbus.uihelper.XAxisMultiLineTime;
import com.kodatos.cumulonimbus.utils.KeyConstants;

import java.util.ArrayList;
import java.util.List;

public class GraphsActivity extends AppCompatActivity {

    private GraphsActivityLayoutBinding mBinding;
    private int[] temperatures;
    private int[] winds;
    private double[] rains;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.graphs_activity_layout);
        setSupportActionBar(mBinding.toolbarGraphsActivity);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        int backgroundColor = ContextCompat.getColor(this, R.color.charts_background);
        getWindow().setStatusBarColor(backgroundColor);
        getWindow().setNavigationBarColor(backgroundColor);

        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(getString(R.string.app_name), null, backgroundColor);
        setTaskDescription(taskDescription);

        temperatures = getIntent().getIntArrayExtra(KeyConstants.TEMPERATURE_CHART_DATA);
        winds = getIntent().getIntArrayExtra(KeyConstants.WIND_CHART_DATA);
        rains = getIntent().getDoubleArrayExtra(KeyConstants.RAIN_CHART_DATA);
        createCharts();

    }

    private void createCharts() {
        List<Entry> temperatureData = new ArrayList<>();
        List<BarEntry> windData = new ArrayList<>();
        List<Entry> rainData = new ArrayList<>();
        int temperaturesLength = temperatures.length;
        for (int i = 0; i < temperaturesLength; i++) {
            temperatureData.add(new Entry(i, temperatures[i]));
            windData.add(new BarEntry(i, winds[i]));
            rainData.add(new Entry(i, (float) rains[i]));
        }

        boolean metric = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_metrics_key), true);
        String temperatureLabel = "Temperature (\u00B0" + (metric ? "C" : "F") + ")";
        String windLabel = "Wind (" + (metric ? "km/h" : "mi/h") + ")";
        String rainLabel = "Rain (mm)";
        LineDataSet temperatureChartDataSet = new LineDataSet(temperatureData, temperatureLabel);
        temperatureChartDataSet.setColor(ContextCompat.getColor(this, R.color.apparent_temperature_color));
        temperatureChartDataSet.setFillColor(ContextCompat.getColor(this, R.color.apparent_temperature_color));


        BarDataSet windChartDataSet = new BarDataSet(windData, windLabel);
        windChartDataSet.setColor(ContextCompat.getColor(this, R.color.wind_info_color));
        LineDataSet rainChartDataSet = new LineDataSet(rainData, rainLabel);
        rainChartDataSet.setColor(ContextCompat.getColor(this, R.color.rain_volume_info_color));
        rainChartDataSet.setFillColor(ContextCompat.getColor(this, R.color.rain_volume_info_color));

        for (DataSet dataSet : new DataSet[]{temperatureChartDataSet, windChartDataSet, rainChartDataSet}) {
            dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataSet.setHighlightEnabled(false);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTextSize(14f);
            if (dataSet instanceof LineDataSet) {
                LineDataSet lineDataSet = (LineDataSet) dataSet;
                lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                lineDataSet.setDrawValues(false);
                lineDataSet.setDrawCircles(false);
                lineDataSet.setDrawFilled(true);
                lineDataSet.setFillAlpha(200);
                lineDataSet.setLineWidth(2f);
            }
        }

        XAxisMultiLineTime.XAxisTimeFormatter formatter = new XAxisMultiLineTime.XAxisTimeFormatter(PreferenceManager.getDefaultSharedPreferences(this)
                .getLong(KeyConstants.LAST_UPDATE_DATE_KEY, 0));

        for (BarLineChartBase chart : new BarLineChartBase[]{mBinding.temperatureChart, mBinding.windChart, mBinding.rainChart}) {
            XAxis xAxis = chart.getXAxis();
            YAxis leftAxis = chart.getAxisLeft();
            Legend legend = chart.getLegend();
            xAxis.setValueFormatter(formatter);
            xAxis.setGranularity(1);
            xAxis.setTextSize(12f);
            xAxis.setTextColor(Color.WHITE);
            xAxis.setTypeface(ResourcesCompat.getFont(this, R.font.open_sans));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            //chart.getAxisLeft().setDrawGridLines(false);
            leftAxis.setTextSize(12f);
            leftAxis.setTextColor(Color.WHITE);
            leftAxis.enableGridDashedLine(10f, 10f, 0);
            leftAxis.setDrawAxisLine(false);
            chart.setDrawGridBackground(false);
            chart.getDescription().setEnabled(false);
            chart.getAxisRight().setEnabled(false);
            legend.setTextSize(16f);
            legend.setTypeface(ResourcesCompat.getFont(this, R.font.open_sans));
            legend.setTextColor(Color.WHITE);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            chart.setTouchEnabled(false);
            chart.setXAxisRenderer(new XAxisMultiLineTime.XAxisMultiLineRenderer(chart.getViewPortHandler(), chart.getXAxis(), chart.getTransformer(YAxis.AxisDependency.LEFT)));
        }

        mBinding.temperatureChart.setData(new LineData(temperatureChartDataSet));
        mBinding.windChart.setDrawValueAboveBar(true);
        mBinding.windChart.setData(new BarData(windChartDataSet));
        mBinding.windChart.getAxisLeft().setDrawLabels(false);
        mBinding.rainChart.setData(new LineData(rainChartDataSet));
        mBinding.rainChart.getAxisLeft().setAxisMinimum(0);
        mBinding.rainChart.getAxisLeft().setGranularity(1f);
        mBinding.temperatureChart.invalidate();
        mBinding.windChart.invalidate();
        mBinding.rainChart.invalidate();
    }
}
