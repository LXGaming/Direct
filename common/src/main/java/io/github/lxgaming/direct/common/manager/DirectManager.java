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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.lxgaming.direct.common.Direct;
import io.github.lxgaming.direct.common.configuration.Config;
import io.github.lxgaming.direct.common.configuration.category.LogCategory;
import io.github.lxgaming.direct.common.data.ModData;
import io.github.lxgaming.direct.common.data.ServerData;
import io.github.lxgaming.direct.common.data.User;
import io.github.lxgaming.direct.common.util.Toolbox;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class DirectManager {
    
    private static final Random RANDOM = new Random();
    private static final List<ModData> MODS = Toolbox.newArrayList();
    private static final List<ServerData> SERVERS = Collections.synchronizedList(Toolbox.newArrayList());
    private static final List<String> SERVER_PRIORITY = Collections.synchronizedList(Toolbox.newArrayList());
    private static final Map<String, String> FORCED_HOSTS = Collections.synchronizedMap(Toolbox.newHashMap());
    
    public static void cleanDirect() {
        getForcedHosts().clear();
        getServerPriority().clear();
        getServers().clear();
        getMods().clear();
    }
    
    public static boolean prepareDirect() {
        try {
            if (!Direct.getInstance().getStorage().connect()) {
                Direct.getInstance().getLogger().error("Connection failed");
                return false;
            }
            
            if (!Direct.getInstance().getStorage().getQuery().createTables()) {
                Direct.getInstance().getLogger().error("Failed to create tables");
                return false;
            }
        } catch (Exception ex) {
            Direct.getInstance().getLogger().error("Encountered an error while connecting to {}", Direct.getInstance().getStorage().getClass().getSimpleName(), ex);
            return false;
        }
        
        List<ModData> mods = Direct.getInstance().getStorage().getQuery().getMods();
        if (mods == null) {
            Direct.getInstance().getLogger().error("Failed to get mods");
            return false;
        }
        
        getMods().addAll(mods);
        
        List<ServerData> servers = Direct.getInstance().getStorage().getQuery().getServers();
        if (servers == null) {
            Direct.getInstance().getLogger().error("Failed to get servers");
            return false;
        }
        
        getServers().addAll(servers);
        return !getServers().isEmpty();
    }
    
    public static void buildDirect() {
        for (ServerData serverData : getServers()) {
            if (!serverData.isActive()) {
                continue;
            }
            
            if (serverData.isLobby()) {
                getServerPriority().add(serverData.getName());
            }
            
            buildForcedHosts(serverData);
        }
    }
    
    public static void buildForcedHosts(ServerData serverData) {
        for (String directConnect : serverData.getDirectConnects()) {
            String value = getForcedHosts().putIfAbsent(directConnect, serverData.getName());
            if (Toolbox.isNotBlank(value)) {
                Direct.getInstance().getLogger().error("Direct connect {} is already assigned to {}", directConnect, value);
            }
        }
    }
    
    public static void checkMods(User user, Map<String, String> mods) {
        if (user.hasPermission("direct.bypass.mods")) {
            return;
        }
        
        List<ModData> detectedMods = Toolbox.newArrayList();
        for (Map.Entry<String, String> entry : mods.entrySet()) {
            ModData modData = getModData(entry.getKey()).orElse(null);
            if (modData == null || modData.getExecution().isEmpty()) {
                continue;
            }
            
            Direct.getInstance().getLogger().warn("{} connected with {} ({})", user.getName(), modData.getName(), modData.getId());
            detectedMods.add(modData);
            
            for (String execution : modData.getExecution()) {
                execution = execution
                        .replace("[ID]", modData.getId())
                        .replace("[NAME]", modData.getName())
                        .replace("[PLAYER]", user.getName())
                        .replace("[SERVER]", user.getCurrentServer().map(ServerData::getName).orElse("Unknown"));
                
                if (Toolbox.isBlank(execution) || !Direct.getInstance().getPlatform().executeCommand(execution)) {
                    Direct.getInstance().getLogger().error("{} execution failed: {}", modData.getId(), execution);
                }
            }
        }
        
        if (detectedMods.isEmpty()) {
            return;
        }
        
        if (Direct.getInstance().getConfig().map(Config::getLog).map(LogCategory::isDetectionModList).orElse(false)) {
            Direct.getInstance().getPlatform().executeAsync(() -> {
                Direct.getInstance().getStorage().getQuery().createLog(user.getUniqueId(), "MODLIST", new Gson().toJson(mods));
            });
        }
        
        if (Direct.getInstance().getConfig().map(Config::getLog).map(LogCategory::isDetectionMods).orElse(false)) {
            JsonObject jsonObject = new JsonObject();
            detectedMods.forEach(mod -> jsonObject.addProperty(mod.getId(), mod.getName()));
            
            Direct.getInstance().getPlatform().executeAsync(() -> {
                Direct.getInstance().getStorage().getQuery().createLog(user.getUniqueId(), "DETECTION", new Gson().toJson(jsonObject));
            });
        }
    }
    
    public static Optional<ModData> getModData(String id) {
        List<ModData> mods = Toolbox.newArrayList();
        for (ModData modData : getMods()) {
            if (Toolbox.containsIgnoreCase(id, modData.getId())) {
                mods.add(modData);
            }
        }
        
        if (mods.isEmpty()) {
            return Optional.empty();
        }
        
        if (mods.size() == 1) {
            return Optional.of(mods.get(0));
        }
        
        for (Iterator<ModData> iterator = mods.iterator(); iterator.hasNext(); ) {
            ModData modData = iterator.next();
            if (modData.getId().equalsIgnoreCase(id)) {
                return Optional.of(modData);
            }
            
            if (modData.getExecution().isEmpty()) {
                iterator.remove();
            }
        }
        
        if (!mods.isEmpty()) {
            return Optional.of(mods.get(0));
        }
        
        return Optional.empty();
    }
    
    public static boolean isAccessible(User user, ServerData serverData) {
        return !serverData.isRestricted() || user.hasPermission(Direct.getInstance().getPlatform().getType().getId() + ".server." + serverData.getName());
    }
    
    public static boolean isProtocolSupported(User user, ServerData serverData) {
        return serverData.getProtocolVersions().isEmpty() || serverData.getProtocolVersions().contains(user.getProtocolVersion());
    }
    
    public static Optional<ServerData> getLobby(User user) {
        ServerData currentServer = user.getCurrentServer().orElse(null);
        List<ServerData> servers = Toolbox.newArrayList();
        for (ServerData serverData : getServers()) {
            if (!serverData.isActive() || !serverData.isLobby() || !isAccessible(user, serverData) || !isProtocolSupported(user, serverData)) {
                continue;
            }
            
            if (serverData != currentServer) {
                servers.add(serverData);
            }
        }
        
        if (!servers.isEmpty()) {
            return Optional.of(servers.get(getRandom().nextInt(servers.size())));
        }
        
        return Optional.empty();
    }
    
    public static Optional<ServerData> getServer(String name) {
        for (ServerData serverData : getServers()) {
            if (serverData.isActive() && Toolbox.isNotBlank(serverData.getName()) && serverData.getName().equals(name)) {
                return Optional.of(serverData);
            }
        }
        
        return Optional.empty();
    }
    
    public static Random getRandom() {
        return RANDOM;
    }
    
    public static List<ModData> getMods() {
        return MODS;
    }
    
    public static List<ServerData> getServers() {
        return SERVERS;
    }
    
    public static List<String> getServerPriority() {
        return SERVER_PRIORITY;
    }
    
    public static Map<String, String> getForcedHosts() {
        return FORCED_HOSTS;
    }
}