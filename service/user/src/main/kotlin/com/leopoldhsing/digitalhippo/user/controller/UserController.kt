package com.leopoldhsing.digitalhippo.user.controller

import com.leopoldhsing.digitalhippo.model.entity.User
import com.leopoldhsing.digitalhippo.user.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController @Autowired constructor(private val userService: UserService) {

    @GetMapping
    fun getUser(): ResponseEntity<User> {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUser())
    }
}