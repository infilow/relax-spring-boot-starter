package com.infilos.spring.track.aop;

import com.infilos.spring.track.api.AuditAction;
import com.infilos.spring.track.api.AuditAttri;
import com.infilos.spring.track.api.AuditOption;
import com.infilos.spring.track.api.AuditTransfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class AuditOptions {
    private final AuditAction action;
    private final AuditOption option; // nullable

    public AuditOptions(AuditAction action, AuditOption option) {
        this.action = action;
        this.option = option;
    }

    public Optional<AuditAttri> org() {
        return actionOrOption(AuditAction::org, AuditOption::org);
    }

    public Optional<AuditAttri> role() {
        return actionOrOption(AuditAction::role, AuditOption::role);
    }

    public Optional<AuditAttri> user() {
        return actionOrOption(AuditAction::user, AuditOption::user);
    }

    public Optional<AuditAttri> action() {
        return actionOrOption(AuditAction::action, AuditOption::action);
    }

    /**
     * Extract attri from action, or option(if option defined).
     */
    private Optional<AuditAttri> actionOrOption(
        Function<AuditAction, AuditAttri> actionExtractor,
        Function<AuditOption, AuditAttri> optionExtractor) {
        AuditAttri attriOfAction = actionExtractor.apply(action);
        if (AuditSpec.isValid(attriOfAction)) {
            return Optional.of(attriOfAction);
        } else if (option != null) {
            AuditAttri attriOfOption = optionExtractor.apply(option);
            if (AuditSpec.isValid(attriOfOption)) {
                return Optional.of(attriOfOption);
            }
        }

        return Optional.empty();
    }

    public List<AuditAttri> tags() {
        List<AuditAttri> tags = new ArrayList<>();
        Arrays.stream(action.tags())
            .forEach(
                tag -> {
                    if (AuditSpec.isValid(tag)
                        && tags.stream().noneMatch(exist -> AuditSpec.isEquals(exist, tag))) {
                        tags.add(tag);
                    }
                });
        if (option != null) {
            Arrays.stream(option.tags())
                .forEach(
                    tag -> {
                        if (AuditSpec.isValid(tag)
                            && tags.stream().noneMatch(exist -> AuditSpec.isEquals(exist, tag))) {
                            tags.add(tag);
                        }
                    });
        }

        return tags;
    }

    public boolean loggingEnabled() {
        return action.log() || (option != null && option.log());
    }

    public Optional<Class<? extends AuditTransfer>> transfer() {
        if (action.trans() != AuditTransfer.NoopTransfer.class) {
            return Optional.of(action.trans());
        } else if (option != null && option.trans() != AuditTransfer.NoopTransfer.class) {
            return Optional.of(option.trans());
        }

        return Optional.empty();
    }
}
