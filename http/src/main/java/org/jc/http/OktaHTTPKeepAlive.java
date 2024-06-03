package org.jc.http;
// OktaHTTPKeepAlive

import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.shared.http.HTTPAuthScheme;
import org.zoxweb.shared.http.HTTPHeader;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.GetNameValue;
import org.zoxweb.shared.util.ParamUtil;
import org.zoxweb.shared.util.RateCounter;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class OktaHTTPKeepAlive {




    public static OkHttpClient getUnsafeOkHttpClient(ExecutorService executorService, int connectionPool, long kaTimeoutMillis) {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
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
            // Create a ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            // Optionally configure timeouts
            builder.connectTimeout(20, TimeUnit.SECONDS);
            builder.readTimeout(20, TimeUnit.SECONDS);
            builder.writeTimeout(20, TimeUnit.SECONDS);
            if(connectionPool>0)
                builder.connectionPool(new ConnectionPool(connectionPool, kaTimeoutMillis, TimeUnit.MILLISECONDS));
            builder.dispatcher(new Dispatcher(executorService));
            //builder.connectionPool(new ConnectionPool(10, 10, TimeUnit.SECONDS));
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void testExpiredKeepAlive(OkHttpClient client, String url, String user, String password)
    {

        GetNameValue<String> auth = HTTPAuthScheme.BASIC.toHTTPHeader(user, password);

        System.out.println(auth);
        // First request
        Request first = new Request.Builder()
                .url(url+"/sleep-test/1s")
                .header(auth.getName(), auth.getValue())

                .header(HTTPHeader.CONNECTION.getName(), "keep-alive")
                .build();


        Request second = new Request.Builder()
                .url(url+"/sleep-test/8s")
                .get()

                .header(HTTPHeader.CONNECTION.getName(), "keep-alive")
                .header(auth.getName(), auth.getValue())
                .build();


        try (Response response = client.newCall(first).execute())
        {
            if (response.isSuccessful())
            {
                System.out.println("First " + response.body().string() + " " + response.headers());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try (Response response = client.newCall(second).execute())
        {
            if (response.isSuccessful())
            {
                System.out.println("second " + response.body().string() + " " + response.headers());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }




    }


    public static void main(String[] args) {
//        OkHttpClient client = new OkHttpClient.Builder()
//                .connectionPool(new ConnectionPool(5, 5, TimeUnit.SECONDS))
////                .sslSocketFactory(SSLCheckDisabler.SINGLETON.getSSLFactory(), (X509TrustManager) SSLCheckDisabler.SINGLETON.getTrustManagers()[0])
////                .hostnameVerifier(SSLCheckDisabler.SINGLETON.getHostnameVerifier())
//                .build();






        ParamUtil.ParamMap params = ParamUtil.parse("=", args);
        System.out.println(params);
        String url = params.stringValue("url");
        int repeat = params.intValue("repeat", 0);
        boolean keepAlive = params.booleanValue("ka");
        String user = params.stringValue("user", true);
        String password = params.stringValue("password", true);
        int connectionPool = params.intValue("cp", 0);
        boolean async = params.booleanValue("async", true);
        boolean javaes = params.booleanValue("javaes", true);
        long kaTimeout = Const.TimeInMillis.toMillis(params.stringValue("kato", "5s"));
        System.out.println("Params " + url + " repeat: " + repeat + " Keep-Alive: " + keepAlive);
        GetNameValue<String> auth = null;
        if(user != null && password != null)
            auth = HTTPAuthScheme.BASIC.toHTTPHeader(user, password);

        ExecutorService executorService = javaes ? Executors.newCachedThreadPool() : TaskUtil.defaultTaskProcessor();

        final OkHttpClient client = getUnsafeOkHttpClient(executorService, connectionPool, kaTimeout);
        try {
            // First request
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .get()

                    .header(HTTPHeader.CONNECTION.getName(), keepAlive ? "keep-alive" : "close");
            if(auth != null)
                requestBuilder.header(auth.getName(), auth.getValue());

            Request request = requestBuilder.build();

            int maxLeft = 0;

            int counter = 0;


            int kaCounter = 0;

            // test keep alive on request
            do
            {
               try (Response response = client.newCall(request).execute()) {
                   if (response.isSuccessful()) {
                       String body =  response.body().string();
                       System.out.println( (body.length() > 1024 ? " body length " + body.length() : body) + " " +
                               response.headers());
                       String keepAliveHeader = response.headers().get("Keep-Alive");

                       if(keepAliveHeader != null) {
                           String[] result = response.headers().get("Keep-Alive").split("[, ;]");
                           ParamUtil.ParamMap nvs = ParamUtil.parse("=", result);
                           maxLeft = nvs.intValue("max", 0);
                           System.out.println("repeat=" + maxLeft + " Keep-Alive: " +keepAliveHeader);
                       }
                       else
                           maxLeft = 0;
                   } else {
                       System.out.println("First request failed: " + response.headers());
                   }
                   System.out.println("**************************************************************************");
                   kaCounter++;
               }
            }while(maxLeft > 0);


            AtomicLong execCounter = new AtomicLong();
            if(!async) {
                long delta = System.currentTimeMillis();
                for (counter = 0; counter < repeat; counter++) {
                    executorService.execute(() -> {
                        try (Response response = client.newCall(request).execute()) {
                            if (!response.isSuccessful() || response.code() != 200) {
                                System.out.println("First request failed: " + response.message());
                            }
                            else
                                execCounter.incrementAndGet();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                }


                delta = TaskUtil.completeTermination(executorService, 50) - delta;
                RateCounter rc = new RateCounter("OverAll")
                        .register(delta, counter);

                System.out.println("keep alive counter: " + kaCounter);
                System.out.println("Params " + url + " repeat: " + repeat + " Keep-Alive: " + keepAlive + " connection count: " + client.connectionPool().connectionCount());
                System.out.println("Sync total sent: " + counter + " total executed " + execCounter.get() + " it took: " + Const.TimeInMillis.toString(delta) + " rate: " + rc.rate(Const.TimeInMillis.SECOND.MILLIS));
            }
            else {

                long delta = System.currentTimeMillis();
                for (counter = 0; counter < repeat; counter++) {
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            e.printStackTrace();
                            System.out.println("Request failed: " + e.getMessage());
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            response.close();
                            execCounter.incrementAndGet();
                        }
                    });

                }
                delta = TaskUtil.completeTermination(executorService, 50) - delta;
                RateCounter rc = new RateCounter("async")
                        .register(delta, counter);

                System.out.println("keep alive counter: " + kaCounter);
                System.out.println("Params " + url + " repeat: " + repeat + " Keep-Alive: " + keepAlive + " connection count: " + client.connectionPool().connectionCount());
                System.out.println("Async total sent: " + counter + " total executed " + execCounter.get() + " it took: " + Const.TimeInMillis.toString(delta) + " rate: " + rc.rate(Const.TimeInMillis.SECOND.MILLIS));
            }





           // TaskUtil.waitIfBusyThenClose(50);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

