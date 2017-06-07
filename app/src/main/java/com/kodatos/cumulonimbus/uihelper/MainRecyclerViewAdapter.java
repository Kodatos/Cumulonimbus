package com.kodatos.cumulonimbus.uihelper;


import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kodatos.cumulonimbus.apihelper.DBModel;
import com.kodatos.cumulonimbus.databinding.TestRecycleItemBinding;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.MainRecyclerViewHolder>{

    private Cursor mCursor = null;

    public class MainRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final TestRecycleItemBinding binding;

        public MainRecyclerViewHolder(TestRecycleItemBinding testRecycleItemBinding) {
            super(testRecycleItemBinding.getRoot());
            this.binding = testRecycleItemBinding;
        }

        public void bind(DBModel dbModel){
            binding.setDbmodel(dbModel);
            binding.executePendingBindings();
        }
    }

    @Override
    public MainRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        TestRecycleItemBinding testRecycleItemBinding = TestRecycleItemBinding.inflate(layoutInflater, parent, false);
        return new MainRecyclerViewHolder(testRecycleItemBinding);
    }

    @Override
    public void onBindViewHolder(MainRecyclerViewHolder holder, int position) {
        DBModel dbModel = getDBModelFromCursor(position);
        holder.bind(dbModel);
    }

    @Override
    public int getItemCount() {
        if(null==mCursor)
            return 0;
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(0);
    }

    private DBModel getDBModelFromCursor(int position) {
        mCursor.moveToPosition(position);
        long id = mCursor.getLong(0);
        String main = mCursor.getString(1);
        String desc = mCursor.getString(2);
        float temp = mCursor.getFloat(3);
        float temp_min = mCursor.getFloat(4);
        float temp_max = mCursor.getFloat(5);
        float pressure = mCursor.getFloat(6);
        String wind = mCursor.getString(7);
        long humidity = mCursor.getLong(8);
        return new DBModel(id,main,desc,temp,temp_min,temp_max,pressure,humidity,wind);
    }

    public void swapCursor(Cursor newCursor){
        mCursor = newCursor;
        notifyDataSetChanged();
    }
}
