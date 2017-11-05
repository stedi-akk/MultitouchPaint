package com.stedi.multitouchpaint.dialogs;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stedi.multitouchpaint.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

abstract class BaseDialog extends DialogFragment {
    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseDialog);
    }

    protected View butterKnifeInflate(LayoutInflater inflater, int layoutResId, ViewGroup parent) {
        View root = inflater.inflate(layoutResId, parent, false);
        unbinder = ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            super.show(manager, tag);
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void dismiss() {
        super.dismissAllowingStateLoss();
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null)
            unbinder.unbind();
        super.onDestroyView();
    }
}
