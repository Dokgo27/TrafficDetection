package com.example.Trafficdetector.controller;

import com.example.Trafficdetector.dto.UserDto;
import com.example.Trafficdetector.entity.User;
import com.example.Trafficdetector.service.UserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;

@RestController
@RequestMapping("/api/users")
@Data
public class UserController {


    private final UserService userService;

    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDto userDto) {

        // 로그인 서비스
        boolean success = userService.login(userDto);

        if (success) {
            return new ResponseEntity<>("success", HttpStatus.CREATED);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed");
        }
    }

    // 회원가입 API
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserDto userDto) {

        // 회원 가입 서비스
        boolean success = userService.signup(userDto);

        if (success) {
            return new ResponseEntity<>("success", HttpStatus.CREATED);
        } else {
            return ResponseEntity.badRequest().body("Signup failed");
        }
    }

}

