/*
 * Copyright 2019 Alex Thomson
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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.lxgaming.direct.common.Direct;
import io.github.lxgaming.direct.common.configuration.Config;
import io.github.lxgaming.direct.common.configuration.category.StorageCategory;
import io.github.lxgaming.direct.common.storage.Query;
import io.github.lxgaming.direct.common.storage.Storage;
import io.github.lxgaming.direct.common.util.Reference;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLStorage implements Storage {
    
    private final MySQLQuery query = new MySQLQuery(this);
    private volatile HikariDataSource hikariDataSource;
    
    @Override
    public boolean connect() throws Exception {
        if (!isClosed()) {
            Direct.getInstance().getLogger().warn("HikariDataSource is already connected");
            return hikariDataSource.isRunning();
        }
        
        StorageCategory storage = Direct.getInstance().getConfig().map(Config::getStorage).orElseThrow(IllegalStateException::new);
        
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName(Reference.ID + "-hikari");
        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
        hikariConfig.setJdbcUrl(String.format("jdbc:%s://%s/%s", "mysql", storage.getAddress(), storage.getDatabase()));
        hikariConfig.setUsername(storage.getUsername());
        hikariConfig.setPassword(storage.getPassword());
        hikariConfig.setMaximumPoolSize(storage.getMaximumPoolSize());
        hikariConfig.setMinimumIdle(storage.getMinimumIdle());
        
        hikariDataSource = new HikariDataSource(hikariConfig);
        return hikariDataSource.isRunning();
    }
    
    @Override
    public void close() {
        if (isClosed()) {
            Direct.getInstance().getLogger().warn("HikariDataSource is already closed");
            return;
        }
        
        hikariDataSource.close();
        hikariDataSource = null;
    }
    
    @Override
    public boolean isClosed() {
        return hikariDataSource == null || hikariDataSource.isClosed();
    }
    
    @Override
    public Query getQuery() {
        return query;
    }
    
    public Connection getConnection() throws SQLException {
        if (isClosed()) {
            throw new SQLException("HikariDataSource has been closed");
        }
        
        return hikariDataSource.getConnection();
    }
}