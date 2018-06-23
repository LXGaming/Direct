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
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import nz.co.lolnet.direct.managers.ModManager;
import nz.co.lolnet.direct.util.Toolbox;

import java.util.Set;

public class ModListCommand extends Command {
    
    public ModListCommand() {
        super("modlist", "direct.command.modlist");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 && sender instanceof ProxiedPlayer) {
            ProxiedPlayer proxiedPlayer = (ProxiedPlayer) sender;
            Set<String> mods = ModManager.getPlayerMods(proxiedPlayer);
            if (mods == null) {
                proxiedPlayer.sendMessage(Toolbox.getTextPrefix().append(proxiedPlayer.getName()).append(" is running vanilla").create());
            } else if (mods.isEmpty()) {
                proxiedPlayer.sendMessage(Toolbox.getTextPrefix().append(proxiedPlayer.getName()).append(" Mods: Unknown").create());
            } else {
                proxiedPlayer.sendMessage(Toolbox.getTextPrefix().append(proxiedPlayer.getName()).append(" Mods:\n").append(String.join("\n", mods)).create());
            }
            
            return;
        }
        
        if (args.length == 1) {
            ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(args[0]);
            if (proxiedPlayer == null) {
                sender.sendMessage(Toolbox.getTextPrefix().append(args[0]).append(" not found").color(ChatColor.RED).create());
                return;
            }
            
            if (!proxiedPlayer.isConnected()) {
                sender.sendMessage(Toolbox.getTextPrefix().append(proxiedPlayer.getName()).append(" not connected").color(ChatColor.RED).create());
                return;
            }
            
            Set<String> mods = ModManager.getPlayerMods(proxiedPlayer);
            if (mods == null) {
                sender.sendMessage(Toolbox.getTextPrefix().append(proxiedPlayer.getName()).append(" is running vanilla").create());
            } else if (mods.isEmpty()) {
                sender.sendMessage(Toolbox.getTextPrefix().append(proxiedPlayer.getName()).append(" Mods: Unknown").create());
            } else {
                sender.sendMessage(Toolbox.getTextPrefix().append(proxiedPlayer.getName()).append(" Mods:\n").append(String.join("\n", mods)).create());
            }
            
            return;
        }
        
        sender.sendMessage(Toolbox.getTextPrefix().append("Invalid arguments").color(ChatColor.RED).create());
    }
}