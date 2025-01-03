package com.sparta.sportify.security;

import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
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
        User user = userRepository.findByEmailAndDeletedAtIsNull(email).orElseThrow(
                () -> new CustomApiException(ErrorCode.USER_NOT_FOUND)
        );

        // 사용자 역할 가져오기
        UserRole userRole = user.getRole();

        return new UserDetailsImpl(
                user.getName(),
                userRole,
                user
        );
    }

}
