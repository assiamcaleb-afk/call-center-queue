package com.example.callcenter.model;

import java.util.concurrent.atomic.AtomicLong;

public class Caller {
    private static final AtomicLong SEQ = new AtomicLong();

    private final long id = SEQ.incrementAndGet();
    private final String name;
    private final String phone;
    private final IssueType issueType;
    private final long enqueuedAt = System.currentTimeMillis();
    private volatile long answeredAt;     // 0 = still waiting
    private volatile String handledBy;    // agent id, null = unanswered

    public Caller(String name, String phone, IssueType issueType) {
        this.name = name;
        this.phone = phone;
        this.issueType = issueType;
    }

    public long getId()               { return id; }
    public String getName()           { return name; }
    public String getPhone()          { return phone; }
    public IssueType getIssueType()   { return issueType; }
    public long getEnqueuedAt()       { return enqueuedAt; }
    public long getAnsweredAt()       { return answeredAt; }
    public String getHandledBy()      { return handledBy; }

    public void markAnswered(String agentId) {
        this.answeredAt = System.currentTimeMillis();
        this.handledBy = agentId;
    }

    public long waitMillis() {
        return answeredAt == 0 ? -1 : answeredAt - enqueuedAt;
    }
}