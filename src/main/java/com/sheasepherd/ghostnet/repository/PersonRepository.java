package com.sheasepherd.ghostnet.repository;

import com.sheasepherd.ghostnet.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByNameAndTelefonnummer(String name, String telefonnummer);
}