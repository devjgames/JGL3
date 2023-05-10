package org.jgl3.demo;

public abstract class Demo {
    
    public abstract void init() throws Exception;

    public abstract boolean run() throws Exception;

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
