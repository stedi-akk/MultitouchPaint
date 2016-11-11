package com.stedi.multitouchpaint.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;
import com.stedi.multitouchpaint.App;
import com.stedi.multitouchpaint.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FileWorkDialog extends BaseDialog {
    private Unbinder unbinder;

    public enum Callback {
        ON_NEW_FILE,
        ON_OPEN,
        ON_SAVE
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.file_work_dialog, container, false);
        unbinder = ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @OnClick({R.id.file_work_dialog_new_file, R.id.file_work_dialog_open, R.id.file_work_dialog_save})
    public void onButtonsClick(View v) {
        Bus bus = App.getBus();
        switch (v.getId()) {
            case R.id.file_work_dialog_new_file:
                bus.post(Callback.ON_NEW_FILE);
                break;
            case R.id.file_work_dialog_open:
                bus.post(Callback.ON_OPEN);
                break;
            case R.id.file_work_dialog_save:
                bus.post(Callback.ON_SAVE);
                break;
            default:
                break;
        }
        dismiss();
    }
}
