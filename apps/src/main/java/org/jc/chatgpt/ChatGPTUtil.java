package org.jc.chatgpt;

import org.zoxweb.server.http.HTTPCall;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.io.UByteArrayOutputStream;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.http.*;
import org.zoxweb.shared.util.*;

public class ChatGPTUtil
{
    private ChatGPTUtil(){}


    public static NVGenericMap toData(String model,
                                      String prompt,
                                      String imageType,
                                      int maxTokens,
                                      UByteArrayOutputStream baos)
    {
        return toData(model, prompt, imageType, maxTokens, baos.getInternalBuffer(), 0, baos.size());
    }


    public static NVGenericMap toData(String model,
                                      String prompt,
                                      String imageType,
                                      int maxTokens,
                                      byte[] imageBuffer,
                                      int bufferOffset,
                                      int dataLength)
    {
        String imageBase64 = SharedBase64.encodeAsString(SharedBase64.Base64Type.DEFAULT,
                imageBuffer,
                bufferOffset,
                dataLength);

        NVGenericMap request = new NVGenericMap();
        request.build("model", model);
        NVGenericMapList messages = new NVGenericMapList("messages");
        request.build(messages);
        NVGenericMap imageAnalysis = new NVGenericMap();
        messages.add(imageAnalysis);
        imageAnalysis.build("role", "user");
        NVGenericMapList content = new NVGenericMapList("content");
        imageAnalysis.add(content);
        content.add(new NVGenericMap().build("type", "text")
                .build("text", prompt));
        content.add(new NVGenericMap().build("type", "image_url")
                .build(new NVGenericMap("image_url")
                        .build("url","data:image/" + imageType + ";base64,{" + imageBase64+ "}")));
        request.build(new NVInt("max_tokens", maxTokens));

        return request;
    }

    public static HTTPMessageConfigInterface toHMCI(String url, HTTPMethod httpMethod, String appKey, NVGenericMap payload)
    {
        HTTPMessageConfigInterface hmci = HTTPMessageConfig.createAndInit(url, null, httpMethod);
        hmci.setContentType(HTTPMediaType.APPLICATION_JSON, HTTPConst.CHARSET_UTF_8);
        hmci.setAccept(HTTPMediaType.APPLICATION_JSON, HTTPConst.CHARSET_UTF_8);

        hmci.setAuthorization(new HTTPAuthorization(HTTPAuthScheme.BEARER, appKey));
        hmci.setContent(GSONUtil.toJSONDefault(payload));


        return hmci;

    }


    public static void main(String ...args)
    {


        try
        {
            ParamUtil.ParamMap params = ParamUtil.parse("=", args);
            String url = params.stringValue("gpt-url");
            String apiKey = params.stringValue("gpt-key");
            String content = params.stringValue("content");
            String prompt = params.stringValue("prompt");
            String model = params.stringValue("gpt-model");
            String contentType = params.stringValue("content-type");
            System.out.println(params);

            UByteArrayOutputStream baosImage= IOUtil.inputStreamToByteArray(content, true);
            NVGenericMap message = toData(model, prompt, contentType, 300, baosImage);
            HTTPMessageConfigInterface hmci = toHMCI(url, HTTPMethod.POST, apiKey, message);


            HTTPResponseData rd = HTTPCall.send(hmci);
            System.out.println(rd);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
