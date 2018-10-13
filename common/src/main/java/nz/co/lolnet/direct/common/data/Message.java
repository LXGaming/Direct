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

package nz.co.lolnet.direct.common.data;

import nz.co.lolnet.direct.common.Direct;
import nz.co.lolnet.direct.common.configuration.Config;
import nz.co.lolnet.direct.common.configuration.category.MessageCategory;
import nz.co.lolnet.direct.common.util.Toolbox;

import java.util.Optional;
import java.util.function.Function;

public class Message {
    
    private final String rawMessage;
    
    private Message(String rawMessage) {
        this.rawMessage = rawMessage;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getRawMessage() {
        return rawMessage;
    }
    
    public static class Builder {
        
        private String reason;
        private String server;
        private Type type;
        
        public Message build() {
            if (getType() == null) {
                type(Type.ERROR);
            }
            
            String rawMessage = getType().getRawMessage().orElse("[SERVER] - [REASON]");
            if (Toolbox.isBlank(getServer())) {
                server("Unknown");
            }
            
            rawMessage = rawMessage.replace("[SERVER]", getServer());
            if (Toolbox.isBlank(getReason())) {
                reason("No reason provided");
            }
            
            rawMessage = rawMessage.replace("[REASON]", getReason());
            return new Message(rawMessage);
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
        
        CONNECT("Connect", MessageCategory::getConnect),
        DISCONNECT("Disconnect", MessageCategory::getDisconnect),
        ERROR("Error", MessageCategory::getError),
        FAIL("Fail", MessageCategory::getFail),
        INACTIVE("Inactive", MessageCategory::getInactive),
        INCOMPATIBLE("Incompatible", MessageCategory::getIncompatible),
        KICK("Kick", MessageCategory::getKick),
        REMOVED("Removed", MessageCategory::getRemoved),
        RESTRICTED("Restricted", MessageCategory::getRestricted),
        TIMEOUT("Timeout", MessageCategory::getTimeout);
        
        private final String friendlyName;
        private final Function<MessageCategory, String> function;
        
        Type(String friendlyName, Function<MessageCategory, String> function) {
            this.friendlyName = friendlyName;
            this.function = function;
        }
        
        public Optional<String> getRawMessage() {
            return Direct.getInstance().getConfig().map(Config::getMessages).map(getFunction());
        }
        
        public String getFriendlyName() {
            return friendlyName;
        }
        
        public Function<MessageCategory, String> getFunction() {
            return function;
        }
    }
}