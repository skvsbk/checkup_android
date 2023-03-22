package com.example.checkup_android;
// for RecyclerView

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CheckupsAdapter extends RecyclerView.Adapter<CheckupsAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final List<Checkups> checkups;

    public CheckupsAdapter(Context context, List<Checkups> checkups) {
        this.inflater = LayoutInflater.from(context);
        this.checkups = checkups;
    }



    @Override
    public CheckupsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.checkups_item, parent, false);
        return new CheckupsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CheckupsAdapter.ViewHolder holder, int position) {
        Checkups checkups = this.checkups.get(position);
        holder.dateView.setText(checkups.getDate());
        holder.facilityView.setText(checkups.getFacilityName());
        holder.routeView.setText(checkups.getRouteName());
        if (checkups.getComplete()) {
            holder.layoutCheckup.setBackgroundResource(R.color.green_lihgt);
        } else {
            holder.layoutCheckup.setBackgroundResource(R.color.red_light);
        }

    }

    @Override
    public int getItemCount() {
        return checkups.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView dateView, facilityView, routeView;
        final View layoutCheckup;

        ViewHolder(View view){
            super(view);
            dateView = view.findViewById(R.id.textView_date);
            facilityView = view.findViewById(R.id.textView_facility);
            routeView = view.findViewById(R.id.textView_route);
            layoutCheckup = view.findViewById(R.id.layoutChechup);

        }
    }
}
