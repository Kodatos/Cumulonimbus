package com.kodatos.cumulonimbus.uihelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.kodatos.cumulonimbus.R;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public abstract class SwipeToDeleteHelper extends ItemTouchHelper.SimpleCallback {

    private Drawable deleteIcon;
    private ColorDrawable background;
    private Paint clearPaint;

    public SwipeToDeleteHelper(Context context) {
        super(0, ItemTouchHelper.LEFT);
        background = new ColorDrawable();
        background.setColor(ContextCompat.getColor(context, R.color.apparent_temperature_color));
        deleteIcon = context.getDrawable(R.drawable.ic_delete);
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        final View itemView = viewHolder.itemView;
        boolean isCanceled = dX == 0 && !isCurrentlyActive;
        if (isCanceled) {
            c.drawRect(itemView.getLeft() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), clearPaint);
        } else {
            background.setBounds(itemView.getLeft() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            background.draw(c);

            final int deleteIconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
            final int deleteIconTop = itemView.getTop() + deleteIconMargin;
            final int deleteIconBottom = deleteIconTop + deleteIcon.getIntrinsicHeight();
            final int deleteIconRight = itemView.getRight() - deleteIconMargin;
            final int deleteIconLeft = deleteIconRight - deleteIcon.getIntrinsicWidth();

            deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
            deleteIcon.draw(c);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

}
