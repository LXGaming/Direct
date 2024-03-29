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

package io.github.lxgaming.direct.velocity.util;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import io.github.lxgaming.direct.common.Direct;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;

public class DirectCommandSource implements CommandSource {
    
    @Override
    public void sendMessage(@NonNull Component component) {
        Direct.getInstance().getLogger().info(LegacyComponentSerializer.legacySection().serialize(component));
    }
    
    @Override
    public @NonNull
    Tristate getPermissionValue(@NonNull String permission) {
        return Tristate.TRUE;
    }
    
    @Override
    public boolean hasPermission(@NonNull String permission) {
        return true;
    }
}