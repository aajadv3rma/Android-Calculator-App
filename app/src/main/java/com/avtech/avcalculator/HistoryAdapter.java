package com.avtech.avcalculator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * HistoryAdapter - RecyclerView adapter for displaying calculation history
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryManager.CalculationHistory> historyList;
    private OnHistoryItemClickListener listener;

    public interface OnHistoryItemClickListener {
        void onItemClick(HistoryManager.CalculationHistory item);
    }

    public HistoryAdapter(List<HistoryManager.CalculationHistory> historyList,
                          OnHistoryItemClickListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryManager.CalculationHistory item = historyList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        private TextView tvExpression;
        private TextView tvResult;
        private TextView tvTime;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpression = itemView.findViewById(R.id.tvHistoryExpression);
            tvResult = itemView.findViewById(R.id.tvHistoryResult);
            tvTime = itemView.findViewById(R.id.tvHistoryTime);
        }

        public void bind(HistoryManager.CalculationHistory item,
                         OnHistoryItemClickListener listener) {
            tvExpression.setText(item.expression);
            tvResult.setText("= " + item.result);
            tvTime.setText(item.formattedTime);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}