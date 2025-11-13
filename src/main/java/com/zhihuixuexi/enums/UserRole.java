package com.zhihuixuexi.enums;

/**
 * 用户角色枚举
 */
public enum UserRole {
    /**
     * 学生
     */
    STUDENT("学生"),
    
    /**
     * 培训机构
     */
    INSTITUTION("培训机构"),
    
    /**
     * 管理员
     */
    ADMIN("管理员");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
