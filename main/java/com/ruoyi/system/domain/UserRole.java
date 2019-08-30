package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 用户和角色关联表 sys_user_role
 * 
 * @author ruoyi
 * @date 2019-08-23
 */
public class UserRole extends BaseEntity
{
	private static final long serialVersionUID = 1L;
	
	/** 用户ID */
	private Long userId;
	/** 角色ID */
	private Long roleId;

	public void setUserId(Long userId) 
	{
		this.userId = userId;
	}

	public Long getUserId() 
	{
		return userId;
	}
	public void setRoleId(Long roleId) 
	{
		this.roleId = roleId;
	}

	public Long getRoleId() 
	{
		return roleId;
	}

    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("userId", getUserId())
            .append("roleId", getRoleId())
            .toString();
    }
}
