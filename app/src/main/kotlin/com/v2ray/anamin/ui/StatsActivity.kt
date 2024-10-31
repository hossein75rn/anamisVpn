package com.v2ray.anamin.ui
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.v2ray.anamin.R

class StatsActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var lineData: LineData
    private lateinit var rxDataSet: LineDataSet
    private lateinit var txDataSet: LineDataSet
    private val rxEntries = mutableListOf<Entry>()
    private val txEntries = mutableListOf<Entry>()
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var networkStatsHelper: NetworkStatsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        lineChart = findViewById(R.id.lineChart)
        networkStatsHelper = NetworkStatsHelper(this)

        rxDataSet = LineDataSet(rxEntries, "Download")
        txDataSet = LineDataSet(txEntries, "Upload")

        lineData = LineData(rxDataSet, txDataSet)
        lineChart.data = lineData

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkStats = networkStatsHelper.getNetworkStats()
                updateChart(networkStats[0], networkStats[1])
                handler.postDelayed(runnable, 1000)
            }
        }
        handler.post(runnable)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun updateChart(rxBytes: Long, txBytes: Long) {
        rxEntries.add(Entry(rxEntries.size.toFloat(), rxBytes.toFloat()))
        txEntries.add(Entry(txEntries.size.toFloat(), txBytes.toFloat()))

        rxDataSet.notifyDataSetChanged()
        txDataSet.notifyDataSetChanged()
        lineData.notifyDataChanged()
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}

class NetworkStatsHelper(context: Context) {

    @RequiresApi(Build.VERSION_CODES.M)
    private val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
    private val packageName = context.packageName
    private val uid = getAppUid(context, packageName)

    private fun getAppUid(context: Context, packageName: String): Int {
        return try {
            context.packageManager.getPackageInfo(packageName, 0).applicationInfo.uid
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getNetworkStats(): LongArray {
        val stats = LongArray(2)
        try {
            val bucket = networkStatsManager.querySummaryForDevice(
                ConnectivityManager.TYPE_WIFI, null, 0, System.currentTimeMillis()
            )
            stats[0] = bucket.rxBytes
            stats[1] = bucket.txBytes
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return stats
    }
}
