package com.deare.backend.api.term.service;

import com.deare.backend.domain.term.entity.Term;
import com.deare.backend.domain.term.entity.UserTerm;
import com.deare.backend.domain.term.repository.TermRepository;
import com.deare.backend.domain.term.repository.UserTermRepository;
import com.deare.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTermService {
    
    private final TermRepository termRepository;
    private final UserTermRepository userTermRepository;
    
    /**
     * 사용자의 약관 동의 내역 생성
     * 
     * @param user 약관에 동의한 사용자
     * @param agreedTermIds 동의한 약관 ID 리스트
     */
    @Transactional
    public void createUserTerms(User user, List<Long> agreedTermIds) {

        // 약관 ID가 없으면 종료
        if (agreedTermIds == null || agreedTermIds.isEmpty()) {
            log.warn("약관 동의 내역이 없습니다 - User ID: {}", user.getId());
            return;
        }
        
        // 약관 조회
        List<Term> terms = termRepository.findByIdIn(agreedTermIds);
        
        // 요청한 약관 개수와 조회된 약관 개수가 다르면 예외 -> 약관 에러 코드 추가 시 수정
        if (terms.size() != agreedTermIds.size()) {
            throw new RuntimeException("존재하지 않는 약관 ID가 포함되어 있습니다.");
        }
        
        // 필수 약관 체크
        validateRequiredTerms(terms);
        
        // userTerm 생성 및 저장
        List<UserTerm> userTerms = terms.stream()
                .map(term -> UserTerm.createAgreement(user, term))
                .toList();
        
        userTermRepository.saveAll(userTerms);
        
        log.info("약관 동의 완료 - User ID: {}, 동의한 약관 수: {}", user.getId(), userTerms.size());
    }
    
    /**
     * 필수 약관이 모두 포함되어 있는지 검증
     */
    private void validateRequiredTerms(List<Term> terms) {

        // 모든 필수 약관 조회
        List<Term> allTerms = termRepository.findAll();
        List<Term> requiredTerms = allTerms.stream()
                .filter(Term::isRequired)
                .toList();
        
        // 필수 약관이 없으면 통과
        if (requiredTerms.isEmpty()) {
            return;
        }
        
        // 동의한 약관 중 필수 약관이 모두 포함되어 있는지 확인
        List<Long> requiredTermIds = requiredTerms.stream()
                .map(Term::getId)
                .toList();
        
        List<Long> agreedTermIds = terms.stream()
                .map(Term::getId)
                .toList();
        
        boolean hasAllRequiredTerms = agreedTermIds.containsAll(requiredTermIds);
        
        if (!hasAllRequiredTerms) {
            throw new RuntimeException("필수 약관에 동의하지 않았습니다.");
        }
    }
    
    /**
     * 사용자의 약관 동의 내역 조회
     */
    @Transactional(readOnly = true)
    public List<UserTerm> getUserTerms(Long userId) {
        return userTermRepository.findByUserId(userId);
    }
}
