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
import nz.co.lolnet.direct.common.storage.Query;
import nz.co.lolnet.direct.common.util.Toolbox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class MySQLQuery implements Query {
    
    private final MySQLStorage storage;
    
    MySQLQuery(MySQLStorage storage) {
        this.storage = storage;
    }
    
    @Override
    public boolean createTables() {
        try (Connection connection = storage.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(""
                    + "CREATE TABLE IF NOT EXISTS `log` ("
                    + "`id` INT(11) NOT NULL AUTO_INCREMENT,"
                    + "`timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "`unique_id` CHAR(36) NOT NULL,"
                    + "`type` TEXT DEFAULT NULL,"
                    + "`details` TEXT DEFAULT NULL,"
                    + "PRIMARY KEY (`id`));")) {
                preparedStatement.execute();
            }
            
            try (PreparedStatement preparedStatement = connection.prepareStatement(""
                    + "CREATE TABLE IF NOT EXISTS `mod` ("
                    + "`id` VARCHAR(255) NOT NULL,"
                    + "`name` VARCHAR(255) DEFAULT NULL,"
                    + "`execution` TEXT DEFAULT NULL,"
                    + "PRIMARY KEY (`id`));")) {
                preparedStatement.execute();
            }
            
            try (PreparedStatement preparedStatement = connection.prepareStatement(""
                    + "CREATE TABLE IF NOT EXISTS `server` ("
                    + "`name` VARCHAR(255) NOT NULL,"
                    + "`host` VARCHAR(255) NOT NULL DEFAULT ?,"
                    + "`port` SMALLINT(5) UNSIGNED NOT NULL DEFAULT ?,"
                    + "`direct_connects` TEXT DEFAULT NULL,"
                    + "`protocol_versions` TEXT DEFAULT NULL,"
                    + "`motd` VARCHAR(255) NOT NULL DEFAULT ?,"
                    + "`active` TINYINT(1) NOT NULL DEFAULT ?,"
                    + "`lobby` TINYINT(1) NOT NULL DEFAULT ?,"
                    + "`restricted` TINYINT(1) NOT NULL DEFAULT ?,"
                    + "PRIMARY KEY (`name`));")) {
                preparedStatement.setString(1, "localhost");
                preparedStatement.setInt(2, 25565);
                preparedStatement.setString(3, "&9&l[Direct] &r[SERVER]");
                preparedStatement.setInt(4, 0);
                preparedStatement.setInt(5, 0);
                preparedStatement.setInt(6, 0);
                preparedStatement.execute();
            }
            
            return true;
        } catch (SQLException ex) {
            Direct.getInstance().getLogger().error("Encountered an error processing MySQLQuery::createTables", ex);
            return false;
        }
    }
    
    @Override
    public List<ModData> getMods() {
        try (Connection connection = storage.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `mod`")) {
                ResultSet resultSet = preparedStatement.executeQuery();
                List<ModData> mods = Toolbox.newArrayList();
                while (resultSet.next()) {
                    ModData modData = new ModData();
                    modData.setId(resultSet.getString("id"));
                    modData.setName(resultSet.getString("name"));
                    modData.setExecution(Toolbox.buildElements(resultSet.getString("execution"), String.class).orElse(Toolbox.newHashSet()));
                    mods.add(modData);
                }
                
                Direct.getInstance().getLogger().info("Found {} Mods in MySQL", mods.size());
                return mods;
            }
        } catch (SQLException ex) {
            Direct.getInstance().getLogger().error("Encountered an error processing MySQLQuery::getMods", ex);
            return null;
        }
    }
    
    @Override
    public List<ServerData> getServers() {
        try (Connection connection = storage.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `server`")) {
                ResultSet resultSet = preparedStatement.executeQuery();
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
                return servers;
            }
        } catch (SQLException ex) {
            Direct.getInstance().getLogger().error("Encountered an error processing MySQLQuery::getServers", ex);
            return null;
        }
    }
    
    @Override
    public boolean createLog(UUID uniqueId, String type, String details) {
        try (Connection connection = storage.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(""
                    + "INSERT INTO `log` (`unique_id`, `type`, `details`) VALUES (?, ?, ?);")) {
                preparedStatement.setString(1, uniqueId.toString());
                preparedStatement.setString(2, type);
                preparedStatement.setString(3, details);
                return preparedStatement.executeUpdate() != 0;
            }
        } catch (SQLException ex) {
            Direct.getInstance().getLogger().error("Encountered an error processing MySQLQuery::getServers", ex);
            return false;
        }
    }
}