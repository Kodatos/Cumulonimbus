package com.kodatos.cumulonimbus.uihelper.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kodatos.cumulonimbus.databinding.LocationRecyclerviewItemBinding;

import java.util.List;

public class LocationRecyclerViewAdapter extends RecyclerView.Adapter<LocationRecyclerViewAdapter.LocationRecyclerViewHolder> {

    private List<String> locationList;
    private int selectedPosition;
    private LocationClickListener locationClickListener;

    public LocationRecyclerViewAdapter(List<String> locationList, int selectedPosition, LocationClickListener locationClickListener) {
        this.locationList = locationList;
        this.selectedPosition = selectedPosition;
        this.locationClickListener = locationClickListener;
    }

    private void updateSelectedPosition(int newPosition, boolean forceUpdate) {
        if (selectedPosition == newPosition && !forceUpdate)
            return;
        int previous = selectedPosition;
        selectedPosition = newPosition;
        locationClickListener.onLocationClick(locationList.get(newPosition));
        notifyItemChanged(previous);
        notifyItemChanged(selectedPosition);
    }

    @NonNull
    @Override
    public LocationRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LocationRecyclerviewItemBinding binding = LocationRecyclerviewItemBinding.inflate(inflater, parent, false);
        LocationRecyclerViewHolder holder = new LocationRecyclerViewHolder(binding);
        holder.mBinding.getRoot().setOnClickListener(v -> updateSelectedPosition(holder.getAdapterPosition(), false));
        return holder;
    }

    public String removeLocation(int removedPosition) {
        if (removedPosition == selectedPosition && removedPosition == locationList.size() - 1) {
            //Handle case if last location was selected and is removed
            updateSelectedPosition(selectedPosition - 1, true);
        }
        String removedLocation = locationList.remove(removedPosition);
        notifyItemRemoved(removedPosition);
        if (!locationList.isEmpty() && removedPosition <= selectedPosition) {
            int newSelectedPosition = selectedPosition;
            if (removedPosition < selectedPosition) {
                newSelectedPosition--;
            }
            updateSelectedPosition(newSelectedPosition, true);
        }
        return removedLocation;
    }


    public void unregisterCallback() {
        locationClickListener = null;
    }

    @Override
    public void onBindViewHolder(@NonNull LocationRecyclerViewHolder holder, int position) {
        holder.bind(locationList.get(position), position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return locationList != null ? locationList.size() : 0;
    }

    public void addLocation(String location) {
        locationList.add(location);
        notifyItemInserted(locationList.size() - 1);
    }

    public interface LocationClickListener {
        void onLocationClick(String key);
    }

    static class LocationRecyclerViewHolder extends RecyclerView.ViewHolder {

        LocationRecyclerviewItemBinding mBinding;

        LocationRecyclerViewHolder(LocationRecyclerviewItemBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        void bind(String location, boolean isCurrentlySelected) {
            String[] locationLevels = location.split(", ", 2);
            mBinding.level1TextView.setText(locationLevels[0]);
            mBinding.level2TextView.setText(locationLevels[1]);
            if (isCurrentlySelected)
                mBinding.checkedImageView.setVisibility(View.VISIBLE);
            else
                mBinding.checkedImageView.setVisibility(View.GONE);
        }
    }
}
