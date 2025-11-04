package io.jenkins.plugins.metrics.model.metric;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A definition of a metric.
 */
public final class MetricDefinition implements Serializable, Comparable<MetricDefinition> {
    @Serial
    private static final long serialVersionUID = 5311316796142816504L;

    /**
     * The scope of a {@link MetricDefinition}. Might be a method, or a class, or both.
     */
    public enum Scope {
        CLASS,
        METHOD
    }

    private final String id;
    private final String displayName;
    private final String description;
    private final String reportedBy;
    private final int priority;
    private final Set<Scope> scopes;

    /**
     * Creates a new {@link MetricDefinition}.
     *
     * @param id
     *         the id of the metric
     * @param displayName
     *         the name to display
     * @param description
     *         the description of the metric
     * @param reportedBy
     *         the id of the tool which reported a metric
     * @param priority
     *         the priority of a metric
     * @param scopes
     *         the scopes of a metric (class, method, or both)
     */
    public MetricDefinition(final String id, final String displayName, final String description,
            final String reportedBy, final int priority, final Scope... scopes) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.reportedBy = reportedBy;
        this.priority = priority;
        this.scopes = Arrays.stream(scopes).collect(Collectors.toSet());
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public int getPriority() {
        return priority;
    }

    public Set<Scope> getScopes() {
        return scopes;
    }

    /**
     * Determine if this {@link MetricDefinition} is valid for a certain {@link Scope}.
     *
     * @param scope
     *         the {@link Scope} to check for
     *
     * @return true, if this {@link MetricDefinition} is valid for the provided {@link Scope}, false otherwise
     */
    public boolean isValidForScope(final Scope scope) {
        return scopes.contains(scope);
    }

    @Override
    public String toString() {
        return id; // needs to be just the ID to be usable for jelly
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof MetricDefinition other)) {
            return false;
        }

        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(final MetricDefinition o) {
        return priority - o.priority;
    }
}
