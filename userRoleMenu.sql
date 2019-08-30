-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, url,menu_type, visible, perms, icon, create_by, create_time, update_by, update_time, remark)
values('用户和角色关联', '3', '1', '/system/userRole', 'C', '0', 'system:userRole:view', '#', 'admin', '2018-03-01', 'ry', '2018-03-01', '用户和角色关联菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu  (menu_name, parent_id, order_num, url,menu_type, visible, perms, icon, create_by, create_time, update_by, update_time, remark)
values('用户和角色关联查询', @parentId, '1',  '#',  'F', '0', 'system:userRole:list',         '#', 'admin', '2018-03-01', 'ry', '2018-03-01', '');

insert into sys_menu  (menu_name, parent_id, order_num, url,menu_type, visible, perms, icon, create_by, create_time, update_by, update_time, remark)
values('用户和角色关联新增', @parentId, '2',  '#',  'F', '0', 'system:userRole:add',          '#', 'admin', '2018-03-01', 'ry', '2018-03-01', '');

insert into sys_menu  (menu_name, parent_id, order_num, url,menu_type, visible, perms, icon, create_by, create_time, update_by, update_time, remark)
values('用户和角色关联修改', @parentId, '3',  '#',  'F', '0', 'system:userRole:edit',         '#', 'admin', '2018-03-01', 'ry', '2018-03-01', '');

insert into sys_menu  (menu_name, parent_id, order_num, url,menu_type, visible, perms, icon, create_by, create_time, update_by, update_time, remark)
values('用户和角色关联删除', @parentId, '4',  '#',  'F', '0', 'system:userRole:remove',       '#', 'admin', '2018-03-01', 'ry', '2018-03-01', '');
