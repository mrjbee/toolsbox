package org.monroe.team.toolsbox.us.model.impl.dependecy;

public abstract class Dependency<DependencyType> {

    private DependencyType dependency;

    protected Dependency() {}

    protected Dependency(DependencyType dependency) {
        this.dependency = dependency;
    }

    final public DependencyType get(){
        if (dependency == null){
            refresh();
        }

        return dependency;
    }

    final public DependencyType refresh() {
        dependency = refreshImpl();
        return dependency;
    }

    final protected void set(DependencyType dependency){
        this.dependency = dependency;
    }

    protected abstract DependencyType refreshImpl();

    public abstract void save();

    public boolean exists(){
        if (dependency != null) return true;
        refresh();
        return dependency!=null;
    };

    public abstract void delete();

    public void unSet() {
        dependency = null;
    }
}
