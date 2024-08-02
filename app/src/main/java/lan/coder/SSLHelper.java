package lan.coder;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SSLHelper {
    public static X509Certificate readCertificateFromResource(Context context, int resourceId) throws CertificateException {
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            byteArrayOutputStream.close();
            inputStream.close();
        } catch (IOException e) {
            throw new CertificateException("Error reading certificate", e);
        }

        byte[] certificateBytes = byteArrayOutputStream.toByteArray();

        CertificateFactory certFactory;
        certFactory = CertificateFactory.getInstance("X.509");

        X509Certificate certificate;
        certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certificateBytes));
        return certificate;
    }
}
