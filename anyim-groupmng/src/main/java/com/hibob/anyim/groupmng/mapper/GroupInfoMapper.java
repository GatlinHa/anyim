package com.hibob.anyim.groupmng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hibob.anyim.groupmng.entity.GroupInfo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface GroupInfoMapper extends BaseMapper<GroupInfo> {

    @Select("SELECT t1.*, t2.role as my_role FROM anyim_group_info t1 " +
            " INNER JOIN anyim_group_member t2 " +
            " ON t1.group_id = t2.group_id AND t2.account = #{account}  AND t2.in_status = 0")
    List<GroupInfo> selectGroupList(String account);

    @Select("SELECT t1.*, t2.role as my_role FROM anyim_group_info t1 " +
            " INNER JOIN anyim_group_member t2 " +
            " ON t1.group_id = t2.group_id AND t2.account = #{account} AND t1.group_id = #{groupId} AND t2.in_status = 0 limit 1")
    GroupInfo selectGroupInfoOne(String account, String groupId);

    @Select("select * from anyim_group_info " +
            " where group_id in (SELECT group_id FROM anyim_group_member WHERE account = #{account} AND in_status = 0)" +
            " and (group_id = #{searchKey} or group_name like CONCAT('%', #{searchKey}, '%'))")
    List<GroupInfo> searchGroupInfo(String account, String searchKey);
}
