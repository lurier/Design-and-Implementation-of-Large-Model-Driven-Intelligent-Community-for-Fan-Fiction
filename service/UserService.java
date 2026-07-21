package com.fanfaction.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fanfaction.dto.LoginDTO;
import com.fanfaction.dto.RegisterDTO;
import com.fanfaction.dto.RoleUpdateDTO;
import com.fanfaction.dto.UserProfileDTO;
import com.fanfaction.entity.User;
import com.fanfaction.vo.LoginVO;
import com.fanfaction.vo.UserVO;

public interface UserService extends IService<User> {
    User getUserByUsername(String username);
    void register(RegisterDTO registerDTO);
    LoginVO login(LoginDTO loginDTO);
    UserVO getCurrentUserInfo(String username);
    IPage<UserVO> getUserPage(int pageNum, int pageSize, String keyword);
    void updateUserRole(RoleUpdateDTO roleUpdateDTO);
    void updateUserProfile(Long userId, UserProfileDTO profileDTO);
}
