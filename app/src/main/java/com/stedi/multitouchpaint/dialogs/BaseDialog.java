package com.stedi.multitouchpaint.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;

import com.stedi.multitouchpaint.R;

abstract class BaseDialog extends DialogFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseDialog);
    }
}
