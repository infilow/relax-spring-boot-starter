package com.infilos.spring.track.api;

import java.time.LocalDateTime;
import java.util.*;

public class AuditCriteria {
    private String app;
    private String org;
    private String role;
    private String user;
    private String action;
    private Boolean succed;
    private Map<String, String> tags;
    private LocalDateTime after;
    private LocalDateTime before;
    private Integer size = 20;
    private Integer offset = 0;
    private Boolean orderByDesc = true;

    private AuditCriteria() {
    }

    public static AuditCriteriaBuilder builder() {
        return new AuditCriteriaBuilder();
    }

    public Optional<String> app() {
        return Optional.ofNullable(app);
    }

    public Optional<String> org() {
        return Optional.ofNullable(org);
    }

    public Optional<String> role() {
        return Optional.ofNullable(role);
    }

    public Optional<String> user() {
        return Optional.ofNullable(user);
    }

    public Optional<String> action() {
        return Optional.ofNullable(action);
    }

    public Optional<Boolean> succed() {
        return Optional.ofNullable(succed);
    }

    public Map<String, String> tags() {
        return Collections.unmodifiableMap(tags == null ? new HashMap<>() : tags);
    }

    public Optional<LocalDateTime> after() {
        return Optional.ofNullable(after);
    }

    public Optional<LocalDateTime> before() {
        return Optional.ofNullable(before);
    }

    public Integer size() {
        return size;
    }

    public Integer offset() {
        return offset;
    }

    public Boolean orderByTimeAsc() {
        return !orderByDesc;
    }

    public Boolean orderByTimeDesc() {
        return orderByDesc;
    }

    public static final class AuditCriteriaBuilder {
        private String app;
        private String org;
        private String role;
        private String user;
        private String action;
        private Boolean succed;
        private Map<String, String> tags;
        private LocalDateTime after;
        private LocalDateTime before;
        private Integer size = 20;
        private Integer offset = 0;
        private Boolean desc = false;

        private AuditCriteriaBuilder() {
        }

        public AuditCriteriaBuilder app(String app) {
            this.app = app;
            return this;
        }

        public AuditCriteriaBuilder org(String org) {
            this.org = org;
            return this;
        }

        public AuditCriteriaBuilder role(String role) {
            this.role = role;
            return this;
        }

        public AuditCriteriaBuilder user(String user) {
            this.user = user;
            return this;
        }

        public AuditCriteriaBuilder action(String action) {
            this.action = action;
            return this;
        }

        public AuditCriteriaBuilder succed(Boolean succed) {
            this.succed = succed;
            return this;
        }

        public AuditCriteriaBuilder tag(String key, String value) {
            if (tags == null) {
                tags = new HashMap<>();
            }

            this.tags.put(key, value);
            return this;
        }

        public AuditCriteriaBuilder after(LocalDateTime after) {
            if (Objects.nonNull(before) && after.isAfter(before)) {
                throw new IllegalArgumentException();
            }
            this.after = after;
            return this;
        }

        public AuditCriteriaBuilder before(LocalDateTime before) {
            if (Objects.nonNull(after) && after.isBefore(after)) {
                throw new IllegalArgumentException();
            }
            this.before = before;
            return this;
        }

        public AuditCriteriaBuilder between(LocalDateTime from, LocalDateTime to) {
            if (from.isAfter(to)) {
                throw new IllegalArgumentException();
            }
            this.after = from;
            this.before = to;
            return this;
        }

        public AuditCriteriaBuilder take(Integer size) {
            this.size = size;
            return this;
        }

        public AuditCriteriaBuilder offset(Integer offset) {
            this.offset = offset;
            return this;
        }

        public AuditCriteriaBuilder orderByTimeAsc() {
            this.desc = false;
            return this;
        }

        public AuditCriteriaBuilder orderByTimeDesc() {
            this.desc = true;
            return this;
        }

        public AuditCriteria build() {
            AuditCriteria criteria = new AuditCriteria();
            criteria.app = this.app;
            criteria.org = this.org;
            criteria.role = this.role;
            criteria.user = this.user;
            criteria.action = this.action;
            criteria.tags = this.tags;
            criteria.succed = this.succed;
            criteria.after = this.after;
            criteria.before = this.before;
            criteria.size = this.size;
            criteria.offset = this.offset;
            criteria.orderByDesc = this.desc;
            return criteria;
        }
    }
}
