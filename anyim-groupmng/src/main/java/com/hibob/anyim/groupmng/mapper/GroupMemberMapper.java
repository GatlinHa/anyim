package com.hibob.anyim.groupmng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hibob.anyim.groupmng.entity.GroupMember;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

public interface GroupMemberMapper extends BaseMapper<GroupMember> {

    int insertBatchSomeColumn(Collection<GroupMember> entityList);

    @Select("select * from anyim_group_member " +
            " where group_id in (SELECT group_id FROM anyim_group_member WHERE account = #{account}) " +
            " and (account = #{searchKey} or nick_name like CONCAT('%', #{searchKey}, '%'))")
    List<GroupMember> selectGroupByMember(String account, String searchKey);

}
