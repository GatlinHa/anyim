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
        boolean allInvite = true; //创建时，默认全员可以邀请新成员
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
        groupInfo.setAllInvite(allInvite);
        groupInfo.setCreator(creator);
        groupInfo.setMyRole(2); //不是数据库字段,加了注解不会插入进去,这里是为了返回结果

        List<GroupMember> insertMembers = new ArrayList<>();
        List<Map<String, Object>> members = dto.getMembers();
        if (members.size() < 3) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_NOT_ENOUGH_MEMBER);
        }

        for (Map<String, Object> item: members) {
            GroupMember member = new GroupMember();
            member.setGroupId(groupId);
            member.setAccount(item.get("account").toString());
            member.setNickName(item.get("nickName").toString());
            if (account.equals(item.get("account"))) {
                member.setRole(2);
            }
            else {
                member.setRole(0);
            }
            insertMembers.add(member);
        }

        groupInfoMapper.insert(groupInfo);
        groupMemberMapper.insertBatchSomeColumn(insertMembers);

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
        if (groupInfo == null) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_PERMISSION_DENIED);
        }

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
        }

        GroupVO vo = new GroupVO();
        vo.setGroupInfo(groupInfo);
        vo.setMembers(usersMap);
        return ResultUtil.success(vo);
    }

    /**
     * 按关键字检索某个用户的所有群组下有没有叫xxx(或者账号是xxx)的人
     * @param dto 当前用户账号, 检索关键字
     * @return 检索结果: anyim_group_member表记录
     */
    public ResponseEntity<IMHttpResponse> searchGroupByMember(SearchGroupByMemberReq dto) {
        log.info("GroupMngService::searchGroupByMember");
        String account = ReqSession.getSession().getAccount();
        String searchKey = dto.getSearchKey();
        List<GroupMember> members = groupMemberMapper.selectGroupByMember(account, searchKey);
        return ResultUtil.success(members);
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
        Boolean allInvite = dto.getAllInvite();
        if (announcement == null // 注意: ""空串是有效值, 表示没有公告
                && !StringUtils.hasLength(groupName)
                && !StringUtils.hasLength(avatar)
                && !StringUtils.hasLength(avatarThumb)
                && historyBrowse == null
                && allMuted == null
                && allInvite == null) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_EMPTY_PARAM);
        }

        LambdaUpdateWrapper<GroupInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupInfo::getGroupId, groupId);
        if (announcement != null) {
            updateWrapper.set(GroupInfo::getAnnouncement, announcement);
        }
        if (StringUtils.hasLength(groupName)) {
            updateWrapper.set(GroupInfo::getGroupName, groupName);
        }
        if (StringUtils.hasLength(avatar)) {
            updateWrapper.set(GroupInfo::getAvatar, avatar);
        }
        if (StringUtils.hasLength(avatarThumb)) {
            updateWrapper.set(GroupInfo::getAvatarThumb, avatarThumb);
        }
        if (historyBrowse != null) {
            updateWrapper.set(GroupInfo::isHistoryBrowse, historyBrowse);
        }
        if (allMuted != null) {
            updateWrapper.set(GroupInfo::isAllMuted, allMuted);
        }
        if (allInvite != null) {
            updateWrapper.set(GroupInfo::isAllInvite, allInvite);
        }
        groupInfoMapper.update(updateWrapper);

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

        LambdaQueryWrapper<GroupMember> groupMemberWrapper = new LambdaQueryWrapper<>();
        groupMemberWrapper.eq(GroupMember::getGroupId, groupId);
        groupMemberMapper.delete(groupMemberWrapper);

        LambdaQueryWrapper<GroupInfo> groupInfoWrapper = new LambdaQueryWrapper<>();
        groupInfoWrapper.eq(GroupInfo::getGroupId, groupId);
        groupInfoMapper.delete(groupInfoWrapper);

        // 这里不删除群组的聊天记录，让其自然老化

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

        List<GroupMember> addMembers = new ArrayList<>();
        for (Map member : dto.getMembers()) {
            GroupMember insertMember = new GroupMember();
            insertMember.setGroupId(String.valueOf(groupId));
            insertMember.setAccount(member.get("account").toString());
            insertMember.setNickName(member.get("nickName").toString());
            insertMember.setRole(0);
            addMembers.add(insertMember);
        }
        groupMemberMapper.insertBatchSomeColumn(addMembers);

        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId);
        List<GroupMember> members = groupMemberMapper.selectList(queryWrapper);
        List<String> accounts = members.stream().map(item -> item.getAccount()).collect(Collectors.toList());
        Map<String, Map<String, Object>> usersMap = rpcClient.getUserRpcService().queryUserInfoBatch(accounts);
        for (GroupMember member : members) {
            usersMap.get(member.getAccount()).put("nickName", member.getNickName()); //群昵称不用user表中的
            usersMap.get(member.getAccount()).put("role", member.getRole());
            usersMap.get(member.getAccount()).put("mutedMode", member.getMutedMode());
        }

        GroupVO vo = new GroupVO();
        vo.setMembers(usersMap);
        return ResultUtil.success(vo);
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

        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId);
        queryWrapper.ne(GroupMember::getRole, 2); // 群主不能直接删除
        queryWrapper.in(GroupMember::getAccount, dto.getAccounts());
        groupMemberMapper.delete(queryWrapper);

        queryWrapper.clear();
        queryWrapper.eq(GroupMember::getGroupId, groupId);
        List<GroupMember> members = groupMemberMapper.selectList(queryWrapper);
        List<String> accounts = members.stream().map(item -> item.getAccount()).collect(Collectors.toList());
        Map<String, Map<String, Object>> usersMap = rpcClient.getUserRpcService().queryUserInfoBatch(accounts);
        for (GroupMember member : members) {
            usersMap.get(member.getAccount()).put("nickName", member.getNickName()); //群昵称不用user表中的
            usersMap.get(member.getAccount()).put("role", member.getRole());
            usersMap.get(member.getAccount()).put("mutedMode", member.getMutedMode());
        }

        GroupVO vo = new GroupVO();
        vo.setMembers(usersMap);
        return ResultUtil.success(vo);
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

        return ResultUtil.success();
    }

    /**
     * 修改自己的群昵称
     * @param dto 目标群组id, 群昵称
     * @return 成功或失败, 不返回数据
     */
    public ResponseEntity<IMHttpResponse> updateGroupNickName(UpdateGroupNickNameReq dto) {
        log.info("GroupMngService::updateGroupNickName");
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

        String account = dto.getAccount();
        int mutedMode = dto.getMutedMode();
        LambdaUpdateWrapper<GroupMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupMember::getGroupId, groupId).eq(GroupMember::getAccount, account);
        updateWrapper.set(GroupMember::getMutedMode, mutedMode);
        int update = groupMemberMapper.update(updateWrapper);
        if (update == 0) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_HAS_NO_THIS_MEMBER);
        }

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
        LambdaUpdateWrapper<GroupMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getAccount, account)
                .ne(GroupMember::getRole, 2); //群主不能退群,要先转移出去
        int delete = groupMemberMapper.delete(updateWrapper);
        if (delete == 0) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_HAS_NO_THIS_MEMBER);
        }

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
                if (!groupInfo.isAllInvite()) {
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
