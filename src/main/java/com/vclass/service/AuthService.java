package com.vclass.service;
import com.vclass.db.DBConnection;
import com.vclass.model.User;
import com.vclass.util.PasswordUtil;
import java.io.IOException;
import java.sql.*;
// Purpose: Checks login details against the users table.
public class AuthService {
// Purpose: Returns the matching active user, or null when credentials do not match.
  public User login(String email,char[] password) throws SQLException,IOException {
    String sql="SELECT user_id,full_name,email,role FROM users WHERE email=? AND password_hash=? AND is_active=TRUE";
    try(Connection c=DBConnection.getConnection(); PreparedStatement s=c.prepareStatement(sql)) {
      s.setString(1,email.trim()); s.setString(2,PasswordUtil.sha256(password));
      try(ResultSet r=s.executeQuery()) {
        if(!r.next()) return null;
        return new User(r.getInt("user_id"),r.getString("full_name"),r.getString("email"),r.getString("role"));
      }
    }
  }
}


