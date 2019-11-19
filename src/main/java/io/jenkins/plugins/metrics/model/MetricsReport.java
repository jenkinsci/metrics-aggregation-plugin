package io.jenkins.plugins.metrics.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;

public class MetricsReport extends ArrayList<MetricsMeasurement> {
    private static final long serialVersionUID = 8966464467211357593L;

    private final List<String> infoMessages = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();

    /**
     * Logs the specified information message. Use this method to log any useful information when composing this
     * report.
     *
     * @param format
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     * @param args
     *         Arguments referenced by the format specifiers in the format string.  If there are more arguments than
     *         format specifiers, the extra arguments are ignored.  The number of arguments is variable and may be
     *         zero.
     *
     * @see #getInfoMessages()
     */
    public void logInfo(final String format, final Object... args) {
        infoMessages.add(String.format(format, args));
    }

    /**
     * Logs the specified error message. Use this method to log any error when composing this report.
     *
     * @param format
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     * @param args
     *         Arguments referenced by the format specifiers in the format string.  If there are more arguments than
     *         format specifiers, the extra arguments are ignored.  The number of arguments is variable and may be
     *         zero.
     *
     * @see #getInfoMessages()
     */
    public void logError(final String format, final Object... args) {
        errorMessages.add(String.format(format, args));
    }

    /**
     * Returns the info messages that have been reported since the creation of this set of issues.
     *
     * @return the info messages
     */
    public ImmutableList<String> getInfoMessages() {
        return Lists.immutable.ofAll(infoMessages);
    }

    /**
     * Returns the error messages that have been reported since the creation of this set of issues.
     *
     * @return the error messages
     */
    public ImmutableList<String> getErrorMessages() {
        return Lists.immutable.ofAll(errorMessages);
    }
}
