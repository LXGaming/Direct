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

package io.github.lxgaming.direct.common;

import com.google.common.base.Preconditions;
import io.github.lxgaming.direct.common.configuration.Config;
import io.github.lxgaming.direct.common.configuration.Configuration;
import io.github.lxgaming.direct.common.configuration.category.StorageCategory;
import io.github.lxgaming.direct.common.manager.CommandManager;
import io.github.lxgaming.direct.common.manager.DirectManager;
import io.github.lxgaming.direct.common.manager.LocaleManager;
import io.github.lxgaming.direct.common.storage.Storage;
import io.github.lxgaming.direct.common.storage.mysql.MySQLStorage;
import io.github.lxgaming.direct.common.util.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Direct {
    
    public static final String ID = "direct";
    public static final String NAME = "Direct";
    public static final String VERSION = "@version@";
    public static final String DESCRIPTION = "Server Management";
    public static final String AUTHORS = "LX_Gaming";
    public static final String SOURCE = "https://github.com/LXGaming/Direct";
    public static final String WEBSITE = "https://lxgaming.github.io/";
    
    private static Direct instance;
    private static Platform platform;
    private final Logger logger;
    private final Configuration configuration;
    private Storage storage;
    
    public Direct(Platform platform) {
        Direct.instance = this;
        Direct.platform = platform;
        this.logger = LoggerFactory.getLogger(Direct.NAME);
        this.configuration = new Configuration(platform.getPath());
    }
    
    public void load() {
        getLogger().info("Initializing...");
        if (!reload()) {
            getLogger().error("Failed to load");
            return;
        }
        
        LocaleManager.prepare();
        CommandManager.prepare();
        
        getLogger().info("{} v{} has loaded", Direct.NAME, Direct.VERSION);
    }
    
    public boolean reload() {
        if (!getConfiguration().loadConfiguration()) {
            return false;
        }
        
        getConfiguration().saveConfiguration();
        
        try {
            if (getStorage() != null && !getStorage().isClosed()) {
                getLogger().warn("Closing {}", getStorage().getClass().getSimpleName());
                getStorage().close();
            }
            
            String engine = getConfig().map(Config::getStorageCategory).map(StorageCategory::getEngine).orElse(null);
            if (StringUtils.isBlank(engine)) {
                getLogger().warn("No storage engine configured.");
                return true;
            }
            
            if (engine.equalsIgnoreCase("mysql")) {
                this.storage = new MySQLStorage();
            } else {
                getLogger().warn("Invalid storage engine configured.");
                return true;
            }
            
            if (!getStorage().connect()) {
                throw new IllegalStateException("Connection failed");
            }
            
            if (!getStorage().getQuery().createTables()) {
                throw new IllegalStateException("Failed to create tables");
            }
            
            if (DirectManager.prepare()) {
                DirectManager.execute();
                getPlatform().registerServers();
                return true;
            }
            
            return false;
        } catch (Exception ex) {
            getLogger().error("Encountered an error while configuring Storage", ex);
            return false;
        }
    }
    
    private static <T> T check(@Nullable T instance) {
        Preconditions.checkState(instance != null, "%s has not been initialized!", Direct.NAME);
        return instance;
    }
    
    public static boolean isAvailable() {
        return instance != null;
    }
    
    public static Direct getInstance() {
        return check(instance);
    }
    
    public static Platform getPlatform() {
        return check(platform);
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }
    
    public Optional<? extends Config> getConfig() {
        return Optional.ofNullable(getConfiguration().getConfig());
    }
    
    public Storage getStorage() {
        return storage;
    }
}