/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.ndatasource.datasources;

import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * This class represents an {@link XADataSource} implementation which uses a local transaction
 * for its operations. 
 */
public class LocalXADataSource implements XADataSource {

    private PrintWriter out;

    private String url;

    private String username;

    private String password;

    private String driverClassName;

    private String validationQuery;

    private ThreadLocal<Connection> tlConn = new ThreadLocal<Connection>();

    private ConnectionPool connectionPool;

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return this.out;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.out = out;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        //ignore
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    private Connection createConnection() throws SQLException {
        if (this.connectionPool == null) {
            synchronized (LocalXADataSource.class) {
                if (this.connectionPool == null) {
                    this.createConnectionPool();
                }
            }
        }
        Connection conn = this.connectionPool.getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    @Override
    public XAConnection getXAConnection() throws SQLException {
        return new LocalXAConnection();
    }

    @Override
    public XAConnection getXAConnection(String user, String password)
            throws SQLException {
        throw new SQLException("Not Implemented");
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
        try {
            Class.forName(this.driverClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void createConnectionPool() throws SQLException {
    	PoolProperties props = new PoolProperties();
        props.setUrl(this.getUrl());
        props.setUsername(this.getUsername());
        props.setPassword(this.getPassword());
        props.setDriverClassName(this.getDriverClassName());
        props.setValidationQuery(this.getValidationQuery());
        this.connectionPool = new ConnectionPool(props);
    }

    public void initConn() throws SQLException {
        Connection conn = this.tlConn.get();
        if (conn == null || conn.isClosed()) {
            conn = createConnection();
            this.tlConn.set(conn);
        }
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    /**
     * This class represents an {@link XAConnection} implementation which is backed by
     * a normal JDBC {@link Connection}.
     */
    public class LocalXAConnection implements XAConnection {

        private List<ConnectionEventListener> ceListeners = new ArrayList<ConnectionEventListener>();

        @Override
        public Connection getConnection() throws SQLException {
            return new LocalConnection();
        }

        @Override
        public void close() throws SQLException {
            for (ConnectionEventListener listener : this.ceListeners) {
                listener.connectionClosed(new ConnectionEvent(null));
            }
        }

        @Override
        public void addConnectionEventListener(ConnectionEventListener listener) {
            this.ceListeners.add(listener);
        }

        @Override
        public void removeConnectionEventListener(
                ConnectionEventListener listener) {
        	this.ceListeners.remove(listener);
        }

        @Override
        public void addStatementEventListener(StatementEventListener listener) {
            //ignore
        }

        @Override
        public void removeStatementEventListener(StatementEventListener listener) {
            //ignore
        }

        @Override
        public XAResource getXAResource() throws SQLException {
            return new LocalXAResource();
        }

    }

    /**
     * This class represents an {@link XAResource} implementation based on
     * the local transaction model.
     */
    public class LocalXAResource implements XAResource {

        @Override
        public void commit(Xid xid, boolean onePhase) throws XAException {
            try {
                Connection conn = tlConn.get();
                if (conn != null) {
                    conn.commit();
                    conn.close();
                }
            } catch (SQLException e) {
                throw new XAException(e.getMessage());
            } finally {
                tlConn.set(null);
            }
        }

        @Override
        public void end(Xid xid, int flags) throws XAException {
        	//ignore
        }

        @Override
        public void forget(Xid xid) throws XAException {
        	//ignore
        }

        @Override
        public int getTransactionTimeout() throws XAException {
            return 0;
        }

        @Override
        public boolean isSameRM(XAResource xares) throws XAException {
            return false;
        }

        @Override
        public int prepare(Xid xid) throws XAException {
            try {
                Connection conn = tlConn.get();
                if (conn != null) {
                    conn.commit();
                    conn.close();
                }
            } catch (SQLException e) {
                throw new XAException(e.getMessage());
            } finally {
                tlConn.set(null);
            }
            return XAResource.XA_OK;
        }

        @Override
        public Xid[] recover(int flag) throws XAException {
            return new Xid[0];
        }

        @Override
        public void rollback(Xid xid) throws XAException {
            try {
                Connection conn = tlConn.get();
                if (conn != null) {
                    conn.rollback();
                    conn.close();
                }
            } catch (SQLException e) {
                throw new XAException(e.getMessage());
            } finally {
                tlConn.set(null);
            }
        }

        @Override
        public boolean setTransactionTimeout(int seconds) throws XAException {
            return true;
        }

        @Override
        public void start(Xid xid, int flags) throws XAException {
            //ignore
        }

    }

    /**
     * Local {@link Connection} implementation.
     */
    public class LocalConnection implements Connection {

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            initConn();
            return tlConn.get().unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            initConn();
            return tlConn.get().isWrapperFor(iface);
        }

        @Override
        public Statement createStatement() throws SQLException {
            initConn();
            return tlConn.get().createStatement();
        }

        @Override
        public PreparedStatement prepareStatement(String sql)
                throws SQLException {
            initConn();
            return tlConn.get().prepareStatement(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            initConn();
            return tlConn.get().prepareCall(sql);
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            initConn();
            return tlConn.get().nativeSQL(sql);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            initConn();
            tlConn.get().setAutoCommit(autoCommit);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            initConn();
            return tlConn.get().getAutoCommit();
        }

        @Override
        public void commit() throws SQLException {
            initConn();
            tlConn.get().commit();
        }

        @Override
        public void rollback() throws SQLException {
            initConn();
            tlConn.get().rollback();
        }

        @Override
        public void close() throws SQLException {
            initConn();
            tlConn.get().close();
        }

        @Override
        public boolean isClosed() throws SQLException {
            initConn();
            return tlConn.get().isClosed();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            initConn();
            return tlConn.get().getMetaData();
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            initConn();
            tlConn.get().setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            initConn();
            return tlConn.get().isReadOnly();
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {
            initConn();
            tlConn.get().setCatalog(catalog);
        }

        @Override
        public String getCatalog() throws SQLException {
            initConn();
            return tlConn.get().getCatalog();
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            initConn();
            tlConn.get().setTransactionIsolation(level);
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            initConn();
            return tlConn.get().getTransactionIsolation();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            initConn();
            return tlConn.get().getWarnings();
        }

        @Override
        public void clearWarnings() throws SQLException {
            initConn();
            tlConn.get().clearWarnings();
        }

        @Override
        public Statement createStatement(int resultSetType,
                                         int resultSetConcurrency) throws SQLException {
            initConn();
            return tlConn.get().createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public PreparedStatement prepareStatement(String sql,
                                                  int resultSetType, int resultSetConcurrency)
                throws SQLException {
            initConn();
            return tlConn.get().prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType,
                                             int resultSetConcurrency) throws SQLException {
            initConn();
            return tlConn.get().prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            initConn();
            return tlConn.get().getTypeMap();
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
            initConn();
            tlConn.get().setTypeMap(map);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            initConn();
            tlConn.get().setHoldability(holdability);
        }

        @Override
        public int getHoldability() throws SQLException {
            initConn();
            return tlConn.get().getHoldability();
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            initConn();
            return tlConn.get().setSavepoint();
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            initConn();
            return tlConn.get().setSavepoint(name);
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {
            initConn();
            tlConn.get().rollback();
        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            initConn();
            tlConn.get().releaseSavepoint(savepoint);
        }

        @Override
        public Statement createStatement(int resultSetType,
                                         int resultSetConcurrency, int resultSetHoldability)
                throws SQLException {
            initConn();
            return tlConn.get().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql,
                                                  int resultSetType, int resultSetConcurrency,
                                                  int resultSetHoldability) throws SQLException {
            initConn();
            return tlConn.get().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType,
                                             int resultSetConcurrency, int resultSetHoldability)
                throws SQLException {
            initConn();
            return tlConn.get().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql,
                                                  int autoGeneratedKeys) throws SQLException {
            initConn();
            return tlConn.get().prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public PreparedStatement prepareStatement(String sql,
                                                  int[] columnIndexes) throws SQLException {
            initConn();
            return tlConn.get().prepareStatement(sql, columnIndexes);
        }

        @Override
        public PreparedStatement prepareStatement(String sql,
                                                  String[] columnNames) throws SQLException {
            initConn();
            return tlConn.get().prepareStatement(sql, columnNames);
        }

        @Override
        public Clob createClob() throws SQLException {
            initConn();
            return tlConn.get().createClob();
        }

        @Override
        public Blob createBlob() throws SQLException {
            initConn();
            return tlConn.get().createBlob();
        }

        @Override
        public NClob createNClob() throws SQLException {
            initConn();
            return tlConn.get().createNClob();
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            initConn();
            return tlConn.get().createSQLXML();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            initConn();
            return tlConn.get().isValid(timeout);
        }

        @Override
        public void setClientInfo(String name, String value)
                throws SQLClientInfoException {
            try {
                initConn();
            } catch (SQLException e) {
                throw new SQLClientInfoException(e.getMessage(), e.getSQLState(), null);
            }
            tlConn.get().setClientInfo(name, value);
        }

        @Override
        public void setClientInfo(Properties properties)
                throws SQLClientInfoException {
            try {
                initConn();
            } catch (SQLException e) {
                throw new SQLClientInfoException(e.getMessage(), e.getSQLState(), null);
            }
            tlConn.get().setClientInfo(properties);
        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            initConn();
            return tlConn.get().getClientInfo(name);
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            initConn();
            return tlConn.get().getClientInfo();
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements)
                throws SQLException {
            initConn();
            return tlConn.get().createArrayOf(typeName, elements);
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes)
                throws SQLException {
            initConn();
            return tlConn.get().createStruct(typeName, attributes);
        }

        @Override
        public void setSchema(String schema) throws SQLException {
        	initConn();
            tlConn.get().setSchema(schema);
        }

        @Override
        public String getSchema() throws SQLException {
        	initConn();
            return tlConn.get().getSchema();
        }

        @Override
        public void abort(Executor executor) throws SQLException {
        	initConn();
            tlConn.get().abort(executor);
        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        	initConn();
            tlConn.get().setNetworkTimeout(executor, milliseconds);
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
        	initConn();
            return tlConn.get().getNetworkTimeout();
        }

    }

}
