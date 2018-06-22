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

package nz.co.lolnet.direct.storage;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import nz.co.lolnet.direct.Direct;

import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.Optional;

public abstract class StorageAdapter implements AutoCloseable {
    
    private final String storageType;
    
    protected StorageAdapter(String storageType) {
        this.storageType = storageType;
    }
    
    protected DataSource getDataSource() {
        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        Direct.getInstance().getConfiguration().map(configuration -> configuration.getSection("Direct.DataSource")).ifPresent(configuration -> {
            getJdbcUrl(configuration.getString("Host"), configuration.getInt("Port"), configuration.getString("Database")).ifPresent(mysqlDataSource::setUrl);
            mysqlDataSource.setUser(configuration.getString("Username"));
            mysqlDataSource.setPassword(configuration.getString("Password"));
        });
        
        return mysqlDataSource;
    }
    
    protected void close(String name, AutoCloseable autoCloseable) {
        try {
            if (autoCloseable != null) {
                autoCloseable.close();
            }
        } catch (Exception ex) {
            Direct.getInstance().debugMessage("Failed to close " + name);
        }
    }
    
    private Optional<String> getJdbcUrl(String host, int port, String database) {
        try {
            return Optional.of(MessageFormat.format("jdbc:{0}://{1}:{2,number,#}/{3}", getStorageType(), host, port, database));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
    
    private String getStorageType() {
        return storageType;
    }
}