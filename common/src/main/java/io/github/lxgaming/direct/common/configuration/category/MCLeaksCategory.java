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

package io.github.lxgaming.direct.common.configuration.category;

import io.github.lxgaming.direct.common.util.Toolbox;

import java.util.Set;

public class MCLeaksCategory {
    
    private boolean enabled = false;
    private long expireAfter = 86400L;
    private int threadCount = 2;
    private Set<String> execution = Toolbox.newHashSet("alert [PLAYER] Account is Untrusted");
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public long getExpireAfter() {
        return expireAfter;
    }
    
    public int getThreadCount() {
        return threadCount;
    }
    
    public Set<String> getExecution() {
        return execution;
    }
}