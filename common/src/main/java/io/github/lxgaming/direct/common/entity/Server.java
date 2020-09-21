/*
 * Copyright 2020 Alex Thomson
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

package io.github.lxgaming.direct.common.entity;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;

public class Server {
    
    private String name;
    private String host;
    private int port;
    private Set<String> directConnects;
    private Set<Integer> protocolVersions;
    private String motd;
    private boolean active;
    private boolean lobby;
    private boolean restricted;
    
    public @NonNull String getName() {
        return name;
    }
    
    public void setName(@NonNull String name) {
        this.name = name;
    }
    
    public @NonNull String getHost() {
        return host;
    }
    
    public void setHost(@NonNull String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public @NonNull Set<String> getDirectConnects() {
        return directConnects;
    }
    
    public void setDirectConnects(@NonNull Set<String> directConnects) {
        this.directConnects = directConnects;
    }
    
    public @NonNull Set<Integer> getProtocolVersions() {
        return protocolVersions;
    }
    
    public void setProtocolVersions(@NonNull Set<Integer> protocolVersions) {
        this.protocolVersions = protocolVersions;
    }
    
    public @NonNull String getMotd() {
        return motd;
    }
    
    public void setMotd(@NonNull String motd) {
        this.motd = motd;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isLobby() {
        return lobby;
    }
    
    public void setLobby(boolean lobby) {
        this.lobby = lobby;
    }
    
    public boolean isRestricted() {
        return restricted;
    }
    
    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }
}