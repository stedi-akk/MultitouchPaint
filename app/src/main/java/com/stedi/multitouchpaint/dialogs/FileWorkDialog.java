package com.stedi.multitouchpaint.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stedi.multitouchpaint.R;

public class FileWorkDialog extends BaseDialog implements View.OnClickListener {
    public static final String RESULT_KEY_NEW_FILE = "result_key_new_file";
    public static final String RESULT_KEY_OPEN = "result_key_open";
    public static final String RESULT_KEY_SAVE = "result_key_save";

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
        Bundle args = new Bundle();
        switch (v.getId()) {
            case R.id.file_work_dialog_new_file:
                args.putBoolean(RESULT_KEY_NEW_FILE, true);
                break;
            case R.id.file_work_dialog_open:
                args.putBoolean(RESULT_KEY_OPEN, true);
                break;
            case R.id.file_work_dialog_save:
                args.putBoolean(RESULT_KEY_SAVE, true);
                break;
        }
        setResult(args);
        dismiss();
    }
}
