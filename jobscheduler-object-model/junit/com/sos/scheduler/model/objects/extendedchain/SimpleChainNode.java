package com.sos.scheduler.model.objects.extendedchain;

public class SimpleChainNode {

    public String state;
    public String job;
    public String nextState;
    public String errorState;
    public String previousState;

    public SimpleChainNode(String state, String job, String nextState, String previousState, String errorState) {
        this.state = state;
        this.job = job;
        this.nextState = nextState;
        this.previousState = previousState;
        this.errorState = errorState;
    }

    public SimpleChainNode(String state) {
        this.state = state;
        this.job = null;
        this.nextState = null;
        this.previousState = null;
        this.errorState = null;
    }

}
