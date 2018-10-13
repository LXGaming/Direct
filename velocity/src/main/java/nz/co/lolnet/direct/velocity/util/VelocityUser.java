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

package nz.co.lolnet.direct.velocity.util;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.ServerInfo;
import nz.co.lolnet.direct.common.data.Message;
import nz.co.lolnet.direct.common.data.ServerData;
import nz.co.lolnet.direct.common.data.User;
import nz.co.lolnet.direct.common.manager.DirectManager;
import nz.co.lolnet.direct.velocity.VelocityPlugin;

import java.util.Optional;
import java.util.UUID;

public class VelocityUser implements User {
    
    private final UUID uniqueId;
    
    private VelocityUser(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
    
    public static VelocityUser of(UUID uniqueId) {
        return new VelocityUser(uniqueId);
    }
    
    @Override
    public void disconnect(Message message) {
        getPlayer().ifPresent(player -> player.disconnect(VelocityToolbox.getTextPrefix().append(VelocityToolbox.deserializeLegacy(message.getRawMessage()))));
    }
    
    @Override
    public void sendMessage(Message message) {
        getPlayer().ifPresent(player -> player.sendMessage(VelocityToolbox.getTextPrefix().append(VelocityToolbox.deserializeLegacy(message.getRawMessage()))));
    }
    
    @Override
    public boolean hasPermission(String permission) {
        return getPlayer().map(player -> player.hasPermission(permission)).orElse(false);
    }
    
    @Override
    public String getName() {
        return getPlayer().map(Player::getUsername).orElse(null);
    }
    
    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    @Override
    public int getProtocolVersion() {
        return getPlayer().map(Player::getProtocolVersion).orElse(0);
    }
    
    @Override
    public Optional<ServerData> getCurrentServer() {
        return getPlayer().flatMap(Player::getCurrentServer).map(ServerConnection::getServerInfo).map(ServerInfo::getName).flatMap(DirectManager::getServer);
    }
    
    private Optional<Player> getPlayer() {
        return VelocityPlugin.getInstance().getProxy().getPlayer(getUniqueId());
    }
}