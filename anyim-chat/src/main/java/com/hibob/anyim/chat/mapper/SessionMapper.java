package com.hibob.anyim.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hibob.anyim.chat.entity.Session;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SessionMapper extends BaseMapper<Session> {

    int insertBatchSomeColumn(Collection<Session> entityList);

    /**
     * 单聊查询, 这里引用Left join的方式查到对方的已读消息Id(remote_read)
     * @param account
     * @return
     */
    @Select("SELECT t1.*, IFNULL(t2.read_msg_id, 0) AS remote_read FROM anyim_chat_session t1 " +
            " LEFT JOIN anyim_chat_session t2 ON t2.account = t1.remote_id AND t2.remote_id = t1.account " +
            " WHERE t1.closed = false AND t1.account = #{account} AND t1.session_type = 2 " +
            " ORDER BY t1.session_id ASC ")
    List<Session> selectSessionListForChat(String account);

    /**
     * 查询指定的session信息，不论这个session是不是closed=true状态
     * @param account
     * @param sessionId
     * @return
     */
    @Select("SELECT t1.*, IFNULL(t2.read_msg_id, 0) AS remote_read FROM anyim_chat_session t1 " +
            " LEFT JOIN anyim_chat_session t2 ON t2.account = t1.remote_id AND t2.remote_id = t1.account " +
            " WHERE t1.account = #{account} AND t1.session_id = #{sessionId}")
    Session selectSession(String account, String sessionId);

    /**
     * 批量插入数据，如果联合主键已存在则更新
     * @param sessionList
     * @return
     */
    @Insert("<script>" +
            " INSERT INTO anyim_chat_session (account, session_id, remote_id, session_type, leave_flag, leave_msg_id)" +
            " values" +
            " <foreach item='item' index='index' collection='sessionList' separator=', '> " +
            " (#{item.account}, #{item.sessionId}, #{item.remoteId}, #{item.sessionType}, #{item.leaveFlag}, #{item.leaveMsgId})" +
            " </foreach> " +
            " as new" +
            " ON DUPLICATE KEY UPDATE " +
            " remote_id = new.remote_id, " +
            " session_type = new.session_type, " +
            " leave_flag = new.leave_flag, " +
            " leave_msg_id = new.leave_msg_id " +
            "</script>")
    int batchInsertOrUpdate(List<Map<String, Object>> sessionList);
}
