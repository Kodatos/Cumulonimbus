package com.kodatos.cumulonimbus.uihelper;

import android.graphics.Canvas;
import android.text.TextUtils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class XAxisMultiLineTime {
    /* Class to format integers to 3 hour intervals on the X Axis*/
    public static class XAxisTimeFormatter implements IAxisValueFormatter {

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
            if (formatted.length() > 6)
                return TextUtils.join("\n", formatted.split(" "));
            return formatted;
        }
    }

    /* Class to enable X Axis labels to wrap to the next line*/
    public static class XAxisMultiLineRenderer extends XAxisRenderer {

        public XAxisMultiLineRenderer(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans) {
            super(viewPortHandler, xAxis, trans);
        }

        @Override
        protected void drawLabel(Canvas c, String formattedLabel, float x, float y, MPPointF anchor, float angleDegrees) {
            String lines[] = formattedLabel.split("\n");
            Utils.drawXAxisValue(c, lines[0], x, y, mAxisLabelPaint, anchor, angleDegrees);
            if (lines.length == 2)
                Utils.drawXAxisValue(c, lines[1], x, y + mAxisLabelPaint.getTextSize(), mAxisLabelPaint, anchor, angleDegrees);
        }
    }
}
