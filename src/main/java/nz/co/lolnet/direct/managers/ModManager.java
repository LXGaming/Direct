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
import net.md_5.bungee.api.connection.ProxiedPlayer;
import nz.co.lolnet.direct.Direct;
import nz.co.lolnet.direct.data.ModData;
import nz.co.lolnet.direct.storage.mysql.MySQLQuery;
import nz.co.lolnet.direct.util.DirectCommandSender;
import nz.co.lolnet.direct.util.Toolbox;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModManager {
    
    private static final List<ModData> MODS = Toolbox.newArrayList();
    
    public static boolean prepareMods() {
        List<ModData> mods = MySQLQuery.getMods();
        if (mods == null) {
            return false;
        }
        
        getMods().clear();
        getMods().addAll(mods);
        return true;
    }
    
    public static void checkPlayer(ProxiedPlayer proxiedPlayer) {
        if (!proxiedPlayer.isForgeUser() || proxiedPlayer.getModList().isEmpty() || proxiedPlayer.hasPermission("direct.bypass")) {
            return;
        }
        
        String server = "Unknown";
        if (proxiedPlayer.getServer() != null) {
            server = proxiedPlayer.getServer().getInfo().getName();
        }
        
        for (String id : proxiedPlayer.getModList().keySet()) {
            ModData modData = getMod(id);
            if (modData == null || modData.getExecution().isEmpty()) {
                continue;
            }
            
            Direct.getInstance().getLogger().warning(proxiedPlayer.getName() + " connected with " + modData.getName() + " (" + id + " v" + proxiedPlayer.getModList().get(id) + ")");
            for (String execution : modData.getExecution()) {
                execution = execution
                        .replace("[ID]", modData.getId())
                        .replace("[NAME]", modData.getName())
                        .replace("[PLAYER]", proxiedPlayer.getName())
                        .replace("[SERVER]", server);
                
                if (Toolbox.isBlank(execution) || !ProxyServer.getInstance().getPluginManager().dispatchCommand(DirectCommandSender.getInstance(), execution)) {
                    Direct.getInstance().getLogger().severe(modData.getId() + " execution failed: " + execution);
                }
            }
            
            return;
        }
    }
    
    public static Set<String> getPlayerMods(ProxiedPlayer proxiedPlayer) {
        if (!proxiedPlayer.isForgeUser()) {
            return null;
        }
        
        Set<String> mods = Toolbox.newHashSet();
        for (Map.Entry<String, String> entry : proxiedPlayer.getModList().entrySet()) {
            mods.add(entry.getKey() + " v" + entry.getValue());
        }
        
        return mods;
    }
    
    public static ModData getMod(String id) {
        List<ModData> mods = Toolbox.newArrayList();
        for (ModData modData : getMods()) {
            if (Toolbox.containsIgnoreCase(id, modData.getId())) {
                mods.add(modData);
            }
        }
        
        if (mods.isEmpty()) {
            return null;
        }
        
        if (mods.size() == 1) {
            return mods.get(0);
        }
        
        for (Iterator<ModData> iterator = mods.iterator(); iterator.hasNext(); ) {
            ModData modData = iterator.next();
            if (modData.getId().equalsIgnoreCase(id)) {
                return modData;
            }
            
            if (modData.getExecution().isEmpty()) {
                iterator.remove();
            }
        }
        
        if (!mods.isEmpty()) {
            return mods.get(0);
        }
        
        return null;
    }
    
    public static List<ModData> getMods() {
        return MODS;
    }
}