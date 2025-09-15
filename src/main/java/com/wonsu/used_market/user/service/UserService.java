package com.wonsu.used_market.user.service;

import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.dto.UserCreateDto;
import com.wonsu.used_market.user.dto.UserLoginDto;
import com.wonsu.used_market.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional()
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User  create(UserCreateDto userCreateDto) {
        User user = User.builder()
                .email(userCreateDto.getEmail())
                .password(passwordEncoder.encode(userCreateDto.getPassword()))//비밀번호 암호화
                .nickname(userCreateDto.getNickname())
                .name(userCreateDto.getName())
                .birthDate(userCreateDto.getBirthDate())
                .phone(userCreateDto.getPhone())
                .address(userCreateDto.getAddress())
                .build();
        userRepository.save(user);
        return user;
    }

    public User login(UserLoginDto userLoginDto) {
        //유저가 있을수도 있고 없을수도 있다
        Optional<User> optUser = userRepository.findByEmail(userLoginDto.getEmail());
        //이메일이 있는지 없는지 검증
        if(!optUser.isPresent()) {
            throw new IllegalArgumentException("email이 존재하지 않습니다.");
        }

        User user = optUser.get();

        //로그인할때의 비밀번호가 데이터베이스에 있는 암호화된 비밀번호와 일치하는지 비교
        if(!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("password가 일치하지 않습니다.");
        }

        return user;
    }


}
