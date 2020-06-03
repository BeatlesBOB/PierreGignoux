package com.nathanael.pierregignoux;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;


import android.view.LayoutInflater;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import com.squareup.picasso.Picasso;

import org.mariuszgromada.math.mxparser.Expression;

import java.util.List;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private LayoutInflater layoutInflater;
    private List<Vehicule> data;
    private OnVehiculeListener mOnVehiculeListener;
    private Context context;

    public ImageAdapter(Context context, List<Vehicule> data, OnVehiculeListener onVehiculeListener){

        this.layoutInflater = LayoutInflater.from(context);
        this.data=data;
        this.mOnVehiculeListener = onVehiculeListener;
        this.context = context;

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

        if (calculeconso.contains("Y")){
            String interconsocalcule = calculeconso.replace("X", "1");
            String inter2consocalcule =interconsocalcule.replace("Y","2");
            Expression expressionconso = new Expression(inter2consocalcule);
            double inter = expressionconso.calculate();

                double result = inter / 10;
                int roundofresult = (int) Math.floor(result);
                String ofresult = ""+roundofresult;

                viewHolder.txtconso.setText(ofresult);

                if (result < 50){
                    viewHolder.txtconso.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                }else if (result < 100){
                    viewHolder.txtconso.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
                }else {
                    viewHolder.txtconso.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
                }



        }else {
            String interconsocalcule = calculeconso.replace("X", "1");
            Expression expressionconso = new Expression(interconsocalcule);
            double inter = expressionconso.calculate();


                double result = inter / 10;
                int roundofresult = (int) Math.round(result);
                String ofresult = ""+roundofresult;

                viewHolder.txtconso.setText(ofresult);

                if (result < 50){
                    viewHolder.txtconso.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                }else if (result < 100){
                    viewHolder.txtconso.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
                }else {
                    viewHolder.txtconso.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
                }





        }
//        String intercompar = viewHolder.txtconso.getText().toString();
//        Double finalcompar = Double.parseDouble(intercompar);
//
//        if (finalcompar < 50){
//            viewHolder.txtconso.setBackgroundColor(context.getResources().getColor(R.color.colorGreen));
//        }else if ( finalcompar < 100){
//            viewHolder.txtconso.setBackgroundColor(context.getResources().getColor(R.color.colorOrange));
//        }else {
//            viewHolder.txtconso.setBackgroundColor(context.getResources().getColor(R.color.colorRed));
//        }
        String methode = mont.getMethode();
        Picasso.get().load(imageurl).into(viewHolder.imgVehicule);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{

        TextView txtconso;
        ImageView imgVehicule;
        OnVehiculeListener onVehiculeListener;

        public  ViewHolder(@NonNull View itemView, OnVehiculeListener onVehiculeListener){

            super(itemView);
            imgVehicule = itemView.findViewById(R.id.imageVehicule);
            txtconso = itemView.findViewById(R.id.ponderation);
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