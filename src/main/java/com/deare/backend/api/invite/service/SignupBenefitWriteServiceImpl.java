package com.deare.backend.api.invite.service;

import com.deare.backend.api.auth.exception.AuthErrorCode;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupBenefitWriteServiceImpl implements SignupBenefitWriteService {

    private final UserRepository userRepository;
    private final InviteService inviteService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void apply(String inviteCode, Long userId) {
        User invitee = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(AuthErrorCode.USER_NOT_FOUND));
        inviteService.applySignupBenefit(inviteCode, invitee);
    }
}
