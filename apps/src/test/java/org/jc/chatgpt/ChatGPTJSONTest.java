package org.jc.chatgpt;

import org.junit.jupiter.api.Test;
import org.zoxweb.server.io.UByteArrayOutputStream;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.http.HTTPMessageConfigInterface;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.NVGenericMapList;
import org.zoxweb.shared.util.NVInt;
import org.zoxweb.shared.util.SharedStringUtil;

public class ChatGPTJSONTest {

    @Test
    public void message()
    {
        NVGenericMap request = new NVGenericMap();
        request.build("model", "gpt-4o-mini");
        NVGenericMapList messages = new NVGenericMapList("messages");
        request.build(messages);
        NVGenericMap imageAnalysis = new NVGenericMap();
        messages.add(imageAnalysis);
        imageAnalysis.build("role", "user");
        NVGenericMapList content = new NVGenericMapList("content");
        imageAnalysis.add(content);
        content.add(new NVGenericMap().build("type", "text")
                .build("text","What is this image"));
        content.add(new NVGenericMap().build("type", "image_url")
                .build(new NVGenericMap("image_url")
                        .build("url","data:image/png;base64,{base_64_image=}")));
        request.build(new NVInt("max_tokens", 300));
        String json = GSONUtil.toJSONDefault(request, true);
        UByteArrayOutputStream baos = new UByteArrayOutputStream();
        baos.write("base_64_image");

        NVGenericMap payload = ChatGPTUtil.toData("gpt-4o-mini",
                "What is this image", "png", 300, baos);
        String json2 = GSONUtil.toJSONDefault(payload, true);
        System.out.println(json);
        System.out.println(json2);
        NVGenericMap result = GSONUtil.fromJSONDefault(json2, NVGenericMap.class);



        System.out.println(result);
        baos.reset();
        baos.write(json2);

        int index = baos.indexOf("u003");
        System.out.println(index + " "  + SharedStringUtil.toString(SharedStringUtil.getBytes("==")));
        HTTPMessageConfigInterface hmci = ChatGPTUtil.toHMCI("https://batab.com", HTTPMethod.POST, "dsfdsfdsfd", payload);
        System.out.println(hmci.getHeaders().get("accept") + " " + hmci.getHeaders().get("content-Type"));
    }
}
