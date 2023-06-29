/*
 * Copyright (c) 2019.
 * Bismillahir Rahmanir Rahim,
 * Developer : Saadat Sayem
 */

package com.sam43.namespace

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sam43.namespace.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    companion object {
        const val JAVASCRIPT_OBJ = "javascript_obj"
        const val BASE_URL = "file:///android_asset/web/"
    }

    private lateinit var fileDownloaderVM: FileDownloaderVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initVM()
        setupButtonActions()
        setupWebLayout()
    }

    private fun setupButtonActions() {
        binding.btnZoomIn.setOnClickListener { binding.webView.zoomIn() }
        binding.btnZoomOut.setOnClickListener { binding.webView.zoomOut() }
        binding.btnSendToWeb.setOnClickListener {
            binding.webView.evaluateJavascript(
                "javascript: " +
                        "updateFromAndroid(\"" + binding.etSendDataField.text + "\")",
                null
            )
        }
    }

    private fun initVM() {
        fileDownloaderVM = ViewModelProvider(this).get(FileDownloaderVM::class.java)
    }

    override fun onResume() {
        super.onResume()
        callVM()
    }

    private fun callVM() {
        val url = "file:///android_asset/mapa.svg"
        //val url = "https://svgshare.com/i/ugT.svg"
        try {
            fileDownloaderVM.downloadFileFromServer(url)
                .observe(this, Observer { responseBody ->
                    val svgString = responseBody.string()
                    binding.webView.loadDataWithBaseURL(
                        BASE_URL, getHTMLBody(svgString), "text/html",
                        "UTF-8", null
                    )
                })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("AddJavascriptInterface", "SetJavaScriptEnabled")
    private fun setupWebLayout() {
        binding.webView.setInitialScale(150)
        binding.webView.settings.builtInZoomControls = true
        binding.webView.settings.displayZoomControls = false
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.addJavascriptInterface(
            JavaScriptInterface(),
            JAVASCRIPT_OBJ
        )
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                injectJavaScriptFunction()
            }
        }
        binding.webView.webChromeClient = WebChromeClient()
    }

    private fun injectJavaScriptFunction() {
        val textToAndroid = "javascript: window.androidObj.textToAndroid = function(message) { " +
                JAVASCRIPT_OBJ + ".textFromWeb(message) }"
        binding.webView.loadUrl(textToAndroid)
    }


    inner class JavaScriptInterface {
        @SuppressLint("SetTextI18n")
        @JavascriptInterface
        fun textFromWeb(fromWeb: String) {
            runOnUiThread {
                binding.tvStateName.text = fromWeb
            }
            toast(fromWeb)
        }
    }

    override fun onDestroy() {
        binding.webView.removeJavascriptInterface(JAVASCRIPT_OBJ)
        super.onDestroy()
    }
}
