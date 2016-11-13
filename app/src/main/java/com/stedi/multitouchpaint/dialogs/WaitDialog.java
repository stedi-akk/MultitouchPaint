package com.stedi.multitouchpaint.dialogs;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stedi.multitouchpaint.R;

public class WaitDialog extends BaseDialog {
    private static final String TAG = WaitDialog.class.getName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setCancelable(false);
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
        return inflater.inflate(R.layout.wait_dialog, container, false);
    }

    public static WaitDialog start(FragmentManager fm) {
        WaitDialog dlg = new WaitDialog();
        dlg.show(fm, TAG);
        return dlg;
    }

    public static void stop(FragmentManager fm) {
        Fragment frg = fm.findFragmentByTag(TAG);
        if (frg != null && frg instanceof WaitDialog)
            ((WaitDialog) frg).dismiss();
    }
}
