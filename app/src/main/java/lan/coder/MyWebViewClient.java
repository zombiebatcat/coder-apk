package lan.coder;

import android.net.http.SslError;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManagerFactory;

public class MyWebViewClient extends WebViewClient {

    private static final String TAG = "MyWebViewClient";

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
        try {
            X509Certificate certificate = SSLHelper.readCertificateFromResource(view.getContext(), R.raw.mkcert);
            trustSelfSignedCertificate(certificate);
            view.reload();
        } catch (Exception e) {
            Log.e(TAG, "Error handling SSL error", e);
            // handler.cancel(); // 拒绝加载页面
            handler.proceed();
            // view.reload();
        }

    }
    
    private void trustSelfSignedCertificate(X509Certificate certificate) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);

        String alias = "coder";
        keyStore.setCertificateEntry(alias, certificate);

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
    }
}
