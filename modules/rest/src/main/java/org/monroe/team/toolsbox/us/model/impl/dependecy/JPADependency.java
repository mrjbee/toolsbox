package org.monroe.team.toolsbox.us.model.impl.dependecy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;

public class JPADependency<DependencyType, DependencyId extends Serializable> extends Dependency<DependencyType> {

    private final JpaRepository<DependencyType, DependencyId> repository;
    private final DependencyId id;

    public JPADependency(JpaRepository<DependencyType, DependencyId> repository, DependencyId id) {
        this.repository = repository;
        this.id = id;
    }

    public JPADependency(JpaRepository<DependencyType, DependencyId> repository, DependencyId id, DependencyType dependency) {
        super(dependency);
        this.repository = repository;
        this.id = id;
    }

    @Override
    final public void save(){
       set(repository.save(get()));
    }

    @Override
    public void delete() {
       repository.delete(id);
       super.unSet();
    }



    @Override
    protected DependencyType refreshImpl() {
        return repository.findOne(id);
    }
}
