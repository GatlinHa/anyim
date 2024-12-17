package com.hibob.anyim.groupmng.rpc;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hibob.anyim.common.rpc.service.GroupMngRpcService;
import com.hibob.anyim.common.utils.BeanUtil;
import com.hibob.anyim.groupmng.entity.GroupInfo;
import com.hibob.anyim.groupmng.entity.GroupMember;
import com.hibob.anyim.groupmng.mapper.GroupInfoMapper;
import com.hibob.anyim.groupmng.mapper.GroupMemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@DubboService
@RequiredArgsConstructor
public class GroupMngRpcServiceImpl implements GroupMngRpcService {

    private final GroupInfoMapper groupInfoMapper;
    private final GroupMemberMapper groupMemberMapper;

    @Override
    public Map<String, Object> queryGroupInfo(String groupId) {
        log.info("GroupMngRpcServiceImpl::queryGroupInfo start......");
        LambdaQueryWrapper<GroupInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GroupInfo::getGroupId, groupId);
        List<GroupInfo> groups = groupInfoMapper.selectList(queryWrapper);
        if (groups.size() > 0) {
            try {
                return BeanUtil.objectToMap(groups.get(0));
            } catch (IllegalAccessException e) {
                log.error("GroupMngRpcServiceImpl::queryGroupInfo type conversion error......exception: {}", e.getMessage());
            }
        }
        return null;
    }

    @Override
    public Map<String, Map<String, Object>> queryGroupInfoBatch(List<String> groupIdList) {
        log.info("GroupMngRpcServiceImpl::queryGroupInfoBatch start......");
        LambdaQueryWrapper<GroupInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(GroupInfo::getGroupId, groupIdList.toArray());
        List<GroupInfo> groups = groupInfoMapper.selectList(queryWrapper);
        Map<String, Map<String, Object>> result = new HashMap<>();
        try {
            for (GroupInfo item : groups) {
                result.put(item.getGroupId(), BeanUtil.objectToMap(item));
            }
        } catch (IllegalAccessException e) {
            log.error("GroupMngRpcServiceImpl::queryGroupInfoBatch type conversion error......exception: {}", e.getMessage());
        }
        return result;
    }

    @Override
    public List<String> queryGroupMembers(String groupId) {
        LambdaQueryWrapper<GroupMember> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(GroupMember::getAccount);
        queryWrapper.eq(GroupMember::getGroupId, groupId);
        queryWrapper.eq(GroupMember::getInStatus, 0);
        List<String> accounts = new ArrayList<>();
        groupMemberMapper.selectList(queryWrapper).forEach(x -> {
            accounts.add(x.getAccount());
        });
        return accounts;
    }
}
