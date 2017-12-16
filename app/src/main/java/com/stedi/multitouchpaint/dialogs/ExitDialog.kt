package com.stedi.multitouchpaint.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.stedi.multitouchpaint.App
import com.stedi.multitouchpaint.R

class ExitDialog : BaseDialog() {

    class Callback

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.exit_dialog, container)
        arrayOf(R.id.done, R.id.cancel).forEach {
            root.findViewById<View>(it).setOnClickListener {
                if (it.id == R.id.done) {
                    App.getBus().post(Callback())
                }
                dismiss()
            }
        }
        return root
    }
}