package com.infilos.spring.track.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infilos.spring.track.api.AuditCriteria;
import com.infilos.spring.track.api.AuditRecord;
import com.infilos.spring.track.api.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

@Service
@ConditionalOnExpression("${track.record.audit.enable-jdbc:true}")
public class AuditRecordJdbcService implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditRecordJdbcService.class);
    private static final DateTimeFormatter Formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RowMapper<AuditRecord> recordMapper = new AuditRecordMapper(objectMapper);
    @Autowired(required = false)
    private Optional<JdbcTemplate> jdbc;

    @Override
    public int collect(List<AuditRecord> records) {
        if (!jdbc.isPresent() || Objects.isNull(records) || records.isEmpty()) {
            return 0;
        }

        String sql =
            "INSERT INTO auditing (app, org, role, user, action, tags, time, succed) VALUES (?,?,?,?,?,?,?,?)";

        int[] inserted =
            jdbc.get()
                .batchUpdate(
                    sql,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement prepare, int i) throws SQLException {
                            AuditRecord record = records.get(i);
                            prepare.setString(1, record.app().orElse(null));
                            prepare.setString(2, record.org().orElse(null));
                            prepare.setString(3, record.role().orElse(null));
                            prepare.setString(4, record.user());
                            prepare.setString(5, record.action());
                            try {
                                prepare.setString(6, objectMapper.writeValueAsString(record.tags()));
                            } catch (JsonProcessingException ex) {
                                prepare.setString(
                                    6,
                                    String.format(
                                        "{\"tags-ser-error\":\"%s-%s\"}",
                                        ex.getClass().getName(), ex.getMessage()));
                            }
                            prepare.setTimestamp(7, Timestamp.valueOf(record.time()));
                            prepare.setBoolean(8, record.isSucced());
                        }

                        @Override
                        public int getBatchSize() {
                            return records.size();
                        }
                    });

        return IntStream.of(inserted).sum();
    }

    @Override
    public List<AuditRecord> resolve(AuditCriteria criteria) {
        if (!jdbc.isPresent()) {
            return Collections.emptyList();
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM auditing ");
        StringJoiner where = new StringJoiner(" AND ", " WHERE ", "").setEmptyValue("");

        if (criteria.app().isPresent()) {
            where.add(String.format("app = '%s'", criteria.app().get()));
        }
        if (criteria.org().isPresent()) {
            where.add(String.format("org = '%s'", criteria.org().get()));
        }
        if (criteria.role().isPresent()) {
            where.add(String.format("role = '%s'", criteria.role().get()));
        }
        if (criteria.user().isPresent()) {
            where.add(String.format("user = '%s'", criteria.user().get()));
        }
        if (criteria.action().isPresent()) {
            where.add(String.format("action = '%s'", criteria.action().get()));
        }
        if (criteria.succed().isPresent()) {
            where.add(String.format("succed = %s", criteria.succed().get()));
        }
        for (Map.Entry<String, String> tag : criteria.tags().entrySet()) {
            where.add("tags like '%" + tag.getKey() + "%'");
            where.add("tags like '%" + tag.getValue() + "%'");
        }
        if (criteria.after().isPresent()) {
            where.add(String.format("time >= '%s'", Formatter.format(criteria.after().get())));
        }
        if (criteria.before().isPresent()) {
            where.add(String.format("time <= '%s'", Formatter.format(criteria.before().get())));
        }

        sql.append(where);
        sql.append(" ");

        if (criteria.orderByTimeAsc()) {
            sql.append("ORDER BY time ");
        }
        if (criteria.orderByTimeDesc()) {
            sql.append("ORDER BY time DESC ");
        }

        sql.append("LIMIT ");
        sql.append(criteria.size());
        sql.append(" ");
        sql.append("OFFSET ");
        sql.append(criteria.offset());

        String statement = sql.toString();
        log.debug("Resolve audit records: " + statement);
        System.out.println(statement);

        return jdbc.get().query(statement, recordMapper);
    }

    private static final class AuditRecordMapper implements RowMapper<AuditRecord> {

        private final ObjectMapper objectMapper;

        AuditRecordMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public AuditRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> tags = new HashMap<>();
            try {
                tags =
                    objectMapper.convertValue(
                        objectMapper.readTree(rs.getString("tags")),
                        new TypeReference<Map<String, Object>>() {
                        });
            } catch (Throwable e) {
                tags.put("tags-des-error", String.format("%s-%s", e.getClass().getName(), e.getMessage()));
            }
            LocalDateTime time = rs.getTimestamp("time").toLocalDateTime();

            return new AuditRecord(
                rs.getString("app"),
                rs.getString("org"),
                rs.getString("role"),
                rs.getString("user"),
                rs.getString("action"),
                tags,
                time,
                rs.getBoolean("succed"));
        }
    }
}
