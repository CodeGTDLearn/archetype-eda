package it.pkg.MyArchetypeController;

import it.pkg.MyArchetype.MyArchetype;
import it.pkg.MyArchetypeService.MyArchetypeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/myarchetypes")
public class MyArchetypeController {

  @Autowired
  private MyArchetypeService myArchetypeService;

  @PostMapping
  public MyArchetype createMyArchetype(@RequestBody MyArchetype myArchetype) {

    return myArchetypeService.saveMyArchetype(myArchetype);
  }

  @GetMapping
  public List<MyArchetype> getAllMyArchetypes() {

    return myArchetypeService.getAllMyArchetypes();
  }
}