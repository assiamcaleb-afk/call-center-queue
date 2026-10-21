package com.example.callcenter.web.dto;

import com.example.callcenter.model.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CallerRequest(
        @NotBlank String name,
        @NotBlank String phone,
        @NotNull IssueType issueType) {}