package com.vclass.model;
// Purpose: Immutable data object for the account returned by login.
public class User {
  private final int id;
  private final String fullName,email,role;
  public User(int id,String fullName,String email,String role) { this.id=id; this.fullName=fullName; this.email=email; this.role=role; }
  public int getId(){return id;} public String getFullName(){return fullName;} public String getEmail(){return email;} public String getRole(){return role;}
}

