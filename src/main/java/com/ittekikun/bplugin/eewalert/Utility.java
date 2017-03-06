package com.ittekikun.bplugin.eewalert;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.ittekikun.plugin.eewalert.APIKey;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Utility
{
    /**
     * ArrayUnion
     *
     * @param par1 繋げたい配列（配列String型）
     * @param par2 どこの配列から繋げたいか（int型）
     */
    public static String JoinArray(String[] par1, int par2)
    {
        StringBuilder stringBuilder = new StringBuilder();

        for (int a = par2; a < par1.length; ++a)
        {
            if (a > par2)
            {
                stringBuilder.append(" ");
            }

            String s = par1[a];

            stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }

    /**
     * 文字列が整数値に変換可能かどうかを判定する
     *
     * @param source 変換対象の文字列
     * @return 整数に変換可能かどうか
     * @author https://github.com/ucchyocean/
     */
    public static boolean checkIntParse(String source)
    {

        return source.matches("^-?[0-9]{1,9}$");
    }

    /**
     * 短縮URL生成
     *
     * @param longUrl
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public static String getShortUrl(String longUrl, String apikey) throws ClientProtocolException, IOException
    {
        HttpPost post = new HttpPost("https://www.googleapis.com/urlshortener/v1/url?key=" + apikey);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity("{'longUrl': '"+longUrl+"'}", "UTF-8"));

        HttpResponse response = new DefaultHttpClient().execute(post);

        String responseText = EntityUtils.toString(response.getEntity());

        // JsonFactoryの生成
        JsonFactory factory = new JsonFactory();
        // JsonParserの取得
        @SuppressWarnings("deprecation")
        JsonParser parser = factory.createJsonParser(responseText);

        //JSONのパース処理
        String shotUrl = "";
        while (parser.nextToken() != JsonToken.END_OBJECT)
        {
            String name = parser.getCurrentName();
            if (name != null)
            {
                parser.nextToken();
                if (name.equals("id"))
                {
                    shotUrl = parser.getText();
                }
            }
        }
        return shotUrl;
    }

    public static APIKey decodeAPIKey(InputStream inputStream) throws IOException, ClassNotFoundException
    {
        byte[] indata = new byte[4096];
        inputStream.read(indata);
        inputStream.close();

        byte[] outdata = Base64.decodeBase64(indata);

        ByteArrayInputStream bais = new ByteArrayInputStream(outdata);
        ObjectInputStream ois = new ObjectInputStream(bais);
        APIKey apiKey = (APIKey)ois.readObject();
        bais.close();
        ois.close();

        return apiKey;
    }

    /**
     * HTTPサーバー上のテキストの内容を読み込む
     *
     * @param par1 URL
     * @return テキストをListで返す
     */
    /**
     * HTTPサーバー上のテキストの内容を読み込む
     *
     * @param par1 URL
     * @return テキストをListで返す
     */
    public static List getHttpServerText(String par1) throws IOException
    {
        URL url = new URL(par1);
        InputStream i = url.openConnection().getInputStream();

        BufferedReader buf = new BufferedReader(new InputStreamReader(i, "UTF-8"));

        String line;
        List<String> arrayList = new ArrayList();

        while ((line = buf.readLine()) != null)
        {
            arrayList.add(line);
        }
        buf.close();

        return arrayList;
    }
}