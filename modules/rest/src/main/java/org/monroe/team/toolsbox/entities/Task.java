package org.monroe.team.toolsbox.entities;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.persistence.*;
import java.util.List;
import java.util.Properties;

@Entity
public class Task {

    @Id @GeneratedValue
    public Integer id;

    @Column(nullable = false)
    public ExecutionStatus status;

    @Column(nullable = false)
    public Type type;

    @Column(nullable = false)
    public Long creationTime;

    @OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    public List<Property> properties;

    public <T> T getProperty(final String key, Class<T> type){
        Property property = Iterables.find(properties, new Predicate<Property>() {
            @Override
            public boolean apply(Property property) {
                return key.equals(property.name);
            }
        });
        if (property == null) return null;
        if (String.class.equals(type)){
            return (T) property.value;
        } if (Integer.class.equals(type)) {
            return (T) new Integer(Integer.parseInt(property.value));
        }else{
            throw new RuntimeException("Unsupported type");
        }
    }

    public static enum Type{
        COPY, TRANSFER, DELETE
    }

    public static enum ExecutionStatus {
        AWAITING, BLOCKED, IN_PROGRESS, DONE, FAIL
    }

}
