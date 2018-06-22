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

package nz.co.lolnet.direct.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import nz.co.lolnet.direct.Direct;
import nz.co.lolnet.direct.data.Message;
import nz.co.lolnet.direct.managers.ServerManager;
import nz.co.lolnet.direct.util.Toolbox;

public class LobbyCommand extends Command {
    
    public LobbyCommand() {
        super("lobby", "", "hub");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(Toolbox.getTextPrefix().append("You just tried to send " + sender.getName() + " to a server...").color(ChatColor.DARK_GRAY).create());
            return;
        }
        
        ProxiedPlayer player = (ProxiedPlayer) sender;
        ServerInfo serverInfo = ServerManager.getLobby(player).orElse(null);
        if (serverInfo != null) {
            player.connect(serverInfo);
            Direct.getInstance().debugMessage(player.getName() + " - " + player.getServer().getInfo().getName() + " -> " + serverInfo.getName());
            return;
        }
        
        Message.builder().type(Message.Type.FAIL).build().sendMessage(player);
    }
}