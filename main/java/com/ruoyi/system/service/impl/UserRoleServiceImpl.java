package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.UserRoleMapper;
import com.ruoyi.system.domain.UserRole;
import com.ruoyi.system.service.IUserRoleService;
import com.ruoyi.common.core.text.Convert;

/**
 * 用户和角色关联 服务层实现
 * 
 * @author ruoyi
 * @date 2019-08-23
 */
@Service
public class UserRoleServiceImpl implements IUserRoleService 
{
	@Autowired
	private UserRoleMapper userRoleMapper;

	/**
     * 查询用户和角色关联信息
     * 
     * @param userId 用户和角色关联ID
     * @return 用户和角色关联信息
     */
    @Override
	public UserRole selectUserRoleById(Long userId)
	{
	    return userRoleMapper.selectUserRoleById(userId);
	}
	
	/**
     * 查询用户和角色关联列表
     * 
     * @param userRole 用户和角色关联信息
     * @return 用户和角色关联集合
     */
	@Override
	public List<UserRole> selectUserRoleList(UserRole userRole)
	{
	    return userRoleMapper.selectUserRoleList(userRole);
	}
	
    /**
     * 新增用户和角色关联
     * 
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
	@Override
	public int insertUserRole(UserRole userRole)
	{
	    return userRoleMapper.insertUserRole(userRole);
	}
	
	/**
     * 修改用户和角色关联
     * 
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
	@Override
	public int updateUserRole(UserRole userRole)
	{
	    return userRoleMapper.updateUserRole(userRole);
	}

	/**
     * 删除用户和角色关联对象
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
	@Override
	public int deleteUserRoleByIds(String ids)
	{
		return userRoleMapper.deleteUserRoleByIds(Convert.toStrArray(ids));
	}
	
}
