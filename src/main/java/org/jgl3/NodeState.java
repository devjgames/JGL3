package org.jgl3;

public abstract class NodeState extends Node {
    
    private State state = new State();

    public final State getState() {
        return state;
    }
}
