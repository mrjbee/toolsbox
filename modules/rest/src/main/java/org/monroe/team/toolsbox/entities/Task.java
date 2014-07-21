package org.monroe.team.toolsbox.entities;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.monroe.team.toolsbox.us.model.TaskModel;

import javax.persistence.*;
import javax.persistence.metamodel.Type;
import java.util.List;
import java.util.Properties;

@Entity
public class Task {

    @Id @GeneratedValue
    public Integer id;

    @Column(nullable = false)
    public TaskModel.ExecutionStatus status;

    @Column(nullable = false)
    public TaskModel.Type type;

    @Column(nullable = false)
    public Long creationTime;

    @OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    public List<Property> properties;

    @Column
    public String pendingReason;

    public <T> T getProperty(final String key, Class<T> type){
        Optional<Property> propertyHolder = Iterables.tryFind(properties, new Predicate<Property>() {
            @Override
            public boolean apply(Property property) {
                return key.equals(property.name);
            }
        });
        if (!propertyHolder.isPresent()) return null;
        Property property = propertyHolder.get();
        if (String.class.equals(type)){
            return (T) property.value;
        } if (Integer.class.equals(type)) {
            return (T) new Integer(Integer.parseInt(property.value));
        }if (Boolean.class.equals(type) || boolean.class.equals(type)) {
            return (T) new Boolean(Boolean.parseBoolean(property.value));
        }else{
            throw new RuntimeException("Unsupported type");
        }
    }

}
