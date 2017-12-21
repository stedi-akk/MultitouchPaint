package com.stedi.multitouchpaint.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import com.stedi.multitouchpaint.App
import com.stedi.multitouchpaint.R

class ExitDialog : BaseDialog() {

    class Callback

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return butterKnifeInflate(inflater, R.layout.exit_dialog, container)
    }

    @OnClick(R.id.done, R.id.cancel)
    fun onButtonsClick(v: View) {
        if (v.id == R.id.done) {
            App.BUS.post(Callback())
        }
        dismiss()
    }
}