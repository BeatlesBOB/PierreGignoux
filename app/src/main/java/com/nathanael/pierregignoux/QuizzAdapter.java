package com.nathanael.pierregignoux;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;


public class QuizzAdapter extends RecyclerView.Adapter<QuizzAdapter.ViewHolder> {

    private LayoutInflater layoutInflater;
    private List<Quizz> data;
    private OnQuizzListener mOnQuizzListener;
    private Context context;


    public QuizzAdapter(Context context, List<Quizz> data, OnQuizzListener onQuizzListener){

        this.layoutInflater = LayoutInflater.from(context);
        this.data=data;
        this.mOnQuizzListener = onQuizzListener;
        this.context = context;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = layoutInflater.inflate(R.layout.layout_quizz, viewGroup, false);
        return new ViewHolder(view,mOnQuizzListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Quizz mont = data.get(i);


        String score = mont.getScore();
        String donedate = context.getResources().getString(R.string.date_history);

        String date = donedate+" "+mont.getDate();


        int finalscore = Integer.parseInt(score);

        String histscore = context.getResources().getString(R.string.score_history);


        String finalscore2 = histscore+" "+score;

        viewHolder.progress.setProgress(finalscore);
        viewHolder.txtdate.setText(date);
        viewHolder.txtscore.setText(finalscore2);




    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{

        TextView txtscore,txtdate;
        ProgressBar progress;
        OnQuizzListener onQuizzListener;

        public  ViewHolder(@NonNull View itemView, OnQuizzListener onQuizzListener){

            super(itemView);
            txtscore = itemView.findViewById(R.id.scoreQuizz);
            progress = itemView.findViewById(R.id.progresshisto);
            txtdate = itemView.findViewById(R.id.dateQuizz);


            this.onQuizzListener = onQuizzListener;

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onQuizzListener.onQuizzClick(getAdapterPosition());

        }
    }

    public interface OnQuizzListener{
        void onQuizzClick(int position);
    }






}