package it.pkg.MyArchetypeService;

import it.pkg.MyArchetype.MyArchetype;
import it.pkg.MyArchetypeRepository.MyArchetypeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service(value = "myArchetypeService")
public class MyArchetypeService {

  @Autowired
  private MyArchetypeRepository myArchetypeRepository;

  public MyArchetype saveMyArchetype(MyArchetype myArchetype) {

    return myArchetypeRepository.save(myArchetype);
  }

  public List<MyArchetype> getAllMyArchetypes() {

    return myArchetypeRepository.findAll();
  }
}