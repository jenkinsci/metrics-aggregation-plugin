package io.jenkins.plugins.metrics.view;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test for the class {@link JacksonFacade}.
 */
class JacksonFacadeTest {
    /**
     * Test if a list is correctly converted to json.
     */
    @Test
    void shouldConvertListToJson() {
        var facade = new JacksonFacade();

        assertThat(facade.toJson(Arrays.asList("hello", "world", 1))).isEqualTo("[\"hello\",\"world\",1]");
    }

    /**
     * Test if a illegalArgumentException is thrown if an impossible object is provided.
     */
    @Test
    void shouldThrowIllegalArgumentException() {
        Object faultyObject = mock(Object.class);
        when(faultyObject.toString()).thenReturn(faultyObject.getClass().getName());

        var facade = new JacksonFacade();
        assertThatIllegalArgumentException().isThrownBy(() -> facade.toJson(faultyObject));
    }
}
