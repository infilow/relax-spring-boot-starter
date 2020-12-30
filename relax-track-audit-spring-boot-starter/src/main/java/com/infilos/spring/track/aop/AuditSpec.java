package com.infilos.spring.track.aop;

import com.infilos.spring.track.api.Audit;
import com.infilos.spring.track.api.AuditAttri;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AuditSpec {
    private AuditSpec() {
    }

    /**
     * Should add more composite check conditions.
     */
    public static boolean isValid(AuditAttri attri) {
        return attri.from() != Audit.Nowhere;
    }

    public static boolean isEquals(AuditAttri left, AuditAttri right) {
        return left.from() == right.from()
            && left.name().equals(right.name())
            && left.locate().equals(right.locate())
            && left.value().equals(right.value())
            && left.alias().equals(right.alias());
    }

    public static AuditAttri create(Audit from, Map<String, String> attributes) {
        return new AuditAttri() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return AuditAttri.class;
            }

            @Override
            public Audit from() {
                return from;
            }

            @Override
            public String name() {
                return Optional.ofNullable(attributes.get("name")).orElse("");
            }

            @Override
            public String locate() {
                return Optional.ofNullable(attributes.get("locate")).orElse("");
            }

            @Override
            public String value() {
                return Optional.ofNullable(attributes.get("value")).orElse("");
            }

            @Override
            public String alias() {
                return Optional.ofNullable(attributes.get("alias")).orElse("");
            }
        };
    }

    public static AuditAttri patch(AuditAttri origin, Map<String, String> updates) {
        return create(
            origin.from(),
            new HashMap<String, String>() {
                {
                    put("name", origin.name());
                    put("locate", origin.locate());
                    put("value", origin.value());
                    put("alias", origin.alias());
                    putAll(updates);
                }
            });
    }
}
