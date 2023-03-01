package com.example.checkup_android;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlantsAdapter extends RecyclerView.Adapter<PlantsAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final List<Plants> plants;

    PlantsAdapter(Context context, List<Plants> plants) {
        this.plants = plants;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public PlantsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.plants_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlantsAdapter.ViewHolder holder, int position) {
        Plants plants = this.plants.get(position);

        holder.iconView.setImageResource(plants.getIconResource());
        holder.plantView.setText(plants.getPlant_name());
        holder.nfcserialView.setText(plants.getNfc_serial());
    }

    @Override
    public int getItemCount() {
        return plants.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView iconView;
        final TextView plantView, nfcserialView;
        ViewHolder(View view){
            super(view);
//            activeView = view.findViewById(R.id.imageViewIcon);
            iconView = view.findViewById(R.id.imageViewIcon);
            plantView = view.findViewById(R.id.plant_name);
            nfcserialView = view.findViewById(R.id.nfc_serial);
        }
    }
}
