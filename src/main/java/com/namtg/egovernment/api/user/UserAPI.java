package com.namtg.egovernment.api.user;

import com.namtg.egovernment.dto.response.ServerResponseDto;
import com.namtg.egovernment.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserAPI {

    @Autowired
    private UserService userService;

    @GetMapping("/getNameUser/{userId}")
    public ResponseEntity<ServerResponseDto> getNameUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ServerResponseDto.successWithData(userService.getNameUserById(userId)));
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<ServerResponseDto> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(userService.forgotPassword(email));
    }

    @PostMapping(value = "/setPassword")
    public ResponseEntity<ServerResponseDto> setPassword(@RequestParam("token") String token,
                                                         @RequestParam("password") String password) {
        return ResponseEntity.ok().body(userService.setPassword(token, password));
    }

}
