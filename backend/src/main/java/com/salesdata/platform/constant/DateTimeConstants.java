package com.salesdata.platform.constant;

import java.time.format.DateTimeFormatter;

public final class DateTimeConstants {

  private DateTimeConstants() {}

  public static final String STANDARD_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

  // Pre-built formatters (thread-safe)
  public static final DateTimeFormatter STANDARD_DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern(STANDARD_DATETIME_PATTERN);
}
