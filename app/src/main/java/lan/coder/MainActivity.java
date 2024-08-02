package lan.coder;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebResourceRequest;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    protected WebView webView;
    protected String url;
    public String dir;
    public String domain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setupWebView();
    }

    private void setupWebView() {
        webView = findViewById(R.id.webView);
        
        // 获取 RelativeLayout 并将 WebView 添加进去
        // ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
        //     ViewGroup.LayoutParams.MATCH_PARENT,
        //     ViewGroup.LayoutParams.MATCH_PARENT
        // );
        // ViewGroup viewGroup = findViewById(R.id.activity_main);
        // viewGroup.addView(webView, params);

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
        // webView.setWebContentsDebuggingEnabled(true);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        initParams();
        initClient();
        
        Log.d("webview-init", "url: " + url +"\ndomain: " + domain);
        webView.loadUrl(url);
    }

    private void initClient() {
        webView.setWebViewClient(new WebViewClient() {
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

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
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
        initObbDir();
        url = getString(R.string.url);
        String name = "url";
        File file = new File(dir, name);
        if ( file.exists() ){
            String newUrl = readFile(name);
            if (isValidURL(newUrl)) {
                url = newUrl;
                Log.d("webview-init", "load url from obb directory: " + name);
            }else{
                Log.d("webview-init", "failed load newUrl data: " + newUrl);
            }
        }
        writeFile(file, url);
        domain = Uri.parse(url).getHost();
    }

    private Boolean writeFile(File file, String str){
        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(str);
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
        }
        return false;
    }

    
    private String readFile(String name ){
        File file = new File(dir, name);
        if ( file.exists() ) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                }
                int lastLineStartIndex = stringBuilder.lastIndexOf("\n");
                if (lastLineStartIndex != -1 && lastLineStartIndex < stringBuilder.length()) {
                    stringBuilder.delete(lastLineStartIndex, stringBuilder.length());
                }
                reader.close();
                return stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void initObbDir(){
        File filesDir = getApplicationContext().getObbDir();
        dir = filesDir.getAbsolutePath();
        if (!filesDir.exists()) filesDir.mkdir();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //gobackHandler();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     *
     */
    private void gobackHandler(){
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            // 如果WebView没有可以后退的网页，需要用户点击确认后才关闭
            // 使用 AlertDialog 提供确认关闭应用的对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String appName = getString(R.string.app_name);
            builder.setTitle(appName);
            builder.setMessage("Are you sure you want to exit the " + appName + "?");
            builder.setPositiveButton("Yes", (dialog, which) -> finish());
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();

        }
    }
    private static final String URL_PATTERN =
            "((http|https|file|smb|ftp|ftps)://)(www.)?[a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)";
    private static final Pattern pattern = Pattern.compile(URL_PATTERN);
    public static boolean isValidURL(String url) {
        if (url == null) {
            return false;
        }
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }
}

