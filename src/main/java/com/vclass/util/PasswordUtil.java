package com.vclass.util;
import java.nio.charset.StandardCharsets;
import java.security.*;
// Purpose: Utility for converting a password into the hash stored in MySQL.
public final class PasswordUtil {
  private PasswordUtil() { }
// Purpose: Computes the SHA-256 hash and formats it as hexadecimal text.
  public static String sha256(char[] password) {
    try {
      byte[] hash=MessageDigest.getInstance("SHA-256").digest(new String(password).getBytes(StandardCharsets.UTF_8));
      StringBuilder out=new StringBuilder(hash.length*2);
      for(byte b:hash) out.append(String.format("%02x",b & 0xff));
      return out.toString();
    } catch(NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 unavailable.",e); }
  }
}


