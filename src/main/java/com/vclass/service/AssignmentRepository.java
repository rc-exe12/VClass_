package com.vclass.service;

import com.vclass.db.DBConnection;
import com.vclass.model.AssignmentCourseChoice;
import com.vclass.model.AssignmentRecord;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Purpose: Contains assignment queries for Admin, Faculty, and Student screens.
public class AssignmentRepository {
// Purpose: Lists courses an Admin or signed-in Faculty member can assign work to.
  public List<AssignmentCourseChoice> findCoursesForStaff(String role,int userId)throws SQLException,IOException{
    String sql="SELECT c.course_id,c.course_code,c.course_name FROM courses c LEFT JOIN faculty f ON f.faculty_id=c.faculty_id "
      +"LEFT JOIN users u ON u.user_id=f.user_id WHERE (?='ADMIN' OR u.user_id=?) ORDER BY c.course_name";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
      p.setString(1,role);p.setInt(2,userId);
      try(ResultSet r=p.executeQuery()){
        List<AssignmentCourseChoice> rows=new ArrayList<>();while(r.next())rows.add(new AssignmentCourseChoice(r.getInt("course_id"),r.getString("course_code"),r.getString("course_name")));return rows;
      }
    }
  }

// Purpose: Lists assignments that the signed-in staff role may manage.
  public List<AssignmentRecord> findForStaff(String role,int userId,String search)throws SQLException,IOException{
    String sql="SELECT a.assignment_id,a.course_id,c.course_code,c.course_name,a.title,a.description,a.due_at,a.max_marks "
      +"FROM assignments a JOIN courses c ON c.course_id=a.course_id LEFT JOIN faculty f ON f.faculty_id=c.faculty_id LEFT JOIN users u ON u.user_id=f.user_id "
      +"WHERE (?='ADMIN' OR u.user_id=?) AND (?='' OR a.title LIKE ? OR c.course_code LIKE ? OR c.course_name LIKE ?) ORDER BY a.due_at";
    String term=search==null?"":search.trim(),pattern="%"+term+"%";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
      p.setString(1,role);p.setInt(2,userId);p.setString(3,term);p.setString(4,pattern);p.setString(5,pattern);p.setString(6,pattern);
      try(ResultSet r=p.executeQuery()){
        List<AssignmentRecord> rows=new ArrayList<>();while(r.next())rows.add(new AssignmentRecord(r.getInt("assignment_id"),r.getInt("course_id"),r.getString("course_code"),r.getString("course_name"),r.getString("title"),r.getString("description"),r.getTimestamp("due_at"),r.getBigDecimal("max_marks"),null,null));return rows;
      }
    }
  }

// Purpose: Lists assignments with the students current submission status and file path.
  public List<AssignmentRecord> findForStudent(int userId,String search)throws SQLException,IOException{
    String sql="SELECT a.assignment_id,a.course_id,c.course_code,c.course_name,a.title,a.description,a.due_at,a.max_marks,s.status,s.file_path "
      +"FROM assignments a JOIN courses c ON c.course_id=a.course_id "
      +"LEFT JOIN submissions s ON s.assignment_id=a.assignment_id AND s.student_id=(SELECT student_id FROM students WHERE user_id=?) "
      +"WHERE EXISTS(SELECT 1 FROM students WHERE user_id=?) AND (?='' OR a.title LIKE ? OR c.course_code LIKE ? OR c.course_name LIKE ?) ORDER BY a.due_at";
    String term=search==null?"":search.trim(),pattern="%"+term+"%";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
      p.setInt(1,userId);p.setInt(2,userId);p.setString(3,term);p.setString(4,pattern);p.setString(5,pattern);p.setString(6,pattern);
      try(ResultSet r=p.executeQuery()){
        List<AssignmentRecord> rows=new ArrayList<>();while(r.next())rows.add(new AssignmentRecord(r.getInt("assignment_id"),r.getInt("course_id"),r.getString("course_code"),r.getString("course_name"),r.getString("title"),r.getString("description"),r.getTimestamp("due_at"),r.getBigDecimal("max_marks"),r.getString("status"),r.getString("file_path")));return rows;
      }
    }
  }

// Purpose: Adds a new assignment for a course.
  public void create(int courseId,String title,String description,Timestamp dueAt,double maxMarks)throws SQLException,IOException{
    String sql="INSERT INTO assignments(course_id,title,description,due_at,max_marks) VALUES(?,?,?,?,?)";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
      p.setInt(1,courseId);p.setString(2,title.trim());p.setString(3,blankToNull(description));p.setTimestamp(4,dueAt);p.setDouble(5,maxMarks);p.executeUpdate();
    }
  }
// Purpose: Changes an assignments course, title, description, deadline, or marks.
  public void update(AssignmentRecord assignment,int courseId,String title,String description,Timestamp dueAt,double maxMarks)throws SQLException,IOException{
    String sql="UPDATE assignments SET course_id=?,title=?,description=?,due_at=?,max_marks=? WHERE assignment_id=?";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
      p.setInt(1,courseId);p.setString(2,title.trim());p.setString(3,blankToNull(description));p.setTimestamp(4,dueAt);p.setDouble(5,maxMarks);p.setInt(6,assignment.getAssignmentId());p.executeUpdate();
    }
  }
// Purpose: Deletes an assignment and removes saved submission files after the database commit.
  public void delete(int assignmentId)throws SQLException,IOException{
    List<String> paths=new ArrayList<>();
    try(Connection c=DBConnection.getConnection()){
      c.setAutoCommit(false);
      try(PreparedStatement find=c.prepareStatement("SELECT file_path FROM submissions WHERE assignment_id=? AND file_path IS NOT NULL")){
        find.setInt(1,assignmentId);try(ResultSet r=find.executeQuery()){while(r.next())paths.add(r.getString(1));}
      }
      try(PreparedStatement p=c.prepareStatement("DELETE FROM assignments WHERE assignment_id=?")){
        p.setInt(1,assignmentId);if(p.executeUpdate()==0)throw new SQLException("Assignment was not found.");
      }
      c.commit();
    }
    for(String path:paths)SubmissionFileStorage.delete(path);
  }
  private String blankToNull(String value){return value==null||value.isBlank()?null:value.trim();}
}







