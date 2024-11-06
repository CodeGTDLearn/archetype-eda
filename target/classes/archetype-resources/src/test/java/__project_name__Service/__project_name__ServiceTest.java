#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.${project_name}Service;


import ${package}.${project_name}.${project_name};
import ${package}.${project_name}Repository.${project_name}Repository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ${project_name}ServiceTest {

    @MockBean
    private ${project_name}Repository userRepository;

    @Autowired
    private ${project_name}Service userService;

    @Test
    public void testSave${project_name}() {
        var item = new ${project_name}(null, "John Doe");
        when(${project_name}Repository.save(item)).thenReturn(new ${project_name}(1L, "John Doe"));

        var saved = ${project_name}Service.save${project_name}(item);
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    public void testGetAll${project_name}s() {
        var item1 = new ${project_name}(1L, "John Doe");
        var item2 = new ${project_name}(2L, "Jane Doe");
        when(${project_name}Repository.findAll()).thenReturn(Arrays.asList(item1, item2));

        var items = ${project_name}Service.getAll${project_name}s();
        assertThat(items).hasSize(2);
    }
}