package com.example.semantic;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class LightsPluginTest {

    @Mock
    private LightsPlugin lightsPlugin;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetLights() {
        List<LightModel> mockLights = List.of(
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
        LightModel mockLight = new LightModel(1, "Table Lamp", false);

        when(lightsPlugin.changeState(1, true)).thenReturn(new LightModel(1, "Table Lamp", true));

        LightModel updatedLight = lightsPlugin.changeState(1, true);
        assertTrue(updatedLight.isOn());
    }
}
