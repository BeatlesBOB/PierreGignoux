package com.example.pierregignoux;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;


import android.view.LayoutInflater;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import com.squareup.picasso.Picasso;

import java.util.List;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private LayoutInflater layoutInflater;
    private List<Vehicule> data;
    private OnVehiculeListener mOnVehiculeListener;

    public ImageAdapter(Context context, List<Vehicule> data, OnVehiculeListener onVehiculeListener){

        this.layoutInflater = LayoutInflater.from(context);
        this.data=data;
        this.mOnVehiculeListener = onVehiculeListener;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = layoutInflater.inflate(R.layout.layout_vehicule, viewGroup, false);
        return new ViewHolder(view,mOnVehiculeListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Vehicule mont = data.get(i);
        String titre = mont.getTitre();
        String imageurl = mont.getImage();
        Log.d("TEST",imageurl);
        String calculeconso = mont.getConsocalcule();
        String methode = mont.getMethode();
        Picasso.get().load(imageurl).into(viewHolder.imgVehicule);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{

        ImageView imgVehicule;
        OnVehiculeListener onVehiculeListener;

        public  ViewHolder(@NonNull View itemView, OnVehiculeListener onVehiculeListener){

            super(itemView);
            imgVehicule = itemView.findViewById(R.id.imageVehicule);
            this.onVehiculeListener = onVehiculeListener;

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onVehiculeListener.onVehiculeClick(getAdapterPosition());

        }
    }

    public interface OnVehiculeListener{
        void onVehiculeClick(int position);
    }


}