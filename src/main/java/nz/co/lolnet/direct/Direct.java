/*
 * Copyright 2018 lolnet.co.nz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.co.lolnet.direct;

import com.google.common.base.Stopwatch;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import nz.co.lolnet.direct.commands.DirectCommand;
import nz.co.lolnet.direct.commands.LobbyCommand;
import nz.co.lolnet.direct.commands.ModListCommand;
import nz.co.lolnet.direct.configuration.Config;
import nz.co.lolnet.direct.listeners.DirectListener;
import nz.co.lolnet.direct.managers.ModManager;
import nz.co.lolnet.direct.managers.ServerManager;
import nz.co.lolnet.direct.storage.mysql.MySQLQuery;
import nz.co.lolnet.direct.util.Reference;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Direct extends Plugin {
    
    private static Direct instance;
    private final Config config = new Config();
    
    @Override
    public void onEnable() {
        instance = this;
        reload();
        getProxy().getPluginManager().registerCommand(getInstance(), new DirectCommand());
        getProxy().getPluginManager().registerCommand(getInstance(), new LobbyCommand());
        getProxy().getPluginManager().registerCommand(getInstance(), new ModListCommand());
        getProxy().getPluginManager().registerListener(getInstance(), new DirectListener());
        getLogger().info(Reference.PLUGIN_NAME + " v" + Reference.PLUGIN_VERSION + " loaded");
    }
    
    @Override
    public void onDisable() {
        getLogger().info(Reference.PLUGIN_NAME + " v" + Reference.PLUGIN_VERSION + " unloaded");
    }
    
    public boolean reload() {
        getConfig().loadConfig();
        if (!getConfiguration().isPresent()) {
            getLogger().severe("Cannot load direct as the configuration is null!");
            return false;
        }
        
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (MySQLQuery.createTables() && ModManager.prepareMods() && ServerManager.prepareServers()) {
            ServerManager.registerServers();
            getLogger().info("Successful build after " + stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) + "ms");
            return true;
        } else {
            getLogger().severe("Failed build after " + stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) + "ms");
            return false;
        }
    }
    
    public void debugMessage(String msg) {
        if (getConfiguration().map(configuration -> configuration.getBoolean("Direct.Debug")).orElse(false)) {
            getLogger().info(msg);
        }
    }
    
    public static Direct getInstance() {
        return instance;
    }
    
    private Config getConfig() {
        return config;
    }
    
    public Optional<Configuration> getConfiguration() {
        if (getConfig() != null) {
            return Optional.ofNullable(getConfig().getConfiguration());
        }
        
        return Optional.empty();
    }
}