package com.wonsu.used_market.user.service;

import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.user.domain.Provider;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.dto.UserCreateDto;
import com.wonsu.used_market.user.dto.UserLoginDto;
import com.wonsu.used_market.user.dto.UserUpdateRequestDto;
import com.wonsu.used_market.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User  create(UserCreateDto userCreateDto) {
        // 이메일 중복 확인
        if(userRepository.existsByEmail(userCreateDto.getEmail())){
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        // 닉네임 중복 확인
        if(userRepository.existsByNickname(userCreateDto.getNickname())){
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }


        User user = User.builder()
                .email(userCreateDto.getEmail())
                .password(passwordEncoder.encode(userCreateDto.getPassword()))//비밀번호 암호화
                .nickname(userCreateDto.getNickname())
                .name(userCreateDto.getName())
                .birthDate(userCreateDto.getBirthDate())
                .phone(userCreateDto.getPhone())
                .address(userCreateDto.getAddress())
                .build();

        //일단 이메일 인증을 회원가입시 true로 설정 나중에 디벨롭 예정
        user.verifyEmail();

        userRepository.save(user);
        return user;
    }

    public User login(UserLoginDto userLoginDto) {
        //유저가 있을수도 있고 없을수도 있다
        Optional<User> optUser = userRepository.findByEmail(userLoginDto.getEmail());
        //이메일이 있는지 없는지 검증
        if(!optUser.isPresent()) {
            log.warn("로그인 실패 - 존재하지 않는 이메일: {}", userLoginDto.getEmail());
            throw new BusinessException(ErrorCode.LOGIN_FAILED); // ✅ 예외 던지기
        }

        User user = optUser.get();

        //로그인할때의 비밀번호가 데이터베이스에 있는 암호화된 비밀번호와 일치하는지 비교
        if(!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) {
            log.warn("로그인 실패 - 비밀번호 불일치 (email: {})", userLoginDto.getEmail());
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        return user;
    }

    public User getUserByProviderId(Provider provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .orElse(null);
    }

    @Transactional
    public User createOauth(String providerId, String email, Provider provider,boolean emailVerified) {
        // oauth 회원가입자들을 위한 닉네임 자동생성
        String randomNickname;

        // 중복 방어 루프
        do {
            randomNickname = "user_" + UUID.randomUUID().toString().substring(0, 8);
        } while (userRepository.existsByNickname(randomNickname));

        User user = User.builder()
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .nickname(randomNickname)
                .build();

        if(emailVerified){
            user.verifyEmail();
        }

        userRepository.save(user);
        return user;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    //내 정보 수정
    //jpa 더티체킹으로 자동반영
    @Transactional
    public User updateUser(Long userId, UserUpdateRequestDto dto){
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        //닉네임 변경시 중복검사
        if(dto.getNickname() != null && !dto.getNickname().equals(user.getNickname())) {
            if(userRepository.existsByNickname(dto.getNickname())) {
                throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
            }
            user.changeNickname(dto.getNickname());
        }

        if (dto.getPhone() != null) user.changePhone(dto.getPhone());
        if (dto.getAddr() != null) user.changeAddr(dto.getAddr());
        if (dto.getName() != null) user.changeName(dto.getName());
        if (dto.getBirthDate() != null) user.changeBirthDate(dto.getBirthDate());
        if (dto.getPassword() != null) user.changePassword(passwordEncoder.encode(dto.getPassword()));


        return user;
    }

    @Transactional
    //내정보 삭제
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }


}
