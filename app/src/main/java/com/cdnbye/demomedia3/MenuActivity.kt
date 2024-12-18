package com.cdnbye.demomedia3

import android.R
import android.content.Intent
import android.os.Bundle
import android.support.multidex.MultiDex
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.ListView
import androidx.fragment.app.FragmentActivity
import com.cdnbye.demomedia3.databinding.ActivityMenuBinding
import com.p2pengine.core.p2p.EngineExceptionListener
import com.p2pengine.core.p2p.P2pConfig
import com.p2pengine.core.tracking.TrackerZone
import com.p2pengine.core.utils.EngineException
import com.p2pengine.core.utils.LogLevel
import com.p2pengine.sdk.P2pEngine
import java.util.*

class MenuActivity : FragmentActivity() {
    private lateinit var menuBinding: ActivityMenuBinding
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MultiDex.install(this)
        menuBinding = ActivityMenuBinding.inflate(layoutInflater)

        setContentView(menuBinding.root)

        listView = menuBinding.listView
        val adapter: ListAdapter = ArrayAdapter(
            this,
            R.layout.simple_list_item_1,
            R.id.text1,
            buildListData()
        )
        listView.adapter = adapter
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val item: ListEntry =
                    listView.adapter.getItem(position) as ListEntry
                startActivity(Intent(this@MenuActivity, item.activityClass))
        }

        val config = P2pConfig.Builder()
            .p2pEnabled(true)
            .logEnabled(true)
            .logLevel(LogLevel.DEBUG)
            .trackerZone(TrackerZone.Europe)
//            .trackerZone(TrackerZone.HongKong)
//            .trackerZone(TrackerZone.USA)
            .build()

        println("MainActivity P2pEngine init")
        P2pEngine.init(applicationContext, "ZMuO5qHZg", config)

        P2pEngine.instance?.registerExceptionListener(object : EngineExceptionListener {
            override fun onTrackerException(e: EngineException) {
                println("onTrackerException ${e.message}")
            }

            override fun onSignalException(e: EngineException) {
                println("onSignalException ${e.message}")
            }

            override fun onSchedulerException(e: EngineException) {
                println("onSchedulerException ${e.message}")
            }

            override fun onOtherException(e: EngineException) {
                println("onOtherException ${e.message}")
            }

        })

    }

    private fun buildListData(): List<ListEntry> {
        return Arrays.asList(
            ListEntry("EXO", ExoActivity::class.java),
        )
    }

    private class ListEntry(private val title: String, val activityClass: Class<*>) {
        override fun toString(): String {
            return title
        }
    }


}