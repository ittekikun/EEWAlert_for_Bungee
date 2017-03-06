package com.ittekikun.bplugin.eewalert;

import com.ittekikun.plugin.eewalert.APIKey;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.ittekikun.bplugin.eewalert.EEW.AlarmType.GENERAL;

public class EEWAlert extends Plugin
{
    public APIKey apiKey;
    public EEWAlertConfig eewAlertConfig;
    public TwitterManager twitterManager;
    public static final String prefix = "[EEWAlert] ";
    public static ArrayList<String> notifiedEEWIDList = new ArrayList<>();
    public static Logger log;


    @Override
    public void onEnable()
    {
        log = Logger.getLogger("EEWAlert");
        log.setFilter(new LogFilter(prefix));

        eewAlertConfig = new EEWAlertConfig(this);
        eewAlertConfig.loadConfig();

        apiKey = loadAPIkey();

        twitterManager = new TwitterManager(this);
        twitterManager.startSetup();
    }

    @Override
    public void onDisable()
    {
        twitterManager.shutdownRecieveStream();
    }

    public APIKey loadAPIkey()
    {
        try
        {
            return Utility.decodeAPIKey(getResourceAsStream("imas"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public void noticeEewMessage(final EEW eew)
    {
        List<String> eewMes = new ArrayList<String>();

        //通知済か判定
        boolean notified = (!(notifiedEEWIDList.lastIndexOf(eew.getEewArray()[5]) == -1));

        if(eew.alarmType == GENERAL)
        {
            //TODO ddd
            if(!notified && !eew.isRetweet())
            {
                eewMes.add(ChatColor.RED +    "----------緊急地震速報----------");

                eewMes.add(ChatColor.YELLOW + "発表時刻: " + ChatColor.WHITE + eew.getOccurrenceTime());
                eewMes.add(ChatColor.YELLOW + "震源地: " + ChatColor.WHITE + eew.getEpicenter());
                eewMes.add(ChatColor.YELLOW + "マグニチュード: " + ChatColor.WHITE + eew.getMagnitude());
                eewMes.add(ChatColor.YELLOW + "深さ: " + ChatColor.WHITE + eew.getDepth()+ "km");
                eewMes.add(ChatColor.YELLOW + "最大震度: " + ChatColor.WHITE + eew.getMaxScale());
                if(eew.focusType == EEW.FocusType.LAND)
                {
                    eewMes.add(ChatColor.YELLOW + "震源海陸判定: " + ChatColor.WHITE + "陸");
                }
                else if(eew.focusType == EEW.FocusType.SEA)
                {
                    eewMes.add(ChatColor.YELLOW + "震源海陸判定: " + ChatColor.RED + "海");
                }

                eewMes.add(ChatColor.RED +    "震源地付近にお住まいの方は大きな地震に注意してください。");
                if(eew.focusType == EEW.FocusType.SEA)
                {
                    eewMes.add(ChatColor.YELLOW +    "※※※震源が海の為、津波が発生する可能性があります。※※※");

                }
                eewMes.add(ChatColor.RED +    "この情報を鵜呑みにせず、テレビ・ラジオ等で正確な情報を収集してください。");
                eewMes.add(ChatColor.RED +    "※この情報は震度速報ではありません。あくまでも、地震の規模を早期に推定するものです。");

                eewMes.add(ChatColor.RED +    "--------------------------------");

                //追加機能
                getProxy().getScheduler().runAsync(this, new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (eewAlertConfig.sendTitle)
                                {
                                    Title title = getProxy().createTitle();
                                    title.fadeIn(0);
                                    title.stay(20 * 20);
                                    title.fadeOut(0);
                                    title.title(new TextComponent(ChatColor.RED + "緊急地震速報が発表されました"));
                                    title.subTitle(new TextComponent(ChatColor.YELLOW + eew.getEpicenter() + "付近にお住まいの方は強い揺れに注意して下さい"));

                                    for (ProxiedPlayer proxiedPlayer : getProxy().getInstance().getPlayers())
                                    {
                                        proxiedPlayer.sendTitle(title);
                                    }
                                }
                            }
                        });

                EEWAlert.log.info("緊急地震速報を受信しました。");

                //通知済リストへ追加
                notifiedEEWIDList.add(eew.getEewArray()[5]);
            }
            else if(eew.isRetweet() && eewAlertConfig.demoMode)
            {
                eewMes.add(ChatColor.RED +    "----------緊急地震速報(動作確認モード有効中)----------");

                eewMes.add(ChatColor.RED +    "テレビなどで普段発表されていない緊急地震速報を表示しています。");
                eewMes.add(ChatColor.RED +    "過去に発表された緊急地震速報を表示している可能性があります。");
                eewMes.add(ChatColor.RED +    "念の為、テレビ・ラジオ等で正確な情報を収集してください。");
                eewMes.add(ChatColor.RED +    "動作確認出来し次第、動作確認モードを無効にして下さい。");

                eewMes.add(ChatColor.RED + "※これはリツイートされたツイートを表示しています");

                eewMes.add(ChatColor.YELLOW + "発表時刻: " + ChatColor.WHITE + eew.getOccurrenceTime());
                eewMes.add(ChatColor.YELLOW + "震源地: " + ChatColor.WHITE + eew.getEpicenter());
                eewMes.add(ChatColor.YELLOW + "マグニチュード: " + ChatColor.WHITE + eew.getMagnitude());
                eewMes.add(ChatColor.YELLOW + "深さ: " + ChatColor.WHITE + eew.getDepth() + "km");
                eewMes.add(ChatColor.YELLOW + "最大震度: " + ChatColor.WHITE + eew.getMaxScale());
                if(eew.focusType == EEW.FocusType.LAND)
                {
                    eewMes.add(ChatColor.YELLOW + "震源海陸判定: " + ChatColor.WHITE + "陸");
                }
                else if(eew.focusType == EEW.FocusType.SEA)
                {
                    eewMes.add(ChatColor.YELLOW + "震源海陸判定: " + ChatColor.RED + "海");
                }

                if(eew.focusType == EEW.FocusType.SEA)
                {
                    eewMes.add(ChatColor.YELLOW +    "※※※震源が海の為、津波が発生する可能性があります。※※※");

                }

                eewMes.add(ChatColor.RED +    "※この情報は震度速報ではありません。あくまでも、地震の規模を早期に推定するものです。");

                eewMes.add(ChatColor.RED +    "--------------------------------");

                //追加機能
                getProxy().getScheduler().runAsync(this, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (eewAlertConfig.sendTitle)
                        {
                            Title title = getProxy().createTitle();
                            title.fadeIn(0);
                            title.stay(20 * 20);
                            title.fadeOut(0);
                            title.title(new TextComponent(ChatColor.RED + "(動作試験モード有効中)緊急地震速報が発表されました"));
                            title.subTitle(new TextComponent(ChatColor.YELLOW + eew.getEpicenter() + "付近にお住まいの方は強い揺れに注意して下さい"));

                            for (ProxiedPlayer proxiedPlayer : getProxy().getInstance().getPlayers())
                            {
                                proxiedPlayer.sendTitle(title);
                            }
                        }
                    }
                });

                EEWAlert.log.info("緊急地震速報を受信しました。(動作確認モード)");
            }
        }
        else if(eewAlertConfig.demoMode)
        {
            eewMes.add(ChatColor.RED +    "----------緊急地震速報(動作確認モード有効中)----------");

            eewMes.add(ChatColor.RED +    "テレビなどで普段発表されていない緊急地震速報を表示しています。");
            eewMes.add(ChatColor.RED +    "過去に発表された緊急地震速報を表示している可能性があります。");
            eewMes.add(ChatColor.RED +    "念の為、テレビ・ラジオ等で正確な情報を収集してください。");
            eewMes.add(ChatColor.RED +    "動作確認出来し次第、動作確認モードを無効にして下さい。");

            if(eew.isRetweet())
            {
                eewMes.add(ChatColor.RED + "※これはリツイートされたツイートを表示しています");
            }

            eewMes.add(ChatColor.YELLOW + "発表時刻: " + ChatColor.WHITE + eew.getOccurrenceTime());
            eewMes.add(ChatColor.YELLOW + "震源地: " + ChatColor.WHITE + eew.getEpicenter());
            eewMes.add(ChatColor.YELLOW + "マグニチュード: " + ChatColor.WHITE + eew.getMagnitude());
            eewMes.add(ChatColor.YELLOW + "深さ: " + ChatColor.WHITE + eew.getDepth() + "km");
            eewMes.add(ChatColor.YELLOW + "最大震度: " + ChatColor.WHITE + eew.getMaxScale());
            if(eew.focusType == EEW.FocusType.LAND)
            {
                eewMes.add(ChatColor.YELLOW + "震源海陸判定: " + ChatColor.WHITE + "陸");
            }
            else if(eew.focusType == EEW.FocusType.SEA)
            {
                eewMes.add(ChatColor.YELLOW + "震源海陸判定: " + ChatColor.RED + "海");
            }

            eewMes.add(ChatColor.RED +    "※この情報は震度速報ではありません。あくまでも、地震の規模を早期に推定するものです。");

            eewMes.add(ChatColor.RED +    "--------------------------------");

            //追加機能
            getProxy().getScheduler().runAsync(this, new Runnable()
            {
                @Override
                public void run()
                {
                    if (eewAlertConfig.sendTitle)
                    {
                        Title title = getProxy().createTitle();
                        title.fadeIn(0);
                        title.stay(20 * 20);
                        title.fadeOut(0);
                        title.title(new TextComponent(ChatColor.RED + "(動作試験モード)緊急地震速報が発表されました"));
                        title.subTitle(new TextComponent(ChatColor.YELLOW + eew.getEpicenter() + "付近にお住まいの方は強い揺れに注意して下さい"));

                        for (ProxiedPlayer proxiedPlayer : getProxy().getInstance().getPlayers())
                        {
                            proxiedPlayer.sendTitle(title);
                        }
                    }
                }
            });

            EEWAlert.log.info("緊急地震速報を受信しました。(動作確認モード)");
        }
        else
        {
            return;
        }

        broadcastMessage(eewMes);
    }

    public void broadcastMessage(final List<String> eewMes)
    {
        getProxy().getScheduler().runAsync(this, new Runnable()
        {
            @Override
            public void run()
            {
                for(String line : eewMes)
                {
                    getProxy().broadcast(new TextComponent(line));
                }
            }
        });
    }
}