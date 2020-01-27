package com.example.pierregignoux;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.mariuszgromada.math.mxparser.Expression;

public class BottomSheetDialog extends BottomSheetDialogFragment {

    private BottomSheetListener mListener;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_modif_km,container,false);

        EditText editkm = v.findViewById(R.id.newkm);
        editkm.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                final String aspassenger = editkm.getText().toString();
                mListener.onTextChanged(aspassenger);
            }
        });

        return v;
    }

    public interface BottomSheetListener{
        void onTextChanged(String text);
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
