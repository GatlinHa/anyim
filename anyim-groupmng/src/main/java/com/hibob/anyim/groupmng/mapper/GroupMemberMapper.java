package com.hibob.anyim.groupmng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hibob.anyim.groupmng.entity.GroupMember;

import java.util.Collection;

public interface GroupMemberMapper extends BaseMapper<GroupMember> {

    int insertBatchSomeColumn(Collection<GroupMember> entityList);

}
