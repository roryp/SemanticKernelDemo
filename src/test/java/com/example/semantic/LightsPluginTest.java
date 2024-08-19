package com.example.semantic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class LightsPluginTest {

    private LightsPlugin lightsPlugin;

    @BeforeEach
    public void setUp() {
        lightsPlugin = mock(LightsPlugin.class);
    }

    @Test
    public void testGetLights() {
        List<LightModel> mockLights = Arrays.asList(
            new LightModel(1, "Table Lamp", false),
            new LightModel(2, "Porch light", false),
            new LightModel(3, "Chandelier", true)
        );

        when(lightsPlugin.getLights()).thenReturn(mockLights);

        List<LightModel> lights = lightsPlugin.getLights();
        assertEquals(3, lights.size());
        assertEquals("Table Lamp", lights.get(0).getName());
        assertFalse(lights.get(0).isOn());
    }

    @Test
    public void testChangeState() {
        when(lightsPlugin.changeState(1, true)).thenReturn(new LightModel(1, "Table Lamp", true));
        LightModel updatedLight = lightsPlugin.changeState(1, true);
        assertEquals(1, updatedLight.getId());
        assertEquals("Table Lamp", updatedLight.getName());
        assertTrue(updatedLight.isOn());
    }
}