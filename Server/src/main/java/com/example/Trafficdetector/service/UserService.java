package com.example.Trafficdetector.service;

import com.example.Trafficdetector.dto.UserDto;
import com.example.Trafficdetector.entity.User;
import com.example.Trafficdetector.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 로그인 처리
    public boolean login(UserDto userDto) {
        // 유저 정보 검증 로직
        User user = userRepository.findByUserid(userDto.getUserid());
        return user != null && user.getPassword().equals(userDto.getPassword()); // 비밀번호 비교 시 암호화를 적용해야 합니다.
    }

    // 회원가입 처리
    public boolean signup(UserDto userDto) {
        // 유저 정보 저장
        User user = new User();
        user.setName(userDto.getName());
        user.setUserid(userDto.getUserid());
        user.setPassword(userDto.getPassword()); // 비밀번호는 암호화하여 저장하는 것이 좋습니다.
        userRepository.save(user);
        return true;
    }
}
