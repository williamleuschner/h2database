package org.h2.value;

import org.h2.engine.CastDataProvider;
import org.h2.engine.SysProperties;
import org.h2.util.Argon2Singleton;
import org.h2.util.StringUtils;

/**
 * Implementation of the PASSWORD data type.
 */
public final class ValuePassword extends ValueStringBase {
  private ValuePassword(String value) {
    super(Argon2Singleton.get().hash(value.toCharArray()));
  }

  @Override
  public int getValueType() {
    return PASSWORD;
  }

  @Override
  public int compareTypeSafe(Value v, CompareMode mode,
      CastDataProvider provider) {
    char[] other_pass = v.convertToChar().getString().toCharArray();
    if (Argon2Singleton.get().verify(convertToChar().getString(), other_pass)) {
      return 0;
    } else {
      // This is almost certainly the wrong behavior, because it forces
      // mismatched passwords to be less than other passwords.  But password
      // hashes aren't really orderable?  Particularly because making a new
      // password hash also creates a new salt, so repeated invocations with
      // the same string will have different orderings.
      return -1;
    }
  }

  @Override
  public StringBuilder getSQL(StringBuilder builder, int sqlFlags) {
    // TODO: Nate: change this to use whatever type name you end up picking
    //  for the password type. "PASSWORD" may already be reserved for something.
    if ((sqlFlags & NO_CASTS) == 0) {
      int length = value.length();
      return StringUtils.quoteStringSQL(builder.append("CAST("), value).append(" AS CHAR(").append(length > 0 ? length : 1).append("))");
    }
    return StringUtils.quoteStringSQL(builder, value);
  }

  /**
   * Get or create a PASSWORD value for the given string.
   *
   * @param s the string
   * @return the password version of the string.
   */
  public static ValuePassword get(String s) {
    String hashed_password = Argon2Singleton.get().hash(s.toCharArray());
    ValuePassword obj = new ValuePassword(hashed_password);
    if (hashed_password.length() > SysProperties.OBJECT_CACHE_MAX_PER_ELEMENT_SIZE) {
      return obj;
    }
    // Value.cache caches based on the Value object's hash code, which is
    // computed by xoring the value type's hash code with the contained
    // value's hash code.  This is fine (if unnecessary) for passwords,
    // because the salt values will make them unique.
    return (ValuePassword) Value.cache(obj);
  }
}
