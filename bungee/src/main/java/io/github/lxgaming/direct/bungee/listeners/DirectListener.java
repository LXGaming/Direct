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

package io.github.lxgaming.direct.bungee.listeners;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import io.github.lxgaming.direct.bungee.BungeePlugin;
import io.github.lxgaming.direct.bungee.util.BungeeToolbox;
import io.github.lxgaming.direct.bungee.util.BungeeUser;
import io.github.lxgaming.direct.common.Direct;
import io.github.lxgaming.direct.common.configuration.Config;
import io.github.lxgaming.direct.common.data.Message;
import io.github.lxgaming.direct.common.data.ServerData;
import io.github.lxgaming.direct.common.data.User;
import io.github.lxgaming.direct.common.manager.DirectManager;
import io.github.lxgaming.direct.common.manager.MCLeaksManager;
import io.github.lxgaming.direct.common.util.Toolbox;

import java.util.concurrent.TimeUnit;

public class DirectListener implements Listener {
    
    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        MCLeaksManager.checkAccount(BungeeUser.of(event.getPlayer().getUniqueId()));
    }
    
    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        User user = BungeeUser.of(event.getPlayer().getUniqueId());
        ServerData serverData = BungeeToolbox.getServer(event.getTarget()).orElse(null);
        if (serverData == null) {
            user.sendMessage(Message.builder().type(Message.Type.ERROR).build());
            event.setCancelled(true);
        } else if (!serverData.isActive()) {
            user.sendMessage(Message.builder().type(Message.Type.INACTIVE).server(serverData.getName()).build());
            event.setCancelled(true);
            Direct.getInstance().getLogger().info("{} was denied access to {}: {}", user.getName(), serverData.getName(), "Inactive");
        } else if (!DirectManager.isAccessible(user, serverData)) {
            user.sendMessage(Message.builder().type(Message.Type.RESTRICTED).server(serverData.getName()).build());
            event.setCancelled(true);
            Direct.getInstance().getLogger().info("{} was denied access to {}: {}", user.getName(), serverData.getName(), "Restricted");
        } else if (!DirectManager.isProtocolSupported(user, serverData)) {
            user.sendMessage(Message.builder().type(Message.Type.INCOMPATIBLE).server(serverData.getName()).build());
            event.setCancelled(true);
            Direct.getInstance().getLogger().info("{} was denied access to {}: {}", user.getName(), serverData.getName(), "Incompatible");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnectPost(ServerConnectEvent event) {
        if (!event.isCancelled()) {
            return;
        }
        
        User user = BungeeUser.of(event.getPlayer().getUniqueId());
        ServerInfo lobby = DirectManager.getLobby(user).flatMap(BungeeToolbox::getServer).orElse(null);
        if (lobby != null && event.getTarget() != lobby) {
            event.setTarget(lobby);
            event.setCancelled(false);
        } else {
            user.disconnect(Message.builder().type(Message.Type.FAIL).build());
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerConnected(ServerConnectedEvent event) {
        DirectManager.checkMods(BungeeUser.of(event.getPlayer().getUniqueId()), event.getPlayer().getModList());
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerKick(ServerKickEvent event) {
        User user = BungeeUser.of(event.getPlayer().getUniqueId());
        String kickReason = TextComponent.toPlainText(event.getKickReasonComponent());
        if (Toolbox.isNotBlank(kickReason)) {
            if (Message.Type.TIMEOUT.getRawMessage().map(kickReason::equals).orElse(false)) {
                ServerData serverData = BungeeToolbox.getServer(event.getKickedFrom()).orElse(null);
                if (serverData != null && serverData.isLobby()) {
                    user.disconnect(Message.builder().type(Message.Type.DISCONNECT).reason(kickReason).build());
                    return;
                }
            }
        }
        
        ServerInfo serverInfo = DirectManager.getLobby(user).flatMap(BungeeToolbox::getServer).orElse(null);
        if (serverInfo != null) {
            event.setCancelServer(serverInfo);
            event.setCancelled(true);
            
            Message message = Message.builder().type(Message.Type.KICK).server(serverInfo.getName()).reason(kickReason).build();
            BungeePlugin.getInstance().getProxy().getScheduler().schedule(BungeePlugin.getInstance(), () -> {
                user.sendMessage(message);
            }, Direct.getInstance().getConfig().map(Config::getKickMessageDelay).orElse(0L), TimeUnit.MILLISECONDS);
        } else {
            user.disconnect(Message.builder().type(Message.Type.DISCONNECT).reason(kickReason).build());
        }
    }
}