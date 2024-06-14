package com.hibob.anyim.groupmng.service;

import com.hibob.anyim.common.enums.ServiceErrorCode;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.utils.ResultUtil;
import com.hibob.anyim.common.utils.SnowflakeId;
import com.hibob.anyim.groupmng.dao.request.CreateGroupReq;
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
        groupInfoMapper.insert(groupInfo);

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

        groupMemberMapper.insertBatchSomeColumn(members);

        GroupVO vo = new GroupVO();
        vo.setGroupInfo(groupInfo);
        vo.setMembers(members);
        return ResultUtil.success(vo);
    }

    private long generateGroupId() {
        if (snowflakeId == null) { // 懒加载
            snowflakeId = SnowflakeId.getInstance();
        }
        return snowflakeId.nextId();
    }
}
