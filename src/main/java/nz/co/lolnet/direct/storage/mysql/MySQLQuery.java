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

package nz.co.lolnet.direct.storage.mysql;

import nz.co.lolnet.direct.Direct;
import nz.co.lolnet.direct.data.ServerData;
import nz.co.lolnet.direct.util.Toolbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MySQLQuery {
    
    public static boolean createTable() {
        try (MySQLStorageAdapter storageAdapter = new MySQLStorageAdapter()) {
            storageAdapter.createConnection().prepareStatement("CREATE TABLE IF NOT EXISTS `server` (" +
                    "`name` VARCHAR(255) NOT NULL," +
                    "`host` VARCHAR(255) NOT NULL DEFAULT ?," +
                    "`port` SMALLINT(5) UNSIGNED NOT NULL DEFAULT ?," +
                    "`direct_connects` TEXT DEFAULT NULL," +
                    "`protocol_versions` TEXT DEFAULT NULL," +
                    "`motd` VARCHAR(255) NOT NULL DEFAULT ?," +
                    "`active` TINYINT(1) NOT NULL DEFAULT ?," +
                    "`lobby` TINYINT(1) NOT NULL DEFAULT ?," +
                    "`restricted` TINYINT(1) NOT NULL DEFAULT ?," +
                    "PRIMARY KEY (`name`));");
            storageAdapter.getPreparedStatement().setString(1, "localhost");
            storageAdapter.getPreparedStatement().setInt(2, 25565);
            storageAdapter.getPreparedStatement().setString(3, "&9&l[Direct] &r[SERVER]");
            storageAdapter.getPreparedStatement().setInt(4, 0);
            storageAdapter.getPreparedStatement().setInt(5, 0);
            storageAdapter.getPreparedStatement().setInt(6, 0);
            storageAdapter.execute();
            return true;
        } catch (SQLException ex) {
            Direct.getInstance().getLogger().severe("Encountered an error processing MySQLQuery::createTable");
            ex.printStackTrace();
            return false;
        }
    }
    
    public static Optional<List<ServerData>> getServers() {
        try (MySQLStorageAdapter storageAdapter = new MySQLStorageAdapter()) {
            storageAdapter.createConnection().prepareStatement("SELECT * FROM `server`");
            ResultSet resultSet = storageAdapter.execute();
            Objects.requireNonNull(resultSet, "ResultSet is null");
            List<ServerData> servers = Toolbox.newArrayList();
            while (resultSet.next()) {
                ServerData serverData = new ServerData();
                serverData.setName(resultSet.getString("name"));
                serverData.setHost(resultSet.getString("host"));
                serverData.setPort(resultSet.getInt("port"));
                serverData.setDirectConnects(Toolbox.buildElements(resultSet.getString("direct_connects"), String.class).orElse(Toolbox.newHashSet()));
                serverData.setProtocolVersions(Toolbox.buildElements(resultSet.getString("protocol_versions"), Integer.class).orElse(Toolbox.newHashSet()));
                serverData.setMotd(resultSet.getString("motd"));
                serverData.setActive(resultSet.getBoolean("active"));
                serverData.setLobby(resultSet.getBoolean("lobby"));
                serverData.setRestricted(resultSet.getBoolean("restricted"));
                servers.add(serverData);
            }
            
            Direct.getInstance().debugMessage("Found " + servers.size() + " Servers in MySQL.");
            return Optional.of(servers);
        } catch (SQLException ex) {
            Direct.getInstance().getLogger().severe("Encountered an error processing MySQLQuery::getServers");
            ex.printStackTrace();
            return Optional.empty();
        }
    }
}