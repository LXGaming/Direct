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

import com.zaxxer.hikari.HikariDataSource;
import nz.co.lolnet.direct.common.storage.StorageAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLStorageAdapter extends StorageAdapter {
    
    private static HikariDataSource hikariDataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private boolean retrieveGeneratedKeys;
    
    public MySQLStorageAdapter() {
        super("mysql", "com.mysql.jdbc.Driver");
    }
    
    public MySQLStorageAdapter createConnection() throws SQLException {
        if (hikariDataSource == null) {
            hikariDataSource = getHikariDataSource();
        }
        
        if (getConnection() != null && !getConnection().isClosed()) {
            throw new SQLException("Cannot create a connection while there is a pre-existing one");
        }
        
        setConnection(hikariDataSource.getConnection());
        return this;
    }
    
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return prepareStatement(sql, false);
    }
    
    public PreparedStatement prepareStatement(String sql, boolean retrieveGeneratedKeys) throws SQLException {
        if (getConnection() == null) {
            throw new SQLException("Connection is null");
        }
        
        close("PreparedStatement", getPreparedStatement());
        setRetrieveGeneratedKeys(retrieveGeneratedKeys);
        if (isRetrieveGeneratedKeys()) {
            setPreparedStatement(getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS));
        } else {
            setPreparedStatement(getConnection().prepareStatement(sql, Statement.NO_GENERATED_KEYS));
        }
        
        return getPreparedStatement();
    }
    
    public ResultSet execute() throws SQLException {
        if (getPreparedStatement() == null) {
            throw new SQLException("PreparedStatement is null");
        }
        
        close("ResultSet", getResultSet());
        if (getPreparedStatement().execute()) {
            setResultSet(getPreparedStatement().getResultSet());
        } else if (isRetrieveGeneratedKeys()) {
            setResultSet(getPreparedStatement().getGeneratedKeys());
        } else {
            setResultSet(null);
        }
        
        return getResultSet();
    }
    
    @Override
    public void close() {
        close("ResultSet", getResultSet());
        close("PreparedStatement", getPreparedStatement());
        close("Connection", getConnection());
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    protected void setConnection(Connection connection) {
        this.connection = connection;
    }
    
    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }
    
    protected void setPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }
    
    public ResultSet getResultSet() {
        return resultSet;
    }
    
    protected void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }
    
    public boolean isRetrieveGeneratedKeys() {
        return retrieveGeneratedKeys;
    }
    
    protected void setRetrieveGeneratedKeys(boolean retrieveGeneratedKeys) {
        this.retrieveGeneratedKeys = retrieveGeneratedKeys;
    }
}