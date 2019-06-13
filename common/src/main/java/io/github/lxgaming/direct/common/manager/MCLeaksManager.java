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

import me.gong.mcleaks.MCLeaksAPI;
import io.github.lxgaming.direct.common.Direct;
import io.github.lxgaming.direct.common.configuration.Config;
import io.github.lxgaming.direct.common.configuration.category.MCLeaksCategory;
import io.github.lxgaming.direct.common.data.ServerData;
import io.github.lxgaming.direct.common.data.User;
import io.github.lxgaming.direct.common.util.Toolbox;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MCLeaksManager {
    
    private static MCLeaksAPI mcLeaks;
    
    public static void buildMCLeaks() {
        MCLeaksCategory mcLeaksCategory = Direct.getInstance().getConfig().map(Config::getMcLeaks).orElse(null);
        if (mcLeaksCategory == null || !mcLeaksCategory.isEnabled()) {
            return;
        }
        
        MCLeaksAPI.Builder mcLeaksBuilder = MCLeaksAPI.builder();
        if (mcLeaksCategory.getThreadCount() > 0) {
            mcLeaksBuilder.threadCount(mcLeaksCategory.getThreadCount());
        }
        
        if (mcLeaksCategory.getExpireAfter() > 0) {
            mcLeaksBuilder.expireAfter(mcLeaksCategory.getExpireAfter(), TimeUnit.SECONDS);
        }
        
        mcLeaks = mcLeaksBuilder.build();
    }
    
    public static void shutdown() {
        if (mcLeaks != null) {
            mcLeaks.shutdown();
        }
    }
    
    public static void checkAccount(User user) {
        if (mcLeaks == null || user.hasPermission("direct.bypass.mcleaks")) {
            return;
        }
        
        Optional<Boolean> cached = mcLeaks.getCachedCheck(user.getUniqueId());
        if (cached.isPresent()) {
            if (cached.get()) {
                execute(user);
            }
            
            return;
        }
        
        mcLeaks.checkAccount(user.getUniqueId(), success -> {
            if (success) {
                execute(user);
            }
        }, failure -> {
            Direct.getInstance().getLogger().error("Encountered an error processing MCLeaksManager::checkAccount", failure);
        });
    }
    
    private static void execute(User user) {
        Set<String> execution = Direct.getInstance().getConfig().map(Config::getMcLeaks).map(MCLeaksCategory::getExecution).orElse(null);
        if (execution == null || execution.isEmpty()) {
            return;
        }
        
        Direct.getInstance().getLogger().warn("{} ({}) connected with an MCLeaks account", user.getName(), user.getUniqueId());
        for (String command : execution) {
            command = command
                    .replace("[PLAYER]", user.getName())
                    .replace("[SERVER]", user.getCurrentServer().map(ServerData::getName).orElse("Unknown"));
            
            if (Toolbox.isBlank(command) || !Direct.getInstance().getPlatform().executeCommand(command)) {
                Direct.getInstance().getLogger().error("MCLeaks execution failed: {}", command);
            }
        }
    }
}