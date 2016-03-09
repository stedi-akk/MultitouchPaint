package com.stedi.multitouchpaint.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;
import com.stedi.multitouchpaint.App;
import com.stedi.multitouchpaint.R;

public class FileWorkDialog extends BaseDialog implements View.OnClickListener {
    public enum CallbackEvent {
        ON_NEW_FILE,
        ON_OPEN,
        ON_SAVE
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.file_work_dialog, container, false);
        root.findViewById(R.id.file_work_dialog_new_file).setOnClickListener(this);
        root.findViewById(R.id.file_work_dialog_open).setOnClickListener(this);
        root.findViewById(R.id.file_work_dialog_save).setOnClickListener(this);
        return root;
    }

    @Override
    public void onClick(View v) {
        Bus bus = App.getBus();
        switch (v.getId()) {
            case R.id.file_work_dialog_new_file:
                bus.post(CallbackEvent.ON_NEW_FILE);
                break;
            case R.id.file_work_dialog_open:
                bus.post(CallbackEvent.ON_OPEN);
                break;
            case R.id.file_work_dialog_save:
                bus.post(CallbackEvent.ON_SAVE);
                break;
        }
        dismiss();
    }
}
