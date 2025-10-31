package io.jenkins.plugins.metrics.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Facade for Jackson that does wrap an exception into a {@link RuntimeException}.
 */
public class JacksonFacade {
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates a JSON representation of the specified object using Jackson data binding.
     *
     * @param object
     *         the object to convert
     *
     * @return the JSON representation (as a String)
     */
    public String toJson(final Object object) {
        try {
            return mapper.writeValueAsString(object);
        }
        catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(
                    String.format("Can't convert %s to JSON object", object), exception);
        }
    }
}
