package com.hibob.anyim.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hibob.anyim.chat.entity.Session;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

public interface SessionMapper extends BaseMapper<Session> {

    int insertBatchSomeColumn(Collection<Session> entityList);

    /**
     * 单聊查询, 这里引用Left join的方式查到对方的已读消息Id(remote_read)
     * @param account
     * @return
     */
    @Select("SELECT t1.*, IFNULL(t2.read_msg_id, 0) AS remote_read FROM anyim_chat_session t1 " +
            " LEFT JOIN anyim_chat_session t2 ON t2.account = t1.remote_id AND t2.remote_id = t1.account " +
            " WHERE t1.del_flag = false AND t1.account = #{account} AND t1.session_type = 2 " +
            " ORDER BY t1.session_id ASC ")
    List<Session> selectSessionListForChat(String account);

    @Select("SELECT t1.*, IFNULL(t2.read_msg_id, 0) AS remote_read FROM anyim_chat_session t1 " +
            " LEFT JOIN anyim_chat_session t2 ON t2.account = t1.remote_id AND t2.remote_id = t1.account " +
            " WHERE t1.account = #{account} AND t1.session_id = #{sessionId}")
    Session selectSession(String account, String sessionId);
}
