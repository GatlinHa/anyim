package com.hibob.anyim.groupmng.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hibob.anyim.common.enums.ServiceErrorCode;
import com.hibob.anyim.common.exception.ServiceException;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.protobuf.MsgType;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupMngService {
    private final GroupInfoMapper groupInfoMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final RpcClient rpcClient;
    private SnowflakeId snowflakeId = null;

    /**
     * 创建群组
     * @param dto 群组名称, 群组类型, 群组成员的账号数组
     * @return 群组信息, 不返回成员基本信息
     */
    @Transactional
    public ResponseEntity<IMHttpResponse> createGroup(CreateGroupReq dto) {
        log.info("GroupMngService::createGroup");
        if (dto.getMembers().size() < 3) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_NOT_ENOUGH_MEMBER);
        }

        ReqSession reqSession = ReqSession.getSession();
        String account = reqSession.getAccount();
        String groupId = String.valueOf(generateGroupId());
        int groupType = dto.getGroupType();
        String groupName = dto.getGroupName();
        String announcement = ""; //创建时，不带公告
        String avatar = ""; //创建时，不带群头像
        String avatarThumb = ""; //创建时，不带群头像缩略图
        boolean historyBrowse = false; //创建时，默认新成员不能查看历史消息
        boolean allMuted = false; //创建时，默认非全场静音
        boolean joinGroupApproval = false; //创建时，默认全员可以邀请新成员
        String creator = account;

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(groupId);
        groupInfo.setGroupType(groupType);
        groupInfo.setGroupName(groupName);
        groupInfo.setAnnouncement(announcement);
        groupInfo.setAvatar(avatar);
        groupInfo.setAvatarThumb(avatarThumb);
        groupInfo.setHistoryBrowse(historyBrowse);
        groupInfo.setAllMuted(allMuted);
        groupInfo.setJoinGroupApproval(joinGroupApproval);
        groupInfo.setCreator(creator);
        groupInfo.setMyRole(2); //不是数据库字段,加了注解不会插入进去,这里是为了返回结果
        groupInfoMapper.insert(groupInfo);

        List<Map<String, Object>> insertMemberList = new ArrayList<>();
        List<Map<String, Object>> insertSessionList = new ArrayList<>();
        String creatorNickName = "";
        for (Map<String, Object> item: dto.getMembers()) {
            Map<String, Object> memberMap = new HashMap<>();
            memberMap.put("groupId", groupId);
            memberMap.put("account", item.get("account"));
            memberMap.put("nickName", item.get("nickName"));
            memberMap.put("role", item.get("account").toString().equals(account) ? 2 : 0);
            memberMap.put("mutedMode", 0);
            memberMap.put("inStatus", 0);
            insertMemberList.add(memberMap);

            Map<String, Object> sessionMap = new HashMap<>();
            sessionMap.put("account", item.get("account"));
            sessionMap.put("sessionId", groupId);
            sessionMap.put("remoteId", groupId);
            sessionMap.put("sessionType", MsgType.GROUP_CHAT.getNumber());
            insertSessionList.add(sessionMap);

            if (creator.equals(item.get("account"))) {
                creatorNickName = item.get("nickName").toString();
            }
        }

        groupMemberMapper.batchInsertOrUpdate(insertMemberList);
        rpcClient.getChatRpcService().insertGroupSessions(groupId, insertSessionList); // 群组创建成功后, 为所有成员创建session, 向所有成员发送创建新群的系统消息

        Map<String, String> operator = new HashMap<>();
        operator.put("account", creator);
        operator.put("nickName", creatorNickName);
        Map<String, Object> content = new HashMap<>();
        content.put("operator", operator);
        content.put("members", dto.getMembers());

        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("msgType", MsgType.SYS_GROUP_CREATE.getNumber());
        msgMap.put("groupId", groupId);
        msgMap.put("content", content);
        rpcClient.getNettyRpcService().sendSysMsg(msgMap);

        GroupVO vo = new GroupVO();
        vo.setGroupInfo(groupInfo);
        return ResultUtil.success(vo);
    }

    /**
     * 查询某个用户下的所有群组列表
     * @param dto 当前用户账号
     * @return 群组信息列表, 外加当前用户在这个群的role, 不返回成员信息
     */
    public ResponseEntity<IMHttpResponse> queryGroupList(QueryGroupListReq dto) {
        log.info("GroupMngService::queryGroupList");
        String account = ReqSession.getSession().getAccount();
        List<GroupInfo> groupInfos = groupInfoMapper.selectGroupList(account);
        return ResultUtil.success(groupInfos);
    }

    /**
     * 查询某个groupId的所有信息:群基本信息 + (成员在群信息 + 成员基本信息)
     * @param dto 当前用户账号, 目标群组id
     * @return 群基本信息 + (成员在群信息 + 成员基本信息)
     */
    public ResponseEntity<IMHttpResponse> queryGroupInfo(QueryGroupInfoReq dto) {
        log.info("GroupMngService::queryGroupInfo");
        String groupId = dto.getGroupId();
        String account = ReqSession.getSession().getAccount();
        // 1.查询群基本信息
        GroupInfo groupInfo = groupInfoMapper.selectGroupInfoOne(account, groupId);
        // 2.查询群成员列表及成员在群信息
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId);
        List<GroupMember> members = groupMemberMapper.selectList(queryWrapper);
        List<String> accounts = members.stream().map(item -> item.getAccount()).collect(Collectors.toList());
        // 3.查询成员基本信息
        Map<String, Map<String, Object>> usersMap = rpcClient.getUserRpcService().queryUserInfoBatch(accounts);
        for (GroupMember member : members) {
            usersMap.get(member.getAccount()).put("nickName", member.getNickName()); //群昵称不用user表中的
            usersMap.get(member.getAccount()).put("role", member.getRole());
            usersMap.get(member.getAccount()).put("mutedMode", member.getMutedMode());
            usersMap.get(member.getAccount()).put("inStatus", member.getInStatus());
        }

        GroupVO vo = new GroupVO();
        vo.setGroupInfo(groupInfo);
        vo.setMembers(usersMap);
        return ResultUtil.success(vo);
    }

    /**
     * 在用户的所有群组中：1精确匹配account，2模糊匹配nickName，查出member列表
     * @param dto 当前用户账号, 检索关键字
     * @return 检索结果: member集合
     */
    public ResponseEntity<IMHttpResponse> searchGroupMember(SearchGroupMemberReq dto) {
        log.info("GroupMngService::searchGroupMember");
        String account = ReqSession.getSession().getAccount();
        String searchKey = dto.getSearchKey();
        List<GroupMember> members = groupMemberMapper.searchGroupMember(account, searchKey);
        return ResultUtil.success(members);
    }

    /**
     * 在用户的所有群组中：1精确匹配groupId，2模糊匹配groupName，查出groupInfo
     * @param dto 当前用户账号, 检索关键字
     * @return 检索结果: groupInfo集合
     */
    public ResponseEntity<IMHttpResponse> searchGroupInfo(SearchGroupInfoReq dto) {
        log.info("GroupMngService::searchGroupInfo");
        String account = ReqSession.getSession().getAccount();
        String searchKey = dto.getSearchKey();
        List<GroupInfo> groups = groupInfoMapper.searchGroupInfo(account, searchKey);
        return ResultUtil.success(groups);
    }

    /**
     * 修改群组信息
     * @param dto 参数可以包括: 群组名称, 群组公告, 群组头像(及缩略图)等, 只需要包含其中一项即可
     * @return 成功或失败, 不返回数据
     */
    public ResponseEntity<IMHttpResponse> updateGroupInfo(UpdateGroupInfoReq dto) {
        log.info("GroupMngService::updateGroupInfo");
        String groupId = dto.getGroupId();
        String account = ReqSession.getSession().getAccount();
        if (!operationPermissionCheck(groupId, account, "updateGroupInfo")) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_PERMISSION_DENIED);
        }

        String announcement = dto.getAnnouncement();
        String groupName = dto.getGroupName();
        String avatar = dto.getAvatar();
        String avatarThumb = dto.getAvatarThumb();
        Boolean historyBrowse = dto.getHistoryBrowse();
        Boolean allMuted = dto.getAllMuted();
        Boolean joinGroupApproval = dto.getJoinGroupApproval();
        if (announcement == null // 注意: ""空串是有效值, 表示没有公告
                && !StringUtils.hasLength(groupName)
                && !StringUtils.hasLength(avatar)
                && !StringUtils.hasLength(avatarThumb)
                && historyBrowse == null
                && allMuted == null
                && joinGroupApproval == null) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_EMPTY_PARAM);
        }

        LambdaUpdateWrapper<GroupInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupInfo::getGroupId, groupId);
        Map<String, Object> msgMap = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        if (announcement != null) {
            updateWrapper.set(GroupInfo::getAnnouncement, announcement);
            msgMap.put("msgType", MsgType.SYS_GROUP_UPDATE_ANNOUNCEMENT.getNumber());
            content.put("announcement", announcement);
        } else if (StringUtils.hasLength(groupName)) {
            updateWrapper.set(GroupInfo::getGroupName, groupName);
            msgMap.put("msgType", MsgType.SYS_GROUP_UPDATE_NAME.getNumber());
            content.put("groupName", groupName);
        } else if (StringUtils.hasLength(avatar) && StringUtils.hasLength(avatarThumb)) {
            updateWrapper.set(GroupInfo::getAvatar, avatar);
            updateWrapper.set(GroupInfo::getAvatarThumb, avatarThumb);
            msgMap.put("msgType", MsgType.SYS_GROUP_UPDATE_AVATAR.getNumber());
        } else if (historyBrowse != null) {
            updateWrapper.set(GroupInfo::isHistoryBrowse, historyBrowse);
            if (historyBrowse.booleanValue()) {
                msgMap.put("msgType", MsgType.SYS_GROUP_SET_HISTORY_BROWSE.getNumber());
            } else {
                msgMap.put("msgType", MsgType.SYS_GROUP_CANCEL_HISTORY_BROWSE.getNumber());
            }
        } else if (allMuted != null) {
            updateWrapper.set(GroupInfo::isAllMuted, allMuted);
            if (allMuted.booleanValue()) {
                msgMap.put("msgType", MsgType.SYS_GROUP_SET_ALL_MUTED.getNumber());
            } else {
                msgMap.put("msgType", MsgType.SYS_GROUP_CANCEL_ALL_MUTED.getNumber());
            }
        } else if (joinGroupApproval != null) {
            updateWrapper.set(GroupInfo::isJoinGroupApproval, joinGroupApproval);
            if (joinGroupApproval.booleanValue()) {
                msgMap.put("msgType", MsgType.SYS_GROUP_SET_JOIN_APPROVAL.getNumber());
            } else {
                msgMap.put("msgType", MsgType.SYS_GROUP_CANCEL_JOIN_APPROVAL.getNumber());
            }
        }
        groupInfoMapper.update(updateWrapper);

        Map<String, String> operator = new HashMap<>();
        operator.put("account", account);
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getAccount, account);
        operator.put("nickName", groupMemberMapper.selectOne(queryWrapper).getNickName());
        content.put("operator", operator);

        msgMap.put("groupId", groupId);
        msgMap.put("content", content);
        rpcClient.getNettyRpcService().sendSysMsg(msgMap);

        return ResultUtil.success();
    }

    /**
     * 删除群组
     * @param dto 目标群组id
     * @return 成功或失败, 不返回数据
     */
    @Transactional
    public ResponseEntity<IMHttpResponse> dropGroup(DropGroupReq dto) {
        log.info("GroupMngService::dropGroup");
        String groupId = dto.getGroupId();
        String account = ReqSession.getSession().getAccount();

        if (!operationPermissionCheck(groupId, account, "dropGroup")) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_PERMISSION_DENIED);
        }

        // update GroupMember之前先查询群内所有成员，用于发送系统通知的目标用户
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getInStatus, 0);
        List<GroupMember> members = groupMemberMapper.selectList(queryWrapper);

        LambdaUpdateWrapper<GroupMember> groupMemberWrapper = new LambdaUpdateWrapper<>();
        groupMemberWrapper.set(GroupMember::getInStatus, 2)
            .eq(GroupMember::getGroupId, groupId);
        groupMemberMapper.update(groupMemberWrapper);

        // 这里采用软删除方式，因为GroupInfo的信息在展示session会话信息时还需要用到
        LambdaUpdateWrapper<GroupInfo> groupInfoWrapper = new LambdaUpdateWrapper<>();
        groupInfoWrapper.eq(GroupInfo::getGroupId, groupId);
        groupInfoWrapper.set(GroupInfo::isDelFlag, true);
        groupInfoWrapper.set(GroupInfo::getDelTime, new Date(System.currentTimeMillis()));
        groupInfoMapper.update(groupInfoWrapper);

        // 这里不删除群组的聊天记录，让其自然老化

        String operatorNickName = "";
        List<String> toAccounts = new ArrayList<>();
        List<Map<String, Object>> updateLeaveGroupParamList = new ArrayList<>();
        for (GroupMember member : members) {
            if (member.getAccount().equals(account)) {
                operatorNickName = member.getNickName();
            }
            toAccounts.add(member.getAccount());

            Map<String, Object> map = new HashMap<>();
            map.put("sessionId", groupId);
            map.put("account", member.getAccount());
            updateLeaveGroupParamList.add(map);
        }

        rpcClient.getChatRpcService().updateGroupSessionsForLeave(updateLeaveGroupParamList); // 往session表中更新离群信息

        Map<String, String> operator = new HashMap<>();
        operator.put("account", account);
        operator.put("nickName", operatorNickName);
        Map<String, Object> content = new HashMap<>();
        content.put("operator", operator);

        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("groupId", groupId);
        msgMap.put("msgType", MsgType.SYS_GROUP_DROP.getNumber());
        msgMap.put("content", content);
        msgMap.put("toAccounts", toAccounts);
        rpcClient.getNettyRpcService().sendSysMsg(msgMap);

        return ResultUtil.success();
    }

    /**
     * 添加成员
     * @param dto 目标群组id, 待添加的成员账号数组
     * @return 成功或失败, 最新的(成员在群信息 + 成员基本信息)
     */
    public ResponseEntity<IMHttpResponse> addMembers(AddMembersReq dto) {
        log.info("GroupMngService::addMembers");
        String groupId = dto.getGroupId();
        String account = ReqSession.getSession().getAccount();
        if (!operationPermissionCheck(groupId, account, "addMembers")) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_PERMISSION_DENIED);
        }

        List<Map<String, Object>> addMemberList = new ArrayList<>();
        List<Map<String, Object>> insertSessionList = new ArrayList<>();
        for (Map<String, Object> item: dto.getMembers()) {
            Map<String, Object> sessionMap = new HashMap<>();
            sessionMap.put("account", item.get("account"));
            sessionMap.put("sessionId", groupId);
            sessionMap.put("remoteId", groupId);
            sessionMap.put("sessionType", MsgType.GROUP_CHAT.getNumber());
            insertSessionList.add(sessionMap);

            Map<String, Object> memberMap = new HashMap<>();
            memberMap.put("groupId", groupId);
            memberMap.put("account", item.get("account"));
            memberMap.put("nickName", item.get("nickName"));
            memberMap.put("role", 0);
            memberMap.put("mutedMode", 0);
            memberMap.put("inStatus", 0);
            addMemberList.add(memberMap);
        }
        groupMemberMapper.batchInsertOrUpdate(addMemberList);
        rpcClient.getChatRpcService().insertGroupSessions(groupId, insertSessionList); // 邀请成功后, 为新成员创建session

        Map<String, String> operator = new HashMap<>();
        operator.put("account", dto.getOperatorId());
        operator.put("nickName", dto.getOperatorNickName());
        Map<String, Object> content = new HashMap<>();
        content.put("operator", operator);
        content.put("members", dto.getMembers());

        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("msgType", MsgType.SYS_GROUP_ADD_MEMBER.getNumber());
        msgMap.put("groupId", groupId);
        msgMap.put("content", content);
        rpcClient.getNettyRpcService().sendSysMsg(msgMap);

        return ResultUtil.success();
    }

    /**
     * 移除成员
     * @param dto 目标群组id, 待移除的成员账号数组
     * @return 成功或失败, 最新的(成员在群信息 + 成员基本信息)
     */
    public ResponseEntity<IMHttpResponse> delMembers(DelMembersReq dto) {
        log.info("GroupMngService::delMembers");
        String groupId = dto.getGroupId();
        String account = ReqSession.getSession().getAccount();
        if (!operationPermissionCheck(groupId, account, "delMembers")) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_PERMISSION_DENIED);
        }

        List<Map<String, Object>> delMembers = dto.getMembers();
        List<Object> delMemberAccounts = delMembers.stream().map(item -> item.get("account")).collect(Collectors.toList());
        LambdaUpdateWrapper<GroupMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(GroupMember::getInStatus, 1)
            .eq(GroupMember::getGroupId, groupId)
            .ne(GroupMember::getRole, 2) // 群主不能直接删除
            .in(GroupMember::getAccount, delMemberAccounts);
        groupMemberMapper.update(updateWrapper);

        // 往session表中更新离群信息
        List<Map<String, Object>> updateLeaveGroupParamList = delMembers.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("sessionId", groupId);
            map.put("account", item.get("account"));
            return map;
        }).collect(Collectors.toList());
        rpcClient.getChatRpcService().updateGroupSessionsForLeave(updateLeaveGroupParamList);

        Map<String, String> operator = new HashMap<>();
        operator.put("account", dto.getOperatorId());
        operator.put("nickName", dto.getOperatorNickName());
        Map<String, Object> content = new HashMap<>();
        content.put("operator", operator);
        content.put("members", delMembers);

        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("msgType", MsgType.SYS_GROUP_DEL_MEMBER.getNumber());
        msgMap.put("groupId", groupId);
        msgMap.put("content", content);
        rpcClient.getNettyRpcService().sendSysMsg(msgMap);

        return ResultUtil.success();
    }

    /**
     * 修改成员的角色
     * @param dto 目标群组id, 目标成员账号
     * @return 成功或失败, 不返回数据
     */
    public ResponseEntity<IMHttpResponse> changeRole(ChangeRoleReq dto) {
        log.info("GroupMngService::changeRole");
        String groupId = dto.getGroupId();
        String myAccount = ReqSession.getSession().getAccount();
        if (!operationPermissionCheck(groupId, myAccount, "changeRole")) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_PERMISSION_DENIED);
        }

        String memberAccount = dto.getAccount();
        int memberRole = dto.getRole();
        LambdaUpdateWrapper<GroupMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getAccount, memberAccount);
        updateWrapper.set(GroupMember::getRole, memberRole);
        int update = groupMemberMapper.update(updateWrapper);
        if (update == 0) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_PERMISSION_DENIED);
        }

        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId)
                .in(GroupMember::getAccount, new String[]{myAccount, memberAccount});
        List<GroupMember> members = groupMemberMapper.selectList(queryWrapper);
        Map<String, String> operator = new HashMap<>();
        Map<String, Object> updatedMember = new HashMap<>();
        for (GroupMember member : members) {
            if (member.getAccount().equals(myAccount)) {
                operator.put("account", myAccount);
                operator.put("nickName", member.getNickName());
            } else if (member.getAccount().equals(memberAccount)){
                updatedMember.put("account", myAccount);
                updatedMember.put("nickName", member.getNickName());
            }
        }
        Map<String, Object> content = new HashMap<>();
        content.put("operator", operator);
        content.put("member", updatedMember);

        Map<String, Object> msgMap = new HashMap<>();
        if (dto.getRole() == 1) {
            msgMap.put("msgType", MsgType.SYS_GROUP_SET_ADMIN.getNumber());
        } else if (dto.getRole() == 0) {
            msgMap.put("msgType", MsgType.SYS_GROUP_CANCEL_ADMIN.getNumber());
        }
        msgMap.put("groupId", groupId);
        msgMap.put("content", content);
        rpcClient.getNettyRpcService().sendSysMsg(msgMap);

        return ResultUtil.success();
    }

    /**
     * 修改自己的群昵称
     * @param dto 目标群组id, 群昵称
     * @return 成功或失败, 不返回数据
     */
    public ResponseEntity<IMHttpResponse> updateNickNameInGroup(UpdateNickNameInGroup dto) {
        log.info("GroupMngService::updateNickNameInGroup");
        String groupId = dto.getGroupId();
        String account = ReqSession.getSession().getAccount();
        LambdaUpdateWrapper<GroupMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getAccount, account);
        updateWrapper.set(GroupMember::getNickName, dto.getNickName());
        int update = groupMemberMapper.update(updateWrapper);
        if (update == 0) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_HAS_NO_THIS_MEMBER);
        }

        return ResultUtil.success();
    }

    /**
     * 设置或取消成员禁言
     * @param dto 目标群组id, 操作对象的账号, 禁言模式
     * @return 成功或失败, 不返回数据
     */
    public ResponseEntity<IMHttpResponse> updateMute(UpdateMuteReq dto) {
        log.info("GroupMngService::updateMute");
        String groupId = dto.getGroupId();
        String myAccount = ReqSession.getSession().getAccount();
        if (!operationPermissionCheck(groupId, myAccount, "updateMute")) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_PERMISSION_DENIED);
        }

        String targetAccount = dto.getAccount();
        int mutedMode = dto.getMutedMode();
        LambdaUpdateWrapper<GroupMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getAccount, targetAccount)
                .set(GroupMember::getMutedMode, mutedMode);
        int update = groupMemberMapper.update(updateWrapper);
        if (update == 0) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_HAS_NO_THIS_MEMBER);
        }


        Map<String, String> operator = new HashMap<>();
        Map<String, String> target = new HashMap<>();
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId)
                .in(GroupMember::getAccount, new String[]{myAccount, targetAccount});
        List<GroupMember> members = groupMemberMapper.selectList(queryWrapper);
        for (GroupMember member : members) {
            if (member.getAccount().equals(myAccount)) {
                operator.put("account", member.getAccount());
                operator.put("nickName", member.getNickName());
            }
            if (member.getAccount().equals(targetAccount)){
                target.put("account", member.getAccount());
                target.put("nickName", member.getNickName());
            }
        }
        Map<String, Object> content = new HashMap<>();
        content.put("operator", operator);
        content.put("member", target);
        content.put("mutedMode", mutedMode);

        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("groupId", groupId);
        msgMap.put("msgType", MsgType.SYS_GROUP_UPDATE_MEMBER_MUTED.getNumber());
        msgMap.put("content", content);
        rpcClient.getNettyRpcService().sendSysMsg(msgMap);

        return ResultUtil.success();
    }

    /**
     * 转移群主
     * @param dto 目标群组id, 目标成员账号
     * @return 成功或失败, 不返回数据
     */
    @Transactional
    public ResponseEntity<IMHttpResponse> ownerTransfer(OwnerTransferReq dto) {
        log.info("GroupMngService::ownerTransfer");
        String myAccount = ReqSession.getSession().getAccount();
        String groupId = dto.getGroupId();
        if (!operationPermissionCheck(groupId, myAccount, "ownerTransfer")) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_NOT_OWNER);
        }

        String targetAccount = dto.getAccount();
        LambdaUpdateWrapper<GroupMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getAccount, targetAccount)
                .set(GroupMember::getRole, 2); // 升级为群主
        if (groupMemberMapper.update(updateWrapper) == 0) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_OWNER_TRANSFER);
        }

        updateWrapper.clear();
        updateWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getAccount, myAccount)
                .set(GroupMember::getRole, 1); // 降级为管理员
        if (groupMemberMapper.update(updateWrapper) == 0) {
             // 抛出异常，让事务回滚
            throw new ServiceException(ServiceErrorCode.ERROR_GROUP_MNG_OWNER_TRANSFER);
        }

        Map<String, String> operator = new HashMap<>();
        Map<String, String> target = new HashMap<>();
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId)
                .in(GroupMember::getAccount, new String[]{myAccount, targetAccount});
        List<GroupMember> members = groupMemberMapper.selectList(queryWrapper);
        for (GroupMember member : members) {
            if (member.getAccount().equals(myAccount)) {
                operator.put("account", myAccount);
                operator.put("nickName", member.getNickName());
            } else if (member.getAccount().equals(targetAccount)){
                target.put("account", myAccount);
                target.put("nickName", member.getNickName());
            }
        }
        Map<String, Object> content = new HashMap<>();
        content.put("operator", operator);
        content.put("member", target);

        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("groupId", groupId);
        msgMap.put("msgType", MsgType.SYS_GROUP_OWNER_TRANSFER.getNumber());
        msgMap.put("content", content);
        rpcClient.getNettyRpcService().sendSysMsg(msgMap);

        return ResultUtil.success();
    }

    /**
     * 退出群组
     * @param dto 目标群组id
     * @return 成功或失败, 不返回数据
     */
    @Transactional
    public ResponseEntity<IMHttpResponse> leaveGroup(LeaveGroupReq dto) {
        log.info("GroupMngService::leaveGroup");
        String account = ReqSession.getSession().getAccount();
        String groupId = dto.getGroupId();

        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getAccount, account);
        GroupMember member = groupMemberMapper.selectOne(queryWrapper);

        LambdaUpdateWrapper<GroupMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(GroupMember::getInStatus, 1)
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getAccount, account)
                .ne(GroupMember::getRole, 2); //群主不能退群,要先转移出去
        int update = groupMemberMapper.update(updateWrapper);
        if (update == 0) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_HAS_NO_THIS_MEMBER);
        }

        // 往session表中更新离群信息
        List<Map<String, Object>> updateLeaveGroupParamList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", groupId);
        map.put("account", account);
        updateLeaveGroupParamList.add(map);
        rpcClient.getChatRpcService().updateGroupSessionsForLeave(updateLeaveGroupParamList);

        Map<String, String> operator = new HashMap<>();
        operator.put("account", account);
        operator.put("nickName", member.getNickName());
        Map<String, Object> content = new HashMap<>();
        content.put("operator", operator);

        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("groupId", groupId);
        msgMap.put("msgType", MsgType.SYS_GROUP_LEAVE.getNumber());
        msgMap.put("content", content);
        rpcClient.getNettyRpcService().sendSysMsg(msgMap);

        return ResultUtil.success();
    }

    private boolean operationPermissionCheck(String groupId, String account, String checkType) {
        LambdaQueryWrapper<GroupMember> queryWrapperGroupMember = new LambdaQueryWrapper<>();
        queryWrapperGroupMember.eq(GroupMember::getGroupId, groupId);
        queryWrapperGroupMember.eq(GroupMember::getAccount, account);
        switch (checkType) {
            case "updateGroupInfo":
            case "updateMute":
            case "delMembers":
                queryWrapperGroupMember.gt(GroupMember::getRole, 0); // 管理员权限
                return groupMemberMapper.selectCount(queryWrapperGroupMember) > 0;
            case "dropGroup":
            case "changeRole":
            case "ownerTransfer":
                queryWrapperGroupMember.eq(GroupMember::getRole, 2); //群主权限
                return groupMemberMapper.selectCount(queryWrapperGroupMember) > 0;
            case "addMembers":
                // 满足其一即可: 1.全员可邀请; 2.管理员权限
                LambdaQueryWrapper<GroupInfo> queryWrapperGroupInfo = new LambdaQueryWrapper<>();
                queryWrapperGroupInfo.eq(GroupInfo::getGroupId, groupId);
                GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
                if (!groupInfo.isJoinGroupApproval()) {
                    queryWrapperGroupMember.in(GroupMember::getRole, new Integer[] {1, 2});
                    if (groupMemberMapper.selectCount(queryWrapperGroupMember) == 0) {
                        return false;
                    }
                }
                return true;
            default:
                return false;
        }
    }

    private long generateGroupId() {
        if (snowflakeId == null) { // 懒加载
            snowflakeId = SnowflakeId.getInstance();
        }
        return snowflakeId.nextId();
    }
}
