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

package nz.co.lolnet.direct.common.configuration.category;

public class MessageCategory {
    
    private String connect = "&8You have been connected to &b[SERVER]";
    private String disconnect = "&8You have been disconnected: &f[REASON]";
    private String error = "&8An unexpected error occurred";
    private String fail = "&8Unable to find suitable server";
    private String inactive = "&b[SERVER] &8is not active";
    private String incompatible = "&b[SERVER] &8does not support your Minecraft version";
    private String kick = "&8You have been moved to &b[SERVER]&8: &f[REASON]";
    private String removed = "&b[SERVER] &8is no longer publicly available";
    private String restricted = "&8You do not have permission to join &b[SERVER]";
    private String timeout = "Timed out";
    
    public String getConnect() {
        return connect;
    }
    
    public String getDisconnect() {
        return disconnect;
    }
    
    public String getError() {
        return error;
    }
    
    public String getFail() {
        return fail;
    }
    
    public String getInactive() {
        return inactive;
    }
    
    public String getIncompatible() {
        return incompatible;
    }
    
    public String getKick() {
        return kick;
    }
    
    public String getRemoved() {
        return removed;
    }
    
    public String getRestricted() {
        return restricted;
    }
    
    public String getTimeout() {
        return timeout;
    }
}