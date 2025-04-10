package org.jc;

import okhttp3.*;
import okio.ByteString;
import org.zoxweb.server.http.OkHTTPCall;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.shared.http.HTTPMessageConfigInterface;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.ParamUtil;
import org.zoxweb.shared.util.RateCounter;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class OkHttpWebSocketTest {

    public static void main(String[] args) {
        OkHttpClient client = OkHTTPCall.createOkHttpBuilder(null, null, HTTPMessageConfigInterface.DEFAULT_TIMEOUT_20_SECOND,false, 10, HTTPMessageConfigInterface.DEFAULT_TIMEOUT_20_SECOND).build();
        //OkHttpClient client = new OkHttpClient();
        ParamUtil.ParamMap params = ParamUtil.parse("=", args);
        params.hide("password");
        System.out.println(params);
        String url = params.stringValue("url", false);
        String username = params.stringValue("user", false);
        String password = params.stringValue("password", false);
        boolean binary = params.booleanValue("bin", true);

        int repeat = params.intValue("repeat", 1000);

        // Generate the Basic Authorization header value
        String credential = Credentials.basic(username, password);


        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", credential)
                .build();

        RateCounter rc = new RateCounter();
        long ts = System.currentTimeMillis();
        AtomicInteger ai = new AtomicInteger();
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                System.out.println("WebSocket opened: " + response);
                webSocket.send("Hello, WebSocket!");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
//                synchronized (this)
                {
                    int count = ai.incrementAndGet();
                    //System.out.println(count + " " + text);
                    if (count == repeat) {
                        long delta = System.currentTimeMillis() - ts;
                        rc.register(delta, ai.get());
                        System.out.println(text);
                        System.out.println(ai.get() + "  " + Const.TimeInMillis.toString(delta) + " " + rc.rate(1000) + " msg/sec");
                        TaskUtil.defaultTaskScheduler().queue(Const.TimeInMillis.SECOND.MILLIS*5, ()->
                        {
                            try {
                                webSocket.close(1000, "finished");
                                TaskUtil.sleep(Const.TimeInMillis.SECOND.MILLIS * 5);
                                System.exit(0);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                //System.out.println("Received bytes: " + bytes.string(StandardCharsets.UTF_8));
                int count = ai.incrementAndGet();
                //System.out.println(count + " " + text);
                if (count == repeat) {
                    long delta = System.currentTimeMillis() - ts;
                    rc.register(delta, ai.get());
                    System.out.println(bytes + ": " +bytes.string(StandardCharsets.UTF_8));
                    System.out.println(ai.get() + "  " + Const.TimeInMillis.toString(delta) + " " + rc.rate(1000) + " msg/sec");
                    TaskUtil.defaultTaskScheduler().queue(Const.TimeInMillis.SECOND.MILLIS*5, ()->
                    {
                        try {
                            webSocket.close(1000, "finished");
                            TaskUtil.sleep(Const.TimeInMillis.SECOND.MILLIS * 5);
                            System.exit(0);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                System.out.println("WebSocket is closing: " + code + " / " + reason);
                webSocket.close(code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                System.out.println("WebSocket closed: " + code + " / " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                System.err.println("WebSocket error: " + t.getMessage());
            }
        };

        // Establish the WebSocket connection
        WebSocket ws = client.newWebSocket(request, listener);
        //TaskUtil.sleep(Const.TimeInMillis.SECOND.MILLIS*5);
        for (int i = 0; i < repeat; i++)
        {
            String message =  i + 1 + " hello";
            //TaskUtil.defaultTaskScheduler().execute( ()->ws.send(message);
            if(binary)
                ws.send(ByteString.encodeUtf8(message));
            else
                ws.send(message);

        }


        //TaskUtil.waitIfBusy();
        System.out.println("Done sending " + repeat);

        // The client will continue to run; you can shut it down when you're done:
        // client.dispatcher().executorService().shutdown();
    }
}

