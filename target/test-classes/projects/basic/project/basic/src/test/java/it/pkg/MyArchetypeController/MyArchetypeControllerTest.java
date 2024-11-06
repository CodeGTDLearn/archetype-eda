package it.pkg.MyArchetypeController;

import it.pkg.MyArchetype.MyArchetype;
import it.pkg.MyArchetypeService.MyArchetypeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MyArchetypeController.class)
public class MyArchetypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MyArchetypeService userService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testCreateMyArchetype() throws Exception {
        var item = new MyArchetype(null, "John Doe");
        when(userService.saveMyArchetype(item)).thenReturn(new MyArchetype(1L, "John Doe"));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(new MyArchetype(1L, "John Doe"))));
    }

    @Test
    public void testGetAllMyArchetypes() throws Exception {
        var item1 = new MyArchetype(1L, "John Doe");
        var item2 = new MyArchetype(2L, "Jane Doe");
        when(userService.getAllMyArchetypes()).thenReturn(Arrays.asList(item1, item2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(Arrays.asList(item1, item2))));
    }
}