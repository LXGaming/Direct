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

package io.github.lxgaming.direct.velocity.listener;

import com.google.common.collect.Maps;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerModInfoEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.ModInfo;
import io.github.lxgaming.direct.common.Direct;
import io.github.lxgaming.direct.common.configuration.Config;
import io.github.lxgaming.direct.common.configuration.category.GeneralCategory;
import io.github.lxgaming.direct.common.entity.Locale;
import io.github.lxgaming.direct.common.entity.Server;
import io.github.lxgaming.direct.common.entity.Source;
import io.github.lxgaming.direct.common.manager.DirectManager;
import io.github.lxgaming.direct.common.util.StringUtils;
import io.github.lxgaming.direct.common.util.text.adapter.LocaleAdapter;
import io.github.lxgaming.direct.velocity.VelocityPlugin;
import io.github.lxgaming.direct.velocity.entity.VelocitySource;
import io.github.lxgaming.direct.velocity.util.VelocityToolbox;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VelocityListener {
    
    @Subscribe(order = PostOrder.EARLY)
    public void onServerPreConnectEarly(ServerPreConnectEvent event) {
        // If this is present the Player is most likely switching servers.
        if (event.getPlayer().getCurrentServer().isPresent()) {
            return;
        }
        
        event.getPlayer().getVirtualHost()
                .map(DirectManager::getServer)
                .map(VelocityToolbox::getServer)
                .ifPresent(server -> {
                    event.setResult(ServerPreConnectEvent.ServerResult.allowed(server));
                });
    }
    
    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        Source source = new VelocitySource(event.getPlayer());
        
        Server server = VelocityToolbox.getServer(event.getResult().getServer().orElse(event.getOriginalServer()));
        if (server == null) {
            LocaleAdapter.sendSystemMessage(source, Locale.MESSAGE_ERROR);
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        } else if (!server.isActive()) {
            LocaleAdapter.sendSystemMessage(source, Locale.MESSAGE_INACTIVE, server.getName());
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            Direct.getInstance().getLogger().info("{} was denied access to {}: {}", source.getName(), server.getName(), "Inactive");
        } else if (!DirectManager.isAccessible(source, server)) {
            LocaleAdapter.sendSystemMessage(source, Locale.MESSAGE_RESTRICTED, server.getName());
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            Direct.getInstance().getLogger().info("{} was denied access to {}: {}", source.getName(), server.getName(), "Restricted");
        } else if (!DirectManager.isProtocolSupported(source, server)) {
            LocaleAdapter.sendSystemMessage(source, Locale.MESSAGE_INCOMPATIBLE, server.getName());
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            Direct.getInstance().getLogger().info("{} was denied access to {}: {}", source.getName(), server.getName(), "Incompatible");
        }
    }
    
    @Subscribe(order = PostOrder.LATE)
    public void onServerPreConnectLate(ServerPreConnectEvent event) {
        if (event.getResult().isAllowed()) {
            return;
        }
        
        Source source = new VelocitySource(event.getPlayer());
        RegisteredServer server = VelocityToolbox.getLobby(source);
        if (server != null) {
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(server));
            return;
        }
        
        LocaleAdapter.disconnect(source, Locale.MESSAGE_FAIL);
    }
    
    @Subscribe(order = PostOrder.EARLY)
    public void onPlayerModInfo(PlayerModInfoEvent event) {
        Map<String, String> mods = Maps.newHashMap();
        for (ModInfo.Mod mod : event.getModInfo().getMods()) {
            mods.put(mod.getId(), mod.getVersion());
        }
        
        DirectManager.checkMods(new VelocitySource(event.getPlayer()), mods);
    }
    
    @Subscribe(order = PostOrder.EARLY)
    public void onKickedFromServer(KickedFromServerEvent event) {
        Source source = new VelocitySource(event.getPlayer());
        
        String kickReason = event.getServerKickReason().map(VelocityToolbox::serializePlain).orElse(null);
        if (StringUtils.isNotBlank(kickReason)) {
            if (kickReason.equalsIgnoreCase("Timed out")) {
                Server server = VelocityToolbox.getServer(event.getServer());
                if (server != null && server.isLobby()) {
                    LocaleAdapter.disconnect(source, Locale.MESSAGE_DISCONNECT, kickReason);
                    return;
                }
            }
        }
        
        RegisteredServer server = VelocityToolbox.getLobby(source);
        if (server != null) {
            event.setResult(KickedFromServerEvent.RedirectPlayer.create(server));
            
            VelocityPlugin.getInstance().getProxy().getScheduler()
                    .buildTask(
                            VelocityPlugin.getInstance(),
                            () -> LocaleAdapter.sendSystemMessage(source, Locale.MESSAGE_KICK, server.getServerInfo().getName(), kickReason)
                    )
                    .delay(Direct.getInstance().getConfig().map(Config::getGeneralCategory).map(GeneralCategory::getKickMessageDelay).orElse(0L), TimeUnit.MILLISECONDS)
                    .schedule();
            return;
        }
        
        LocaleAdapter.disconnect(source, Locale.MESSAGE_DISCONNECT, kickReason);
    }
}