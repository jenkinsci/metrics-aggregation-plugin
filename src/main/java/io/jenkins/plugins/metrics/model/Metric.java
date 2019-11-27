package io.jenkins.plugins.metrics.model;

import java.io.Serializable;
import java.util.Objects;

public class Metric implements Serializable {
    private static final long serialVersionUID = -8143304414028170807L;

    private String id;
    private String displayName;
    private String description;
    private double value;

    public Metric(final String id, final String displayName, final String description, final double value) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(final double value) {
        this.value = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("Metric %s: %f", id, value);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Metric)) {
            return false;
        }

        Metric other = (Metric) o;
        return Objects.equals(this.id, other.id)
                && Objects.equals(this.displayName, other.displayName)
                && this.value == other.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, description, value);
    }
}
