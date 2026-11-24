package com.vclass.service;

import com.vclass.db.DBConnection;
import com.vclass.model.StudentRecord;
import com.vclass.util.PasswordUtil;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Purpose: Contains the SQL operations for student accounts and profiles.
public class StudentRepository {
// Purpose: Searches students and converts database rows into StudentRecord objects.
  public List<StudentRecord> findAll(String search) throws SQLException,IOException {
    String sql="SELECT s.student_id,u.user_id,s.student_number,u.full_name,u.email,s.department,s.semester,u.is_active "
      +"FROM students s JOIN users u ON u.user_id=s.user_id "
      +"WHERE (?='' OR s.student_number LIKE ? OR u.full_name LIKE ? OR u.email LIKE ? OR s.department LIKE ?) "
      +"ORDER BY u.full_name";
    String pattern="%"+(search==null?"":search.trim())+"%";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)) {
      String term=search==null?"":search.trim();p.setString(1,term);
      p.setString(2,pattern);p.setString(3,pattern);p.setString(4,pattern);p.setString(5,pattern);
      try(ResultSet r=p.executeQuery()) {
        List<StudentRecord> rows=new ArrayList<>();
        while(r.next()) {
          int semester=r.getInt("semester");Integer semesterValue=r.wasNull()?null:semester;
          rows.add(new StudentRecord(r.getInt("student_id"),r.getInt("user_id"),r.getString("student_number"),
            r.getString("full_name"),r.getString("email"),r.getString("department"),semesterValue,r.getBoolean("is_active")));
        }
        return rows;
      }
    }
  }

// Purpose: Creates a student login and profile together in one transaction.
  public void create(String fullName,String email,char[] password,String studentNumber,String department,int semester) throws SQLException,IOException {
    if(password==null||password.length==0)throw new IllegalArgumentException("A password is required for a new student account.");
    String userSql="INSERT INTO users(full_name,email,password_hash,role,is_active) VALUES(?,?,?,'STUDENT',TRUE)";
    String studentSql="INSERT INTO students(user_id,student_number,department,semester) VALUES(?,?,?,?)";
    try(Connection c=DBConnection.getConnection()) {
      c.setAutoCommit(false);
      try(PreparedStatement user=c.prepareStatement(userSql,Statement.RETURN_GENERATED_KEYS)) {
        user.setString(1,fullName.trim());user.setString(2,email.trim());user.setString(3,PasswordUtil.sha256(password));user.executeUpdate();
        try(ResultSet keys=user.getGeneratedKeys()) {
          if(!keys.next())throw new SQLException("Could not get the new user ID.");
          try(PreparedStatement student=c.prepareStatement(studentSql)) {
            student.setInt(1,keys.getInt(1));student.setString(2,studentNumber.trim());student.setString(3,blankToNull(department));student.setInt(4,semester);student.executeUpdate();
          }
        }
        c.commit();
      } catch(SQLException|RuntimeException e) { c.rollback();throw e; }
    }
  }

// Purpose: Updates the student login and profile, and changes a password only when supplied.
  public void update(StudentRecord student,String fullName,String email,char[] newPassword,String studentNumber,String department,int semester) throws SQLException,IOException {
    boolean changePassword=newPassword!=null&&newPassword.length>0;
    String userSql=changePassword
      ?"UPDATE users SET full_name=?,email=?,password_hash=? WHERE user_id=? AND role='STUDENT'"
      :"UPDATE users SET full_name=?,email=? WHERE user_id=? AND role='STUDENT'";
    String studentSql="UPDATE students SET student_number=?,department=?,semester=? WHERE student_id=?";
    try(Connection c=DBConnection.getConnection()) {
      c.setAutoCommit(false);
      try(PreparedStatement user=c.prepareStatement(userSql)) {
        user.setString(1,fullName.trim());user.setString(2,email.trim());
        if(changePassword){user.setString(3,PasswordUtil.sha256(newPassword));user.setInt(4,student.getUserId());}
        else user.setInt(3,student.getUserId());
        user.executeUpdate();
        try(PreparedStatement profile=c.prepareStatement(studentSql)) {
          profile.setString(1,studentNumber.trim());profile.setString(2,blankToNull(department));profile.setInt(3,semester);profile.setInt(4,student.getStudentId());profile.executeUpdate();
        }
        c.commit();
      } catch(SQLException|RuntimeException e) { c.rollback();throw e; }
    }
  }

// Purpose: Deletes the student linked login; MySQL removes dependent student records.
  public void delete(int studentId) throws SQLException,IOException {
    String sql="DELETE u FROM users u JOIN students s ON s.user_id=u.user_id WHERE s.student_id=? AND u.role='STUDENT'";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)) {
      p.setInt(1,studentId);
      if(p.executeUpdate()==0)throw new SQLException("Student account was not found.");
    }
  }

// Purpose: Converts an empty optional field to SQL NULL.
  private String blankToNull(String value){return value==null||value.isBlank()?null:value.trim();}
}






