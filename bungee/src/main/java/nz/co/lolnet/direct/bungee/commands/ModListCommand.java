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
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import nz.co.lolnet.direct.bungee.BungeePlugin;
import nz.co.lolnet.direct.bungee.util.BungeeToolbox;

import java.util.Map;

public class ModListCommand extends Command {
    
    public ModListCommand() {
        super("modlist", "direct.command.modlist");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            sender.sendMessage(BungeeToolbox.getTextPrefix().append("This command can only be run from Console").color(ChatColor.RED).create());
            return;
        }
        
        if (args.length != 1) {
            sender.sendMessage(BungeeToolbox.getTextPrefix().append("Invalid arguments").color(ChatColor.RED).create());
            return;
        }
        
        ProxiedPlayer player = BungeePlugin.getInstance().getProxy().getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(BungeeToolbox.getTextPrefix().append("Failed to find " + args[0]).color(ChatColor.RED).create());
            return;
        }
        
        if (!player.isForgeUser()) {
            sender.sendMessage(BungeeToolbox.getTextPrefix().append(player.getName() + " is running vanilla").create());
            return;
        } else if (player.getModList() == null || player.getModList().isEmpty()) {
            sender.sendMessage(BungeeToolbox.getTextPrefix().append(player.getName() + " Mods: Unknown").create());
            return;
        }
        
        sender.sendMessage(BungeeToolbox.getTextPrefix().append(player.getName() + " Mods:").create());
        for (Map.Entry<String, String> entry : player.getModList().entrySet()) {
            sender.sendMessage(new ComponentBuilder(entry.getKey() + " v" + entry.getValue()).create());
        }
    }
}