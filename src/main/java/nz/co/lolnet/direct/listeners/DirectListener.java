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

package nz.co.lolnet.direct.listeners;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import nz.co.lolnet.direct.data.Message;
import nz.co.lolnet.direct.data.ServerData;
import nz.co.lolnet.direct.managers.ModManager;
import nz.co.lolnet.direct.managers.ServerManager;
import nz.co.lolnet.direct.util.Toolbox;

public class DirectListener implements Listener {
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerConnect(ServerConnectEvent event) {
        ServerInfo target = event.getTarget();
        if (target != null) {
            ServerData serverData = ServerManager.getServer(target.getName());
            if (serverData == null) {
                Message.builder().type(Message.Type.ERROR).server(target.getName()).build().sendMessage(event.getPlayer());
                target = null;
            } else if (!serverData.isActive()) {
                Message.builder().type(Message.Type.INACTIVE).server(target.getName()).build().sendMessage(event.getPlayer());
                target = null;
            } else if (!ServerManager.isAccessible(serverData, event.getPlayer())) {
                Message.builder().type(Message.Type.RESTRICTED).server(target.getName()).build().sendMessage(event.getPlayer());
                target = null;
            } else if (!ServerManager.isProtocolSupported(serverData, event.getPlayer())) {
                Message.builder().type(Message.Type.INCOMPATIBLE).server(target.getName()).build().sendMessage(event.getPlayer());
                target = null;
            }
        }
        
        if (target == null) {
            target = ServerManager.getLobby(event.getPlayer());
        }
        
        if (target != null) {
            event.setTarget(target);
            return;
        }
        
        Message.builder().type(Message.Type.FAIL).build().sendMessage(event.getPlayer());
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerConnected(ServerConnectedEvent event) {
        ModManager.checkPlayer(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerKick(ServerKickEvent event) {
        String kickReason = TextComponent.toPlainText(event.getKickReasonComponent());
        if (Toolbox.isNotBlank(kickReason) && kickReason.equals(Message.Type.TIMEOUT.getMessage().orElse(""))) {
            ServerData serverData = ServerManager.getServer(event.getKickedFrom().getName());
            if (serverData != null && serverData.isLobby()) {
                event.setCancelled(true);
                Message.builder().type(Message.Type.DISCONNECT).reason(kickReason).build().disconnect(event.getPlayer());
                return;
            }
        }
        
        ServerInfo serverInfo = ServerManager.getLobby(event.getPlayer());
        if (serverInfo != null) {
            event.setCancelled(true);
            event.setCancelServer(serverInfo);
            Message.builder().type(Message.Type.KICK).server(serverInfo.getName()).reason(kickReason).build().sendMessage(event.getPlayer());
            return;
        }
        
        Message.builder().type(Message.Type.FAIL).build().sendMessage(event.getPlayer());
    }
}