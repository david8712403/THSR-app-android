package com.davidchen

import android.app.AlertDialog
import android.content.Context
import android.os.Looper
import android.view.LayoutInflater
import android.widget.TextView
import com.davidchen.thsrapp.R

class ProgressDialogUtil {
    companion object {
        private var mAlertDialog: AlertDialog? = null

        fun showProgressDialog(context: Context) {
            if (mAlertDialog == null) {
                mAlertDialog = AlertDialog.Builder(context, R.style.CustomProgressDialog).create()
            }
            val loadView = LayoutInflater
                .from(context)
                .inflate(R.layout.progress_dialog_view, null)

            mAlertDialog!!.setView(loadView, 0,0,0,0)
            mAlertDialog!!.setCanceledOnTouchOutside(false)
            mAlertDialog!!.show()
        }

        fun showProgressDialog(context: Context, msg: String) {
            if (mAlertDialog == null) {
                mAlertDialog = AlertDialog.Builder(context, R.style.CustomProgressDialog).create()
            }else {
                mAlertDialog!!.findViewById<TextView>(R.id.tv_load_msg).text = msg
            }
            val loadView = LayoutInflater
                .from(context)
                .inflate(R.layout.progress_dialog_view, null)

            mAlertDialog!!.setView(loadView, 0,0,0,0)
            mAlertDialog!!.setCanceledOnTouchOutside(false)

            mAlertDialog!!.show()
        }

        fun dismiss() {
            if (mAlertDialog != null && mAlertDialog!!.isShowing) {
                mAlertDialog!!.dismiss()
            }
        }
    }
}