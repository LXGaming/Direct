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

package nz.co.lolnet.direct.velocity.command;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.ModInfo;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import nz.co.lolnet.direct.velocity.VelocityPlugin;
import nz.co.lolnet.direct.velocity.util.VelocityToolbox;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class ModListCommand implements Command {
    
    @Override
    public void execute(@NonNull CommandSource source, String[] args) {
        if (source instanceof Player) {
            source.sendMessage(VelocityToolbox.getTextPrefix().append(TextComponent.of("This command can only be run from Console", TextColor.RED)));
            return;
        }
        
        if (args.length != 1) {
            source.sendMessage(VelocityToolbox.getTextPrefix().append(TextComponent.of("Invalid arguments", TextColor.RED)));
            return;
        }
        
        Player player = VelocityPlugin.getInstance().getProxy().getPlayer(args[0]).orElse(null);
        if (player == null) {
            source.sendMessage(VelocityToolbox.getTextPrefix().append(TextComponent.of("Failed to find " + args[0], TextColor.RED)));
            return;
        }
        
        if (!player.getModInfo().isPresent()) {
            source.sendMessage(VelocityToolbox.getTextPrefix().append(TextComponent.of(player.getUsername() + " is running vanilla")));
            return;
        } else if (player.getModInfo().get().getMods().isEmpty()) {
            source.sendMessage(VelocityToolbox.getTextPrefix().append(TextComponent.of(player.getUsername() + " Mods: Unknown")));
            return;
        }
        
        source.sendMessage(VelocityToolbox.getTextPrefix().append(TextComponent.of(player.getUsername() + " Mods:")));
        for (ModInfo.Mod mod : player.getModInfo().get().getMods()) {
            source.sendMessage(TextComponent.of(mod.getId() + " v" + mod.getVersion()));
        }
    }
    
    @Override
    public List<String> suggest(@NonNull CommandSource source, String[] currentArgs) {
        return ImmutableList.of();
    }
    
    @Override
    public boolean hasPermission(@NonNull CommandSource source, String[] args) {
        return source.hasPermission("direct.command.modlist");
    }
}