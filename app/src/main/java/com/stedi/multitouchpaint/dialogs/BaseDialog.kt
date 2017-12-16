package com.stedi.multitouchpaint.dialogs

import android.app.DialogFragment
import android.app.FragmentManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.Unbinder
import com.stedi.multitouchpaint.R

abstract class BaseDialog : DialogFragment() {
    private var unbinder: Unbinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseDialog)
    }

    protected fun butterKnifeInflate(inflater: LayoutInflater, layoutResId: Int, parent: ViewGroup?): View {
        val root = inflater.inflate(layoutResId, parent, false)
        unbinder = ButterKnife.bind(this, root)
        return root
    }

    override fun show(manager: FragmentManager, tag: String) {
        try {
            super.show(manager, tag)
        } catch (e: IllegalStateException) { // it happens...
            e.printStackTrace()
        }
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    override fun onDestroyView() {
        val unbinder = unbinder
        if (unbinder != null) {
            unbinder.unbind()
        }
        super.onDestroyView()
    }
}