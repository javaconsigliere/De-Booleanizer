package org.jc;

import org.zoxweb.shared.util.ParamUtil;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ClientEndpoint
public class TyrusWSTest
extends Endpoint{


    private static String TOK = "TXT";
    private AtomicInteger counter = new AtomicInteger();
    // Holds the WebSocket session
    private Session userSession = null;

    // A latch to wait for message responses
    private static CountDownLatch latch = new CountDownLatch(1);

    // Called when the connection is opened
//    @OnOpen
//    public void onOpen(Session session) {
//        System.out.println("Connected to server.");
//        this.userSession = session;
//        // Send a message as soon as the connection is open
//        sendMessage("Hello, server!");
//    }

    // Called when a message is received from the server
//    @OnMessage
//    public void onMessage(String message) {
//        int val = (message.indexOf(TOK) != -1)  ? counter.getAndIncrement() : counter.get();
//        System.out.println(Thread.currentThread() + " ["+val+"] " + message.length() + " Received message: " + message);
//        // Count down the latch so that main thread can continue if waiting
//        latch.countDown();
//    }

    //@Override
    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {

        try {
            session.addMessageHandler(new MessageHandler.Whole<String>() {

                @Override
                public void onMessage(String message) {

                    int val = (message.indexOf(TOK) != -1)  ? counter.getAndIncrement() : counter.get();
                    System.out.println(Thread.currentThread() + " ["+val+"] " + message.length() + " Received message: " + message);
                    // Count down the latch so that main thread can continue if waiting
                    latch.countDown();

                }
            });
            session.getBasicRemote().sendText("heelo");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    // Called when the connection is closed
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("Session closed: " + reason);
        this.userSession = null;
    }

    // Called when an error occurs
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error: " + throwable.getMessage());
    }

    // A helper method to send messages
    public void sendMessage(String message) {
        if (this.userSession != null && this.userSession.isOpen()) {
            try {
                this.userSession.getBasicRemote().sendText(message);
                System.out.println("Sent message: " + message);
            } catch (Exception e) {
                System.err.println("Failed to send message: " + e.getMessage());
            }
        } else {
            System.out.println("Cannot send message. Session is not open.");
        }
    }

    public static void main(String[] args) {

        String extra = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWZYZ-2-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWZYZ-3-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWZYZ-4-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWZYZ-5-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWZYZ";

        ParamUtil.ParamMap params = ParamUtil.parse("=", args);
        System.out.println(params);
        String url = params.stringValue("url", false);
        String username = params.stringValue("user", true);
        String password = params.stringValue("password", true);
        int repeat = params.intValue("repeat", 1000);
        System.out.println("Connecting to " + url);


        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        final String authHeaderValue = "Basic " + encodedAuth;

        // Create a custom configurator to add the Authorization header
        ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
            @Override
            public void beforeRequest(Map<String, List<String>> headers) {
                headers.put("Authorization", Collections.singletonList(authHeaderValue));
                super.beforeRequest(headers);
            }
        };

        // Build the client endpoint configuration with the custom configurator
        ClientEndpointConfig clientConfig = ClientEndpointConfig.Builder.create()
                .configurator(configurator)
                .build();

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        // Connect using the custom configuration



        try {
            // Connect to the server using the annotated endpoint.
            // The container creates an instance of TyrusClient and invokes the lifecycle methods.


            TyrusWSTest tyrusWSTest = new TyrusWSTest();
            Session session = container.connectToServer(tyrusWSTest, clientConfig, new URI(url));

            // Optionally wait for a message or some condition
            latch.await(10, TimeUnit.SECONDS);

            // You can also send additional messages using the session directly:
            //session.getBasicRemote().sendText("Another message from main!");
            for (int i = 0; i < repeat ; i++)
            {
                if(i%2 == 0)
                    session.getBasicRemote().sendText(TOK + "-" + i);
                else
                    session.getBasicRemote().sendText(TOK + "-" + i +"-" + extra);
            }


            // Wait a moment for responses before closing (for demo purposes)
            Thread.sleep(2000);

            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
