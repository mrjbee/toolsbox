package org.monroe.team.toolsbox.us.model.impl.dependecy;

public class InMemoryDependency<DependencyType> extends Dependency<DependencyType>{

    private final InstanceProvider<DependencyType> provider;

    public InMemoryDependency(InstanceProvider<DependencyType> provider) {
        this.provider = provider;
    }

    @Override
    protected DependencyType refreshImpl() {
       return provider.get();
    }

    @Override
    public void save() {/*do nothing as all in memory*/}
    @Override
    public void delete() {/*do nothing as all in memory*/}

    public static interface InstanceProvider<DependencyType> {
        public DependencyType get();
    }
}
