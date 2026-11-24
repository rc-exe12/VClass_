package com.vclass.db;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;
// Purpose: Loads the local MySQL settings and opens JDBC connections.
public final class DBConnection {
  private static final Path CONFIG = Path.of("database.properties");
  private DBConnection() { }
// Purpose: Reads database.properties and returns a live MySQL connection.
  public static Connection getConnection() throws SQLException, IOException {
    if (!Files.exists(CONFIG)) throw new IOException("database.properties not found. Copy database.properties.example and set your MySQL details.");
    Properties p = new Properties();
    try (InputStream in = Files.newInputStream(CONFIG)) { p.load(in); }
    String url = required(p,"db.url"), username = required(p,"db.username");
    return DriverManager.getConnection(url,username,p.getProperty("db.password",""));
  }
// Purpose: Checks that a required connection setting exists.
  private static String required(Properties p,String key) throws IOException {
    String value=p.getProperty(key);
    if(value==null || value.isBlank()) throw new IOException("Missing "+key+" in database.properties.");
    return value.trim();
  }
}



