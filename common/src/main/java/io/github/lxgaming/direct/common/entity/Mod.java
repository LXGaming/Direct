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
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;

public class Mod {
    
    private String id;
    private String name;
    private Set<String> execution;
    
    public @NonNull String getId() {
        return id;
    }
    
    public void setId(@NonNull String id) {
        this.id = id;
    }
    
    public @NonNull String getName() {
        return name;
    }
    
    public void setName(@NonNull String name) {
        this.name = name;
    }
    
    public @Nullable Set<String> getExecution() {
        return execution;
    }
    
    public void setExecution(@NonNull Set<String> execution) {
        this.execution = execution;
    }
}