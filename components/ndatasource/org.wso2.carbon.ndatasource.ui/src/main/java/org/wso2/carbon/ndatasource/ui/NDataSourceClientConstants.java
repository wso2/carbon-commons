package org.wso2.carbon.ndatasource.ui;

public class NDataSourceClientConstants {
	
	public static final String RDBMS_DTAASOURCE_TYPE = "RDBMS";

    /**
     * JDBC URL prefixes used to identify the RDBMS engine from the driver.
     */
    public static final class JDBCDriverPrefixes {

        private JDBCDriverPrefixes() {
            throw new AssertionError();
        }

        public static final String MYSQL = "jdbc:mysql";
        public static final String DERBY = "jdbc:derby";
        public static final String MSSQL = "jdbc:sqlserver";
        public static final String ORACLE = "jdbc:oracle";
        public static final String DB2 = "jdbc:db2";
        public static final String HSQLDB = "jdbc:hsqldb";
        public static final String POSTGRESQL = "jdbc:postgresql";
        public static final String SYBASE = "jdbc:sybase";
        public static final String H2 = "jdbc:h2";
        public static final String INFORMIX = "jdbc:informix-sqli";
    }

    /**
     * List of RDBMS engines.
     */
    public static final class RDBMSEngines {

        private RDBMSEngines() {
            throw new AssertionError();
        }

        public static final String MYSQL = "mysql";
        public static final String DERBY = "derby";
        public static final String MSSQL = "mssqlserver";
        public static final String ORACLE = "oracle";
        public static final String DB2 = "db2";
        public static final String HSQLDB = "hsqldb";
        public static final String POSTGRESQL = "postgresql";
        public static final String SYBASE = "sybase";
        public static final String H2 = "h2";
        public static final String INFORMIX_SQLI = "informix-sqli";
        public static final String GENERIC = "Generic";
    }
}
