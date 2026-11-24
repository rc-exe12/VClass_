package com.vclass.model;

// Purpose: Immutable data object containing a Faculty profile and account fields.
public class FacultyRecord {
  private final int facultyId;
  private final int userId;
  private final String employeeNumber;
  private final String fullName;
  private final String email;
  private final String department;
  private final boolean active;

  public FacultyRecord(int facultyId,int userId,String employeeNumber,String fullName,String email,String department,boolean active) {
    this.facultyId=facultyId;this.userId=userId;this.employeeNumber=employeeNumber;this.fullName=fullName;
    this.email=email;this.department=department;this.active=active;
  }
  public int getFacultyId(){return facultyId;}
  public int getUserId(){return userId;}
  public String getEmployeeNumber(){return employeeNumber;}
  public String getFullName(){return fullName;}
  public String getEmail(){return email;}
  public String getDepartment(){return department;}
  public boolean isActive(){return active;}
}

