package com.avtech.avcalculator;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * HistoryActivity - Displays calculation history
 */
public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerHistory;
    private LinearLayout emptyState;
    private ImageButton btnBack;
    private ImageButton btnClearHistory;

    private HistoryManager historyManager;
    private HistoryAdapter historyAdapter;
    private List<HistoryManager.CalculationHistory> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initializeViews();
        setupHistoryManager();
        setupRecyclerView();
        loadHistory();
        setupListeners();
    }

    private void initializeViews() {
        recyclerHistory = findViewById(R.id.recyclerHistory);
        emptyState = findViewById(R.id.emptyState);
        btnBack = findViewById(R.id.btnBack);
        btnClearHistory = findViewById(R.id.btnClearHistory);
    }

    private void setupHistoryManager() {
        historyManager = new HistoryManager(this);
        historyList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter(historyList, this::onHistoryItemClick);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistory.setAdapter(historyAdapter);
    }

    private void loadHistory() {
        historyList.clear();
        List<HistoryManager.CalculationHistory> history = historyManager.getHistory();

        // Reverse to show newest first
        Collections.reverse(history);
        historyList.addAll(history);

        historyAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnClearHistory.setOnClickListener(v -> showClearHistoryDialog());
    }

    private void onHistoryItemClick(HistoryManager.CalculationHistory item) {
        setResult(RESULT_OK);
        finish();
    }

    private void showClearHistoryDialog() {
        if (historyList.isEmpty()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to delete all calculation history?")
                .setPositiveButton("Delete", (dialog, which) -> clearAllHistory())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAllHistory() {
        historyManager.clearHistory();
        historyList.clear();
        historyAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (historyList.isEmpty()) {
            recyclerHistory.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            btnClearHistory.setVisibility(View.GONE);
        } else {
            recyclerHistory.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            btnClearHistory.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
