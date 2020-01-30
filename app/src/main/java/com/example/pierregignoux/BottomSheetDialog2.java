package com.example.pierregignoux;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.mariuszgromada.math.mxparser.Expression;

public class BottomSheetDialog2 extends BottomSheetDialogFragment {

    private BottomSheetListener mListener;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_prefedit,container,false);

        EditText edtprefdist = v.findViewById(R.id.nprefDistance);
        EditText edtpreftitle = v.findViewById(R.id.ntitlepref);
        Button btnsavepref = v.findViewById(R.id.btnsavepref);

        btnsavepref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String preftitle = edtpreftitle.getText().toString();
                final String prefdist = edtprefdist.getText().toString();

                mListener.addPrefTraj(preftitle,prefdist);
                dismiss();
            }
        });

        return v;
    }

    public interface BottomSheetListener{
        void addPrefTraj(String title,String distance);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mListener= (BottomSheetListener) context;
        } catch(ClassCastException e){
            throw new ClassCastException(context.toString()+"Must implemente Bottomsheetlistener");
        }
    }
}
