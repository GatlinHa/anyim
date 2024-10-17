package com.hibob.anyim.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hibob.anyim.chat.entity.Session;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SessionMapper extends BaseMapper<Session> {

    @Select("SELECT t1.*, IFNULL(t2.read_msg_id, 0) AS remote_read FROM anyim_chat_session t1 " +
            " LEFT JOIN anyim_chat_session t2 ON t2.account = t1.remote_id AND t2.remote_id = t1.account " +
            " WHERE t1.del_flag = false AND t1.account = #{account}")
    List<Session> selectSessionList(String account);

    @Select("SELECT t1.*, IFNULL(t2.read_msg_id, 0) AS remote_read FROM anyim_chat_session t1 " +
            " LEFT JOIN anyim_chat_session t2 ON t2.account = t1.remote_id AND t2.remote_id = t1.account " +
            " WHERE t1.del_flag = false AND t1.account = #{account} AND t1.session_id = #{sessionId}")
    Session selectSession(String account, String sessionId);
}
