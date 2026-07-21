package com.fanfaction.service;

import com.fanfaction.entity.CreatorApplication;

import java.util.List;

/**
 * 创作者申请服务接口
 */
public interface CreatorApplicationService {
    
    /**
     * 提交创作者申请
     * @param application 申请信息
     */
    void submitApplication(CreatorApplication application);
    
    /**
     * 获取用户的申请信息
     * @param userId 用户 ID
     * @return 申请信息
     */
    CreatorApplication getByUserId(Long userId);
    
    /**
     * 审核申请
     * @param id 申请 ID
     * @param status 审核状态
     * @param comment 审核意见
     * @param reviewerId 审核人 ID
     */
    void reviewApplication(Long id, Integer status, String comment, Long reviewerId);
    
    /**
     * 获取申请列表（管理员用）
     * @param status 审核状态筛选（可选）
     * @return 申请列表
     */
    List<CreatorApplication> getApplicationList(Integer status);
}
