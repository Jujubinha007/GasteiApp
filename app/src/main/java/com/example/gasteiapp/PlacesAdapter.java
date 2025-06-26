package com.example.gasteiapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder> {
    
    private List<Place> places;
    private OnPlaceEditListener editListener;
    private OnPlaceDeleteListener deleteListener;
    
    public interface OnPlaceEditListener {
        void onEdit(Place place);
    }
    
    public interface OnPlaceDeleteListener {
        void onDelete(Place place);
    }
    
    public PlacesAdapter(List<Place> places, OnPlaceEditListener editListener, OnPlaceDeleteListener deleteListener) {
        this.places = places;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }
    
    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = places.get(position);
        holder.bind(place);
    }
    
    @Override
    public int getItemCount() {
        return places.size();
    }
    
    class PlaceViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPlaceName;
        private TextView tvCoordinates;
        private ImageButton btnEdit;
        private ImageButton btnDelete;
        
        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvCoordinates = itemView.findViewById(R.id.tvCoordinates);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
        
        public void bind(Place place) {
            tvPlaceName.setText(place.getName());
            tvCoordinates.setText(String.format("%.6f, %.6f", place.getLatitude(), place.getLongitude()));
            
            btnEdit.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onEdit(place);
                }
            });
            
            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(place);
                }
            });
        }
    }
} 