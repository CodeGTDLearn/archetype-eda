#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${project_name}Controller;

import ${package}.${project_name}.${project_name};
import ${package}.${project_name}Service.${project_name}Service;
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

@WebMvcTest(${project_name}Controller.class)
public class ${project_name}ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ${project_name}Service userService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testCreate${project_name}() throws Exception {
        var item = new ${project_name}(null, "John Doe");
        when(userService.save${project_name}(item)).thenReturn(new ${project_name}(1L, "John Doe"));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(new ${project_name}(1L, "John Doe"))));
    }

    @Test
    public void testGetAll${project_name}s() throws Exception {
        var item1 = new ${project_name}(1L, "John Doe");
        var item2 = new ${project_name}(2L, "Jane Doe");
        when(userService.getAll${project_name}s()).thenReturn(Arrays.asList(item1, item2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(Arrays.asList(item1, item2))));
    }
}