package com.infilos.spring.rest;

import com.infilos.spring.model.Gender;
import com.infilos.spring.model.Person;
import com.infilos.spring.model.Region;
import com.infilos.spring.utils.Respond;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/enum")
public class EnumController {

    @GetMapping("/person")
    public Respond<Person> getPerson() {
        return Respond.succed(Person.builder()
            .name("Anna")
            .age(22)
            .gender(Gender.FEMALE)
            .region(Region.OVERSEA)
            .build()
        );
    }

    @PostMapping("/person")
    public Respond<Person> postPerson(@RequestBody Person person) {
        return Respond.succed(person);
    }
}
