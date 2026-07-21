package com.fanfaction.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fanfaction.config.JwtUtils;
import com.fanfaction.dto.LoginDTO;
import com.fanfaction.dto.RegisterDTO;
import com.fanfaction.dto.RoleUpdateDTO;
import com.fanfaction.dto.UserProfileDTO;
import com.fanfaction.entity.User;
import com.fanfaction.mapper.UserMapper;
import com.fanfaction.service.UserService;
import com.fanfaction.vo.LoginVO;
import com.fanfaction.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public User getUserByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        User existingUser = getUserByUsername(registerDTO.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        BeanUtil.copyProperties(registerDTO, user);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setStatus(1);
        user.setRoles("ROLE_USER");
        user.setRole(0);
        user.setDeleted(0);
        save(user);
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        User user = getUserByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        Integer role = user.getRole() != null ? user.getRole() : 0;
        String token = jwtUtils.generateToken(user.getUsername(), user.getId(), role, user.getRoles());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setRole(role);
        return loginVO;
    }

    @Override
    public UserVO getCurrentUserInfo(String username) {
        User user = getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public IPage<UserVO> getUserPage(int pageNum, int pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> userPage = page(page, wrapper);
        return userPage.convert(user -> {
            UserVO vo = new UserVO();
            BeanUtil.copyProperties(user, vo);
            return vo;
        });
    }

    @Override
    public void updateUserRole(RoleUpdateDTO roleUpdateDTO) {
        User user = getUserByUsername(roleUpdateDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 优先使用新的整数角色字段
        if (roleUpdateDTO.getRole() != null) {
            user.setRole(roleUpdateDTO.getRole());
            // 同步更新旧的 roles 字符串字段以保持兼容
            switch (roleUpdateDTO.getRole()) {
                case 2: user.setRoles("ROLE_USER,ROLE_ADMIN"); break;
                case 1: user.setRoles("ROLE_USER,ROLE_CREATOR"); break;
                default: user.setRoles("ROLE_USER"); break;
            }
        } else if (roleUpdateDTO.getRoles() != null) {
            // 兼容旧的字符串 role 更新
            user.setRoles(roleUpdateDTO.getRoles());
        }
        updateById(user);
    }

    @Override
    public void updateUserProfile(Long userId, UserProfileDTO profileDTO) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 只更新非空字段
        if (profileDTO.getNickname() != null) {
            user.setNickname(profileDTO.getNickname());
        }
        if (profileDTO.getEmail() != null) {
            user.setEmail(profileDTO.getEmail());
        }
        if (profileDTO.getPhone() != null) {
            user.setPhone(profileDTO.getPhone());
        }
        if (profileDTO.getAvatar() != null) {
            user.setAvatar(profileDTO.getAvatar());
        }

        updateById(user);
    }
}
