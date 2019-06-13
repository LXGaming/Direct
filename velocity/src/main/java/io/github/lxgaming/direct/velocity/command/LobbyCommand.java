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

package io.github.lxgaming.direct.velocity.command;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import io.github.lxgaming.direct.common.Direct;
import io.github.lxgaming.direct.common.data.Message;
import io.github.lxgaming.direct.common.data.ServerData;
import io.github.lxgaming.direct.common.data.User;
import io.github.lxgaming.direct.common.manager.DirectManager;
import io.github.lxgaming.direct.velocity.VelocityPlugin;
import io.github.lxgaming.direct.velocity.util.VelocityToolbox;
import io.github.lxgaming.direct.velocity.util.VelocityUser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class LobbyCommand implements Command {
    
    @Override
    public void execute(@NonNull CommandSource source, String[] args) {
        if (!(source instanceof Player)) {
            source.sendMessage(VelocityToolbox.getTextPrefix().append(TextComponent.of("You just tried to send CONSOLE to a server...", TextColor.DARK_GRAY)));
            return;
        }
        
        Player player = (Player) source;
        User user = VelocityUser.of(player.getUniqueId());
        
        RegisteredServer registeredServer = DirectManager.getLobby(user).map(ServerData::getName).flatMap(VelocityPlugin.getInstance().getProxy()::getServer).orElse(null);
        if (registeredServer != null) {
            player.createConnectionRequest(registeredServer).fireAndForget();
            Direct.getInstance().getLogger().debug("{} - {} -> {}",
                    user.getName(),
                    user.getCurrentServer().map(ServerData::getName),
                    registeredServer.getServerInfo().getName());
            return;
        }
        
        user.sendMessage(Message.builder().type(Message.Type.FAIL).build());
    }
    
    @Override
    public List<String> suggest(@NonNull CommandSource source, String[] currentArgs) {
        return ImmutableList.of();
    }
    
    @Override
    public boolean hasPermission(@NonNull CommandSource source, String[] args) {
        return source.hasPermission("direct.command.lobby");
    }
}