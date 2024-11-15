package com.hibob.anyim.groupmng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hibob.anyim.groupmng.entity.GroupInfo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface GroupInfoMapper extends BaseMapper<GroupInfo> {

    @Select("SELECT t1.*, t2.role as my_role FROM anyim_group_info t1 " +
            " INNER JOIN anyim_group_member t2 " +
            " ON t1.group_id = t2.group_id AND t2.account = #{account}")
    List<GroupInfo> selectGroupList(String account);

    @Select("SELECT t1.*, t2.role as my_role FROM anyim_group_info t1 " +
            " INNER JOIN anyim_group_member t2 " +
            " ON t1.group_id = t2.group_id AND t2.account = #{account} AND t1.group_id = #{groupId} limit 1")
    GroupInfo selectGroupInfoOne(String account, String groupId);
}
