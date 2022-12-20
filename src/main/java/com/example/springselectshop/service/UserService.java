package com.example.springselectshop.service;

import com.example.springselectshop.dto.LoginRequest;
import com.example.springselectshop.dto.SignupRequest;
import com.example.springselectshop.entity.User;
import com.example.springselectshop.entity.UserRoleEnum;
import com.example.springselectshop.jwt.JwtUtil;
import com.example.springselectshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // ADMIN_TOKEN
    private static final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

    @Transactional
    public void signup(SignupRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        // 회원 중복 확인
        Optional<User> found = userRepository.findByUsername(username);
        if (found.isPresent()) {
            throw new IllegalArgumentException("중복된 사용자가 존재합니다.");
        }

        String email = request.getEmail();
        // 사용자 ROLE 확인
        UserRoleEnum role = UserRoleEnum.USER;
        if (request.isAdmin()) {
            if (!request.getAdminToken().equals(ADMIN_TOKEN)) {
                throw new IllegalArgumentException("관리자 암호가 틀려 등록이 불가능합니다.");
            }
            role = UserRoleEnum.ADMIN;
        }

        User user = new User(username, password, email, role);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public void login(LoginRequest request, HttpServletResponse response) {
        String username = request.getUsername();
        String password = request.getPassword();

        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("등록된 사용자가 없습니다.")
        );

        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // JWT 활용 시 추가
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(user.getUsername(), user.getRole()));

    }
}
