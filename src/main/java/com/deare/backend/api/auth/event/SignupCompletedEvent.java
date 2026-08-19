package com.deare.backend.api.auth.event;

public record SignupCompletedEvent(String inviteCode, Long userId) {
}
