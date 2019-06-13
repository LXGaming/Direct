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

package io.github.lxgaming.direct.common.data;

import java.nio.file.Path;

public interface Platform {
    
    void registerServers();
    
    void executeAsync(Runnable runnable);
    
    boolean executeCommand(String command);
    
    Type getType();
    
    Path getPath();
    
    enum Type {
        
        BUNGEE("bungeecord", "BungeeCord"),
        VELOCITY("velocity", "Velocity");
        
        private final String id;
        private final String name;
        
        Type(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        @Override
        public String toString() {
            return name();
        }
    }
}