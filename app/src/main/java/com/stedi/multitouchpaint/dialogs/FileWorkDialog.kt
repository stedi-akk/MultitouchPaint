package com.stedi.multitouchpaint.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.stedi.multitouchpaint.App
import com.stedi.multitouchpaint.R

class FileWorkDialog : BaseDialog() {

    enum class Callback(val buttonResId: Int) {
        ON_NEW_FILE(R.id.file_work_dialog_new_file),
        ON_OPEN(R.id.file_work_dialog_open),
        ON_SAVE(R.id.file_work_dialog_save)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.file_work_dialog, container, false)
        Callback.values().forEach { callback ->
            root.findViewById<View>(callback.buttonResId).setOnClickListener {
                App.BUS.post(callback)
                dismiss()
            }
        }
        return root
    }
}