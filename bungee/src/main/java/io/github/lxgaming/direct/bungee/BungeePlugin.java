/*
 * Copyright 2018 Alex Thomson
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

package io.github.lxgaming.direct.bungee;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.lxgaming.direct.bungee.command.DirectCommand;
import io.github.lxgaming.direct.bungee.command.LobbyCommand;
import io.github.lxgaming.direct.bungee.entity.BungeeSource;
import io.github.lxgaming.direct.bungee.listener.BungeeListener;
import io.github.lxgaming.direct.bungee.util.BungeeToolbox;
import io.github.lxgaming.direct.bungee.util.DirectCommandSender;
import io.github.lxgaming.direct.common.Direct;
import io.github.lxgaming.direct.common.Platform;
import io.github.lxgaming.direct.common.entity.Locale;
import io.github.lxgaming.direct.common.entity.Server;
import io.github.lxgaming.direct.common.entity.Source;
import io.github.lxgaming.direct.common.manager.DirectManager;
import io.github.lxgaming.direct.common.storage.Storage;
import io.github.lxgaming.direct.common.util.Toolbox;
import io.github.lxgaming.direct.common.util.text.adapter.LocaleAdapter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BungeePlugin extends Plugin implements Platform {
    
    private static BungeePlugin instance;
    
    @Override
    public void onEnable() {
        instance = this;
        
        if (getProxy().getName().equalsIgnoreCase("BungeeCord")) {
            getLogger().severe("\n\n"
                    + "  BungeeCord is not supported - https://github.com/SpigotMC/BungeeCord/pull/1877\n"
                    + "\n"
                    + "  Use Waterfall - https://github.com/PaperMC/Waterfall\n"
            );
            return;
        }
        
        Direct direct = new Direct(this);
        direct.load();
        
        getProxy().getPluginManager().registerCommand(getInstance(), new DirectCommand());
        getProxy().getPluginManager().registerCommand(getInstance(), new LobbyCommand());
        getProxy().getPluginManager().registerListener(getInstance(), new BungeeListener());
    }
    
    @Override
    public void onDisable() {
        if (!Direct.isAvailable()) {
            return;
        }
        
        Storage storage = Direct.getInstance().getStorage();
        if (storage != null && !storage.isClosed()) {
            storage.close();
        }
        
        Direct.getInstance().getLogger().info("{} v{} unloaded", Direct.NAME, Direct.VERSION);
    }
    
    @Override
    public void registerServers() {
        // Building
        Map<String, ServerInfo> proxyServers = Maps.newHashMap();
        for (Server server : DirectManager.SERVERS) {
            if (!server.isActive()) {
                continue;
            }
            
            InetSocketAddress address = Toolbox.parseAddress(server.getHost(), server.getPort());
            if (address == null) {
                server.setActive(false);
                Direct.getInstance().getLogger().warn("Failed to parse address for server {}", server.getName());
                continue;
            }
            
            ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(server.getName(), address, BungeeToolbox.convertColor(server.getMotd()), server.isRestricted());
            if (serverInfo == null) {
                continue;
            }
            
            proxyServers.put(serverInfo.getName(), serverInfo);
        }
        
        // Servers
        BungeeToolbox.getProxyServers().clear();
        BungeeToolbox.getProxyServers().putAll(proxyServers);
        
        for (ListenerInfo listenerInfo : BungeeToolbox.getProxyListeners()) {
            // Priority
            listenerInfo.getServerPriority().clear();
            listenerInfo.getServerPriority().addAll(DirectManager.SERVER_PRIORITY);
            
            // Forced Hosts
            listenerInfo.getForcedHosts().clear();
            listenerInfo.getForcedHosts().putAll(DirectManager.FORCED_HOSTS);
        }
        
        Direct.getInstance().getLogger().info("Successfully registered {} Servers", proxyServers.size());
        
        // Players
        for (ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers()) {
            Source source = new BungeeSource(proxiedPlayer);
            
            Server currentServer = source.getCurrentServer();
            if (currentServer == null) {
                LocaleAdapter.sendSystemMessage(source, Locale.MESSAGE_REMOVED);
            } else if (!DirectManager.isAccessible(source, currentServer)) {
                LocaleAdapter.sendSystemMessage(source, Locale.MESSAGE_RESTRICTED, currentServer.getName());
            } else if (!DirectManager.isProtocolSupported(source, currentServer)) {
                LocaleAdapter.sendSystemMessage(source, Locale.MESSAGE_INCOMPATIBLE, currentServer.getName());
            } else {
                continue;
            }
            
            ServerInfo server = BungeeToolbox.getLobby(source);
            if (server != null) {
                proxiedPlayer.connect(server);
                continue;
            }
            
            LocaleAdapter.disconnect(source, Locale.MESSAGE_FAIL);
        }
    }
    
    @Override
    public void executeAsync(Runnable runnable) {
        ProxyServer.getInstance().getScheduler().runAsync(getInstance(), runnable);
    }
    
    @Override
    public boolean executeCommand(String command) {
        return ProxyServer.getInstance().getPluginManager().dispatchCommand(new DirectCommandSender(), command);
    }
    
    @Override
    public @NonNull Collection<String> getUsernames() {
        List<String> usernames = Lists.newArrayList();
        for (ProxiedPlayer player : getProxy().getPlayers()) {
            usernames.add(player.getName());
        }
        
        return usernames;
    }
    
    @Override
    public @NonNull Path getPath() {
        return getDataFolder().toPath();
    }
    
    @Override
    public @NonNull Type getType() {
        return Type.BUNGEECORD;
    }
    
    public static BungeePlugin getInstance() {
        return instance;
    }
}