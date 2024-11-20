package it.pkg.MyArchetypeRepository;

import it.pkg.MyArchetype.MyArchetype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository(value = "myArchetypeRepository")
public interface MyArchetypeRepository extends JpaRepository<MyArchetype, Long> {
}