package com.infilos.spring.track.service;

import com.infilos.spring.track.api.AuditCriteria;
import com.infilos.spring.track.api.AuditRecord;
import com.infilos.spring.track.api.AuditService;
import com.infilos.spring.track.config.AuditRecordOptions;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@ConditionalOnExpression("${track.record.audit.enable-mem:true}")
public class AuditRecordMemService implements AuditService {
    final CircularFifoQueue<AuditRecord> records;

    @Autowired
    public AuditRecordMemService(AuditRecordOptions options) {
        this.records = new CircularFifoQueue<>(options.getMaxMemSize());
    }

    @Override
    public synchronized int collect(List<AuditRecord> records) {
        if (this.records.addAll(records)) {
            return records.size();
        }

        return 0;
    }

    @Override
    public synchronized List<AuditRecord> resolve(AuditCriteria criteria) {
        Stream<AuditRecord> stream = records.stream();

        if (criteria.app().isPresent()) {
            stream =
                stream.filter(r -> r.app().isPresent() && r.app().get().equals(criteria.app().get()));
        }
        if (criteria.org().isPresent()) {
            stream =
                stream.filter(r -> r.org().isPresent() && r.org().get().equals(criteria.org().get()));
        }
        if (criteria.role().isPresent()) {
            stream =
                stream.filter(r -> r.role().isPresent() && r.role().get().equals(criteria.role().get()));
        }
        if (criteria.user().isPresent()) {
            stream = stream.filter(r -> r.user().equals(criteria.user().get()));
        }
        if (criteria.action().isPresent()) {
            stream = stream.filter(r -> r.action().equals(criteria.action().get()));
        }
        if (criteria.succed().isPresent()) {
            stream = stream.filter(r -> r.isSucced().equals(criteria.succed().get()));
        }

        stream =
            stream
                .filter(
                    r ->
                        criteria.tags().keySet().stream()
                            .allMatch(tk -> r.tags().keySet().stream().anyMatch(k -> k.contains(tk))))
                .filter(
                    r ->
                        criteria.tags().values().stream()
                            .allMatch(
                                tv ->
                                    r.tags().values().stream()
                                        .anyMatch(v -> v.toString().contains(tv))));

        if (criteria.after().isPresent()) {
            stream =
                stream.filter(
                    r ->
                        r.time().isAfter(criteria.after().get())
                            || r.time().isEqual(criteria.after().get()));
        }
        if (criteria.before().isPresent()) {
            stream =
                stream.filter(
                    r ->
                        r.time().isBefore(criteria.before().get())
                            || r.time().isEqual(criteria.before().get()));
        }
        if (criteria.orderByTimeAsc()) {
            stream = stream.sorted(Comparator.comparing(AuditRecord::time));
        }
        if (criteria.orderByTimeDesc()) {
            stream = stream.sorted((r1, r2) -> r2.time().compareTo(r1.time()));
        }

        List<AuditRecord> records = stream.collect(Collectors.toList());

        int takeFrom = Math.min(records.size(), criteria.offset());
        int takeUntil = Math.min(records.size(), takeFrom + criteria.size());

        return records.subList(takeFrom, takeUntil);
    }
}
