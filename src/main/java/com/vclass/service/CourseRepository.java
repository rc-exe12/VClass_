package com.vclass.service;

import com.vclass.db.DBConnection;
import com.vclass.model.CourseRecord;
import com.vclass.model.FacultyChoice;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Purpose: Contains the SQL operations for course records.
public class CourseRepository {
// Purpose: Searches courses and includes the assigned Faculty name.
  public List<CourseRecord> findAll(String search)throws SQLException,IOException{
    String sql="SELECT c.course_id,c.course_code,c.course_name,c.description,f.faculty_id,u.full_name AS faculty_name "
      +"FROM courses c LEFT JOIN faculty f ON f.faculty_id=c.faculty_id LEFT JOIN users u ON u.user_id=f.user_id "
      +"WHERE (?='' OR c.course_code LIKE ? OR c.course_name LIKE ? OR u.full_name LIKE ?) ORDER BY c.course_name";
    String term=search==null?"":search.trim(),pattern="%"+term+"%";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
      p.setString(1,term);p.setString(2,pattern);p.setString(3,pattern);p.setString(4,pattern);
      try(ResultSet r=p.executeQuery()){
        List<CourseRecord> rows=new ArrayList<>();while(r.next()){
          int facultyId=r.getInt("faculty_id");Integer id=r.wasNull()?null:facultyId;
          rows.add(new CourseRecord(r.getInt("course_id"),r.getString("course_code"),r.getString("course_name"),r.getString("description"),id,r.getString("faculty_name")));
        }return rows;
      }
    }
  }

// Purpose: Builds the active Faculty choices used by the course form.
  public List<FacultyChoice> findActiveFaculty()throws SQLException,IOException{
    String sql="SELECT f.faculty_id,u.full_name,u.email FROM faculty f JOIN users u ON u.user_id=f.user_id WHERE u.is_active=TRUE ORDER BY u.full_name";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
      List<FacultyChoice> choices=new ArrayList<>();choices.add(new FacultyChoice(0,"-- Unassigned --",""));
      while(r.next())choices.add(new FacultyChoice(r.getInt("faculty_id"),r.getString("full_name"),r.getString("email")));
      return choices;
    }
  }

// Purpose: Adds a course and optional Faculty assignment.
  public void create(String code,String name,String description,Integer facultyId)throws SQLException,IOException{
    String sql="INSERT INTO courses(course_code,course_name,description,faculty_id) VALUES(?,?,?,?)";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
      p.setString(1,code.trim());p.setString(2,name.trim());p.setString(3,blankToNull(description));setFaculty(p,4,facultyId);p.executeUpdate();
    }
  }
// Purpose: Saves changes to a course and its Faculty assignment.
  public void update(CourseRecord course,String code,String name,String description,Integer facultyId)throws SQLException,IOException{
    String sql="UPDATE courses SET course_code=?,course_name=?,description=?,faculty_id=? WHERE course_id=?";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
      p.setString(1,code.trim());p.setString(2,name.trim());p.setString(3,blankToNull(description));setFaculty(p,4,facultyId);p.setInt(5,course.getCourseId());p.executeUpdate();
    }
  }
// Purpose: Deletes a course and its database-linked child records.
  public void delete(int courseId)throws SQLException,IOException{
    String sql="DELETE FROM courses WHERE course_id=?";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
      p.setInt(1,courseId);if(p.executeUpdate()==0)throw new SQLException("Course was not found.");
    }
  }
  private void setFaculty(PreparedStatement p,int index,Integer facultyId)throws SQLException{if(facultyId==null||facultyId==0)p.setNull(index,Types.INTEGER);else p.setInt(index,facultyId);}
  private String blankToNull(String value){return value==null||value.isBlank()?null:value.trim();}
}






