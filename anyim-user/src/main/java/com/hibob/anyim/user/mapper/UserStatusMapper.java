package com.hibob.anyim.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hibob.anyim.user.entity.UserStatus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface UserStatusMapper extends BaseMapper<UserStatus> {

    // 这里有一个行转列的效果，方便后续使用
    @Select("<script>" +
            " SELECT " +
            " <foreach item='account' index='index' collection='accounts' open='' separator=', ' close=''> " +
            "   MAX(CASE WHEN account = #{account} THEN status END) AS #{account} " +
            " </foreach> " +
            " FROM anyim_user_status " +
            " WHERE account in " +
            " <foreach item='account' index='index' collection='accounts' open='(' separator=',' close=')'>" +
            "   #{account} " +
            " </foreach> " +
            "</script>")
    Map<String, Integer> queryStatusByAccountList(List<String> accounts);

    @Select("SELECT account, MAX(status) AS status " +
            " FROM anyim_user_status " +
            " WHERE account = #{account} " +
            " LIMIT 1 ")
    UserStatus queryStatus(String account);
}
