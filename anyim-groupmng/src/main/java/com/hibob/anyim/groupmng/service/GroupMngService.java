package com.hibob.anyim.groupmng.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hibob.anyim.common.enums.ServiceErrorCode;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.session.ReqSession;
import com.hibob.anyim.common.utils.ResultUtil;
import com.hibob.anyim.common.utils.SnowflakeId;
import com.hibob.anyim.groupmng.dao.request.*;
import com.hibob.anyim.groupmng.dao.vo.GroupVO;
import com.hibob.anyim.groupmng.entity.GroupInfo;
import com.hibob.anyim.groupmng.entity.GroupMember;
import com.hibob.anyim.groupmng.mapper.GroupInfoMapper;
import com.hibob.anyim.groupmng.mapper.GroupMemberMapper;
import com.hibob.anyim.groupmng.rpc.RpcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
        long groupId = generateGroupId();
        int groupType = dto.getGroupType();
        String groupName = dto.getGroupName();
        String announcement = dto.getAnnouncement();
        String avatar = dto.getAvatar();
        String avatarThumb = avatar; //TODO 生成头像缩略图

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(groupId);
        groupInfo.setGroupType(groupType);
        groupInfo.setGroupName(groupName);
        groupInfo.setAnnouncement(announcement);
        groupInfo.setAvatar(avatar);
        groupInfo.setAvatarThumb(avatarThumb);

        List<GroupMember> members = new ArrayList<>();
        for (Map<String, Object> map : dto.getMembers()) {
            String memberAccount = (String)map.get("memberAccount");
            Map<String, Object> user = rpcClient.getUserRpcService().queryUserInfo(memberAccount);
            if (user != null) {
                GroupMember member = new GroupMember();
                member.setGroupId(groupId);
                member.setMemberAccount(memberAccount);
                member.setMemberNickName((String) user.get("nickName"));
                member.setMemberAvatarThumb((String) user.get("avatarThumb"));
                member.setMemberRole((Integer) map.get("memberRole"));
                members.add(member);
            }
        }
        if (members.size() < 3) {
            return ResultUtil.error(HttpStatus.OK,
                    ServiceErrorCode.ERROR_GROUP_MNG_CREATE_GROUP_NOT_ENOUGH.code(),
                    ServiceErrorCode.ERROR_GROUP_MNG_CREATE_GROUP_NOT_ENOUGH.desc());
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

        LambdaQueryWrapper<GroupMember> groupMemberWrapper = new LambdaQueryWrapper<>();
        groupMemberWrapper.select(GroupMember::getGroupId);
        groupMemberWrapper.eq(GroupMember::getMemberAccount, account);
        groupMemberWrapper.groupBy(GroupMember::getGroupId);
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
        long groupId = dto.getGroupId();
        String account = ReqSession.getSession().getAccount();

        // 校验这个用户是否在这个群里
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getMemberAccount, account);
        Long count = groupMemberMapper.selectCount(queryWrapper);
        if (count == 0) {
            return ResultUtil.error(HttpStatus.OK,
                    ServiceErrorCode.ERROR_GROUP_MNG_QUERY_GROUP_NOT_IN_GROUP.code(),
                    ServiceErrorCode.ERROR_GROUP_MNG_QUERY_GROUP_NOT_IN_GROUP.desc());
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

    public ResponseEntity<IMHttpResponse> modifyGroup(ModifyGroupReq dto) {
        log.info("GroupMngService::modifyGroup");
        String announcement = dto.getAnnouncement();
        String groupName = dto.getGroupName();
        String avatar = dto.getAvatar();
        if (!StringUtils.hasLength(announcement)
                && StringUtils.hasLength(groupName)
                && StringUtils.hasLength(avatar)) {
            return ResultUtil.error(HttpStatus.OK,
                    ServiceErrorCode.ERROR_GROUP_MNG_MODIFY_GROUP_EMPTY_PARAM.code(),
                    ServiceErrorCode.ERROR_GROUP_MNG_MODIFY_GROUP_EMPTY_PARAM.desc());
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
        long groupId = dto.getGroupId();
        String account = ReqSession.getSession().getAccount();

        // 校验这个用户是是不是这个群组的owner
        LambdaQueryWrapper<GroupMember> groupMemberWrapper = new LambdaQueryWrapper<>();
        groupMemberWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getMemberAccount, account)
                .eq(GroupMember::getMemberRole, 3);
        Long count = groupMemberMapper.selectCount(groupMemberWrapper);
        if (count == 0) {
            return ResultUtil.error(HttpStatus.OK,
                    ServiceErrorCode.ERROR_GROUP_MNG_DEL_GROUP_USER_INVALID.code(),
                    ServiceErrorCode.ERROR_GROUP_MNG_DEL_GROUP_USER_INVALID.desc());
        }

        groupMemberWrapper.clear();
        groupMemberWrapper.eq(GroupMember::getGroupId, groupId);
        groupMemberMapper.delete(groupMemberWrapper);

        LambdaQueryWrapper<GroupInfo> groupInfoWrapper = new LambdaQueryWrapper<>();
        groupInfoWrapper.eq(GroupInfo::getGroupId, groupId);
        groupInfoMapper.delete(groupInfoWrapper);

        // TODO 调用RPC请求通知chat删除群组的聊天记录

        return ResultUtil.success();
    }

    public ResponseEntity<IMHttpResponse> changeMembers(ChangeMembersReq dto) {
        log.info("GroupMngService::changeMembers");
        long groupId = dto.getGroupId();
        List<GroupMember> addMembers = new ArrayList<>();

        for (Map<String, Object> map : dto.getAddMembers()) {
            String memberAccount = (String)map.get("memberAccount");
            Map<String, Object> user = rpcClient.getUserRpcService().queryUserInfo(memberAccount);
            if (user != null) {
                GroupMember member = new GroupMember();
                member.setGroupId(groupId);
                member.setMemberAccount(memberAccount);
                member.setMemberNickName((String) user.get("nickName"));
                member.setMemberAvatarThumb((String) user.get("avatarThumb"));
                member.setMemberRole((Integer) map.get("memberRole"));
                addMembers.add(member);
            }
        }

        groupMemberMapper.insertBatchSomeColumn(addMembers);
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId);
        queryWrapper.in(GroupMember::getMemberAccount, dto.getDelMembers().stream().map(map -> map.get("memberAccount")).toArray());
        groupMemberMapper.delete(queryWrapper);

        return ResultUtil.success();
    }

    public ResponseEntity<IMHttpResponse> changeRole(ChangeRoleReq dto) {
        log.info("GroupMngService::changeRole");
        long groupId = dto.getGroupId();
        String memberAccount = dto.getMemberAccount();
        int memberRole = dto.getMemberRole();

        LambdaUpdateWrapper<GroupMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getMemberAccount, memberAccount);
        updateWrapper.set(GroupMember::getMemberRole, memberRole);
        int update = groupMemberMapper.update(updateWrapper);
        if (update == 0) {
            return ResultUtil.error(HttpStatus.OK,
                    ServiceErrorCode.ERROR_GROUP_MNG_CHANGE_ROLE_NOT_IN_GROUP.code(),
                    ServiceErrorCode.ERROR_GROUP_MNG_CHANGE_ROLE_NOT_IN_GROUP.desc());
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
