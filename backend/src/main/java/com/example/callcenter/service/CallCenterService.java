package com.example.callcenter.service;

import com.example.callcenter.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Service
public class CallCenterService {

    private static final int MAX_QUEUE_CAPACITY = 200;

    /** The FIFO queue — the heart of the whole project. */
    private final BlockingQueue<Caller> waitingQueue =
            new LinkedBlockingQueue<>(MAX_QUEUE_CAPACITY);

    /** Agent pool, keyed by id. */
    private final Map<String, Agent> agents = new ConcurrentHashMap<>();

    /** Analytics. */
    private final AtomicLong totalServed = new AtomicLong();
    private final AtomicLong totalAbandoned = new AtomicLong();
    private final LongAdder totalWaitMillis = new LongAdder();

    public CallCenterService() {
        // Seed three agents so the dashboard has something to show immediately.
        addAgent("agent-1", "Alice");
        addAgent("agent-2", "Bob");
        addAgent("agent-3", "Carol");
    }

    // ---------- Customer side ----------

    /**
     * Enqueue a caller.
     * Returns the caller, or null if the queue is full.
     */
    public Caller enqueue(String name, String phone, IssueType issueType) {
        Caller c = new Caller(name, phone, issueType);
        boolean accepted = waitingQueue.offer(c);   // non-blocking, bounded
        return accepted ? c : null;
    }

    // ---------- Agent side ----------

    /**
     * Agent grabs the next caller (FIFO).
     * Returns null if no caller is waiting OR the agent isn't available.
     */
    public Caller answerNext(String agentId) {
        Agent agent = agents.get(agentId);
        if (agent == null || agent.getStatus() != Agent.Status.AVAILABLE) {
            return null;
        }
        Caller caller = waitingQueue.poll();      // O(1) FIFO dequeue
        if (caller == null) {
            return null;
        }
        caller.markAnswered(agentId);
        agent.assign(caller.getId());
        return caller;
    }

    /**
     * Agent ends the current call. Frees the agent and records stats.
     */
    public boolean endCall(String agentId) {
        Agent agent = agents.get(agentId);
        if (agent == null || agent.getCurrentCallerId() == null) {
            return false;
        }
        // Look up the caller object to record wait time — cheap because we
        // only keep aggregate stats, not a historical log.
        // (If you want a call log later, keep a List<Caller> history here.)
        totalServed.incrementAndGet();
        agent.release();
        return true;
    }

    /**
     * Called internally when a caller's wait time is being aggregated.
     * Kept separate so endCall stays cheap if you later add persistence.
     */
    public void recordWait(Caller caller) {
        if (caller != null && caller.waitMillis() >= 0) {
            totalWaitMillis.add(caller.waitMillis());
        }
    }

    public boolean setAgentBreak(String agentId, boolean onBreak) {
        Agent agent = agents.get(agentId);
        if (agent == null) return false;
        agent.setOnBreak(onBreak);
        return true;
    }

    // ---------- Read side (for dashboard) ----------

    public List<Caller> snapshotQueue() {
        return List.copyOf(waitingQueue);         // iteration order = FIFO
    }

    public List<Agent> snapshotAgents() {
        return agents.values().stream()
                .sorted(Comparator.comparing(Agent::getId))
                .collect(Collectors.toList());
    }

    public int queueSize() {
        return waitingQueue.size();
    }

    public Stats stats() {
        long served = totalServed.get();
        double avgWaitSec = served == 0
                ? 0.0
                : (totalWaitMillis.sum() / 1000.0) / served;
        long waiting = waitingQueue.size();
        long busyAgents = agents.values().stream()
                .filter(a -> a.getStatus() == Agent.Status.BUSY).count();
        long availableAgents = agents.values().stream()
                .filter(a -> a.getStatus() == Agent.Status.AVAILABLE).count();
        return new Stats(waiting, served, totalAbandoned.get(),
                         avgWaitSec, busyAgents, availableAgents);
    }

    public record Stats(long waiting, long served, long abandoned,
                        double avgWaitSeconds,
                        long busyAgents, long availableAgents) {}

    private void addAgent(String id, String name) {
        agents.put(id, new Agent(id, name));
    }
}