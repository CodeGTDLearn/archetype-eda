package it.pkg.MyArchetypeService;


import it.pkg.MyArchetype.MyArchetype;
import it.pkg.MyArchetypeRepository.MyArchetypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
public class MyArchetypeServiceTest {

    @MockBean
    private MyArchetypeRepository userRepository;

    @Autowired
    private MyArchetypeService userService;

    @Test
    public void testSaveMyArchetype() {
        var item = new MyArchetype(null, "John Doe");
        when(MyArchetypeRepository.save(item)).thenReturn(new MyArchetype(1L, "John Doe"));

        var saved = MyArchetypeService.saveMyArchetype(item);
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    public void testGetAllMyArchetypes() {
        var item1 = new MyArchetype(1L, "John Doe");
        var item2 = new MyArchetype(2L, "Jane Doe");
        when(MyArchetypeRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        var items = MyArchetypeService.getAllMyArchetypes();
        assertThat(items).hasSize(2);
    }
}