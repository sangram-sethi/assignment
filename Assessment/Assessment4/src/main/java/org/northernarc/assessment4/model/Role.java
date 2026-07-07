package org.northernarc.assessment4.model;

/**
 * Authorization roles a {@link Customer} can hold. Persisted as a string so the
 * database stays human-readable and resilient to enum ordinal reordering.
 * <p>
 * Self-registration always yields {@link #USER}; elevated roles ({@link #MANAGER},
 * {@link #ADMIN}) are provisioned out-of-band by trusted operators.
 */
public enum Role {
    USER,
    MANAGER,
    ADMIN
}
