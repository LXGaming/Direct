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

package io.github.lxgaming.direct.bungee.util;

import io.github.lxgaming.direct.bungee.BungeePlugin;
import io.github.lxgaming.direct.common.entity.Server;
import io.github.lxgaming.direct.common.entity.Source;
import io.github.lxgaming.direct.common.manager.DirectManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Collection;
import java.util.Map;

public class BungeeToolbox {
    
    public static String convertColor(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
    
    public static ServerInfo getLobby(Source source) {
        Server server = DirectManager.getLobby(source);
        if (server != null) {
            return getServer(server);
        }
        
        return null;
    }
    
    public static ServerInfo getServer(Server server) {
        return BungeePlugin.getInstance().getProxy().getServerInfo(server.getName());
    }
    
    public static Server getServer(net.md_5.bungee.api.connection.Server server) {
        if (server != null) {
            return getServer(server.getInfo());
        }
        
        return null;
    }
    
    public static Server getServer(ServerInfo serverInfo) {
        return DirectManager.getServer(serverInfo.getName());
    }
    
    public static Map<String, ServerInfo> getProxyServers() {
        return ProxyServer.getInstance().getConfig().getServers();
    }
    
    public static Collection<ListenerInfo> getProxyListeners() {
        return ProxyServer.getInstance().getConfig().getListeners();
    }
}