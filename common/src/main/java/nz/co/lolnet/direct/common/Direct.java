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

package nz.co.lolnet.direct.common;

import nz.co.lolnet.direct.common.configuration.Config;
import nz.co.lolnet.direct.common.configuration.Configuration;
import nz.co.lolnet.direct.common.data.Platform;
import nz.co.lolnet.direct.common.manager.DirectManager;
import nz.co.lolnet.direct.common.manager.MCLeaksManager;
import nz.co.lolnet.direct.common.util.Logger;
import nz.co.lolnet.direct.common.util.Reference;

import java.util.Optional;

public class Direct {
    
    private static Direct instance;
    private final Platform platform;
    private final Logger logger;
    private final Configuration configuration;
    
    public Direct(Platform platform) {
        instance = this;
        this.platform = platform;
        this.logger = new Logger();
        this.configuration = new Configuration();
    }
    
    public void loadDirect() {
        getLogger().info("Initializing...");
        reloadDirect();
        MCLeaksManager.buildMCLeaks();
        getLogger().info("{} v{} has loaded", Reference.NAME, Reference.VERSION);
    }
    
    public boolean reloadDirect() {
        getConfiguration().loadConfiguration();
        if (!getConfig().isPresent()) {
            return false;
        }
        
        getConfiguration().saveConfiguration();
        if (getConfig().map(Config::isDebug).orElse(false)) {
            getLogger().debug("Debug mode enabled");
        } else {
            getLogger().info("Debug mode disabled");
        }
        
        DirectManager.cleanDirect();
        if (DirectManager.prepareDirect()) {
            DirectManager.buildDirect();
            getPlatform().registerServers();
            getLogger().info("Successfully reloaded");
            return true;
        } else {
            getLogger().error("Failed to reload");
            return false;
        }
    }
    
    public static Direct getInstance() {
        return instance;
    }
    
    public Platform getPlatform() {
        return platform;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }
    
    public Optional<Config> getConfig() {
        if (getConfiguration() != null) {
            return Optional.ofNullable(getConfiguration().getConfig());
        }
        
        return Optional.empty();
    }
}