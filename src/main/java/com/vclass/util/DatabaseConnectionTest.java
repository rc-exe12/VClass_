package com.vclass.util;
import com.vclass.db.DBConnection;
import java.sql.*;
// Purpose: Small command-line entry point used to check JDBC settings.
public class DatabaseConnectionTest {
// Purpose: Opens a test connection and prints success or a useful error.
  public static void main(String[] args) {
    try(Connection c=DBConnection.getConnection()) {
      DatabaseMetaData m=c.getMetaData();
      System.out.println("Connected successfully to "+m.getDatabaseProductName()+" "+m.getDatabaseProductVersion());
      System.out.println("Database: "+c.getCatalog());
    } catch(Exception e) {
      System.err.println("Database connection failed: "+e.getMessage());
      System.err.println("Check MySQL is running and database.properties has the correct URL, username and password.");
      System.exit(1);
    }
  }
}


