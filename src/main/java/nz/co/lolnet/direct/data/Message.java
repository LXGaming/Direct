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

package nz.co.lolnet.direct.data;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import nz.co.lolnet.direct.Direct;
import nz.co.lolnet.direct.util.Toolbox;

import java.util.Optional;

public class Message {
    
    private final String message;
    
    private Message(String message) {
        this.message = message;
    }
    
    public static Message.Builder builder() {
        return new Message.Builder();
    }
    
    public boolean disconnect(ProxiedPlayer proxiedPlayer) {
        if (proxiedPlayer != null) {
            proxiedPlayer.disconnect(createBaseComponent());
            return true;
        }
        
        return false;
    }
    
    public boolean sendMessage(ProxiedPlayer proxiedPlayer) {
        if (proxiedPlayer != null) {
            proxiedPlayer.sendMessage(createBaseComponent());
            return true;
        }
        
        return false;
    }
    
    public BaseComponent[] createBaseComponent() {
        return Toolbox.getTextPrefix().append(Toolbox.convertColor(getMessage())).create();
    }
    
    private String getMessage() {
        return message;
    }
    
    public static class Builder {
        
        private String reason;
        private String server;
        private Type type;
        
        public Message build() {
            if (getType() == null) {
                type(Type.ERROR);
            }
            
            String message = getType().getMessage().orElse("[SERVER] - [REASON]");
            if (Toolbox.isBlank(getServer())) {
                server("Unknown");
            }
            
            message = message.replace("[SERVER]", getServer());
            if (Toolbox.isBlank(getReason())) {
                reason("No reason provided");
            }
            
            message = message.replace("[REASON]", getReason());
            return new Message(message);
        }
        
        private String getReason() {
            return reason;
        }
        
        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }
        
        private String getServer() {
            return server;
        }
        
        public Builder server(String server) {
            this.server = server;
            return this;
        }
        
        private Type getType() {
            return type;
        }
        
        public Builder type(Type type) {
            this.type = type;
            return this;
        }
    }
    
    public enum Type {
        
        CONNECT("Connect"),
        DISCONNECT("Disconnect"),
        ERROR("Error"),
        FAIL("Fail"),
        INACTIVE("Inactive"),
        INCOMPATIBLE("Incompatible"),
        KICK("Kick"),
        REMOVED("Removed"),
        RESTRICTED("Restricted"),
        TIMEOUT("Timeout");
        
        private final String name;
        
        Type(String name) {
            this.name = name;
        }
        
        public Optional<String> getMessage() {
            return Direct.getInstance().getConfiguration().map(configuration -> configuration.getString("Direct.Messages." + getName()));
        }
        
        public String getName() {
            return name;
        }
    }
}