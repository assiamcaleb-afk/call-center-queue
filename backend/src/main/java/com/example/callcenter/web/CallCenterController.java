package com.example.callcenter.web;

import com.example.callcenter.model.*;
import com.example.callcenter.service.CallCenterService;
import com.example.callcenter.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")   // loosen for dev; restrict in prod
public class CallCenterController {

    private final CallCenterService service;

    public CallCenterController(CallCenterService service) {
        this.service = service;
    }

    // -------- Customer --------

    /** POST /api/callers  { name, phone, issueType } */
    @PostMapping("/callers")
    public ResponseEntity<?> joinQueue(@Valid @RequestBody CallerRequest req) {
        Caller c = service.enqueue(req.name(), req.phone(), req.issueType());
        if (c == null) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", "Queue is full"));
        }
        return ResponseEntity.ok(c);
    }

    // -------- Agent --------

    /** POST /api/agents/{id}/next  -> returns the caller or 204 */
    @PostMapping("/agents/{id}/next")
    public ResponseEntity<Caller> next(@PathVariable String id) {
        Caller c = service.answerNext(id);
        return c == null ? ResponseEntity.noContent().build()
                         : ResponseEntity.ok(c);
    }

    /** POST /api/agents/{id}/end */
    @PostMapping("/agents/{id}/end")
    public ResponseEntity<?> end(@PathVariable String id) {
        boolean ok = service.endCall(id);
        return ok ? ResponseEntity.ok(Map.of("status", "ended"))
                  : ResponseEntity.status(409)
                      .body(Map.of("error", "Agent has no active call"));
    }

    /** POST /api/agents/{id}/break?on=true|false */
    @PostMapping("/agents/{id}/break")
    public ResponseEntity<?> toggleBreak(@PathVariable String id,
                                         @RequestParam boolean on) {
        boolean ok = service.setAgentBreak(id, on);
        return ok ? ResponseEntity.ok(Map.of("status", "ok"))
                  : ResponseEntity.status(404)
                      .body(Map.of("error", "Agent not found"));
    }

    // -------- Dashboard --------

    /** GET /api/state  -> { queue: [...], agents: [...], queueSize } */
    @GetMapping("/state")
    public StateResponse state() {
        return new StateResponse(
                service.snapshotQueue(),
                service.snapshotAgents(),
                service.queueSize());
    }

    /** GET /api/stats */
    @GetMapping("/stats")
    public CallCenterService.Stats stats() {
        return service.stats();
    }
}