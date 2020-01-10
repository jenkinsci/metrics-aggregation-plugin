package io.jenkins.plugins.metrics.util;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JacksonFacadeTest {

    @Test
    public void shouldConvertListToJson() {
        JacksonFacade facade = new JacksonFacade();

        assertThat(facade.toJson(Arrays.asList("hello", "world", 1))).isEqualTo("[\"hello\",\"world\",1]");
    }

    @Test
    public void shouldThrowIllegalArgumentException() {
        Object faultyObject = mock(Object.class);
        when(faultyObject.toString()).thenReturn(faultyObject.getClass().getName());

        JacksonFacade facade = new JacksonFacade();
        assertThrows(IllegalArgumentException.class, () -> {
            facade.toJson(faultyObject);
        });
    }
}
