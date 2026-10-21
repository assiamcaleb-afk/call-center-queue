package com.example.callcenter.web.dto;

import com.example.callcenter.model.Agent;
import com.example.callcenter.model.Caller;
import java.util.List;

public record StateResponse(
        List<Caller> queue,
        List<Agent> agents,
        int queueSize) {}