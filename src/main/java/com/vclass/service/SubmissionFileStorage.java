package com.vclass.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

// Purpose: Copies selected assignment files into the project uploads folder.
public final class SubmissionFileStorage {
  public static final long MAX_FILE_SIZE=25L*1024L*1024L;
  private static final String DIRECTORY="uploads"+File.separator+"submissions";
  private SubmissionFileStorage(){ }

// Purpose: Validates and copies a chosen file, then returns its relative saved path.
  public static String store(File source)throws IOException{
    if(source==null||!source.exists()||!source.isFile())throw new IOException("Choose a file that exists.");
    if(source.length()==0)throw new IOException("The selected file is empty.");
    if(source.length()>MAX_FILE_SIZE)throw new IOException("The selected file is larger than 25 MB.");
    File directory=new File(System.getProperty("user.dir"),DIRECTORY);
    if(!directory.exists()&&!directory.mkdirs())throw new IOException("Could not create the uploads/submissions folder.");
    String original=source.getName().replaceAll("[^A-Za-z0-9._-]","_");
    if(original.length()>160)original=original.substring(original.length()-160);
    String storedName=UUID.randomUUID()+"_"+original;
    File destination=new File(directory,storedName);
    byte[] buffer=new byte[8192];
    try(FileInputStream input=new FileInputStream(source);FileOutputStream output=new FileOutputStream(destination)){
      int count;while((count=input.read(buffer))!=-1)output.write(buffer,0,count);
    }catch(IOException e){destination.delete();throw e;}
    return "uploads/submissions/"+storedName;
  }

// Purpose: Finds a saved upload on this computer from its stored relative path.
  public static File resolve(String relativePath){return new File(System.getProperty("user.dir"),relativePath);}
// Purpose: Removes a saved upload when it is replaced or no longer needed.
  public static void delete(String relativePath){if(relativePath!=null&&!relativePath.isBlank())resolve(relativePath).delete();}
}




