package com.kodatos.cumulonimbus.datahelper;

import android.content.Context;
import android.database.Cursor;

import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.apihelper.DBModel;
import com.kodatos.cumulonimbus.uihelper.ForecastCalculatedData;
import com.kodatos.cumulonimbus.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

public class DBModelsAndDataPresenter {

    private ArrayList<DBModel> dbModelList;

    public DBModelsAndDataPresenter(){
        dbModelList = new ArrayList<>();
    }

    public void populateModelsFromCursor(Cursor dataCursor){
        if(dataCursor == null || dataCursor.getCount() == 0)
            return;
        invalidateModels();
        int position = 0;
        while (dataCursor.moveToNext()){
            dbModelList.add(getDBModelFromCursor(position++, dataCursor));
        }
    }

    public ArrayList<DBModel> getParcelableArrayListForSaveInstance() {
        return dbModelList;
    }

    public void populateModelsFromParcel(ArrayList<DBModel> parceledList) {
        dbModelList = parceledList;
    }

    public DBModel getCurrentWeatherModel(){
        return dbModelList.get(0);
    }

    public List<ForecastCalculatedData> getForecastModelForEachDay(Context context, boolean metric, long lastUpdatedInMillis){
        List<ForecastCalculatedData> returnList = new ArrayList<>();
        for(int i=1; i<=4; i++){
            returnList.add(MiscUtils.getForecastModelfromDBModel(dbModelList.get(i*8), context, i, metric, lastUpdatedInMillis));
        }
        return returnList;
    }

    public ArrayList<DBModel> getModelsForOneForecastDay(int day, int currentDayModelsOffset) {
        int startingRow = currentDayModelsOffset + (day * 8);
        return new ArrayList<>(dbModelList.subList(startingRow, startingRow + 8));
    }

    public List<DBModel> getAllAvailableModelsForToday(int currentDayModelOffset){
        return new ArrayList<>(dbModelList.subList(1, currentDayModelOffset));
    }

    public int[] getTemperatureChartData() {
        int[] temperatures = new int[8];
        for (int i = 0; i < 8; i++) {
            temperatures[i] = Integer.parseInt(dbModelList.get(i + 1).getTemp());
        }
        return temperatures;
    }

    public int[] getWindChartData(boolean metric) {
        int[] winds = new int[8];
        float currentWind;
        for (int i = 0; i < 8; i++) {
            currentWind = Float.parseFloat(dbModelList.get(i + 1).getWind().split("/")[0]);
            winds[i] = (int) (metric ? currentWind * 3.6 : currentWind);
        }
        return winds;
    }

    public double[] getRainChartData() {
        double rains[] = new double[8];
        for (int i = 0; i < 8; i++) {
            rains[i] = dbModelList.get(i + 1).getRain_3h();
        }
        return rains;
    }

    public String getFeelsLikeTemperatureDescription(Context context, boolean metric){
        String description;
        DBModel currentDBModel = dbModelList.get(0);
        double temperatureToConvert = Double.valueOf(currentDBModel.getTemp());
        int windChill = MiscUtils.getWindChill(temperatureToConvert, Double.valueOf(currentDBModel.getWind().split("/")[0]), metric);
        if(windChill != MiscUtils.IMPOSSIBLE_TEMPERATURE)
            description = context.getString(R.string.wind_chill_desc) + " " + MiscUtils.makeTemperaturePretty(String.valueOf(windChill), metric);
        else {
            int heatIndex = MiscUtils.getHeatIndex(temperatureToConvert, currentDBModel.getHumidity(), metric);
            if(heatIndex != MiscUtils.IMPOSSIBLE_TEMPERATURE)
                description = context.getString(R.string.heat_index_desc) + " " + MiscUtils.makeTemperaturePretty(String.valueOf(heatIndex), metric);
            else
                description = context.getString(R.string.no_apparent_temperature_desc);
        }
        return description;
    }

    public boolean isEmpty(){
        return dbModelList == null || dbModelList.size() < 33;
    }

    public void invalidateModels(){
        dbModelList.clear();
    }

    private DBModel getDBModelFromCursor(int position, Cursor dataCursor) {
        dataCursor.moveToPosition(position);
        long id = dataCursor.getLong(dataCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry._ID));
        String main = dataCursor.getString(dataCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_WEATHER_MAIN));
        String desc = dataCursor.getString(dataCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_WEATHER_DESC));
        String temp = dataCursor.getString(dataCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP));
        float temp_min = dataCursor.getFloat(dataCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP_MIN));
        float temp_max = dataCursor.getFloat(dataCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP_MAX));
        float pressure = dataCursor.getFloat(dataCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_PRESSURE));
        String wind = dataCursor.getString(dataCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_WIND));
        long humidity = dataCursor.getLong(dataCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_HUMIDITY));
        long clouds = dataCursor.getLong(dataCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_CLOUDS));
        String icon_id = dataCursor.getString(dataCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_ICON_ID));
        double uvIndex = dataCursor.getDouble(dataCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_UV_INDEX));
        double rain_3h = dataCursor.getDouble(dataCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_RAIN_3H));
        return new DBModel(id, main, desc, temp, temp_min, temp_max, pressure, humidity, wind, clouds, icon_id, uvIndex, rain_3h);
    }
}
