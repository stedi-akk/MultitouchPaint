package com.stedi.multitouchpaint.dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;

import com.stedi.multitouchpaint.R;

public abstract class BaseDialog extends DialogFragment {
    private final String REQUEST_CODE_KEY = "request_code_key";

    private int requestCode = -1;

    public interface OnResult {
        void onDialogResult(int requestCode, Bundle args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseDialog);
        requestCode = getArguments().getInt(REQUEST_CODE_KEY, -1);
    }

    protected void setResult(Bundle args) {
        Activity activity = getActivity();
        if (activity != null && activity instanceof OnResult && requestCode != -1)
            ((OnResult) activity).onDialogResult(requestCode, args);
    }

    public void showForResult(Activity activity, int requestCode) {
        Bundle args = getArguments();
        if (args == null)
            args = new Bundle();
        args.putInt(REQUEST_CODE_KEY, requestCode);
        setArguments(args);
        show(activity.getFragmentManager(), getClass().getName());
    }
}
