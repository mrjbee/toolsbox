package org.monroe.team.toolsbox.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Property {

    @Id @GeneratedValue
    public Integer id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false, length = 2000)
    public String value;

    public Property() {}

    public Property(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
