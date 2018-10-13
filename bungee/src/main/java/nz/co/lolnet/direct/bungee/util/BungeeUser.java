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

package nz.co.lolnet.direct.bungee.util;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import nz.co.lolnet.direct.bungee.BungeePlugin;
import nz.co.lolnet.direct.common.data.Message;
import nz.co.lolnet.direct.common.data.ServerData;
import nz.co.lolnet.direct.common.data.User;
import nz.co.lolnet.direct.common.manager.DirectManager;

import java.util.Optional;
import java.util.UUID;

public final class BungeeUser implements User {
    
    private final UUID uniqueId;
    
    private BungeeUser(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
    
    public static BungeeUser of(UUID uniqueId) {
        return new BungeeUser(uniqueId);
    }
    
    @Override
    public void disconnect(Message message) {
        getPlayer().ifPresent(player -> player.disconnect(BungeeToolbox.getTextPrefix().append(BungeeToolbox.convertColor(message.getRawMessage())).create()));
    }
    
    @Override
    public void sendMessage(Message message) {
        getPlayer().ifPresent(player -> player.sendMessage(BungeeToolbox.getTextPrefix().append(BungeeToolbox.convertColor(message.getRawMessage())).create()));
    }
    
    @Override
    public boolean hasPermission(String permission) {
        return getPlayer().map(player -> player.hasPermission(permission)).orElse(false);
    }
    
    @Override
    public String getName() {
        return getPlayer().map(ProxiedPlayer::getName).orElse(null);
    }
    
    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    @Override
    public int getProtocolVersion() {
        return getPlayer().map(ProxiedPlayer::getPendingConnection).map(PendingConnection::getVersion).orElse(0);
    }
    
    @Override
    public Optional<ServerData> getCurrentServer() {
        return getPlayer().map(ProxiedPlayer::getServer).map(Server::getInfo).map(ServerInfo::getName).flatMap(DirectManager::getServer);
    }
    
    private Optional<ProxiedPlayer> getPlayer() {
        return Optional.ofNullable(BungeePlugin.getInstance().getProxy().getPlayer(getUniqueId()));
    }
}