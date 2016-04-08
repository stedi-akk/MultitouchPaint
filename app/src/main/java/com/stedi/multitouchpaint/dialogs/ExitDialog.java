package com.stedi.multitouchpaint.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stedi.multitouchpaint.App;
import com.stedi.multitouchpaint.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExitDialog extends BaseDialog {
    public class CallbackEvent {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.exit_dialog, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @OnClick({R.id.done, R.id.cancel})
    public void onButtonsClick(View v) {
        if (v.getId() == R.id.done)
            App.getBus().post(new CallbackEvent());
        dismiss();
    }
}
