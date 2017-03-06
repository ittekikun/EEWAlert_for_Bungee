package com.ittekikun.bplugin.eewalert;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;

public class ConfigAccessor
{
    private final String fileName;
    private final EEWAlert plugin;

    private File configFile;
    private Configuration configuration;

    public ConfigAccessor(EEWAlert plugin, String fileName)
    {
        if(plugin == null)
        {
            throw new IllegalArgumentException("plugin cannot be null");
        }
        this.plugin = plugin;
        this.fileName = fileName;
        File dataFolder = plugin.getDataFolder();
        if(dataFolder == null)
        {
            throw new IllegalStateException();
        }
        this.configFile = new File(plugin.getDataFolder(), fileName);
    }

    public void reloadConfig() throws IOException
    {
        configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
    }

    public Configuration getConfig()
    {
        if(configuration == null)
        {
            try
            {
                this.reloadConfig();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return configuration;
    }

    public void saveConfig()
    {
        if(configuration == null || configFile == null)
        {
            return;
        }
        else
        {
            try
            {
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, configFile);
            }
            catch(IOException ex)
            {
                EEWAlert.log.severe("Could not save config to " + configFile);
                ex.printStackTrace();
            }
        }
    }

    public void saveDefaultConfig()
    {
        if(!configFile.exists())
        {
            if (!plugin.getDataFolder().exists())
            {
                plugin.getDataFolder().mkdir();
            }
            if (!configFile.exists())
            {
                try
                {
                    InputStream is = plugin.getResourceAsStream(fileName);
                    Files.copy(is, configFile.toPath());
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Unable to create configuration file", e);
                }
            }
        }
    }
}