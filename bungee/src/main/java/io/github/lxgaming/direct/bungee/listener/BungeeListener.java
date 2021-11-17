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

package io.github.lxgaming.direct.bungee.listener;

import io.github.lxgaming.direct.bungee.BungeePlugin;
import io.github.lxgaming.direct.bungee.entity.BungeeSource;
import io.github.lxgaming.direct.bungee.util.BungeeToolbox;
import io.github.lxgaming.direct.common.Direct;
import io.github.lxgaming.direct.common.configuration.Config;
import io.github.lxgaming.direct.common.configuration.category.GeneralCategory;
import io.github.lxgaming.direct.common.entity.Locale;
import io.github.lxgaming.direct.common.entity.Server;
import io.github.lxgaming.direct.common.entity.Source;
import io.github.lxgaming.direct.common.manager.DirectManager;
import io.github.lxgaming.direct.common.util.StringUtils;
import io.github.lxgaming.direct.common.util.text.adapter.LocaleAdapter;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.concurrent.TimeUnit;

public class BungeeListener implements Listener {
    
    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        Source source = new BungeeSource(event.getPlayer());
        
        Server server = BungeeToolbox.getServer(event.getTarget());
        if (server == null) {
            LocaleAdapter.sendSystemMessage(source, Locale.MESSAGE_ERROR);
            event.setCancelled(true);
        } else if (!server.isActive()) {
            LocaleAdapter.sendSystemMessage(source, Locale.MESSAGE_INACTIVE, server.getName());
            event.setCancelled(true);
            Direct.getInstance().getLogger().info("{} was denied access to {}: {}", source.getName(), server.getName(), "Inactive");
        } else if (!DirectManager.isAccessible(source, server)) {
            LocaleAdapter.sendSystemMessage(source, Locale.MESSAGE_RESTRICTED, server.getName());
            event.setCancelled(true);
            Direct.getInstance().getLogger().info("{} was denied access to {}: {}", source.getName(), server.getName(), "Restricted");
        } else if (!DirectManager.isProtocolSupported(source, server)) {
            LocaleAdapter.sendSystemMessage(source, Locale.MESSAGE_INCOMPATIBLE, server.getName());
            event.setCancelled(true);
            Direct.getInstance().getLogger().info("{} was denied access to {}: {}", source.getName(), server.getName(), "Incompatible");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnectPost(ServerConnectEvent event) {
        if (!event.isCancelled()) {
            return;
        }
        
        Source source = new BungeeSource(event.getPlayer());
        ServerInfo serverInfo = BungeeToolbox.getLobby(source);
        if (serverInfo != null && event.getTarget() != serverInfo) {
            event.setTarget(serverInfo);
            event.setCancelled(false);
            return;
        }
        
        LocaleAdapter.disconnect(source, Locale.MESSAGE_FAIL);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerConnected(ServerConnectedEvent event) {
        DirectManager.checkMods(new BungeeSource(event.getPlayer()), event.getPlayer().getModList().keySet());
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerKick(ServerKickEvent event) {
        Source source = new BungeeSource(event.getPlayer());
        
        String kickReason = TextComponent.toPlainText(event.getKickReasonComponent());
        if (StringUtils.isNotBlank(kickReason)) {
            if (kickReason.equalsIgnoreCase("Timed out")) {
                Server server = BungeeToolbox.getServer(event.getKickedFrom());
                if (server != null && server.isLobby()) {
                    LocaleAdapter.disconnect(source, Locale.MESSAGE_DISCONNECT, kickReason);
                    return;
                }
            }
        }
        
        ServerInfo server = BungeeToolbox.getLobby(source);
        if (server != null) {
            event.setCancelServer(server);
            event.setCancelled(true);
            
            BungeePlugin.getInstance().getProxy().getScheduler().schedule(
                    BungeePlugin.getInstance(),
                    () -> LocaleAdapter.sendSystemMessage(source, Locale.MESSAGE_KICK, server.getName(), kickReason),
                    Direct.getInstance().getConfig().map(Config::getGeneralCategory).map(GeneralCategory::getKickMessageDelay).orElse(0L),
                    TimeUnit.MILLISECONDS
            );
            return;
        }
        
        LocaleAdapter.disconnect(source, Locale.MESSAGE_DISCONNECT, kickReason);
    }
}