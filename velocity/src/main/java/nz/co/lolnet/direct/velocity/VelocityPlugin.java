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

package nz.co.lolnet.direct.velocity;

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
import nz.co.lolnet.direct.common.Direct;
import nz.co.lolnet.direct.common.configuration.Config;
import nz.co.lolnet.direct.common.data.Message;
import nz.co.lolnet.direct.common.data.Platform;
import nz.co.lolnet.direct.common.data.ServerData;
import nz.co.lolnet.direct.common.data.User;
import nz.co.lolnet.direct.common.manager.DirectManager;
import nz.co.lolnet.direct.common.manager.MCLeaksManager;
import nz.co.lolnet.direct.common.util.Logger;
import nz.co.lolnet.direct.common.util.Reference;
import nz.co.lolnet.direct.common.util.Toolbox;
import nz.co.lolnet.direct.velocity.command.DirectCommand;
import nz.co.lolnet.direct.velocity.command.LobbyCommand;
import nz.co.lolnet.direct.velocity.command.ModListCommand;
import nz.co.lolnet.direct.velocity.listener.DirectListener;
import nz.co.lolnet.direct.velocity.util.DirectCommandSource;
import nz.co.lolnet.direct.velocity.util.VelocityToolbox;
import nz.co.lolnet.direct.velocity.util.VelocityUser;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.List;

@Plugin(
        id = Reference.ID,
        name = Reference.NAME,
        version = Reference.VERSION,
        description = Reference.DESCRIPTION,
        url = Reference.WEBSITE,
        authors = {Reference.AUTHORS}
)
public class VelocityPlugin implements Platform {
    
    private static VelocityPlugin instance;
    
    @Inject
    ProxyServer proxy;
    
    @Inject
    @DataDirectory
    private Path path;
    
    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        instance = this;
        Direct direct = new Direct(this);
        direct.getLogger()
                .add(Logger.Level.INFO, LoggerFactory.getLogger(Reference.NAME)::info)
                .add(Logger.Level.WARN, LoggerFactory.getLogger(Reference.NAME)::warn)
                .add(Logger.Level.ERROR, LoggerFactory.getLogger(Reference.NAME)::error)
                .add(Logger.Level.DEBUG, message -> {
                    if (Direct.getInstance().getConfig().map(Config::isDebug).orElse(false)) {
                        LoggerFactory.getLogger(Reference.NAME).info(message);
                    }
                });
        
        direct.loadDirect();
        getProxy().getEventManager().register(getInstance(), new DirectListener());
        getProxy().getCommandManager().register(new DirectCommand(), "direct");
        getProxy().getCommandManager().register(new LobbyCommand(), "lobby", "hub");
        getProxy().getCommandManager().register(new ModListCommand(), "modlist");
    }
    
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        MCLeaksManager.shutdown();
        Direct.getInstance().getStorage().close();
        Direct.getInstance().getLogger().info("{} v{} unloaded", Reference.NAME, Reference.VERSION);
    }
    
    @Override
    public void registerServers() {
        // Building
        List<ServerInfo> proxyServers = Toolbox.newArrayList();
        for (ServerData serverData : DirectManager.getServers()) {
            if (Toolbox.isBlank(serverData.getName())) {
                Direct.getInstance().getLogger().warn("Cannot build ServerInfo as the name is blank");
                continue;
            }
            
            if (Toolbox.isBlank(serverData.getHost()) || serverData.getPort() < 0 || serverData.getPort() > 65535) {
                Direct.getInstance().getLogger().warn("Cannot build ServerInfo for {} as the address is invalid", serverData.getName());
                continue;
            }
            
            if (!serverData.isActive()) {
                continue;
            }
            
            InetSocketAddress address = Toolbox.parseAddress(serverData.getHost(), serverData.getPort()).orElse(null);
            if (address == null) {
                Direct.getInstance().getLogger().warn("Cannot build ServerInfo for {} as the address couldn't be parsed", serverData.getName());
                continue;
            }
            
            ServerInfo serverInfo = new ServerInfo(serverData.getName(), address);
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
            User user = VelocityUser.of(player.getUniqueId());
            ServerData serverData = user.getCurrentServer().orElse(null);
            Message.Builder messageBuilder = Message.builder();
            if (serverData == null) {
                messageBuilder.type(Message.Type.REMOVED);
            } else if (!DirectManager.isAccessible(user, serverData)) {
                messageBuilder.type(Message.Type.RESTRICTED).server(serverData.getName());
            } else if (!DirectManager.isProtocolSupported(user, serverData)) {
                messageBuilder.type(Message.Type.INCOMPATIBLE).server(serverData.getName());
            } else {
                continue;
            }
            
            RegisteredServer lobby = DirectManager.getLobby(user).flatMap(VelocityToolbox::getServer).orElse(null);
            if (lobby != null) {
                player.createConnectionRequest(lobby).fireAndForget();
                user.sendMessage(messageBuilder.build());
            } else {
                user.disconnect(Message.builder().type(Message.Type.FAIL).build());
            }
        }
    }
    
    @Override
    public void executeAsync(Runnable runnable) {
        getProxy().getScheduler().buildTask(getInstance(), runnable).schedule();
    }
    
    @Override
    public boolean executeCommand(String command) {
        return getProxy().getCommandManager().execute(new DirectCommandSource(), command);
    }
    
    @Override
    public Platform.Type getType() {
        return Type.VELOCITY;
    }
    
    @Override
    public Path getPath() {
        return path;
    }
    
    public static VelocityPlugin getInstance() {
        return instance;
    }
    
    public ProxyServer getProxy() {
        return proxy;
    }
}