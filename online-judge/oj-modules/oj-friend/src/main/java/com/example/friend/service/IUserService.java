package com.example.friend.service;

import com.example.friend.domain.dto.UserDTO;

public interface IUserService {
    void sendCode(UserDTO userDTO);
}
