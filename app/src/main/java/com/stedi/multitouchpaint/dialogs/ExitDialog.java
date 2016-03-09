package com.stedi.multitouchpaint.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stedi.multitouchpaint.App;
import com.stedi.multitouchpaint.R;

public class ExitDialog extends BaseDialog implements View.OnClickListener {
    public class CallbackEvent {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.exit_dialog, container, false);
        root.findViewById(R.id.done).setOnClickListener(this);
        root.findViewById(R.id.cancel).setOnClickListener(this);
        return root;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.done)
            App.getBus().post(new CallbackEvent());
        dismiss();
    }
}
