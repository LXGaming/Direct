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

package io.github.lxgaming.direct.common.storage.mysql;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonParseException;
import io.github.lxgaming.direct.common.Direct;
import io.github.lxgaming.direct.common.entity.Mod;
import io.github.lxgaming.direct.common.entity.Server;
import io.github.lxgaming.direct.common.storage.Query;
import io.github.lxgaming.direct.common.util.Toolbox;

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
            Direct.getInstance().getLogger().error("Encountered an error while creating tables", ex);
            return false;
        }
    }
    
    @Override
    public List<Mod> getMods() {
        try (Connection connection = storage.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `mod`")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    List<Mod> mods = Lists.newArrayList();
                    while (resultSet.next()) {
                        Mod mod = new Mod();
                        mod.setId(resultSet.getString("id"));
                        mod.setName(resultSet.getString("name"));
                        
                        try {
                            String[] execution = Toolbox.GSON.fromJson(resultSet.getString("execution"), String[].class);
                            mod.setExecution(Sets.newHashSet(execution));
                        } catch (JsonParseException ex) {
                            Direct.getInstance().getLogger().error("Encountered an error while parsing execution for {} ({})", mod.getName(), mod.getId(), ex);
                            mod.setExecution(Sets.newHashSet());
                        }
                        
                        mods.add(mod);
                    }
                    
                    Direct.getInstance().getLogger().info("Found {} Mods in MySQL", mods.size());
                    return mods;
                }
            }
        } catch (SQLException ex) {
            Direct.getInstance().getLogger().error("Encountered an error while getting mods", ex);
            return null;
        }
    }
    
    @Override
    public List<Server> getServers() {
        try (Connection connection = storage.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `server`")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    List<Server> servers = Lists.newArrayList();
                    while (resultSet.next()) {
                        Server server = new Server();
                        server.setName(resultSet.getString("name"));
                        server.setHost(resultSet.getString("host"));
                        server.setPort(resultSet.getInt("port"));
                        
                        try {
                            String[] directConnects = Toolbox.GSON.fromJson(resultSet.getString("direct_connects"), String[].class);
                            server.setDirectConnects(Sets.newHashSet(directConnects));
                        } catch (JsonParseException ex) {
                            Direct.getInstance().getLogger().error("Encountered an error while parsing directConnects for {}", server.getName(), ex);
                            server.setDirectConnects(Sets.newHashSet());
                        }
                        
                        try {
                            Integer[] protocolVersions = Toolbox.GSON.fromJson(resultSet.getString("protocol_versions"), Integer[].class);
                            server.setProtocolVersions(Sets.newHashSet(protocolVersions));
                        } catch (JsonParseException ex) {
                            Direct.getInstance().getLogger().error("Encountered an error while parsing protocolVersions for {}", server.getName(), ex);
                            server.setProtocolVersions(Sets.newHashSet());
                        }
                        
                        server.setMotd(resultSet.getString("motd"));
                        server.setActive(resultSet.getBoolean("active"));
                        server.setLobby(resultSet.getBoolean("lobby"));
                        server.setRestricted(resultSet.getBoolean("restricted"));
                        servers.add(server);
                    }
                    
                    Direct.getInstance().getLogger().info("Found {} Servers in MySQL", servers.size());
                    return servers;
                }
            }
        } catch (SQLException ex) {
            Direct.getInstance().getLogger().error("Encountered an error while getting servers", ex);
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
            Direct.getInstance().getLogger().error("Encountered an error while creating log", ex);
            return false;
        }
    }
}