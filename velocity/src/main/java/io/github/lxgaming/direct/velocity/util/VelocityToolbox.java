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

import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.github.lxgaming.direct.common.entity.Server;
import io.github.lxgaming.direct.common.entity.Source;
import io.github.lxgaming.direct.common.manager.DirectManager;
import io.github.lxgaming.direct.velocity.VelocityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class VelocityToolbox {
    
    public static String serializePlain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
    
    public static RegisteredServer getLobby(Source source) {
        Server server = DirectManager.getLobby(source);
        if (server != null) {
            return getServer(server);
        }
        
        return null;
    }
    
    public static RegisteredServer getServer(Server server) {
        return VelocityPlugin.getInstance().getProxy().getServer(server.getName()).orElse(null);
    }
    
    public static Server getServer(RegisteredServer registeredServer) {
        return DirectManager.getServer(registeredServer.getServerInfo().getName());
    }
}