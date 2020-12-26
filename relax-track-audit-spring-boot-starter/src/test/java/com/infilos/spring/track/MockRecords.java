package com.infilos.spring.track;

import com.infilos.spring.track.api.AuditRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MockRecords {
  private static final DateTimeFormatter Formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final Map<String, Object> Tags1 =
      new HashMap<String, Object>() {
        {
          put("tag-1-a", "value-1-a");
          put("tag-1-b", "value-1-b");
        }
      };
  private static final Map<String, Object> Tags2 =
      new HashMap<String, Object>() {
        {
          put("tag-2-a", "value-2-a");
          put("tag-2-b", "value-2-b");
        }
      };
  private static final Map<String, Object> Tags3 =
      new HashMap<String, Object>() {
        {
          put("tag-3-a", "value-3-a");
          put("tag-3-b", "value-3-b");
        }
      };
  public static LocalDateTime HeadDay = LocalDateTime.parse("2020-04-19 08:00:00", Formatter);
  public static LocalDateTime MiddleDay = LocalDateTime.parse("2020-04-20 08:00:00", Formatter);
  public static LocalDateTime LastDay = LocalDateTime.parse("2020-04-21 08:00:00", Formatter);
  // String app, String org, String role, String user, String act, Map<String, Object>
  // tags,LocalDateTime time, Boolean succed
  public static final List<AuditRecord> Mock =
      Arrays.asList(
          new AuditRecord("app-1", "org-1", "role-1", "user-1", "act-1", Tags1, HeadDay, true),
          new AuditRecord("app-1", "org-1", "role-1", "user-1", "act-1", Tags1, MiddleDay, true),
          new AuditRecord("app-1", "org-1", "role-1", "user-1", "act-1", Tags1, LastDay, false),
          new AuditRecord("app-2", "org-2", "role-2", "user-2", "act-2", Tags2, HeadDay, true),
          new AuditRecord("app-2", "org-2", "role-2", "user-2", "act-2", Tags2, MiddleDay, true),
          new AuditRecord("app-2", "org-2", "role-2", "user-2", "act-2", Tags2, LastDay, false),
          new AuditRecord("app-3", "org-3", "role-3", "user-3", "act-3", Tags3, HeadDay, true),
          new AuditRecord("app-3", "org-3", "role-3", "user-3", "act-3", Tags3, MiddleDay, true),
          new AuditRecord("app-3", "org-3", "role-3", "user-3", "act-3", Tags3, LastDay, false));

  private MockRecords() {}
}
