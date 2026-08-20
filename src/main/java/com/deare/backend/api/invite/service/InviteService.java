package com.deare.backend.api.invite.service;

import com.deare.backend.api.invite.dto.response.InviteCodeResponseDTO;
import com.deare.backend.api.invite.dto.response.InviteValidationResponseDTO;
import com.deare.backend.domain.user.entity.User;

public interface InviteService {

    InviteCodeResponseDTO issueCode(Long userId);

    InviteValidationResponseDTO validate(String inviteCode);

    void applySignupBenefit(String inviteCode, User invitee);
}
