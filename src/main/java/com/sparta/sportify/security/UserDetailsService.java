package com.sparta.sportify.security;

import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import com.sparta.sportify.exception.CustomValidationException;
import com.sparta.sportify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserRepository userRepository; // 사용자 정보를 가져올 Repository

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이메일로 사용자 조회, 존재하지 않으면 예외 처리
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 삭제된 사용자 체크
        if (user.getDeletedAt() != null) {
            throw new RuntimeException("삭제된 유저입니다.");
        }

        // 사용자 역할 가져오기
        UserRole userRole = user.getUserRole();

        // UserDetailsImpl로 변환하여 반환
        return new UserDetailsImpl(
                user.getName(),
                userRole, // UserRole을 String으로 변환하여 전달
                user
        );
    }
}
