/*
 * Copyright 2019 lolnet.co.nz
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

package nz.co.lolnet.direct.common.storage;

import nz.co.lolnet.direct.common.data.ModData;
import nz.co.lolnet.direct.common.data.ServerData;

import java.util.List;
import java.util.UUID;

public interface Query {
    
    boolean createTables();
    
    List<ModData> getMods();
    
    List<ServerData> getServers();
    
    boolean createLog(UUID uniqueId, String type, String details);
}