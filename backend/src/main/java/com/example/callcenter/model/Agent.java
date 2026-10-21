package com.example.callcenter.model;

public class Agent {
    public enum Status { AVAILABLE, BUSY, ON_BREAK }

    private final String id;
    private final String name;
    private volatile Status status = Status.AVAILABLE;
    private volatile Long currentCallerId;

    public Agent(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId()             { return id; }
    public String getName()           { return name; }
    public Status getStatus()         { return status; }
    public Long getCurrentCallerId()  { return currentCallerId; }

    public void assign(Long callerId) {
        this.currentCallerId = callerId;
        this.status = Status.BUSY;
    }

    public void release() {
        this.currentCallerId = null;
        this.status = Status.AVAILABLE;
    }

    public void setOnBreak(boolean onBreak) {
        if (onBreak && status == Status.AVAILABLE) {
            status = Status.ON_BREAK;
        } else if (!onBreak && status == Status.ON_BREAK) {
            status = Status.AVAILABLE;
        }
    }
}