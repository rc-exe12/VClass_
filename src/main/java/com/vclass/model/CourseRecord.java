package com.vclass.model;

// Purpose: Immutable data object for a Course row and assigned Faculty.
public class CourseRecord {
  private final int courseId;
  private final String courseCode;
  private final String courseName;
  private final String description;
  private final Integer facultyId;
  private final String facultyName;
  public CourseRecord(int courseId,String courseCode,String courseName,String description,Integer facultyId,String facultyName){
    this.courseId=courseId;this.courseCode=courseCode;this.courseName=courseName;this.description=description;this.facultyId=facultyId;this.facultyName=facultyName;
  }
  public int getCourseId(){return courseId;} public String getCourseCode(){return courseCode;} public String getCourseName(){return courseName;}
  public String getDescription(){return description;} public Integer getFacultyId(){return facultyId;} public String getFacultyName(){return facultyName;}
}

