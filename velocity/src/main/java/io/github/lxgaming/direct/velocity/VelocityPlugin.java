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

package io.github.lxgaming.direct.velocity;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import io.github.lxgaming.direct.common.Direct;
import io.github.lxgaming.direct.common.Platform;
import io.github.lxgaming.direct.common.entity.Locale;
import io.github.lxgaming.direct.common.entity.Server;
import io.github.lxgaming.direct.common.entity.Source;
import io.github.lxgaming.direct.common.manager.CommandManager;
import io.github.lxgaming.direct.common.manager.DirectManager;
import io.github.lxgaming.direct.common.storage.Storage;
import io.github.lxgaming.direct.common.util.Toolbox;
import io.github.lxgaming.direct.common.util.text.adapter.LocaleAdapter;
import io.github.lxgaming.direct.velocity.command.DirectCommand;
import io.github.lxgaming.direct.velocity.entity.VelocitySource;
import io.github.lxgaming.direct.velocity.listener.VelocityListener;
import io.github.lxgaming.direct.velocity.util.DirectCommandSource;
import io.github.lxgaming.direct.velocity.util.VelocityToolbox;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

@Plugin(
        id = Direct.ID,
        name = Direct.NAME,
        version = Direct.VERSION,
        description = Direct.DESCRIPTION,
        url = Direct.WEBSITE,
        authors = {Direct.AUTHORS}
)
public class VelocityPlugin implements Platform {
    
    private static VelocityPlugin instance;
    
    @Inject
    private ProxyServer proxy;
    
    @Inject
    @DataDirectory
    private Path path;
    
    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        instance = this;
        
        Direct direct = new Direct(this);
        direct.load();
        
        getProxy().getCommandManager().register(
                getProxy().getCommandManager()
                        .metaBuilder(CommandManager.getPrefix())
                        .aliases(CommandManager.getPlatformCommands().toArray(new String[0]))
                        .build(),
                new DirectCommand()
        );
        
        getProxy().getEventManager().register(getInstance(), new VelocityListener());
    }
    
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (!Direct.isAvailable()) {
            return;
        }
        
        Storage storage = Direct.getInstance().getStorage();
        if (storage != null && !storage.isClosed()) {
            storage.close();
        }
        
        Direct.getInstance().getLogger().info("{} v{} unloaded", Direct.NAME, Direct.VERSION);
    }
    
    @Override
    public void registerServers() {
        // Building
        List<ServerInfo> proxyServers = Lists.newArrayList();
        for (Server server : DirectManager.SERVERS) {
            if (!server.isActive()) {
                continue;
            }
            
            InetSocketAddress address = Toolbox.parseAddress(server.getHost(), server.getPort());
            if (address == null) {
                server.setActive(false);
                Direct.getInstance().getLogger().warn("Failed to parse address for server {}", server.getName());
                continue;
            }
            
            ServerInfo serverInfo = new ServerInfo(server.getName(), address);
            proxyServers.add(serverInfo);
        }
        
        // Servers
        for (RegisteredServer registeredServer : getProxy().getAllServers()) {
            getProxy().unregisterServer(registeredServer.getServerInfo());
        }
        
        proxyServers.forEach(getProxy()::registerServer);
        
        Direct.getInstance().getLogger().info("Successfully registered {} Servers", proxyServers.size());
        
        // Players
        for (Player player : getProxy().getAllPlayers()) {
            Source source = new VelocitySource(player);
            
            Server currentServer = source.getCurrentServer();
            if (currentServer == null) {
                LocaleAdapter.sendSystemMessage(source, Locale.MESSAGE_REMOVED);
            } else if (!DirectManager.isAccessible(source, currentServer)) {
                LocaleAdapter.sendSystemMessage(source, Locale.MESSAGE_RESTRICTED, currentServer.getName());
            } else if (!DirectManager.isProtocolSupported(source, currentServer)) {
                LocaleAdapter.sendSystemMessage(source, Locale.MESSAGE_INCOMPATIBLE, currentServer.getName());
            } else {
                continue;
            }
            
            RegisteredServer server = VelocityToolbox.getLobby(source);
            if (server != null) {
                player.createConnectionRequest(server).fireAndForget();
                continue;
            }
            
            LocaleAdapter.disconnect(source, Locale.MESSAGE_FAIL);
        }
    }
    
    @Override
    public void executeAsync(@NonNull Runnable runnable) {
        getProxy().getScheduler().buildTask(getInstance(), runnable).schedule();
    }
    
    @Override
    public boolean executeCommand(@NonNull String command) {
        return getProxy().getCommandManager().execute(new DirectCommandSource(), command);
    }
    
    @Override
    public @NonNull Collection<String> getUsernames() {
        List<String> usernames = Lists.newArrayList();
        for (Player player : getProxy().getAllPlayers()) {
            usernames.add(player.getUsername());
        }
        
        return usernames;
    }
    
    @Override
    public @NonNull Path getPath() {
        return path;
    }
    
    @Override
    public @NonNull Type getType() {
        return Type.VELOCITY;
    }
    
    public static VelocityPlugin getInstance() {
        return instance;
    }
    
    public ProxyServer getProxy() {
        return proxy;
    }
}