package com.example.smartfarm.ui.m02.ctrl;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CtrlViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CtrlViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}