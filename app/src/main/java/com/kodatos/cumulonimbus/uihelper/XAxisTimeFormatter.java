package com.kodatos.cumulonimbus.uihelper;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class XAxisTimeFormatter implements IAxisValueFormatter {

    private Calendar mCalendar;
    private DateFormat mSdf;

    public XAxisTimeFormatter(long lastUpdated) {
        mCalendar = new GregorianCalendar();
        mCalendar.setTime(new Date(lastUpdated));
        mCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        int startingPosition = (mCalendar.get(Calendar.HOUR_OF_DAY) / 3) + 1;
        mCalendar.set(Calendar.HOUR_OF_DAY, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.add(Calendar.HOUR_OF_DAY, startingPosition * 3);
        mSdf = DateFormat.getTimeInstance(DateFormat.SHORT);
        mSdf.setTimeZone(TimeZone.getDefault());
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        mCalendar.add(Calendar.HOUR, (int) (3 * value));
        String formatted = mSdf.format(mCalendar.getTime());
        mCalendar.add(Calendar.HOUR, (int) (-3 * value));
        return formatted;
    }

}
