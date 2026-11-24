package com.vclass.model;
// Purpose: Data object used as one Course option in an assignment form.
public class AssignmentCourseChoice {
  private final int courseId;private final String courseCode,courseName;
  public AssignmentCourseChoice(int courseId,String courseCode,String courseName){this.courseId=courseId;this.courseCode=courseCode;this.courseName=courseName;}
  public int getCourseId(){return courseId;}
  @Override public String toString(){return courseCode+" — "+courseName;}
}

