package com.vclass.model;

// Purpose: Immutable data object containing a Student profile and account fields.
public class StudentRecord {
  private final int studentId;
  private final int userId;
  private final String studentNumber;
  private final String fullName;
  private final String email;
  private final String department;
  private final Integer semester;
  private final boolean active;

  public StudentRecord(int studentId,int userId,String studentNumber,String fullName,String email,String department,Integer semester,boolean active) {
    this.studentId=studentId;this.userId=userId;this.studentNumber=studentNumber;this.fullName=fullName;
    this.email=email;this.department=department;this.semester=semester;this.active=active;
  }
  public int getStudentId(){return studentId;}
  public int getUserId(){return userId;}
  public String getStudentNumber(){return studentNumber;}
  public String getFullName(){return fullName;}
  public String getEmail(){return email;}
  public String getDepartment(){return department;}
  public Integer getSemester(){return semester;}
  public boolean isActive(){return active;}
}

