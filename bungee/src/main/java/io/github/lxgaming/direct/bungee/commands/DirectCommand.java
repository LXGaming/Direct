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

package io.github.lxgaming.direct.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import io.github.lxgaming.direct.bungee.BungeePlugin;
import io.github.lxgaming.direct.bungee.util.BungeeToolbox;
import io.github.lxgaming.direct.common.Direct;

public class DirectCommand extends Command {
    
    public DirectCommand() {
        super("direct");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("direct.command.reload")) {
            BungeePlugin.getInstance().getProxy().getScheduler().runAsync(BungeePlugin.getInstance(), () -> {
                if (Direct.getInstance().reloadDirect()) {
                    sender.sendMessage(BungeeToolbox.getTextPrefix().append("Configuration reloaded").color(ChatColor.GREEN).create());
                } else {
                    sender.sendMessage(BungeeToolbox.getTextPrefix().append("An error occurred. Please check the console").color(ChatColor.RED).create());
                }
            });
            
            return;
        }
        
        sender.sendMessage(BungeeToolbox.getPluginInformation().create());
    }
}