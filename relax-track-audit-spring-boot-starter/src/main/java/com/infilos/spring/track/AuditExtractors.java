package com.infilos.spring.track;

import com.fasterxml.jackson.databind.JsonNode;
import com.infilos.relax.Json;
import com.infilos.spring.track.aop.*;
import com.infilos.spring.track.api.Audit;
import com.infilos.spring.track.api.AuditAttri;
import com.infilos.spring.track.config.SpringContextConfigure;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class AuditExtractors {
    private static final Logger log = LoggerFactory.getLogger(AuditTransfers.class);
    private static final List<AuditExtractor<?>> Customizes =
        SpringContextConfigure.injectAll(AuditExtractor.class).stream()
            .map(e -> (AuditExtractor<?>) e)
            .collect(Collectors.toList());
    private static final String Blank = "";
    private static final Map<Audit, AuditExtractor<?>> Predefines =
        new HashMap<Audit, AuditExtractor<?>>() {
            {
                put(Audit.Nowhere, NowhereExtractor());
                put(Audit.Constant, ConstantExtractor());
                put(Audit.ReqPath, ReqPathExtractor());
                put(Audit.ReqQuery, ReqQueryExtractor());
                put(Audit.ReqHeader, ReqHeaderExtractor());
                put(Audit.ReqCookie, ReqCookieExtractor());
                put(Audit.ReqSession, ReqSessionExtractor());
                put(Audit.ReqBody, ReqBodyExtractor());
                put(Audit.ReqMethod, ReqMethodExtractor());
                put(Audit.ReqParam, ReqParamExtractor());
                put(Audit.ResStatus, ResStatusExtractor());
                put(Audit.ResValue, ResValueExtractor());
                put(Audit.ResCause, ResCauseExtractor());
                put(Audit.ResHeader, ResHeaderExtractor());
            }
        };

    private AuditExtractors() {
    }

    @SuppressWarnings("unchecked")
    public static <CTX extends AuditContext> AuditExtractor<CTX> of(Audit audit) {
        return (AuditExtractor<CTX>)
            Customizes.stream()
                .filter(e -> e.from() == audit)
                .findFirst()
                .orElse(Predefines.get(audit));
    }

    static AuditMethodExtractor NowhereExtractor() {
        return new AuditMethodExtractor() {
            @Override
            public Audit from() {
                return Audit.Nowhere;
            }

            @Override
            public AuditAttribute extract(AuditAttri attri, AuditMethodContext context) {
                return AuditAttribute.of(attri, Blank);
            }
        };
    }

    static AuditMethodExtractor ConstantExtractor() {
        return new AuditMethodExtractor() {
            @Override
            public Audit from() {
                return Audit.Constant;
            }

            @Override
            public AuditAttribute extract(AuditAttri attri, AuditMethodContext context) {
                return AuditAttribute.of(attri, attri.value());
            }
        };
    }

    static AuditRestExtractor ReqPathExtractor() {
        return new AuditRestExtractor() {
            @Override
            public Audit from() {
                return Audit.ReqPath;
            }

            @Override
            public AuditAttribute extract(AuditAttri attri, AuditRestContext context) {
                return AuditAttribute.of(attri, definedOrOption(context.reqPath()).orElse(attri.value()));
            }
        };
    }

    static AuditRestExtractor ReqQueryExtractor() {
        return new AuditRestExtractor() {
            @Override
            public Audit from() {
                return Audit.ReqQuery;
            }

            @Override
            public AuditAttribute extract(AuditAttri attri, AuditRestContext context) {
                return AuditAttribute.of(attri, context.reqQuery(attri.name()).orElse(attri.value()));
            }
        };
    }

    static AuditRestExtractor ReqHeaderExtractor() {
        return new AuditRestExtractor() {
            @Override
            public Audit from() {
                return Audit.ReqHeader;
            }

            @Override
            public AuditAttribute extract(AuditAttri attri, AuditRestContext context) {
                return AuditAttribute.of(attri, context.reqHeader(attri.name()).orElse(attri.value()));
            }
        };
    }

    static AuditRestExtractor ReqCookieExtractor() {
        return new AuditRestExtractor() {
            @Override
            public Audit from() {
                return Audit.ReqCookie;
            }

            @Override
            public AuditAttribute extract(AuditAttri attri, AuditRestContext context) {
                return AuditAttribute.of(attri, context.reqCookie(attri.name()).orElse(attri.value()));
            }
        };
    }

    static AuditRestExtractor ReqSessionExtractor() {
        return new AuditRestExtractor() {
            @Override
            public Audit from() {
                return Audit.ReqSession;
            }

            @Override
            public AuditAttribute extract(AuditAttri attri, AuditRestContext context) {
                return AuditAttribute.of(attri, context.reqSession(attri.name()).orElse(attri.value()));
            }
        };
    }

    static AuditRestExtractor ReqBodyExtractor() {
        return new AuditRestExtractor() {
            @Override
            public Audit from() {
                return Audit.ReqBody;
            }

            @Override
            public AuditAttribute extract(AuditAttri attri, AuditRestContext context) {
                Optional<Object> value = Optional.of(attri.value());
                if (isDefined(attri.locate())) {
                    value = evaluate(context.reqBody().orElse("{}"), attri.locate());
                } else if (context.reqBody().isPresent() && isDefined(context.reqBody().get())) {
                    value = Optional.of(context.reqBody().orElse(Blank));
                }
                return AuditAttribute.of(attri, value);
            }
        };
    }

    static AuditMethodExtractor ReqMethodExtractor() {
        return new AuditMethodExtractor() {
            @Override
            public Audit from() {
                return Audit.ReqMethod;
            }

            @Override
            public AuditAttribute extract(AuditAttri attri, AuditMethodContext context) {
                return AuditAttribute.of(attri, context.reqMethod());
            }
        };
    }

    static AuditMethodExtractor ReqParamExtractor() {
        return new AuditMethodExtractor() {
            @Override
            public Audit from() {
                return Audit.ReqParam;
            }

            @Override
            public AuditAttribute extract(AuditAttri attri, AuditMethodContext context) {
                Optional<Object> value = context.reqParam(attri.name());
                if (isDefined(attri.locate())) {
                    value = value.flatMap(v -> evaluate(v, attri.locate()));
                }
                return AuditAttribute.of(attri, value.orElse(attri.value()));
            }
        };
    }

    static AuditRestExtractor ResStatusExtractor() {
        return new AuditRestExtractor() {
            @Override
            public Audit from() {
                return Audit.ResStatus;
            }

            @Override
            public AuditAttribute extract(AuditAttri attri, AuditRestContext context) {
                return AuditAttribute.of(attri, context.resStatus());
            }
        };
    }

    static AuditMethodExtractor ResValueExtractor() {
        return new AuditMethodExtractor() {
            @Override
            public Audit from() {
                return Audit.ResValue;
            }

            @Override
            public AuditAttribute extract(AuditAttri attri, AuditMethodContext context) {
                Optional<Object> value = context.resValue();
                if (isDefined(attri.locate())) {
                    value = value.flatMap(v -> evaluate(v, attri.locate()));
                }
                return AuditAttribute.of(attri, value.orElse(Blank));
            }
        };
    }

    static AuditMethodExtractor ResCauseExtractor() {
        return new AuditMethodExtractor() {
            @Override
            public Audit from() {
                return Audit.ResCause;
            }

            @Override
            public AuditAttribute extract(AuditAttri attri, AuditMethodContext context) {
                return AuditAttribute.of(
                    attri,
                    context
                        .resCause()
                        .map(
                            c ->
                                String.format(
                                    "%s(%s)",
                                    c.getClass().getName(), Optional.ofNullable(c.getMessage()).orElse("")))
                        .orElse(""));
            }
        };
    }

    static AuditRestExtractor ResHeaderExtractor() {
        return new AuditRestExtractor() {
            @Override
            public Audit from() {
                return Audit.ResHeader;
            }

            @Override
            public AuditAttribute extract(AuditAttri attri, AuditRestContext context) {
                return AuditAttribute.of(attri, context.resHeader(attri.name()).orElse(attri.value()));
            }
        };
    }

    private static boolean isDefined(String attribute) {
        return attribute != null && attribute.length() != 0;
    }

    private static Optional<String> definedOrOption(String attribute) {
        return isDefined(attribute) ? Optional.of(attribute) : Optional.empty();
    }

    private static Optional<Object> evaluate(String string, String path) {
        try {
            JsonNode result = JsonPath.read(Json.from(string).asJsonNode(), path);
            return Optional.ofNullable(result)
                .map(res -> res.isTextual() ? Json.escape(res.asText()) : res.toString());
        } catch (Throwable ex) {
            log.error("Audit evaluator eval json-path failed: {}, {}", string, path, ex);
            return Optional.empty();
        }
    }

    private static Optional<Object> evaluate(Object object, String path) {
        try {
            JsonNode result = JsonPath.read(Json.from(object).asJsonNode(), path);
            return Optional.ofNullable(result)
                .map(res -> res.isTextual() ? Json.escape(res.asText()) : res.toString());
        } catch (Throwable ex) {
            log.error("Audit evaluator eval json-path failed: {}, {}", object, path, ex);
            return Optional.empty();
        }
    }
}
