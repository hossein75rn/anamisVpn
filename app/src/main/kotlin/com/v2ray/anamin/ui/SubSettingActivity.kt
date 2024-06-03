package com.v2ray.anamin.ui

import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.v2ray.anamin.R
import android.os.Bundle
import com.v2ray.anamin.databinding.ActivitySubSettingBinding
import com.v2ray.anamin.dto.SubscriptionItem
import com.v2ray.anamin.util.MmkvManager

class SubSettingActivity : BaseActivity() {
    private lateinit var binding: ActivitySubSettingBinding

    var subscriptions:List<Pair<String, SubscriptionItem>> = listOf()
    private val adapter by lazy { SubSettingRecyclerAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubSettingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        title = getString(R.string.title_sub_setting)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        subscriptions = MmkvManager.decodeSubscriptions()
        adapter.notifyDataSetChanged()
    }

}
