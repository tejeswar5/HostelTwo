package com.pgm.renter.audit;

import com.pgm.renter.entity.AuditLog;
import com.pgm.renter.repository.AuditLogRepository;
import com.pgm.renter.security.JwtAuthenticationToken;
import com.pgm.renter.security.UserPrincipal;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result = joinPoint.proceed();

        auditLogRepository.save(AuditLog.builder()
                .actorId(currentActorId())
                .action(auditable.action())
                .entityType(auditable.entityType())
                .entityId(extractId(result).orElse(null))
                .details(summarize(joinPoint.getArgs()))
                .build());

        return result;
    }

    private Long currentActorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwt && jwt.getPrincipal() instanceof UserPrincipal principal) {
            return principal.userId();
        }
        return null;
    }

    /**
     * Every audited method returns either the mutated entity's id directly (a plain
     * {@code Long}) or a response DTO whose id we read reflectively. DTOs are Java
     * records, which expose an id-shaped accessor named after the component itself
     * ({@code id()}), not the JavaBean {@code getId()} - so both are tried.
     * Deliberately does not fall back to scanning method arguments for a
     * {@code Long}: a mutation like {@code raise(Long renterId, RaiseComplaintRequest)}
     * has a Long argument that is not the entity being audited, and guessing from it
     * silently logs the wrong id.
     */
    private Optional<Long> extractId(Object candidate) {
        if (candidate == null) {
            return Optional.empty();
        }
        if (candidate instanceof Long l) {
            return Optional.of(l);
        }
        for (String accessor : new String[] {"id", "getId"}) {
            try {
                Method method = candidate.getClass().getMethod(accessor);
                Object id = method.invoke(candidate);
                if (id instanceof Long l) {
                    return Optional.of(l);
                }
            } catch (ReflectiveOperationException ignored) {
                // try the next accessor style
            }
        }
        return Optional.empty();
    }

    private String summarize(Object[] args) {
        String joined = Arrays.stream(args).map(String::valueOf).collect(java.util.stream.Collectors.joining(", "));
        return joined.length() > 2000 ? joined.substring(0, 2000) : joined;
    }
}
