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

package io.github.lxgaming.direct.common.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.lxgaming.direct.common.Direct;
import io.github.lxgaming.direct.common.configuration.Config;
import io.github.lxgaming.direct.common.configuration.category.LogCategory;
import io.github.lxgaming.direct.common.entity.Mod;
import io.github.lxgaming.direct.common.entity.Server;
import io.github.lxgaming.direct.common.entity.Source;
import io.github.lxgaming.direct.common.util.StringUtils;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class DirectManager {
    
    public static final Map<String, String> FORCED_HOSTS = Maps.newConcurrentMap();
    public static final List<String> SERVER_PRIORITY = Lists.newCopyOnWriteArrayList();
    public static final List<Server> SERVERS = Lists.newCopyOnWriteArrayList();
    private static final List<Mod> MODS = Lists.newArrayList();
    private static final Random RANDOM = new Random();
    
    public static boolean prepare() {
        FORCED_HOSTS.clear();
        SERVER_PRIORITY.clear();
        SERVERS.clear();
        MODS.clear();
        
        List<Mod> mods = Direct.getInstance().getStorage().getQuery().getMods();
        if (mods == null) {
            Direct.getInstance().getLogger().error("Failed to get mods");
            return false;
        }
        
        MODS.addAll(mods);
        
        List<Server> servers = Direct.getInstance().getStorage().getQuery().getServers();
        if (servers == null) {
            Direct.getInstance().getLogger().error("Failed to get servers");
            return false;
        }
        
        servers.removeIf(server -> {
            if (StringUtils.isBlank(server.getName())) {
                Direct.getInstance().getLogger().warn("Server name is blank");
                return true;
            }
            
            if (StringUtils.isBlank(server.getHost()) || server.getPort() <= 0 || server.getPort() > 65535) {
                Direct.getInstance().getLogger().warn("Server {} address is invalid", server.getName());
                return true;
            }
            
            return false;
        });
        
        SERVERS.addAll(servers);
        return !SERVERS.isEmpty();
    }
    
    public static void execute() {
        for (Server server : SERVERS) {
            if (!server.isActive()) {
                continue;
            }
            
            if (server.isLobby()) {
                SERVER_PRIORITY.add(server.getName());
            }
            
            buildForcedHosts(server);
        }
    }
    
    public static void buildForcedHosts(Server server) {
        for (String directConnect : server.getDirectConnects()) {
            String value = FORCED_HOSTS.putIfAbsent(directConnect, server.getName());
            if (StringUtils.isNotBlank(value)) {
                Direct.getInstance().getLogger().error("Direct connect {} is already assigned to {}", directConnect, value);
            }
        }
    }
    
    public static void checkMods(Source source, Map<String, String> mods) {
        if (source.hasPermission("direct.bypass.mods")) {
            return;
        }
        
        List<Mod> detectedMods = Lists.newArrayList();
        for (Map.Entry<String, String> entry : mods.entrySet()) {
            Mod mod = getMod(entry.getKey());
            if (mod == null || mod.getExecution().isEmpty()) {
                continue;
            }
            
            Direct.getInstance().getLogger().warn("{} connected with {} ({})", source.getName(), mod.getName(), mod.getId());
            detectedMods.add(mod);
            
            String serverName;
            if (source.getCurrentServer() != null) {
                serverName = source.getCurrentServer().getName();
            } else {
                serverName = "Unknown";
            }
            
            for (String execution : mod.getExecution()) {
                execution = execution
                        .replace("[ID]", mod.getId())
                        .replace("[NAME]", mod.getName())
                        .replace("[PLAYER]", source.getName())
                        .replace("[SERVER]", serverName);
                
                if (StringUtils.isBlank(execution) || !Direct.getPlatform().executeCommand(execution)) {
                    Direct.getInstance().getLogger().error("{} execution failed: {}", mod.getId(), execution);
                }
            }
        }
        
        if (detectedMods.isEmpty()) {
            return;
        }
        
        if (Direct.getInstance().getConfig().map(Config::getLogCategory).map(LogCategory::isDetectionModList).orElse(false)) {
            Direct.getPlatform().executeAsync(() -> {
                Direct.getInstance().getStorage().getQuery().createLog(source.getUniqueId(), "MODLIST", new Gson().toJson(mods));
            });
        }
        
        if (Direct.getInstance().getConfig().map(Config::getLogCategory).map(LogCategory::isDetectionMods).orElse(false)) {
            JsonObject jsonObject = new JsonObject();
            detectedMods.forEach(mod -> jsonObject.addProperty(mod.getId(), mod.getName()));
            
            Direct.getPlatform().executeAsync(() -> {
                Direct.getInstance().getStorage().getQuery().createLog(source.getUniqueId(), "DETECTION", new Gson().toJson(jsonObject));
            });
        }
    }
    
    public static Mod getMod(String id) {
        List<Mod> mods = Lists.newArrayList();
        for (Mod mod : MODS) {
            if (StringUtils.containsIgnoreCase(id, mod.getId())) {
                mods.add(mod);
            }
        }
        
        if (mods.isEmpty()) {
            return null;
        }
        
        if (mods.size() == 1) {
            return mods.get(0);
        }
        
        for (Iterator<Mod> iterator = mods.iterator(); iterator.hasNext(); ) {
            Mod mod = iterator.next();
            if (mod.getId().equalsIgnoreCase(id)) {
                return mod;
            }
            
            if (mod.getExecution().isEmpty()) {
                iterator.remove();
            }
        }
        
        if (!mods.isEmpty()) {
            return mods.get(0);
        }
        
        return null;
    }
    
    public static boolean isAccessible(Source source, Server server) {
        return !server.isRestricted() || source.hasPermission(String.format("%s.server.%s", Direct.getPlatform().getType(), server.getName()));
    }
    
    public static boolean isProtocolSupported(Source source, Server server) {
        return server.getProtocolVersions().isEmpty() || server.getProtocolVersions().contains(source.getProtocolVersion());
    }
    
    public static Server getLobby(Source source) {
        Server currentServer = source.getCurrentServer();
        List<Server> servers = Lists.newArrayList();
        for (Server server : SERVERS) {
            if (!server.isActive() || !server.isLobby() || !isAccessible(source, server) || !isProtocolSupported(source, server)) {
                continue;
            }
            
            if (server != currentServer) {
                servers.add(server);
            }
        }
        
        if (!servers.isEmpty()) {
            return servers.get(RANDOM.nextInt(servers.size()));
        }
        
        return null;
    }
    
    public static Server getServer(String name) {
        for (Server server : SERVERS) {
            if (server.isActive() && StringUtils.isNotBlank(server.getName()) && server.getName().equals(name)) {
                return server;
            }
        }
        
        return null;
    }
    
    public static Server getServer(InetSocketAddress address) {
        String host = address.getHostString();
        if (StringUtils.isBlank(host)) {
            return null;
        }
        
        String name = FORCED_HOSTS.get(host);
        if (StringUtils.isBlank(name)) {
            return null;
        }
        
        return getServer(name);
    }
}