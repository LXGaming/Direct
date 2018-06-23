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

package nz.co.lolnet.direct.managers;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import nz.co.lolnet.direct.Direct;
import nz.co.lolnet.direct.data.ServerData;
import nz.co.lolnet.direct.storage.mysql.MySQLQuery;
import nz.co.lolnet.direct.util.Toolbox;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ServerManager {
    
    private static final Random RANDOM = new Random();
    private static final List<ServerData> SERVERS = Collections.synchronizedList(Toolbox.newArrayList());
    
    public static boolean prepareServers() {
        List<ServerData> servers = MySQLQuery.getServers();
        if (servers == null || servers.isEmpty()) {
            return false;
        }
        
        getServers().clear();
        getServers().addAll(servers);
        return true;
    }
    
    public static void registerServers() {
        List<String> serverPriority = Toolbox.newArrayList();
        Map<String, ServerInfo> proxyServers = Toolbox.newHashMap();
        Map<String, String> forcedHosts = Toolbox.newHashMap();
        
        for (ServerData serverData : getServers()) {
            ServerInfo serverInfo = serverData.buildServerInfo();
            if (serverInfo == null) {
                continue;
            }
            
            proxyServers.put(serverInfo.getName(), serverInfo);
            if (serverData.isLobby()) {
                serverPriority.add(serverInfo.getName());
            }
            
            for (String directConnect : serverData.getDirectConnects()) {
                String value = forcedHosts.putIfAbsent(directConnect, serverInfo.getName());
                if (Toolbox.isNotBlank(value)) {
                    Direct.getInstance().getLogger().warning("Direct connect " + directConnect + " is already assigned to " + value);
                }
            }
        }
        
        Toolbox.getProxyServers().clear();
        Toolbox.getProxyServers().putAll(proxyServers);
        
        for (ListenerInfo listenerInfo : Toolbox.getProxyListeners()) {
            listenerInfo.getServerPriority().clear();
            listenerInfo.getServerPriority().addAll(serverPriority);
            listenerInfo.getForcedHosts().clear();
            listenerInfo.getForcedHosts().putAll(forcedHosts);
        }
        
        Direct.getInstance().getLogger().info("Successfully registered " + proxyServers.size() + " Servers");
    }
    
    public static boolean isAccessible(ServerData serverData, ProxiedPlayer proxiedPlayer) {
        return !serverData.isRestricted() || proxiedPlayer.hasPermission("bungeecord.server." + serverData.getName());
    }
    
    public static boolean isProtocolSupported(ServerData serverData, ProxiedPlayer proxiedPlayer) {
        return serverData.getProtocolVersions().isEmpty() || serverData.getProtocolVersions().contains(proxiedPlayer.getPendingConnection().getVersion());
    }
    
    public static ServerInfo getLobby(ProxiedPlayer proxiedPlayer) {
        List<ServerInfo> servers = Toolbox.newArrayList();
        for (ServerData serverData : getServers()) {
            if (!serverData.isActive() || !serverData.isLobby() || !isAccessible(serverData, proxiedPlayer) || !isProtocolSupported(serverData, proxiedPlayer)) {
                continue;
            }
            
            ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(serverData.getName());
            if (serverInfo != null && (proxiedPlayer.getServer() == null || serverInfo != proxiedPlayer.getServer().getInfo())) {
                servers.add(serverInfo);
            }
        }
        
        if (!servers.isEmpty()) {
            return servers.get(getRandom().nextInt(servers.size()));
        }
        
        return null;
    }
    
    public static ServerData getServer(String name) {
        for (ServerData serverData : getServers()) {
            if (serverData.isActive() && Toolbox.isNotBlank(serverData.getName()) && serverData.getName().equals(name)) {
                return serverData;
            }
        }
        
        return null;
    }
    
    public static Random getRandom() {
        return RANDOM;
    }
    
    public static List<ServerData> getServers() {
        return SERVERS;
    }
}