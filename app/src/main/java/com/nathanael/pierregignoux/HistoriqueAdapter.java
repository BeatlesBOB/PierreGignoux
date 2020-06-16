package com.nathanael.pierregignoux;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.nathanael.pierregignoux.models.direction.Historique;

import java.util.List;


public class HistoriqueAdapter extends RecyclerView.Adapter<HistoriqueAdapter.ViewHolder> {

    private LayoutInflater layoutInflater;
    private List<Historique> data;
    private OnHistoriqueListener mOnHistoriqueListener;
    private Context context;

    public HistoriqueAdapter(Context context, List<Historique> data, OnHistoriqueListener onHistoriqueListener){
        this.layoutInflater = LayoutInflater.from(context);
        this.data=data;
        this.mOnHistoriqueListener = onHistoriqueListener;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = layoutInflater.inflate(R.layout.layout_historique, viewGroup, false);
        return new ViewHolder(view,mOnHistoriqueListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Historique mont = data.get(i);

        String date = mont.getDate();


        double consommation = mont.getConsommation();


            if (consommation/1000000 >= 1){
                consommation = consommation/1000000;
                viewHolder.txtconsommation.setText(Math.round(consommation)+" t/CO2");

            }else if(consommation/1000 >= 1){
                consommation = consommation/1000;
                viewHolder.txtconsommation.setText(Math.round(consommation)+" kg/CO2");
            }else {
                viewHolder.txtconsommation.setText(Math.round(consommation)+" g/CO2");
            }

        viewHolder.txtkilometre.setText(mont.getKilometre()+" km");

        String economie = mont.getEconomie();

        double doubleEco = Double.parseDouble(economie);

        if (doubleEco/1000000 >= 1){
            doubleEco = doubleEco/1000000;
            viewHolder.txteconomie.setText(Math.round(doubleEco)+" t/CO2");

        }else if(doubleEco/1000 >= 1){
            doubleEco = doubleEco/1000;
            viewHolder.txteconomie.setText(Math.round(doubleEco)+" kg/CO2");
        }else {
            viewHolder.txteconomie.setText(Math.round(doubleEco)+" g/CO2");
        }

        viewHolder.txtdate.setText(date);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener,View.OnLongClickListener{

        TextView txtconsommation,txtdate,txtkilometre, txteconomie;
        OnHistoriqueListener onHistoriqueListener;


        public  ViewHolder(@NonNull View itemView, OnHistoriqueListener onHistoriqueListener){

            super(itemView);

            txtconsommation = itemView.findViewById(R.id.txtConsommation);
            txtdate = itemView.findViewById(R.id.txtMois);
            txtkilometre = itemView.findViewById(R.id.txtkilometre);
            txteconomie = itemView.findViewById(R.id.txtEconomie);

            this.onHistoriqueListener = onHistoriqueListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onHistoriqueListener.onHistoriqueClick(data.get(getAdapterPosition()),getAdapterPosition());

        }


        @Override
        public boolean onLongClick(View v) {
            onHistoriqueListener.onHistoriqueLongClick(data.get(getAdapterPosition()),getAdapterPosition());
            return false;
        }
    }

    public interface OnHistoriqueListener{
        void onHistoriqueClick(Historique historique,int position);
        void onHistoriqueLongClick(Historique historique,int position);

    }




}