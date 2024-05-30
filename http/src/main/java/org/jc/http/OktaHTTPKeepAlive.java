package org.jc.http;
// OktaHTTPKeepAlive

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
import java.util.concurrent.TimeUnit;

public class OktaHTTPKeepAlive {




    public static OkHttpClient getUnsafeOkHttpClient() {
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

            // Optionally configure timeouts
            builder.connectTimeout(30, TimeUnit.SECONDS);
            builder.readTimeout(30, TimeUnit.SECONDS);
            builder.writeTimeout(30, TimeUnit.SECONDS);
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


        final OkHttpClient client = getUnsafeOkHttpClient();

        ParamUtil.ParamMap params = ParamUtil.parse("=", args);
        System.out.println(params);
        String url = params.stringValue("url");
        int repeat = params.intValue("repeat", 0);
        boolean keepAlive = params.booleanValue("ka");
        String user = params.stringValue("user", true);
        String password = params.stringValue("password", true);
        System.out.println("Params " + url + " repeat: " + repeat + " Keep-Alive: " + keepAlive);


        try {
            // First request
            Request request = new Request.Builder()
                    .url(url)
                    .get()

                    .header(HTTPHeader.CONNECTION.getName(), keepAlive ? "keep-alive" : "close")
                    .build();

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



            long delta = System.currentTimeMillis();

            for(counter = 0; counter < repeat; counter++)
            {
                TaskUtil.defaultTaskProcessor().execute(()->{
                    try (Response response = client.newCall(request).execute())
                    {
                        if (!response.isSuccessful())
                        {
                            System.out.println("First request failed: " + response.message());
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                });

            }


            delta = TaskUtil.waitIfBusyThenClose(50) - delta;
            RateCounter rc = new RateCounter("OverAll");
            rc.register(delta, counter);

            System.out.println("keep alive counter: " + kaCounter);
            System.out.println("Params " + url + " repeat: " + repeat + " Keep-Alive: " + keepAlive + " connection count: " + client.connectionPool().connectionCount());
            System.out.println("total sent: " + counter + " it took: " + Const.TimeInMillis.toString(delta) + " rate: " + rc.rate(Const.TimeInMillis.SECOND.MILLIS));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

