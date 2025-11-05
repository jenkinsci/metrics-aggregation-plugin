package io.jenkins.plugins.metrics.model;

import org.apache.commons.lang3.StringUtils;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
     * Creates a new {@link MetricDefinition}. See {@link MetricDefinitionBuilder} for building instances.
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
    private MetricDefinition(final String id, final String displayName, final String description,
            final String reportedBy, final int priority, final Set<Scope> scopes) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.reportedBy = reportedBy;
        this.priority = priority;
        this.scopes = Set.copyOf(scopes);
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

    /**
     * Builder for {@link MetricDefinition} instances.
     */
    @SuppressWarnings({"checkstyle:HiddenField", "ParameterHidesMemberVariable"})
    public static class MetricDefinitionBuilder {
        private final String id;
        private final Set<Scope> scopes = new HashSet<>();

        private String displayName = StringUtils.EMPTY;
        private String description = StringUtils.EMPTY;
        private String reportedBy = StringUtils.EMPTY;
        private int priority = 0;

        /**
         * Creates a new {@link MetricDefinitionBuilder} with the given id.
         *
         * @param id
         *         the id of the metric definition
         */
        public MetricDefinitionBuilder(final String id) {
            this.id = id;
        }

        /**
         * Sets the display name of the metric definition.
         *
         * @param displayName
         *         the display name
         *
         * @return the builder instance
         */
        @CanIgnoreReturnValue
        public MetricDefinitionBuilder withDisplayName(final String displayName) {
            this.displayName = displayName;

            return this;
        }

        /**
         * Sets the description of the metric definition.
         *
         * @param description
         *         the description
         *
         * @return the builder instance
         */
        @CanIgnoreReturnValue
        public MetricDefinitionBuilder withDescription(final String description) {
            this.description = description;

            return this;
        }

        /**
         * Defines the plugin that reported the metric definition.
         *
         * @param reportedBy
         *         the plugin that reported this metric
         *
         * @return the builder instance
         */
        @CanIgnoreReturnValue
        public MetricDefinitionBuilder withReportedBy(final String reportedBy) {
            this.reportedBy = reportedBy;

            return this;
        }

        /**
         * Sets the priority of the metric definition.
         *
         * @param priority
         *         the priority of the metric
         *
         * @return the builder instance
         */
        @CanIgnoreReturnValue
        public MetricDefinitionBuilder withPriority(final int priority) {
            this.priority = priority;

            return this;
        }

        /**
         * Sets the scopes of the metric definition.
         *
         * @param scopes
         *         the scopes of the metric
         *
         * @return the builder instance
         */
        @CanIgnoreReturnValue
        public MetricDefinitionBuilder withScopes(final Scope... scopes) {
            Collections.addAll(this.scopes, scopes);

            return this;
        }

        /**
         * Creates the {@link MetricDefinition} instance.
         *
         * @return the created {@link MetricDefinition} instance
         */
        public MetricDefinition build() {
            return new MetricDefinition(id, displayName, description, reportedBy, priority, scopes);
        }
    }
}
