package org.h2.value;

import static org.h2.value.TypeInfo.TYPE_PASSWORD;

import org.h2.engine.CastDataProvider;
import org.h2.engine.Constants;
import org.h2.engine.SysProperties;
import org.h2.message.DbException;
import org.h2.util.Argon2Singleton;
import org.h2.util.StringUtils;

/**
 * Implementation of the PASSWORD data type.
 */
public final class ValuePassword extends ValueStringBase {
  private char[] unhashed;

  private ValuePassword(String value) {
    // Satisfy Java's requirements.  If the value should be hashed, it will
    // be overwritten very shortly.  The hashed value is well below the
    // maximum string length.
    super(value);
    // If we're constructing this with something that is already an argon2
    // hash, don't try to hash it again.
    if (value.startsWith("$argon2")) {
      this.unhashed = null;
    } else {
      // If it isn't an argon2 hash already, hash it, overwrite the stored
      // value, and save the unhashed value in memory so that we can verify
      // passwords later.
      this.unhashed = value.toCharArray();
      this.value = Argon2Singleton.get().hash(this.unhashed);
    }
  }

  @Override
  public int getValueType() {
    return PASSWORD;
  }

  @Override
  public int compareTypeSafe(Value v, CompareMode mode,
      CastDataProvider provider) {
    // If both of these are TYPE_PASSWORD, and one of them has an unhashed
    // value stored still, verify the password.  If not, compare as with CHAR.
    if (v.getType() == TYPE_PASSWORD) {
      ValuePassword vp = (ValuePassword) v;
      if (vp.unhashed != null) {
        if (Argon2Singleton.get().verify(convertToChar().getString(),
            vp.unhashed)) {
          return 0;
        }
      } else if (this.unhashed != null) {
        if (Argon2Singleton.get().verify(v.convertToChar().getString(),
            this.unhashed)) {
          return 0;
        }
      }
    } else {
      // If the other value is not a password, try verifying it.
      if (Argon2Singleton.get().verify(convertToChar().getString(),
          v.convertToChar().getString().toCharArray())) {
        return 0;
      }
    }
    // In all other cases, fall back to string comparison.
    return mode.compareString(value, ((ValuePassword) v).value, false);
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
