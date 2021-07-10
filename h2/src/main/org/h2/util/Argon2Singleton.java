package org.h2.util;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Helper;

public class Argon2Singleton {

  private static Argon2Singleton singleton;
  private static final int MAX_MILLIS = 1000;
  private static final int MAX_MEM_KB = 65536;
  private static final int MAX_PARALLELISM = 4;

  private final Argon2 a2;
  private final int iteration_count;

  private Argon2Singleton() {
    this.a2 = Argon2Factory.create();
    this.iteration_count = Argon2Helper.findIterations(this.a2, MAX_MILLIS,
        MAX_MEM_KB, MAX_PARALLELISM);
  }

  public static Argon2Singleton get() {
    if (singleton == null) {
      singleton = new Argon2Singleton();
    }
    return singleton;
  }

  public String hash(char[] password) {
    return this.a2.hash(this.iteration_count, MAX_MEM_KB, MAX_PARALLELISM,
        password);
  }

  public boolean verify(String hash, char[] password) {
    return this.a2.verify(hash, password);
  }
}
