package jp.hideyukihosaka.qr_input_browzer

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val webView: WebView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("file:///android_asset/index.html");

        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        })

        // QR読み取りのための準備
        checkPermissions()

        // カメラ起動
        initQRCamera()

    }



    //パーミッションの確認
    private fun checkPermissions() {
        // already we got permission.
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            qr_view.resume()
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.CAMERA
            )) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                999
            )
        }
    }

    // カメラ設定
    companion object {
        const val REQUEST_CAMERA_PERMISSION:Int = 1
    }

    @SuppressLint("WrongConstant")
    private fun initQRCamera() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        val isReadPermissionGranted = (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        val isWritePermissionGranted = (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        val isCameraPermissionGranted = (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)

        if (isReadPermissionGranted && isWritePermissionGranted && isCameraPermissionGranted) {
            openQRCamera() // ← カメラ起動
        } else {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQUEST_CAMERA_PERMISSION
            )
        }
    }


    //カメラ起動
    private fun openQRCamera() {
        qr_view.decodeSingle(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {

                if (result != null) {
                    onPause()
                    Log.d("QRCode", "$result")
                    webView.evaluateJavascript(
                        "var value ='$result';\n" +
                              "qr_input(value)",
                        null)
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
        })
    }



    //QR読み取りのためのパーミッション設定
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            when(requestCode) {
                REQUEST_CAMERA_PERMISSION -> {
                    initQRCamera()
                }
            }
        }




    //メニュー
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            super.onCreateOptionsMenu(menu)

            val inflater = menuInflater
            //メニューのリソース選択
            inflater.inflate(R.menu.option, menu)
            return true

    }


}