package com.example.pierregignoux;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.pierregignoux.models.direction.Trajet;
import com.squareup.picasso.Picasso;

import java.util.List;


public class TrajetAdapter extends RecyclerView.Adapter<TrajetAdapter.ViewHolder> {

    private LayoutInflater layoutInflater;
    private List<Trajet> data;
    private OnTrajetListener mOnTrajetListener;
    private Context context;

    public TrajetAdapter(Context context, List<Trajet> data, OnTrajetListener onTrajetListener){

        this.layoutInflater = LayoutInflater.from(context);
        this.data=data;
        this.mOnTrajetListener = onTrajetListener;

        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = layoutInflater.inflate(R.layout.layout_trajet, viewGroup, false);
        return new ViewHolder(view,mOnTrajetListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Trajet mont = data.get(i);


        String donedate = context.getResources().getString(R.string.date_history);
        String consotrajet = context.getResources().getString(R.string.consotrajet);
        String disttrajet = context.getResources().getString(R.string.disttrajet);

        String vehicule = mont.getVehicule();
        String date = donedate+" "+mont.getDate().split(" ")[0];
        String consommation = consotrajet+" "+mont.getConsommation()+" g/CO2";
        String kilometre = disttrajet+" "+mont.getKilometre();
        String imageurl = mont.getImage();

        viewHolder.txtvehicule.setText(vehicule);
        viewHolder.txtconsommation.setText(consommation);
        viewHolder.txtdate.setText(date);
        viewHolder.txtkilometre.setText(kilometre);

        Picasso.get().load(imageurl).into(viewHolder.imgimage);


    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{

        TextView txtvehicule,txtconsommation,txtdate,txtkilometre;
        ImageView imgimage;
        OnTrajetListener onTicketListener;


        public  ViewHolder(@NonNull View itemView, OnTrajetListener onTicketListener){

            super(itemView);
            txtvehicule = itemView.findViewById(R.id.vehiculeTrajet);
            txtconsommation = itemView.findViewById(R.id.consommationTrajet);
            txtdate = itemView.findViewById(R.id.dateTrajet);
            imgimage = itemView.findViewById(R.id.imageTrajet);
            txtkilometre = itemView.findViewById(R.id.kilometreTrajet);
            this.onTicketListener = onTicketListener;

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onTicketListener.onTrajetClick(getAdapterPosition());

        }
    }

    public interface OnTrajetListener{
        void onTrajetClick(int position);
    }


}