package org.h2.value;

import org.h2.engine.CastDataProvider;
import org.h2.engine.SysProperties;
import org.h2.util.StringUtils;

/**
 * Implementation of the PASSWORD data type.
 */
public final class ValuePassword extends ValueStringBase {
  private ValuePassword(String value) {
    // TODO: this should probably hash the value.
    super(value);
  }

  @Override
  public int getValueType() {
    return PASSWORD;
  }

  @Override
  public int compareTypeSafe(Value v, CompareMode mode,
      CastDataProvider provider) {
    // TODO: this needs more logic to do equality comparisons as hash checks
    //  and ordering comparisions the default way.
    return mode.compareString(convertToChar().getString(),
        v.convertToChar().getString(), false);
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
    // TODO: figure out what StringUtils.cache(String) does and determine if
    //  that should be called on the password hash or the raw password.
    ValuePassword obj = new ValuePassword(StringUtils.cache(s));
    if (s.length() > SysProperties.OBJECT_CACHE_MAX_PER_ELEMENT_SIZE) {
      return obj;
    }
    return (ValuePassword) Value.cache(obj);
  }
}
