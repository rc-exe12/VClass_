package com.vclass.model;

// Purpose: Data object used as one option in a course Faculty dropdown.
public class FacultyChoice {
  private final int facultyId;
  private final String fullName;
  private final String email;
  public FacultyChoice(int facultyId,String fullName,String email){this.facultyId=facultyId;this.fullName=fullName;this.email=email;}
  public int getFacultyId(){return facultyId;}
  @Override public String toString(){return facultyId==0?fullName:fullName+" ("+email+")";}
}

