package com.deare.backend.api.invite.service;

public interface SignupBenefitWriteService {

    void apply(String inviteCode, Long userId);
}
