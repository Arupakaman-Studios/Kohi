package com.arupakaman.kohi.uiModules.base

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    protected lateinit var mActivity: Activity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as Activity
    }

}