package com.fanfaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfaction.entity.CreatorApplication;
import com.fanfaction.entity.User;
import com.fanfaction.mapper.CreatorApplicationMapper;
import com.fanfaction.mapper.UserMapper;
import com.fanfaction.service.CreatorApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创作者申请服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreatorApplicationServiceImpl implements CreatorApplicationService {
    
    private final CreatorApplicationMapper creatorApplicationMapper;
    private final UserMapper userMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitApplication(CreatorApplication application) {
        // 检查是否已申请
        LambdaQueryWrapper<CreatorApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreatorApplication::getUserId, application.getUserId());
        CreatorApplication existing = creatorApplicationMapper.selectOne(wrapper);
        
        if (existing != null) {
            throw new RuntimeException("您已经提交过申请，请勿重复提交");
        }
        
        // 设置默认状态：审核中
        application.setStatus(0);
        creatorApplicationMapper.insert(application);
        
        // 更新用户的 creatorStatus 为审核中
        User user = userMapper.selectById(application.getUserId());
        if (user != null) {
            user.setCreatorStatus(1); // 1-审核中
            userMapper.updateById(user);
        }
        
        log.info("用户 {} 提交创作者申请，笔名：{}, creatorStatus=1", application.getUserId(), application.getPenName());
    }
    
    @Override
    public CreatorApplication getByUserId(Long userId) {
        LambdaQueryWrapper<CreatorApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreatorApplication::getUserId, userId);
        return creatorApplicationMapper.selectOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewApplication(Long id, Integer status, String comment, Long reviewerId) {
        CreatorApplication application = creatorApplicationMapper.selectById(id);
        if (application == null) {
            throw new RuntimeException("申请不存在");
        }
        
        application.setStatus(status);
        application.setReviewComment(comment);
        application.setReviewerId(reviewerId);
        application.setReviewTime(LocalDateTime.now());
        
        creatorApplicationMapper.updateById(application);
        
        // 如果审核通过，更新用户角色为 CREATOR，并更新 creatorStatus 和 role
        if (status == 1) { // 1 表示通过
            User user = userMapper.selectById(application.getUserId());
            if (user != null) {
                // 添加 CREATOR 角色，保留原有角色
                String roles = user.getRoles();
                if (!roles.contains("ROLE_CREATOR")) {
                    roles = roles + ",ROLE_CREATOR";
                }
                user.setRoles(roles);
                // 更新创作者状态为已通过
                user.setCreatorStatus(2); // 2-已通过
                // 更新整数角色字段为创作者
                user.setRole(1); // 1-创作者
                userMapper.updateById(user);
                log.info("用户 {} 审核通过，role=1, creatorStatus=2, roles={}", application.getUserId(), roles);
            }
        } else if (status == 2) { // 2 表示拒绝
            // 更新用户创作者状态为已拒绝
            User user = userMapper.selectById(application.getUserId());
            if (user != null) {
                user.setCreatorStatus(3); // 3-已拒绝
                userMapper.updateById(user);
            }
            log.info("用户 {} 审核被拒绝，原因：{}, creatorStatus=3", application.getUserId(), comment);
        }
        
        log.info("审核员 {} 审核申请 {}，状态：{}, 意见：{}", reviewerId, id, status, comment);
    }
    
    @Override
    public List<CreatorApplication> getApplicationList(Integer status) {
        LambdaQueryWrapper<CreatorApplication> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(CreatorApplication::getStatus, status);
        }
        wrapper.orderByDesc(CreatorApplication::getCreateTime);
        return creatorApplicationMapper.selectList(wrapper);
    }
}
