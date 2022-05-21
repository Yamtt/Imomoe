package com.skyd.imomoe.net

import com.skyd.imomoe.BuildConfig
import com.skyd.imomoe.util.coil.CoilUtil
import com.skyd.imomoe.util.showToast
import okhttp3.Cache
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File

private val okhttpCache = Cache(File("cacheDir", "okhttpcache"), 10 * 1024 * 1024L)
private val bootstrapClient =
    OkHttpClient
        .Builder()
        .cache(okhttpCache)
        .apply {
            if (BuildConfig.DEBUG) addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            })
        }
        .build()

var dns: DnsOverHttps? = DnsServer.dnsServer.let {
    if (it.isNullOrBlank()) null else {
        runCatching {
            DnsOverHttps.Builder().client(bootstrapClient)
                .url(it.toHttpUrl())
                .build()
        }.getOrElse { e ->
            e.printStackTrace()
            e.message?.showToast()
            null
        }
    }
}

var okhttpClient = bootstrapClient.newBuilder().apply { dns?.let { dns(it) } }.build()

fun changeDnsServer(server: String) {
    dns = if (server.isBlank()) null else {
        runCatching {
            DnsOverHttps.Builder().client(bootstrapClient)
                .url(server.toHttpUrl())
                .build()
        }.getOrElse { e ->
            e.printStackTrace()
            e.message?.showToast()
            null
        }
    }
    okhttpClient = bootstrapClient.newBuilder().apply { dns?.let { dns(it) } }.build()
    RetrofitManager.get().client(okhttpClient)
    CoilUtil.setOkHttpClient(okhttpClient)
}
