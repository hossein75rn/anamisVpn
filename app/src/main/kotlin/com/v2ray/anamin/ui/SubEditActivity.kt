package com.v2ray.anamin.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.v2ray.anamin.R
import com.v2ray.anamin.databinding.ActivitySubEditBinding
import com.v2ray.anamin.dto.SubscriptionItem
import com.v2ray.anamin.extension.toast
import com.v2ray.anamin.util.MmkvManager
import com.v2ray.anamin.util.Utils

class SubEditActivity : BaseActivity() {
    private lateinit var binding: ActivitySubEditBinding

    var del_config: MenuItem? = null
    var save_config: MenuItem? = null

    private val subStorage by lazy { MMKV.mmkvWithID(MmkvManager.ID_SUB, MMKV.MULTI_PROCESS_MODE) }
    private val editSubId by lazy { intent.getStringExtra("subId").orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubEditBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        title = getString(R.string.title_sub_setting)

        val json = subStorage?.decodeString(editSubId)
        if (!json.isNullOrBlank()) {
            bindingServer(Gson().fromJson(json, SubscriptionItem::class.java))
        } else {
            clearServer()
        }
    }

    /**
     * bingding seleced server config
     */
    private fun bindingServer(subItem: SubscriptionItem): Boolean {
        binding.etRemarks.text = Utils.getEditable(subItem.remarks)
        binding.etUrl.text = Utils.getEditable(subItem.url)
        binding.chkEnable.isChecked = subItem.enabled
        binding.autoUpdateCheck.isChecked = subItem.autoUpdate
        return true
    }

    /**
     * clear or init server config
     */
    private fun clearServer(): Boolean {
        binding.etRemarks.text = null
        binding.etUrl.text = null
        binding.chkEnable.isChecked = true
        return true
    }

    /**
     * save server config
     */
    private fun saveServer(): Boolean {
        val subItem: SubscriptionItem
        val json = subStorage?.decodeString(editSubId)
        var subId = editSubId
        if (!json.isNullOrBlank()) {
            subItem = Gson().fromJson(json, SubscriptionItem::class.java)
        } else {
            subId = Utils.getUuid()
            subItem = SubscriptionItem()
        }

        subItem.remarks = binding.etRemarks.text.toString()
        subItem.url = binding.etUrl.text.toString()
        subItem.enabled = binding.chkEnable.isChecked
        subItem.autoUpdate = binding.autoUpdateCheck.isChecked

        if (TextUtils.isEmpty(subItem.remarks)) {
            toast(R.string.sub_setting_remarks)
            return false
        }
//        if (TextUtils.isEmpty(subItem.url)) {
//            toast(R.string.sub_setting_url)
//            return false
//        }

        subStorage?.encode(subId, Gson().toJson(subItem))
        toast(R.string.toast_success)
        finish()
        return true
    }

    /**
     * save server config
     */
    private fun deleteServer(): Boolean {
        if (editSubId.isNotEmpty()) {
            AlertDialog.Builder(this).setMessage(R.string.del_config_comfirm)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        MmkvManager.removeSubscription(editSubId)
                        finish()
                    }
                    .setNegativeButton(android.R.string.no) {_, _ ->
                        // do nothing
                    }
                    .show()
        }
        return true
    }


}
