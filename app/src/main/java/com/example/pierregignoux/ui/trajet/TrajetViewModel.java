package com.example.pierregignoux.ui.trajet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TrajetViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public TrajetViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is login fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}