package lan.coder;
import android.content.Intent;
import android.net.http.SslError;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.webkit.SslErrorHandler;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.WebResourceResponse;
import android.webkit.WebResourceRequest;

import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.ByteArrayInputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    protected WebView webView;
    protected String url;
    protected String dir;
    protected String domain;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private int EscapeNum = 0;
    private long eventTime = 0;
    private boolean inited = false;
    protected SharedPreferences.Editor editor;
    protected SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            setupWebView();
            initDialog();
        }
    }
    private void initDialog(){
        builder = new AlertDialog.Builder(this);
        builder.setTitle("更换地址");
        final EditText input = new EditText(this);
        input.setText(url); // 设置默认值
        builder.setView(input);
        // 设置对话框的按钮
        builder.setPositiveButton("确定", (dialog, which) -> {
            // 获取用户输入的值
            String userInput = input.getText().toString();
            Log.d("edit", userInput);
            if ( isValidURL(userInput) && url != userInput ){
                url = userInput;
                editor.putString("url", url );
                editor.apply();
                domain = Uri.parse(url).getHost();
                webView.loadUrl(url);
                Log.d("change-url", "success, value: " + userInput );
            }else{
                Log.d("change-url", "fail, value: " + userInput );
            }
        });
        builder.setNegativeButton("取消", (dialog, which) -> {
            dialog.cancel();
            EscapeNum = 0;
        });
        dialog = builder.create();
        inited = true;
    }
    private void setupWebView() {
        webView = findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setInitialScale(1);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        initParams();
        initClient();
        Log.d("webview-init", "url: " + url +"\ndomain: " + domain);
        webView.loadUrl(url);
    }

    private void initClient() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
                // handler.cancel(); // 拒绝加载页面
                handler.proceed();
                // view.reload();
            }
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest rq ) {
                String uri = rq.getUrl().toString();
                Uri u = Uri.parse(uri);
                if (uri.startsWith("mailto:") ||
                    uri.startsWith("tel:") ||
                    uri.startsWith("wechat:") ||
                    uri.startsWith("alipay:") ||
                    uri.startsWith("douyin:") ||
                    uri.startsWith("tiktok:") ||
                    uri.startsWith("tg:") ||
                    !u.getHost().equals(domain)) {
                    return new WebResourceResponse(null, null, new ByteArrayInputStream(new byte[0]));
                }
                return super.shouldInterceptRequest(view, rq );
            }

            // 优化代码：添加缓存机制，避免每次都重新加载网页
            public final Map<String, String> cache = new HashMap<>();
            @Override
            public void onPageFinished(WebView view, String url) {
                cache.put(url, view.getSettings().getJavaScriptEnabled() ? view.getResources().toString() : "");
            }
        });
    }

    private void initParams() {
        // 获取 SharedPreferences 对象
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        url = sharedPreferences.getString("url", getString(R.string.url));
        domain = Uri.parse(url).getHost();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( !inited )return super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK){
            long now = event.getEventTime();
            long val = now - eventTime;
            eventTime = now;
            if ( val > 200 && val < 1000 ){
                EscapeNum += 1;
                if ( EscapeNum >= 3 ){
                    EscapeNum = 0;
                    dialog.show();
                }
                return false;
            }
            return super.onKeyDown(KeyEvent.KEYCODE_ESCAPE, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     *
     */
    private void gobackHandler(){

        // 显示对话框
        // if (webView.canGoBack()) {
        //     // webView.goBack();
        // } else {
        //     // 如果WebView没有可以后退的网页，需要用户点击确认后才关闭
        //     // 使用 AlertDialog 提供确认关闭应用的对话框
        //     AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //     String appName = getString(R.string.app_name);
        //     builder.setTitle(appName);
        //     builder.setMessage("Are you sure you want to exit the " + appName + "?");
        //     builder.setPositiveButton("Yes", (dialog, which) -> finish());
        //     builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        //     builder.show();

        // }
    }

    public static boolean isValidURL(String url) {
        String regex = "^(http|https|file|smb|ftp|ftps):\\/\\/[a-zA-Z0-9.-]+(\\/[a-zA-Z0-9._~:/?#&=]*)?$";
        return url.matches(regex);
    }
}

