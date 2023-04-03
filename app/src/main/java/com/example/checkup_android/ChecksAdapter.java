package com.example.checkup_android;
// for RecyclerView


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChecksAdapter extends RecyclerView.Adapter<ChecksAdapter.ViewHolder> {

    List<Checks> checksList;
    VarsSingleton vars = VarsSingleton.getInstance();

    SendButtonListener sendButtonListener;

    public ChecksAdapter(List<Checks> checksList, SendButtonListener sendButtonListener) {
        this.checksList = checksList;
        this.sendButtonListener = sendButtonListener;
    }

    public interface SendButtonListener {
        void onSendButtonClick(int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checks_item, parent, false);
        return new ViewHolder(view, sendButtonListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Checks checks = checksList.get(position);
        holder.plantTitleTxt.setText(checks.getPlant_name());
        holder.plantDescrTxt.setText(checks.getPlant_descr());
        holder.plantDescrParamsTxt.setText(checks.getPlant_descr_params());
        holder.nfcLinkedTxt.setText(checks.getNfc_serial());
        holder.nfcReadTxt.setText(checks.getNfc_read());
        holder.textParamNameTxt.setText(checks.getVal_name());

        holder.textParamMinTxt.setText(String.valueOf(checks.getVal_min()));
        holder.editTextParamTxt.setText("");
        holder.textParamMaxTxt.setText(String.valueOf(checks.getVal_max()));
        holder.textUnitNameTxt.setText(checks.getUnit_name());
        holder.editTextСommentTxt.setText("");
        holder.imageViewCheckIcon.setImageResource(0);

        // Hide paramsLayout if paramName is null
        if (holder.textParamMaxTxt.getText().equals("null")) {
            holder.paramsLayout.setVisibility(View.GONE);
        } else {
            holder.paramsLayout.setVisibility(View.VISIBLE);
        }
        // The button is always inactive until the NFC is read and matches
        // **** debug=1, prod=0
        if (vars.getIntvars("debug") == 0) {
            holder.buttonSendChecks.setEnabled(false);
        }
        // ****

        // If nfc_read is and equals nfc_linked (from db), then make background green, else - red
        int colorGreen = ContextCompat.getColor(holder.nfcLinkedTxt.getContext(), R.color.green_lihgt);
        int colorRed = ContextCompat.getColor(holder.nfcLinkedTxt.getContext(), R.color.red_light);
        if (!holder.nfcReadTxt.getText().equals("")) {
            if (holder.nfcReadTxt.getText().equals(holder.nfcLinkedTxt.getText())) {
                holder.nfcLinkedTxt.setBackgroundColor(colorGreen);
                holder.buttonSendChecks.setEnabled(true);
            } else {
                holder.nfcLinkedTxt.setBackgroundColor(colorRed);
            }
        }

        // Expand current item and collapse successfully checked
        boolean isExpandable = checksList.get(position).isExpandable();
        if (isExpandable) {
            holder.expandableLayout.setVisibility(View.VISIBLE);
            holder.imageViewCheckIcon.setImageResource(R.drawable.red_right_arrow);;
        } else {
            holder.expandableLayout.setVisibility(View.GONE);
        }

        // Markup successfully checked
        boolean isSend = checksList.get(position).isSend();
        if (isSend) {
            holder.imageViewCheckIcon.setImageResource(R.drawable.check_mark_icon);;
        }
    }

    @Override
    public int getItemCount() {
        return checksList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView plantTitleTxt, nfcLinkedTxt, nfcReadTxt, textParamNameTxt, textParamMinTxt;
        final TextView editTextParamTxt, textParamMaxTxt, textUnitNameTxt, editTextСommentTxt;
        final TextView plantDescrTxt, plantDescrParamsTxt;
        final ImageView imageViewCheckIcon;
        final Button buttonSendChecks;
        ConstraintLayout linearLayout;
        RelativeLayout expandableLayout;
        ConstraintLayout paramsLayout;

        SendButtonListener sendButtonListener;

        public ViewHolder(@NonNull View itemView, SendButtonListener sendButtonListener) {
            super(itemView);

            this.sendButtonListener = sendButtonListener;

            int pos = 0;
            plantTitleTxt = itemView.findViewById(R.id.plantTitle);
            nfcLinkedTxt = itemView.findViewById(R.id.nfcLinked);
            nfcReadTxt = itemView.findViewById(R.id.nfcRead);
            textParamNameTxt = itemView.findViewById(R.id.textParamName);
            textParamMinTxt = itemView.findViewById(R.id.textParamMin);
            editTextParamTxt = itemView.findViewById(R.id.editTextParam);
            textParamMaxTxt = itemView.findViewById(R.id.textParamMax);
            textUnitNameTxt = itemView.findViewById(R.id.textUnitName);
            editTextСommentTxt = itemView.findViewById(R.id.editTextСomment);
            imageViewCheckIcon = itemView.findViewById(R.id.imageViewCheckIcon);

            linearLayout = itemView.findViewById(R.id.linear_layout);
            expandableLayout = itemView.findViewById(R.id.expandable_layout);
            paramsLayout = itemView.findViewById(R.id.layoutParams);

            plantDescrTxt= itemView.findViewById(R.id.plantDescr);
            plantDescrParamsTxt= itemView.findViewById(R.id.plantDescrParams);

            buttonSendChecks = itemView.findViewById(R.id.buttonSendChecks);

            // Expand first
            if (!checksList.get(0).isSend()){
                checksList.get(0).setExpandable(true);
                vars.setIntVars("current_position", 0);
            }
            // Click handler in CheckupActivity thru interface SendButtonListener
            buttonSendChecks.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAbsoluteAdapterPosition();
            Checks checks = checksList.get(position);
            if (editTextСommentTxt.getText().toString().equals("")){
                checks.setNote(null);
            } else {
                checks.setNote(editTextСommentTxt.getText().toString());
            }
            if (editTextParamTxt.getText().toString().equals("")) {
                checks.setVal_fact(null);
            } else {
                checks.setVal_fact(Float.valueOf(editTextParamTxt.getText().toString()));
            }
            sendButtonListener.onSendButtonClick(position);
        }
    }
}