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

package nz.co.lolnet.direct.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import nz.co.lolnet.direct.bungee.BungeePlugin;
import nz.co.lolnet.direct.bungee.util.BungeeToolbox;
import nz.co.lolnet.direct.bungee.util.BungeeUser;
import nz.co.lolnet.direct.common.Direct;
import nz.co.lolnet.direct.common.data.Message;
import nz.co.lolnet.direct.common.data.ServerData;
import nz.co.lolnet.direct.common.data.User;
import nz.co.lolnet.direct.common.manager.DirectManager;

public class LobbyCommand extends Command {
    
    public LobbyCommand() {
        super("lobby", "direct.command.lobby", "hub");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(BungeeToolbox.getTextPrefix().append("You just tried to send " + sender.getName() + " to a server...").color(ChatColor.DARK_GRAY).create());
            return;
        }
        
        ProxiedPlayer player = (ProxiedPlayer) sender;
        User user = BungeeUser.of(player.getUniqueId());
        ServerInfo serverInfo = DirectManager.getLobby(user).map(ServerData::getName).map(BungeePlugin.getInstance().getProxy()::getServerInfo).orElse(null);
        if (serverInfo != null) {
            player.connect(serverInfo);
            Direct.getInstance().getLogger().debug(player.getName() + " - " + player.getServer().getInfo().getName() + " -> " + serverInfo.getName());
            return;
        }
        
        user.sendMessage(Message.builder().type(Message.Type.FAIL).build());
    }
}