package com.vclass.service;

import com.vclass.db.DBConnection;
import com.vclass.model.FacultyRecord;
import com.vclass.util.PasswordUtil;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Purpose: Contains the SQL operations for Faculty accounts and profiles.
public class FacultyRepository {
// Purpose: Searches Faculty records and returns model objects for the table.
  public List<FacultyRecord> findAll(String search) throws SQLException,IOException {
    String sql="SELECT f.faculty_id,u.user_id,f.employee_number,u.full_name,u.email,f.department,u.is_active "
      +"FROM faculty f JOIN users u ON u.user_id=f.user_id "
      +"WHERE (?='' OR f.employee_number LIKE ? OR u.full_name LIKE ? OR u.email LIKE ? OR f.department LIKE ?) "
      +"ORDER BY u.full_name";
    String term=search==null?"":search.trim(),pattern="%"+term+"%";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)) {
      p.setString(1,term);p.setString(2,pattern);p.setString(3,pattern);p.setString(4,pattern);p.setString(5,pattern);
      try(ResultSet r=p.executeQuery()) {
        List<FacultyRecord> rows=new ArrayList<>();
        while(r.next())rows.add(new FacultyRecord(r.getInt("faculty_id"),r.getInt("user_id"),r.getString("employee_number"),
          r.getString("full_name"),r.getString("email"),r.getString("department"),r.getBoolean("is_active")));
        return rows;
      }
    }
  }

// Purpose: Creates a Faculty login and its profile together.
  public void create(String fullName,String email,char[] password,String employeeNumber,String department) throws SQLException,IOException {
    if(password==null||password.length==0)throw new IllegalArgumentException("A password is required for a new faculty account.");
    String userSql="INSERT INTO users(full_name,email,password_hash,role,is_active) VALUES(?,?,?,'FACULTY',TRUE)";
    String facultySql="INSERT INTO faculty(user_id,employee_number,department) VALUES(?,?,?)";
    try(Connection c=DBConnection.getConnection()) {
      c.setAutoCommit(false);
      try(PreparedStatement user=c.prepareStatement(userSql,Statement.RETURN_GENERATED_KEYS)) {
        user.setString(1,fullName.trim());user.setString(2,email.trim());user.setString(3,PasswordUtil.sha256(password));user.executeUpdate();
        try(ResultSet keys=user.getGeneratedKeys()) {
          if(!keys.next())throw new SQLException("Could not get the new user ID.");
          try(PreparedStatement faculty=c.prepareStatement(facultySql)) {
            faculty.setInt(1,keys.getInt(1));faculty.setString(2,employeeNumber.trim());faculty.setString(3,blankToNull(department));faculty.executeUpdate();
          }
        }
        c.commit();
      }catch(SQLException|RuntimeException e){c.rollback();throw e;}
    }
  }

// Purpose: Updates Faculty account and profile details, including an optional new password.
  public void update(FacultyRecord faculty,String fullName,String email,char[] newPassword,String employeeNumber,String department) throws SQLException,IOException {
    boolean changePassword=newPassword!=null&&newPassword.length>0;
    String userSql=changePassword?"UPDATE users SET full_name=?,email=?,password_hash=? WHERE user_id=? AND role='FACULTY'":"UPDATE users SET full_name=?,email=? WHERE user_id=? AND role='FACULTY'";
    String facultySql="UPDATE faculty SET employee_number=?,department=? WHERE faculty_id=?";
    try(Connection c=DBConnection.getConnection()) {
      c.setAutoCommit(false);
      try(PreparedStatement user=c.prepareStatement(userSql)) {
        user.setString(1,fullName.trim());user.setString(2,email.trim());
        if(changePassword){user.setString(3,PasswordUtil.sha256(newPassword));user.setInt(4,faculty.getUserId());}
        else user.setInt(3,faculty.getUserId());
        user.executeUpdate();
        try(PreparedStatement profile=c.prepareStatement(facultySql)) {
          profile.setString(1,employeeNumber.trim());profile.setString(2,blankToNull(department));profile.setInt(3,faculty.getFacultyId());profile.executeUpdate();
        }
        c.commit();
      }catch(SQLException|RuntimeException e){c.rollback();throw e;}
    }
  }

// Purpose: Deletes the Faculty login and profile; assigned courses become unassigned.
  public void delete(int facultyId) throws SQLException,IOException {
    String sql="DELETE u FROM users u JOIN faculty f ON f.user_id=u.user_id WHERE f.faculty_id=? AND u.role='FACULTY'";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)) {
      p.setInt(1,facultyId);if(p.executeUpdate()==0)throw new SQLException("Faculty account was not found.");
    }
  }
  private String blankToNull(String value){return value==null||value.isBlank()?null:value.trim();}
}





