package com.league.bypass;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.cert.CertificateException;

@RestController
public class ByPassController {

    private final OkHttpClient client = getUnsafeOkHttpClient();
    private static final String SERVER = "https://127.0.0.1:2999";


    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = "**")
    public ResponseEntity<String> handleAll(final HttpServletRequest servletRequest) {
        final String path = servletRequest.getRequestURI();

        Request request = new Request.Builder()
                .url(SERVER + path)
                .build();

        System.out.println(SERVER + path);

        try (Response response = client.newCall(request).execute()) {

            return ResponseEntity.status(response.code())
                    .body(response.body().string());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.badRequest()
                .body("Something went wrong: " + path);
    }

}
