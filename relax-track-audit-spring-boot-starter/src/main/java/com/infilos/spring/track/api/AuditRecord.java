package com.infilos.spring.track.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.*;

public class AuditRecord {
  private final String app;
  private final Map<String, Object> tags;
  private final LocalDateTime time;
  private final Boolean succed;
  private String org;
  private String role;
  private String user;
  private String action;

  public AuditRecord(
      String app,
      String org,
      String role,
      String user,
      String action,
      Map<String, Object> tags,
      Boolean succed) {
    this.app = app;
    this.org = org;
    this.role = role;
    this.user = user;
    this.action = action;
    this.tags = tags;
    this.time = LocalDateTime.now();
    this.succed = succed;
  }

  public AuditRecord(
      String app,
      String org,
      String role,
      String user,
      String action,
      Map<String, Object> tags,
      LocalDateTime time,
      Boolean succed) {
    this.app = app;
    this.org = org;
    this.role = role;
    this.user = user;
    this.action = action;
    this.tags = tags;
    this.time = time;
    this.succed = succed;
  }

  @JsonProperty
  public Optional<String> app() {
    return Optional.ofNullable(app);
  }

  @JsonProperty
  public Optional<String> org() {
    return Optional.ofNullable(org);
  }

  @JsonProperty
  public Optional<String> role() {
    return Optional.ofNullable(role);
  }

  @JsonProperty
  public String user() {
    return user;
  }

  @JsonProperty
  public String action() {
    return action;
  }

  @JsonProperty
  public Map<String, Object> tags() {
    return Collections.unmodifiableMap(tags);
  }

  @JsonProperty
  public LocalDateTime time() {
    return time;
  }

  @JsonProperty
  public Boolean isSucced() {
    return succed;
  }

  @JsonIgnore
  public Boolean isValid() {
    return Objects.nonNull(user)
        && !user.trim().isEmpty()
        && Objects.nonNull(action)
        && !action.trim().isEmpty();
  }

  @JsonIgnore
  public AuditRecord transOrg(String value) {
    this.org = value;
    return this;
  }

  @JsonIgnore
  public AuditRecord transRole(String value) {
    this.role = value;
    return this;
  }

  @JsonIgnore
  public AuditRecord transUser(String value) {
    this.user = value;
    return this;
  }

  @JsonIgnore
  public AuditRecord transAction(String value) {
    this.action = value;
    return this;
  }

  @JsonIgnore
  public AuditRecord transTag(String tag, String value) {
    this.tags.put(tag, value);
    return this;
  }

  @JsonIgnore
  public AuditRecord transTags(Map<String, String> tags) {
    this.tags.putAll(tags);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(this.getClass().getSimpleName());
    builder.append("(");
    builder.append("app=").append(app).append(",");
    builder.append("org=").append(org).append(",");
    builder.append("role=").append(role).append(",");
    builder.append("user=").append(user).append(",");
    builder.append("action=").append(action).append(",");
    builder.append("tags={");
    for (Map.Entry<String, Object> tag : tags.entrySet()) {
      builder.append(tag.getKey());
      builder.append("=");
      builder.append(tag.getValue().toString());
      builder.append(",");
    }
    if (!tags.isEmpty()) {
      builder.delete(builder.length() - 1, builder.length());
    }
    builder.append("},");
    builder.append("time=").append(time).append(",");
    builder.append("succed=").append(succed);
    builder.append(")");

    return builder.toString();
  }
}
