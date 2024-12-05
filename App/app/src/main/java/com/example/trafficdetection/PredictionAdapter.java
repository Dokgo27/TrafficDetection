package com.example.trafficdetection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trafficdetection.dto.PredictionDto;

import java.util.List;

public class PredictionAdapter extends RecyclerView.Adapter<PredictionAdapter.ViewHolder> {

    private List<PredictionDto> predictions;

    public PredictionAdapter(List<PredictionDto> predictions) {
        this.predictions = predictions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_prediction_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PredictionDto prediction = predictions.get(position);
        holder.tvPrediction.setText(prediction.getPrediction());
        holder.tvConfidence.setText(String.valueOf(prediction.getConfidence()));
        holder.tvTimestamp.setText(prediction.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return predictions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvPrediction, tvConfidence, tvTimestamp;

        public ViewHolder(View itemView) {
            super(itemView);
            tvPrediction = itemView.findViewById(R.id.tvPrediction);
            tvConfidence = itemView.findViewById(R.id.tvConfidence);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}

