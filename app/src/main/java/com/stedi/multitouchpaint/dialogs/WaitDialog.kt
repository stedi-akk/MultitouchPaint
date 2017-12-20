package com.stedi.multitouchpaint.dialogs

import android.app.FragmentManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.stedi.multitouchpaint.R

class WaitDialog : BaseDialog() {
    companion object {
        private val TAG = WaitDialog::class.java.name

        fun show(fm: FragmentManager): WaitDialog {
            val dlg = WaitDialog()
            dlg.show(fm, TAG)
            return dlg
        }

        fun dismiss(fm: FragmentManager) {
            val frg = fm.findFragmentByTag(TAG)
            if (frg != null && frg is WaitDialog) {
                frg.dismiss()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        isCancelable = false
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return inflater.inflate(R.layout.wait_dialog, container, false)
    }
}