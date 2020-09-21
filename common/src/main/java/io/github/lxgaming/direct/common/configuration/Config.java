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

import com.google.gson.annotations.SerializedName;
import io.github.lxgaming.direct.common.configuration.category.GeneralCategory;
import io.github.lxgaming.direct.common.configuration.category.LogCategory;
import io.github.lxgaming.direct.common.configuration.category.StorageCategory;

public class Config {
    
    @SerializedName("general")
    private GeneralCategory generalCategory = new GeneralCategory();
    
    @SerializedName("log")
    private LogCategory logCategory = new LogCategory();
    
    @SerializedName("storage")
    private StorageCategory storageCategory = new StorageCategory();
    
    public GeneralCategory getGeneralCategory() {
        return generalCategory;
    }
    
    public LogCategory getLogCategory() {
        return logCategory;
    }
    
    public StorageCategory getStorageCategory() {
        return storageCategory;
    }
}