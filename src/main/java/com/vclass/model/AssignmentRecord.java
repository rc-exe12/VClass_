package com.vclass.model;
import java.math.BigDecimal;
import java.sql.Timestamp;
// Purpose: Immutable data object for assignment details and optional submission details.
public class AssignmentRecord {
  private final int assignmentId,courseId;
  private final String courseCode,courseName,title,description,status,filePath;
  private final Timestamp dueAt;
  private final BigDecimal maxMarks;
  public AssignmentRecord(int assignmentId,int courseId,String courseCode,String courseName,String title,String description,Timestamp dueAt,BigDecimal maxMarks,String status,String filePath){
    this.assignmentId=assignmentId;this.courseId=courseId;this.courseCode=courseCode;this.courseName=courseName;this.title=title;this.description=description;this.dueAt=dueAt;this.maxMarks=maxMarks;this.status=status;this.filePath=filePath;
  }
  public int getAssignmentId(){return assignmentId;} public int getCourseId(){return courseId;} public String getCourseCode(){return courseCode;} public String getCourseName(){return courseName;}
  public String getTitle(){return title;} public String getDescription(){return description;} public Timestamp getDueAt(){return dueAt;} public BigDecimal getMaxMarks(){return maxMarks;} public String getStatus(){return status;} public String getFilePath(){return filePath;}
}

