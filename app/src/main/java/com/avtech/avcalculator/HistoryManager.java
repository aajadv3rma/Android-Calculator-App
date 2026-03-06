package com.avtech.avcalculator;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * HistoryManager - Manages calculation history using SharedPreferences
 */
public class HistoryManager {

    private static final String PREFS_NAME = "av_calculator_history";
    private static final String KEY_HISTORY = "calculation_history";
    private static final int MAX_HISTORY_SIZE = 100;

    private SharedPreferences preferences;
    private Gson gson;

    /**
     * Data class for storing calculation history
     */
    public static class CalculationHistory {
        public String expression;
        public String result;
        public long timestamp;
        public String formattedTime;

        public CalculationHistory(String expression, String result, long timestamp) {
            this.expression = expression;
            this.result = result;
            this.timestamp = timestamp;
            this.formattedTime = formatTimestamp(timestamp);
        }

        private static String formatTimestamp(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    public HistoryManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Add a new calculation to history
     */
    public void addCalculation(String expression, String result) {
        List<CalculationHistory> history = getHistory();

        CalculationHistory newItem = new CalculationHistory(
                expression,
                result,
                System.currentTimeMillis()
        );

        history.add(0, newItem);

        while (history.size() > MAX_HISTORY_SIZE) {
            history.remove(history.size() - 1);
        }

        saveHistory(history);
    }

    /**
     * Get all calculation history
     */
    public List<CalculationHistory> getHistory() {
        String json = preferences.getString(KEY_HISTORY, null);

        if (json == null) {
            return new ArrayList<>();
        }

        try {
            Type listType = new TypeToken<ArrayList<CalculationHistory>>() {}.getType();
            List<CalculationHistory> history = gson.fromJson(json, listType);
            return history != null ? history : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Save history to SharedPreferences
     */
    private void saveHistory(List<CalculationHistory> history) {
        String json = gson.toJson(history);
        preferences.edit().putString(KEY_HISTORY, json).apply();
    }

    /**
     * Clear all calculation history
     */
    public void clearHistory() {
        preferences.edit().remove(KEY_HISTORY).apply();
    }

    /**
     * Get the count of history items
     */
    public int getHistoryCount() {
        return getHistory().size();
    }
}