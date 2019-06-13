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

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import io.github.lxgaming.direct.bungee.commands.DirectCommand;
import io.github.lxgaming.direct.bungee.commands.LobbyCommand;
import io.github.lxgaming.direct.bungee.commands.ModListCommand;
import io.github.lxgaming.direct.bungee.listeners.DirectListener;
import io.github.lxgaming.direct.bungee.util.BungeeToolbox;
import io.github.lxgaming.direct.bungee.util.BungeeUser;
import io.github.lxgaming.direct.bungee.util.DirectCommandSender;
import io.github.lxgaming.direct.common.Direct;
import io.github.lxgaming.direct.common.configuration.Config;
import io.github.lxgaming.direct.common.data.Message;
import io.github.lxgaming.direct.common.data.Platform;
import io.github.lxgaming.direct.common.data.ServerData;
import io.github.lxgaming.direct.common.data.User;
import io.github.lxgaming.direct.common.manager.DirectManager;
import io.github.lxgaming.direct.common.manager.MCLeaksManager;
import io.github.lxgaming.direct.common.util.Logger;
import io.github.lxgaming.direct.common.util.Reference;
import io.github.lxgaming.direct.common.util.Toolbox;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Map;

public class BungeePlugin extends Plugin implements Platform {
    
    private static BungeePlugin instance;
    
    @Override
    public void onEnable() {
        instance = this;
        Direct direct = new Direct(this);
        direct.getLogger()
                .add(Logger.Level.INFO, getLogger()::info)
                .add(Logger.Level.WARN, getLogger()::warning)
                .add(Logger.Level.ERROR, getLogger()::severe)
                .add(Logger.Level.DEBUG, message -> {
                    if (Direct.getInstance().getConfig().map(Config::isDebug).orElse(false)) {
                        getLogger().info(message);
                    }
                });
        
        direct.loadDirect();
        getProxy().getPluginManager().registerCommand(getInstance(), new DirectCommand());
        getProxy().getPluginManager().registerCommand(getInstance(), new LobbyCommand());
        getProxy().getPluginManager().registerCommand(getInstance(), new ModListCommand());
        getProxy().getPluginManager().registerListener(getInstance(), new DirectListener());
    }
    
    @Override
    public void onDisable() {
        MCLeaksManager.shutdown();
        Direct.getInstance().getStorage().close();
        Direct.getInstance().getLogger().info("{} v{} unloaded", Reference.NAME, Reference.VERSION);
    }
    
    @Override
    public void registerServers() {
        // Building
        Map<String, ServerInfo> proxyServers = Toolbox.newHashMap();
        for (ServerData serverData : DirectManager.getServers()) {
            if (Toolbox.isBlank(serverData.getName())) {
                Direct.getInstance().getLogger().warn("Cannot build ServerInfo as the name is blank");
                continue;
            }
            
            if (Toolbox.isBlank(serverData.getHost()) || serverData.getPort() < 0 || serverData.getPort() > 65535) {
                Direct.getInstance().getLogger().warn("Cannot build ServerInfo for {} as the address is invalid", serverData.getName());
                continue;
            }
            
            if (!serverData.isActive()) {
                continue;
            }
            
            InetSocketAddress address = Toolbox.parseAddress(serverData.getHost(), serverData.getPort()).orElse(null);
            if (address == null) {
                Direct.getInstance().getLogger().warn("Cannot build ServerInfo for {} as the address couldn't be parsed", serverData.getName());
                continue;
            }
            
            ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(serverData.getName(), address, BungeeToolbox.convertColor(serverData.getMotd()), serverData.isRestricted());
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
            listenerInfo.getServerPriority().addAll(DirectManager.getServerPriority());
            
            // Forced Hosts
            listenerInfo.getForcedHosts().clear();
            listenerInfo.getForcedHosts().putAll(DirectManager.getForcedHosts());
        }
        
        Direct.getInstance().getLogger().info("Successfully registered {} Servers", proxyServers.size());
        
        // Players
        for (ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers()) {
            User user = BungeeUser.of(proxiedPlayer.getUniqueId());
            ServerData serverData = user.getCurrentServer().orElse(null);
            Message.Builder messageBuilder = Message.builder();
            if (serverData == null) {
                messageBuilder.type(Message.Type.REMOVED);
            } else if (!DirectManager.isAccessible(user, serverData)) {
                messageBuilder.type(Message.Type.RESTRICTED).server(serverData.getName());
            } else if (!DirectManager.isProtocolSupported(user, serverData)) {
                messageBuilder.type(Message.Type.INCOMPATIBLE).server(serverData.getName());
            } else {
                continue;
            }
            
            ServerInfo lobby = DirectManager.getLobby(user).flatMap(BungeeToolbox::getServer).orElse(null);
            if (lobby != null) {
                proxiedPlayer.connect(lobby);
                user.sendMessage(messageBuilder.build());
            } else {
                user.disconnect(Message.builder().type(Message.Type.FAIL).build());
            }
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
    public Platform.Type getType() {
        return Type.BUNGEE;
    }
    
    @Override
    public Path getPath() {
        return getDataFolder().toPath();
    }
    
    public static BungeePlugin getInstance() {
        return instance;
    }
}