package com.fanfaction.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fanfaction.entity.User;
import com.fanfaction.mapper.UserMapper;
import com.fanfaction.vo.AdminUserVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserService.class);
    private static final String USER_BLOCKED_KEY = "user:blocked:";

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    public Page<AdminUserVO> getUserList(int pageNum, int pageSize, String role, String status, String keyword) {
        logger.info("管理端用户列表查询 - pageNum: {}, pageSize: {}, role: {}, status: {}, keyword: {}",
                pageNum, pageSize, role, status, keyword);

        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(role)) {
            int roleVal = parseRoleStr(role);
            wrapper.eq(User::getRole, roleVal);
        }
        if (StringUtils.hasText(status)) {
            int statusVal = parseStatusStr(status);
            wrapper.eq(User::getStatus, statusVal);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getNickname, keyword).or().like(User::getUsername, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> userPage = userMapper.selectPage(page, wrapper);

        Page<AdminUserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());

        List<AdminUserVO> voList = userPage.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        voPage.setRecords(voList);

        logger.info("管理端用户列表查询成功 - 总记录数: {}, 当前页记录数: {}", voPage.getTotal(), voList.size());
        return voPage;
    }

    @Transactional
    public boolean updateUserStatus(Long userId, String status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        int statusVal = parseStatusStr(status);
        user.setStatus(statusVal);
        int rows = userMapper.updateById(user);
        logger.info("管理端修改用户状态 - userId: {}, 新状态: {} ({})", userId, status, statusVal);
        
        // 清除 Redis 缓存，使封禁状态立即生效
        if (rows > 0 && user.getUsername() != null) {
            String cacheKey = USER_BLOCKED_KEY + user.getUsername();
            redisTemplate.delete(cacheKey);
            logger.info("已清除用户封禁状态缓存 - username: {}", user.getUsername());
        }
        
        return rows > 0;
    }

    @Transactional
    public boolean updateUserRole(Long userId, String role) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        int roleVal = parseRoleStr(role);
        user.setRole(roleVal);
        
        // 同步更新 roles 字符串字段，确保 Spring Security 权限正确
        switch (roleVal) {
            case 2:
                user.setRoles("ROLE_USER,ROLE_ADMIN");
                break;
            case 1:
                user.setRoles("ROLE_USER,ROLE_CREATOR");
                break;
            default:
                user.setRoles("ROLE_USER");
                break;
        }
        
        int rows = userMapper.updateById(user);
        logger.info("管理端修改用户角色 - userId: {}, 新角色: {} ({}), roles: {}", 
                userId, role, roleVal, user.getRoles());
        
        // 清除 Redis 缓存，确保缓存与数据库一致
        if (rows > 0 && user.getUsername() != null) {
            String cacheKey = USER_BLOCKED_KEY + user.getUsername();
            redisTemplate.delete(cacheKey);
            logger.info("已清除用户缓存 - username: {}", user.getUsername());
        }
        
        return rows > 0;
    }

    private AdminUserVO toVO(User user) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setRole(toRoleStr(user.getRole()));
        vo.setStatus(toStatusStr(user.getStatus()));
        vo.setCreatorStatus(toCreatorStatusStr(user.getCreatorStatus()));
        vo.setRegisterTime(user.getCreateTime());
        return vo;
    }

    private String toRoleStr(Integer role) {
        if (role == null) return "READER";
        switch (role) {
            case 2: return "ADMIN";
            case 1: return "CREATOR";
            default: return "READER";
        }
    }

    private String toStatusStr(Integer status) {
        if (status == null) return "BLOCKED";
        return status == 1 ? "ACTIVE" : "BLOCKED";
    }

    private String toCreatorStatusStr(Integer cs) {
        if (cs == null) return "NONE";
        switch (cs) {
            case 1: return "PENDING";
            case 2: return "APPROVED";
            case 3: return "REJECTED";
            default: return "NONE";
        }
    }

    private int parseRoleStr(String role) {
        if (role == null) return 0;
        switch (role.toUpperCase()) {
            case "ADMIN": return 2;
            case "CREATOR": return 1;
            default: return 0;
        }
    }

    private int parseStatusStr(String status) {
        if (status == null) return 1;
        return "ACTIVE".equalsIgnoreCase(status) ? 1 : 0;
    }
}
