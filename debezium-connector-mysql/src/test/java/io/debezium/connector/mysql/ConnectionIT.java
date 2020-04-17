/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.connector.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import io.debezium.connector.mysql.MySQLConnection.MySqlVersion;
import io.debezium.connector.mysql.junit.SkipTestDependingOnDatabaseVersionRule;
import io.debezium.connector.mysql.junit.SkipWhenDatabaseVersion;
import io.debezium.util.Testing;

@SkipWhenDatabaseVersion(version = MySqlVersion.MYSQL_5_5, reason = "MySQL 5.5 does not support CURRENT_TIMESTAMP on DATETIME and only a single column can specify default CURRENT_TIMESTAMP, lifted in MySQL 5.6.5")
public class ConnectionIT implements Testing {

    @Rule
    public SkipTestDependingOnDatabaseVersionRule skipRule = new SkipTestDependingOnDatabaseVersionRule();

    @Ignore
    @Test
    public void shouldConnectToDefaultDatabase() throws SQLException {
        try (MySQLConnection conn = MySQLConnection.forTestDatabase("mysql");) {
            conn.connect();
        }
    }

    @Test
    public void shouldDoStuffWithDatabase() throws SQLException {
        final UniqueDatabase DATABASE = new UniqueDatabase("readbinlog", "readbinlog_test");
        DATABASE.createAndInitialize();
        try (MySQLConnection conn = MySQLConnection.forTestDatabase(DATABASE.getDatabaseName());) {
            conn.connect();
            // Set up the table as one transaction and wait to see the events ...
            conn.execute("DROP TABLE IF EXISTS person",
                    "CREATE TABLE person ("
                            + "  name VARCHAR(255) primary key,"
                            + "  birthdate DATE NULL,"
                            + "  age INTEGER NULL DEFAULT 10,"
                            + "  salary DECIMAL(5,2),"
                            + "  bitStr BIT(18)"
                            + ")");
            conn.execute("SELECT * FROM person");
            try (ResultSet rs = conn.connection().getMetaData().getColumns("readbinlog_test", null, null, null)) {
                // if ( Testing.Print.isEnabled() ) conn.print(rs);
            }
        }
    }

    @Ignore
    @Test
    public void shouldConnectToEmptyDatabase() throws SQLException {
        try (MySQLConnection conn = MySQLConnection.forTestDatabase("emptydb");) {
            conn.connect();
        }
    }
}
