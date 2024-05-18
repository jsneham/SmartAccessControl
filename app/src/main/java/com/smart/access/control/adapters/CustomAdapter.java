package com.smart.access.control.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.smart.access.control.R;
import com.smart.access.control.modals.SettingsMenu;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private ArrayList<SettingsMenu> localDataSet;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SettingsMenu item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle, tvDetails;
        public ImageView ivIcon;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            tvDetails = view.findViewById(R.id.tvDetails);
            tvTitle = view.findViewById(R.id.tvTitle);
            ivIcon = view.findViewById(R.id.ivIcon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(localDataSet.get(position));
                        }
                    }
                }
            });
        }


    }


    public CustomAdapter(ArrayList<SettingsMenu> dataSet) {
        localDataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_setting_row, viewGroup, false);

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {


        SettingsMenu sm = localDataSet.get(position);
        viewHolder.tvDetails.setText(sm.getDescription());
        viewHolder.tvTitle.setText(sm.getTitle());
        viewHolder.ivIcon.setImageResource(sm.getIcon());
    }


    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}

