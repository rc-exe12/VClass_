package com.vclass.service;

import com.vclass.db.DBConnection;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;

// Purpose: Saves a students uploaded file and submission details to MySQL.
public class SubmissionRepository {
// Purpose: Copies a file, then inserts or replaces the students submission row.
  public void submit(int userId,int assignmentId,File selectedFile,String comments)throws SQLException,IOException{
    String storedPath=SubmissionFileStorage.store(selectedFile),oldPath=null;boolean committed=false;
    try(Connection c=DBConnection.getConnection()){
      c.setAutoCommit(false);
      try{
        int studentId;
        try(PreparedStatement p=c.prepareStatement("SELECT student_id FROM students WHERE user_id=?")){
          p.setInt(1,userId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("This login does not have a student profile.");studentId=r.getInt(1);}
        }
        Timestamp dueAt;
        try(PreparedStatement p=c.prepareStatement("SELECT due_at FROM assignments WHERE assignment_id=?")){
          p.setInt(1,assignmentId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Assignment was not found.");dueAt=r.getTimestamp(1);}
        }
        try(PreparedStatement p=c.prepareStatement("SELECT file_path FROM submissions WHERE assignment_id=? AND student_id=?")){
          p.setInt(1,assignmentId);p.setInt(2,studentId);try(ResultSet r=p.executeQuery()){if(r.next())oldPath=r.getString(1);}
        }
        String state=dueAt!=null&&LocalDateTime.now().isAfter(dueAt.toLocalDateTime())?"LATE":"SUBMITTED";
        String sql="INSERT INTO submissions(assignment_id,student_id,file_path,comments,status,submitted_at) VALUES(?,?,?,?,?,CURRENT_TIMESTAMP) "
          +"ON DUPLICATE KEY UPDATE file_path=VALUES(file_path),comments=VALUES(comments),status=VALUES(status),submitted_at=CURRENT_TIMESTAMP,marks_awarded=NULL,feedback=NULL";
        try(PreparedStatement p=c.prepareStatement(sql)){
          p.setInt(1,assignmentId);p.setInt(2,studentId);p.setString(3,storedPath);p.setString(4,comments==null||comments.isBlank()?null:comments.trim());p.setString(5,state);p.executeUpdate();
        }
        c.commit();committed=true;
      }catch(SQLException|RuntimeException e){c.rollback();throw e;}
    }finally{
      if(committed){if(oldPath!=null&&!oldPath.equals(storedPath))SubmissionFileStorage.delete(oldPath);}
      else SubmissionFileStorage.delete(storedPath);
    }
  }
}


