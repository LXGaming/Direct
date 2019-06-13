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

package io.github.lxgaming.direct.common.configuration;

import io.github.lxgaming.direct.common.configuration.category.LogCategory;
import io.github.lxgaming.direct.common.configuration.category.MCLeaksCategory;
import io.github.lxgaming.direct.common.configuration.category.MessageCategory;
import io.github.lxgaming.direct.common.configuration.category.StorageCategory;

public class Config {
    
    private boolean debug = false;
    private long kickMessageDelay = 1000L;
    private LogCategory log = new LogCategory();
    private MCLeaksCategory mcLeaks = new MCLeaksCategory();
    private MessageCategory messages = new MessageCategory();
    private StorageCategory storage = new StorageCategory();
    
    public boolean isDebug() {
        return debug;
    }
    
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    public long getKickMessageDelay() {
        return kickMessageDelay;
    }
    
    public LogCategory getLog() {
        return log;
    }
    
    public MCLeaksCategory getMcLeaks() {
        return mcLeaks;
    }
    
    public MessageCategory getMessages() {
        return messages;
    }
    
    public StorageCategory getStorage() {
        return storage;
    }
}