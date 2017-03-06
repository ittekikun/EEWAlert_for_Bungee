package com.ittekikun.bplugin.eewalert;

import com.ittekikun.plugin.eewalert.APIKey;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

public class TwitterManager
{
    public EEWAlert eewAlert;
    public Twitter twitter;
    public TwitterStream eewStream;
    public RequestToken requestToken;
    public APIKey apiKey;

    public boolean canTweet = false;
    public boolean canAuth = false;

    public boolean streamStatus = false;

    public TwitterManager(EEWAlert eewAlert)
    {
        this.eewAlert = eewAlert;
        this.apiKey = eewAlert.apiKey;
    }

    public void startSetup()
    {
        ConfigurationBuilder builder = new ConfigurationBuilder();

        builder.setOAuthConsumerKey(apiKey.getIdol());
        builder.setOAuthConsumerSecret(apiKey.getMaster());
        Configuration conf = builder.build();

        AccessToken accessToken = null;

        twitter = new TwitterFactory(conf).getInstance();
        eewStream = new TwitterStreamFactory(conf).getInstance();

        accessToken = loadAccessToken();

        //初期起動時(ファイルなし)
        if(accessToken == null)
        {
            EEWAlert.log.warning("AccessToken.datがありません。");
        }
        //ファイル有り
        else
        {
            twitter.setOAuthAccessToken(accessToken);
            eewStream.setOAuthAccessToken(accessToken);

            startRecieveStream();

            System.out.println("受信を開始");

            canTweet = true;
        }
    }

    public URL createOAuthUrl()
    {
        URL url = null;

        try
        {
            requestToken = twitter.getOAuthRequestToken();
            url = new URL(requestToken.getAuthorizationURL());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return url;
    }

    public AccessToken loadAccessToken()
    {
        File f = createAccessTokenFileName();

        ObjectInputStream is = null;
        try
        {
            is = new ObjectInputStream(new FileInputStream(f));
            AccessToken accessToken = (AccessToken)is.readObject();
            return accessToken;
        }
        catch (IOException e)
        {
            return null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            if(is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public File createAccessTokenFileName()
    {
        String s = eewAlert.getDataFolder() + "/AccessToken.dat";
        return new File(s);
    }

    public void startRecieveStream()
    {
        eewStream.addListener(new EEWStream(eewAlert));
        eewStream.user();

        streamStatus = true;

        EEWAlert.log.info("ユーザーストリームに接続します。");

        //214358709 = @eewbot
        long[] list = {214358709L};
        FilterQuery query = new FilterQuery(list);
        eewStream.filter(query);
    }

    public void shutdownRecieveStream()
    {
        if(streamStatus)
        {
            eewStream.shutdown();
            EEWAlert.log.info("ユーザーストリームから切断しました。");

            streamStatus = false;
        }
    }
}