package com.v2ray.anamin.service

import android.net.VpnService
import android.os.ParcelFileDescriptor

class DnsService : VpnService() {

    override fun onCreate() {
        super.onCreate()
        val builder = Builder()

        // Configure the VPN
        builder.setSession("DnsVpnSession")
        builder.addAddress("10.0.0.2", 32)  // Virtual address for VPN interface
        builder.addDnsServer("178.22.122.100")  // First DNS server
        builder.addDnsServer("185.51.200.2")    // Second DNS server
        builder.addRoute("0.0.0.0", 0)  // Route all traffic through the VPN
        builder.setMtu(1000)
        startVpn(builder)
    }

    private fun startVpn(builder: Builder) {
        val vpnInterface: ParcelFileDescriptor? = builder.establish()
        // The VPN is now active, and DNS queries go through the specified servers
    }
}
