package com.example.friend.service.user;

import com.example.common.core.domain.R;
import com.example.common.core.domain.vo.LoginUserVO;
import com.example.friend.domain.user.dto.UserDTO;
import com.example.friend.domain.user.dto.UserUpdateDTO;
import com.example.friend.domain.user.vo.UserVO;

public interface IUserService {
    void sendCode(UserDTO userDTO);

    String codeLogin(String email, String code);

    boolean logout(String token);

    R<LoginUserVO> info(String token);

    UserVO detail();

    int edit(UserUpdateDTO userUpdateDTO);

    int updateHeadImage(String headImage);
}
