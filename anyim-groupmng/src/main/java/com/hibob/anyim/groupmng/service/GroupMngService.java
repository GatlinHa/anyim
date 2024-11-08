package com.hibob.anyim.groupmng.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hibob.anyim.common.enums.ServiceErrorCode;
import com.hibob.anyim.common.exception.ServiceException;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.rpc.client.RpcClient;
import com.hibob.anyim.common.session.ReqSession;
import com.hibob.anyim.common.utils.ResultUtil;
import com.hibob.anyim.common.utils.SnowflakeId;
import com.hibob.anyim.groupmng.dao.request.*;
import com.hibob.anyim.groupmng.dao.vo.GroupVO;
import com.hibob.anyim.groupmng.entity.GroupInfo;
import com.hibob.anyim.groupmng.entity.GroupMember;
import com.hibob.anyim.groupmng.mapper.GroupInfoMapper;
import com.hibob.anyim.groupmng.mapper.GroupMemberMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupMngService {
    private final GroupInfoMapper groupInfoMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final RpcClient rpcClient;
    private SnowflakeId snowflakeId = null;

    @Transactional
    public ResponseEntity<IMHttpResponse> createGroup(CreateGroupReq dto) {
        log.info("GroupMngService::createGroup");
        ReqSession reqSession = ReqSession.getSession();
        String account = reqSession.getAccount();
        String groupId = String.valueOf(generateGroupId());
        int groupType = dto.getGroupType();
        String groupName = dto.getGroupName();
        String announcement = ""; //创建时，不带公告
        String avatar = ""; //创建时，不带群头像
        String avatarThumb = ""; //创建时，不带群头像缩略图
        boolean historyBrowse = false; //创建时，默认新成员不能查看历史消息
        boolean muted = false; //创建时，默认非全场静音
        String creator = account;

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(groupId);
        groupInfo.setGroupType(groupType);
        groupInfo.setGroupName(groupName);
        groupInfo.setAnnouncement(announcement);
        groupInfo.setAvatar(avatar);
        groupInfo.setAvatarThumb(avatarThumb);
        groupInfo.setHistoryBrowse(historyBrowse);
        groupInfo.setMuted(muted);
        groupInfo.setCreator(creator);

        List<GroupMember> members = new ArrayList<>();
        List<String> accounts = dto.getAccounts();
        accounts.add(account);
        Map<String, Map<String, Object>> usersMap = rpcClient.getUserRpcService().queryUserInfoBatch(accounts);
        for (Map.Entry entry: usersMap.entrySet()) {
            Map value = (Map) entry.getValue();
            GroupMember member = new GroupMember();
            member.setGroupId(groupId);
            member.setMemberAccount((String) value.get("account"));
            member.setMemberNickName((String) value.get("nickName"));
            if (account.equals(value.get("account"))) {
                member.setMemberRole(2);
            }
            else {
                member.setMemberRole(0);
            }
            members.add(member);
        }

        if (members.size() < 3) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_NOT_ENOUGH_MEMBER);
        }

        groupInfoMapper.insert(groupInfo);
        groupMemberMapper.insertBatchSomeColumn(members);

        GroupVO vo = new GroupVO();
        vo.setGroupInfo(groupInfo);
        vo.setMembers(members);
        return ResultUtil.success(vo);
    }

    public ResponseEntity<IMHttpResponse> queryGroupList(QueryGroupListReq dto) {
        log.info("GroupMngService::queryGroupList");
        String account = ReqSession.getSession().getAccount();
        List<Integer> roleList = dto.getRoleList();

        LambdaQueryWrapper<GroupMember> groupMemberWrapper = new LambdaQueryWrapper<>();
        groupMemberWrapper.select(GroupMember::getGroupId);
        groupMemberWrapper.eq(GroupMember::getMemberAccount, account);
        if (roleList != null && !roleList.isEmpty()) {
            groupMemberWrapper.in(GroupMember::getMemberRole, roleList);
        }
        List<GroupMember> groupIds = groupMemberMapper.selectList(groupMemberWrapper);

        if (groupIds.isEmpty()) {
            return ResultUtil.success();
        }

        LambdaQueryWrapper<GroupInfo> groupInfoWrapper = new LambdaQueryWrapper<>();
        groupInfoWrapper.in(GroupInfo::getGroupId, groupIds.stream().map(member -> member.getGroupId()).toArray());
        List<GroupInfo> groupInfos = groupInfoMapper.selectList(groupInfoWrapper);

        return ResultUtil.success(groupInfos);
    }

    public ResponseEntity<IMHttpResponse> queryGroupInfo(QueryGroupInfoReq dto) {
        log.info("GroupMngService::queryGroupInfo");
        String groupId = dto.getGroupId();
        String account = ReqSession.getSession().getAccount();

        // 校验这个用户是否在这个群里
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getMemberAccount, account);
        Long count = groupMemberMapper.selectCount(queryWrapper);
        if (count == 0) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_NOT_IN_GROUP);
        }

        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);

        queryWrapper.clear();
        queryWrapper.eq(GroupMember::getGroupId, groupId);
        List<GroupMember> members = groupMemberMapper.selectList(queryWrapper);

        GroupVO vo = new GroupVO();
        vo.setGroupInfo(groupInfo);
        vo.setMembers(members);
        return ResultUtil.success(vo);
    }

    public ResponseEntity<IMHttpResponse> searchGroupByMember(SearchGroupByMemberReq dto) {
        log.info("GroupMngService::searchGroupByMember");
        String account = ReqSession.getSession().getAccount();
        String searchKey = dto.getSearchKey();
        List<GroupMember> members = groupMemberMapper.selectGroupByMember(account, searchKey);
        return ResultUtil.success(members);
    }

    public ResponseEntity<IMHttpResponse> modifyGroup(ModifyGroupReq dto) {
        log.info("GroupMngService::modifyGroup");
        String announcement = dto.getAnnouncement();
        String groupName = dto.getGroupName();
        String avatar = dto.getAvatar();
        if (!StringUtils.hasLength(announcement)
                && !StringUtils.hasLength(groupName)
                && !StringUtils.hasLength(avatar)) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_EMPTY_PARAM);
        }

        LambdaUpdateWrapper<GroupInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupInfo::getGroupId, dto.getGroupId());
        if (StringUtils.hasLength(announcement)) {
            updateWrapper.set(GroupInfo::getAnnouncement, announcement);
        }
        if (StringUtils.hasLength(groupName)) {
            updateWrapper.set(GroupInfo::getGroupName, groupName);
        }
        if (StringUtils.hasLength(avatar)) {
            updateWrapper.set(GroupInfo::getAvatar, avatar);
        }
        groupInfoMapper.update(updateWrapper);
        return ResultUtil.success();
    }

    @Transactional
    public ResponseEntity<IMHttpResponse> delGroup(DelGroupReq dto) {
        log.info("GroupMngService::delGroup");
        String groupId = dto.getGroupId();
        String account = ReqSession.getSession().getAccount();

        // 校验这个用户是是不是这个群组的owner
        LambdaQueryWrapper<GroupMember> groupMemberWrapper = new LambdaQueryWrapper<>();
        groupMemberWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getMemberAccount, account)
                .eq(GroupMember::getMemberRole, 3);
        Long count = groupMemberMapper.selectCount(groupMemberWrapper);
        if (count == 0) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_DEL_GROUP_USER_INVALID);
        }

        groupMemberWrapper.clear();
        groupMemberWrapper.eq(GroupMember::getGroupId, groupId);
        groupMemberMapper.delete(groupMemberWrapper);

        LambdaQueryWrapper<GroupInfo> groupInfoWrapper = new LambdaQueryWrapper<>();
        groupInfoWrapper.eq(GroupInfo::getGroupId, groupId);
        groupInfoMapper.delete(groupInfoWrapper);

        // 这里不删除群组的聊天记录，等待自然老化

        return ResultUtil.success();
    }

    public ResponseEntity<IMHttpResponse> changeMembers(ChangeMembersReq dto) {
        log.info("GroupMngService::changeMembers");
        String groupId = dto.getGroupId();
        List<GroupMember> addMembers = new ArrayList<>();

        for (Map<String, Object> map : dto.getAddMembers()) {
            String memberAccount = (String)map.get("memberAccount");
            Map<String, Object> user = rpcClient.getUserRpcService().queryUserInfo(memberAccount);
            if (user != null) {
                GroupMember member = new GroupMember();
                member.setGroupId(String.valueOf(groupId));
                member.setMemberAccount(memberAccount);
                member.setMemberNickName((String) user.get("nickName"));
                member.setMemberRole((Integer) map.get("memberRole"));
                addMembers.add(member);
            }
        }

        if (!addMembers.isEmpty()) {
            groupMemberMapper.insertBatchSomeColumn(addMembers);
        }

        if (!dto.getDelMembers().isEmpty()) {
            LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GroupMember::getGroupId, groupId);
            queryWrapper.ne(GroupMember::getMemberRole, 3); // 群主不能直接删除
            queryWrapper.in(GroupMember::getMemberAccount, dto.getDelMembers().stream().map(map -> map.get("memberAccount")).toArray());
            groupMemberMapper.delete(queryWrapper);
        }

        return ResultUtil.success();
    }

    public ResponseEntity<IMHttpResponse> changeRole(ChangeRoleReq dto) {
        log.info("GroupMngService::changeRole");
        String groupId = dto.getGroupId();
        String memberAccount = dto.getMemberAccount();
        int memberRole = dto.getMemberRole();

        LambdaUpdateWrapper<GroupMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getMemberAccount, memberAccount);
        updateWrapper.set(GroupMember::getMemberRole, memberRole);
        int update = groupMemberMapper.update(updateWrapper);
        if (update == 0) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_NOT_IN_GROUP);
        }

        return ResultUtil.success();
    }

    @Transactional
    public ResponseEntity<IMHttpResponse> ownerTransfer(OwnerTransferReq dto) {
        log.info("GroupMngService::changeRole");
        String localAccount = ReqSession.getSession().getAccount();
        String groupId = dto.getGroupId();
        String account = dto.getAccount();

        // 校验这个用户是是不是这个群组的owner
        LambdaQueryWrapper<GroupMember> groupMemberWrapper = new LambdaQueryWrapper<>();
        groupMemberWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getMemberAccount, localAccount)
                .eq(GroupMember::getMemberRole, 3);
        Long count = groupMemberMapper.selectCount(groupMemberWrapper);
        if (count == 0) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_NOT_OWNER);
        }

        // 校验新群主候选人是否在这个群组中
        groupMemberWrapper.clear();
        groupMemberWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getMemberAccount, account)
                .ne(GroupMember::getMemberRole, 3);
        count = groupMemberMapper.selectCount(groupMemberWrapper);
        if (count == 0) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_NEW_OWNER_NOT_IN_GROUP);
        }

        LambdaUpdateWrapper<GroupMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getMemberAccount, localAccount)
                .set(GroupMember::getMemberRole, 2); // 降级为超级管理员
        if (groupMemberMapper.update(updateWrapper) > 0) {
            updateWrapper.clear();
            updateWrapper.eq(GroupMember::getGroupId, groupId)
                    .eq(GroupMember::getMemberAccount, account)
                    .set(GroupMember::getMemberRole, 3); // 升级为群主
            int update = groupMemberMapper.update(updateWrapper);
            if (update == 0) {
                // 更新失败，需要根据事务回滚
                throw new ServiceException(ServiceErrorCode.ERROR_GROUP_MNG_OWNER_TRANSFER_EXCEPTION);
            }
        }

        return ResultUtil.success();
    }

    private long generateGroupId() {
        if (snowflakeId == null) { // 懒加载
            snowflakeId = SnowflakeId.getInstance();
        }
        return snowflakeId.nextId();
    }
}
