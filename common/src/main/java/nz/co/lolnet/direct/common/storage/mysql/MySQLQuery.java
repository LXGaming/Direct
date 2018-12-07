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

package nz.co.lolnet.direct.common.storage.mysql;

import nz.co.lolnet.direct.common.Direct;
import nz.co.lolnet.direct.common.data.ModData;
import nz.co.lolnet.direct.common.data.ServerData;
import nz.co.lolnet.direct.common.util.Toolbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MySQLQuery {
    
    public static boolean createTables() {
        try (MySQLStorageAdapter storageAdapter = new MySQLStorageAdapter()) {
            storageAdapter.createConnection().prepareStatement("CREATE TABLE IF NOT EXISTS `log` ("
                    + "`id` INT(11) NOT NULL AUTO_INCREMENT,"
                    + "`timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "`unique_id` CHAR(36) NOT NULL,"
                    + "`type` TEXT DEFAULT NULL,"
                    + "`details` TEXT DEFAULT NULL,"
                    + "PRIMARY KEY (`id`));");
            storageAdapter.execute();
            
            storageAdapter.prepareStatement("CREATE TABLE IF NOT EXISTS `mod` ("
                    + "`id` VARCHAR(255) NOT NULL,"
                    + "`name` VARCHAR(255) DEFAULT NULL,"
                    + "`execution` TEXT DEFAULT NULL,"
                    + "PRIMARY KEY (`id`));");
            storageAdapter.execute();
            
            storageAdapter.prepareStatement("CREATE TABLE IF NOT EXISTS `server` ("
                    + "`name` VARCHAR(255) NOT NULL,"
                    + "`host` VARCHAR(255) NOT NULL DEFAULT ?,"
                    + "`port` SMALLINT(5) UNSIGNED NOT NULL DEFAULT ?,"
                    + "`direct_connects` TEXT DEFAULT NULL,"
                    + "`protocol_versions` TEXT DEFAULT NULL,"
                    + "`motd` VARCHAR(255) NOT NULL DEFAULT ?,"
                    + "`active` TINYINT(1) NOT NULL DEFAULT ?,"
                    + "`lobby` TINYINT(1) NOT NULL DEFAULT ?,"
                    + "`restricted` TINYINT(1) NOT NULL DEFAULT ?,"
                    + "PRIMARY KEY (`name`));");
            storageAdapter.getPreparedStatement().setString(1, "localhost");
            storageAdapter.getPreparedStatement().setInt(2, 25565);
            storageAdapter.getPreparedStatement().setString(3, "&9&l[Direct] &r[SERVER]");
            storageAdapter.getPreparedStatement().setInt(4, 0);
            storageAdapter.getPreparedStatement().setInt(5, 0);
            storageAdapter.getPreparedStatement().setInt(6, 0);
            storageAdapter.execute();
            return true;
        } catch (SQLException ex) {
            Direct.getInstance().getLogger().error("Encountered an error processing MySQLQuery::createTables", ex);
            return false;
        }
    }
    
    public static Optional<List<ModData>> getMods() {
        try (MySQLStorageAdapter storageAdapter = new MySQLStorageAdapter()) {
            storageAdapter.createConnection().prepareStatement("SELECT * FROM `mod`");
            ResultSet resultSet = storageAdapter.execute();
            List<ModData> mods = Toolbox.newArrayList();
            while (resultSet.next()) {
                ModData modData = new ModData();
                modData.setId(resultSet.getString("id"));
                modData.setName(resultSet.getString("name"));
                modData.setExecution(Toolbox.buildElements(resultSet.getString("execution"), String.class).orElse(Toolbox.newHashSet()));
                mods.add(modData);
            }
            
            Direct.getInstance().getLogger().info("Found {} Mods in MySQL", mods.size());
            return Optional.of(mods);
        } catch (SQLException ex) {
            Direct.getInstance().getLogger().error("Encountered an error processing MySQLQuery::getMods", ex);
            return Optional.empty();
        }
    }
    
    public static Optional<List<ServerData>> getServers() {
        try (MySQLStorageAdapter storageAdapter = new MySQLStorageAdapter()) {
            storageAdapter.createConnection().prepareStatement("SELECT * FROM `server`");
            ResultSet resultSet = storageAdapter.execute();
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
            
            Direct.getInstance().getLogger().info("Found {} Servers in MySQL", servers.size());
            return Optional.of(servers);
        } catch (SQLException ex) {
            Direct.getInstance().getLogger().error("Encountered an error processing MySQLQuery::getServers", ex);
            return Optional.empty();
        }
    }
    
    public static boolean createLog(UUID uniqueId, String type, String details) {
        try (MySQLStorageAdapter storageAdapter = new MySQLStorageAdapter()) {
            storageAdapter.createConnection().prepareStatement("INSERT INTO `log` (`unique_id`, `type`, `details`) VALUES (?, ?, ?);");
            storageAdapter.getPreparedStatement().setString(1, uniqueId.toString());
            storageAdapter.getPreparedStatement().setString(2, type);
            storageAdapter.getPreparedStatement().setString(3, details);
            return storageAdapter.getPreparedStatement().executeUpdate() != 0;
        } catch (SQLException ex) {
            Direct.getInstance().getLogger().error("Encountered an error processing MySQLQuery::getServers", ex);
            return false;
        }
    }
}