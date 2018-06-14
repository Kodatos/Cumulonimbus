package com.kodatos.cumulonimbus.utils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.kodatos.cumulonimbus.uihelper.ForecastCalculatedData;

import java.util.List;
import java.util.Objects;

public class AdapterDiffUtils {

    public static class MainRVDiffCallback extends DiffUtil.Callback{

        List<ForecastCalculatedData> oldData;
        List<ForecastCalculatedData> newData;

        public MainRVDiffCallback(List<ForecastCalculatedData> oldData, List<ForecastCalculatedData> newData) {
            this.oldData = oldData;
            this.newData = newData;
        }

        @Override
        public int getOldListSize() {
            return oldData != null ? oldData.size() : 0;
        }

        @Override
        public int getNewListSize() {
            return newData != null ? newData.size() : 0;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return Objects.equals(oldData.get(oldItemPosition).getCalculatedDay(), newData.get(newItemPosition).getCalculatedDay());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return Objects.equals(oldData.get(oldItemPosition), newData.get(newItemPosition));
        }
    }

    public static class TimelineRVDiffCallback extends DiffUtil.Callback{

        List<String> oldIconIds;
        List<String> oldTemperatures;
        List<String> newIconIds;
        List<String> newTemperatures;

        public TimelineRVDiffCallback(List<String> oldIconIds, List<String> oldTemperatures, List<String> newIconIds, List<String> newTemperatures) {
            this.oldIconIds = oldIconIds;
            this.oldTemperatures = oldTemperatures;
            this.newIconIds = newIconIds;
            this.newTemperatures = newTemperatures;
        }

        @Override
        public int getOldListSize() {
            return oldIconIds != null ? oldIconIds.size() : 0;
        }

        @Override
        public int getNewListSize() {
            return newIconIds != null ? newIconIds.size() : 0;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldIconIds.size() == newIconIds.size();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return Objects.equals(oldIconIds.get(oldItemPosition), newIconIds.get(newItemPosition))
                    && Objects.equals(oldTemperatures.get(oldItemPosition), newTemperatures.get(newItemPosition));
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            String oldIconID = oldIconIds.get(oldItemPosition);
            String oldTemperature = oldTemperatures.get(oldItemPosition);
            String newIconID = newIconIds.get(newItemPosition);
            String newTemperature = newTemperatures.get(newItemPosition);

            Bundle payload = new Bundle();
            if(!Objects.equals(oldIconID, newIconID))
                payload.putString("icon_id", newIconID);
            if(!Objects.equals(oldTemperature, newTemperature))
                payload.putString("temperature", newTemperature);
            if(payload.size() == 0)
                return null;
            return payload;
        }
    }

}
