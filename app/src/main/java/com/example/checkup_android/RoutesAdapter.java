package com.example.checkup_android;
// for RecyclerView

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final List<Routes> routes;
    TextView choiceView = null;
    VarsSingleton vars = VarsSingleton.getInstance();
    Button button_start_check;

    RoutesAdapter(Context context, List<Routes> routes, Button btn) {
        this.routes = routes;
        this.inflater = LayoutInflater.from(context);
        this.button_start_check = btn;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.routes_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Routes routes = this.routes.get(position);
        holder.routeView.setText(routes.getRoute_name());

        holder.routeView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                button_start_check.setEnabled(true);
                if (choiceView == null){
                    choiceView = holder.routeView;
                }
;                choiceView.setBackgroundColor(0);
                holder.routeView.setBackgroundResource(R.color.choice);
                choiceView = holder.routeView;
                vars.setStrVars("route_name", (String) choiceView.getText());
            }
        });
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView routeView;

        public ViewHolder(View itemView){
            super(itemView);
            routeView = itemView.findViewById(R.id.route_name);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int posItem = getAdapterPosition();
//                    if (choiceView == null){
//                        choiceView = routeView;
//                    }
//                    choiceView.setBackgroundColor(0);
//                    routeView.setBackgroundResource(R.color.choice);
//                    choiceView = routeView;
//                    vars.setStrVars("route_name", (String) choiceView.getText());
//                }
//            });
        }
    }
}
