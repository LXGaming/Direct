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

package nz.co.lolnet.direct.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerModInfoEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.ModInfo;
import nz.co.lolnet.direct.common.Direct;
import nz.co.lolnet.direct.common.configuration.Config;
import nz.co.lolnet.direct.common.data.Message;
import nz.co.lolnet.direct.common.data.ServerData;
import nz.co.lolnet.direct.common.data.User;
import nz.co.lolnet.direct.common.manager.DirectManager;
import nz.co.lolnet.direct.common.manager.MCLeaksManager;
import nz.co.lolnet.direct.common.util.Toolbox;
import nz.co.lolnet.direct.velocity.VelocityPlugin;
import nz.co.lolnet.direct.velocity.util.VelocityToolbox;
import nz.co.lolnet.direct.velocity.util.VelocityUser;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DirectListener {
    
    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        MCLeaksManager.checkAccount(VelocityUser.of(event.getPlayer().getUniqueId()));
    }
    
    @Subscribe(order = PostOrder.EARLY)
    public void onServerPreConnectEarly(ServerPreConnectEvent event) {
        // If this is present the Player is most likely switching servers.
        if (event.getPlayer().getCurrentServer().isPresent()) {
            return;
        }
        
        String virtualHost = event.getPlayer().getVirtualHost().map(InetSocketAddress::getHostString).orElse(null);
        if (Toolbox.isBlank(virtualHost)) {
            return;
        }
        
        DirectManager.getServer(DirectManager.getForcedHosts().get(virtualHost)).flatMap(VelocityToolbox::getServer).ifPresent(registeredServer -> {
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(registeredServer));
        });
    }
    
    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        User user = VelocityUser.of(event.getPlayer().getUniqueId());
        ServerData serverData = VelocityToolbox.getServer(event.getOriginalServer()).orElse(null);
        if (serverData == null) {
            user.sendMessage(Message.builder().type(Message.Type.ERROR).build());
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        } else if (!serverData.isActive()) {
            user.sendMessage(Message.builder().type(Message.Type.INACTIVE).server(serverData.getName()).build());
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        } else if (!DirectManager.isAccessible(user, serverData)) {
            user.sendMessage(Message.builder().type(Message.Type.RESTRICTED).server(serverData.getName()).build());
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        } else if (!DirectManager.isProtocolSupported(user, serverData)) {
            user.sendMessage(Message.builder().type(Message.Type.INCOMPATIBLE).server(serverData.getName()).build());
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }
    
    @Subscribe(order = PostOrder.LATE)
    public void onServerPreConnectLate(ServerPreConnectEvent event) {
        if (event.getResult().isAllowed()) {
            return;
        }
        
        User user = VelocityUser.of(event.getPlayer().getUniqueId());
        RegisteredServer lobby = DirectManager.getLobby(user).flatMap(VelocityToolbox::getServer).orElse(null);
        if (lobby != null && event.getOriginalServer() != lobby) {
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(lobby));
        } else {
            user.disconnect(Message.builder().type(Message.Type.FAIL).build());
        }
    }
    
    @Subscribe(order = PostOrder.EARLY)
    public void onPlayerModInfo(PlayerModInfoEvent event) {
        Map<String, String> mods = Toolbox.newHashMap();
        for (ModInfo.Mod mod : event.getModInfo().getMods()) {
            mods.put(mod.getId(), mod.getVersion());
        }
        
        DirectManager.checkMods(VelocityUser.of(event.getPlayer().getUniqueId()), mods);
    }
    
    @Subscribe(order = PostOrder.EARLY)
    public void onKickedFromServer(KickedFromServerEvent event) {
        User user = VelocityUser.of(event.getPlayer().getUniqueId());
        String kickReason = event.getOriginalReason().map(VelocityToolbox::serializePlain).orElse(null);
        if (Toolbox.isNotBlank(kickReason)) {
            if (Message.Type.TIMEOUT.getRawMessage().map(kickReason::equals).orElse(false)) {
                ServerData serverData = VelocityToolbox.getServer(event.getServer()).orElse(null);
                if (serverData != null && serverData.isLobby()) {
                    user.disconnect(Message.builder().type(Message.Type.DISCONNECT).reason(kickReason).build());
                    return;
                }
            }
        }
        
        RegisteredServer registeredServer = DirectManager.getLobby(user).flatMap(VelocityToolbox::getServer).orElse(null);
        if (registeredServer != null) {
            event.setResult(KickedFromServerEvent.RedirectPlayer.create(registeredServer));
            
            Message message = Message.builder().type(Message.Type.KICK).server(registeredServer.getServerInfo().getName()).reason(kickReason).build();
            VelocityPlugin.getInstance().getProxy().getScheduler().buildTask(VelocityPlugin.getInstance(), () -> {
                user.sendMessage(message);
            }).delay(Direct.getInstance().getConfig().map(Config::getKickMessageDelay).orElse(0L), TimeUnit.MILLISECONDS).schedule();
        } else {
            user.disconnect(Message.builder().type(Message.Type.DISCONNECT).reason(kickReason).build());
        }
    }
}