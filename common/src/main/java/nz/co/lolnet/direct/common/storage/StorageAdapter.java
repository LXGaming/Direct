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

package nz.co.lolnet.direct.common.storage;

import com.zaxxer.hikari.HikariDataSource;
import nz.co.lolnet.direct.common.Direct;
import nz.co.lolnet.direct.common.configuration.Config;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Optional;

public abstract class StorageAdapter implements AutoCloseable {
    
    private final String storageType;
    private final String driverClassName;
    
    protected StorageAdapter(String storageType, String driverClassName) {
        this.storageType = storageType;
        this.driverClassName = driverClassName;
    }
    
    protected HikariDataSource getHikariDataSource() throws SQLException {
        HikariDataSource hikariDataSource = new HikariDataSource();
        Direct.getInstance().getConfig().map(Config::getStorage).ifPresent(storage -> {
            getJdbcUrl(storage.getAddress(), storage.getDatabase()).ifPresent(hikariDataSource::setJdbcUrl);
            hikariDataSource.setDriverClassName(getDriverClassName());
            hikariDataSource.setUsername(storage.getUsername());
            hikariDataSource.setPassword(storage.getPassword());
            hikariDataSource.setMaximumPoolSize(2);
            hikariDataSource.setMinimumIdle(1);
        });
        
        return hikariDataSource;
    }
    
    protected void close(String name, AutoCloseable autoCloseable) {
        try {
            if (autoCloseable != null) {
                autoCloseable.close();
            }
        } catch (Exception ex) {
            Direct.getInstance().getLogger().debug("Failed to close {}", name);
        }
    }
    
    private Optional<String> getJdbcUrl(String address, String database) {
        try {
            return Optional.of(MessageFormat.format("jdbc:{0}://{1}/{2}", getStorageType(), address, database));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
    
    private String getStorageType() {
        return storageType;
    }
    
    public String getDriverClassName() {
        return driverClassName;
    }
}