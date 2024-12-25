package com.hibob.anyim.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hibob.anyim.chat.entity.Session;
import com.hibob.anyim.chat.typeHandler.StringListTypeHandler;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface SessionMapper extends BaseMapper<Session> {

    /**
     * 单聊查询, 这里引用Left join的方式查到对方的已读消息Id(remote_read)
     * @param account
     * @return
     */
    @Select("SELECT t1.*, IFNULL(t2.read_msg_id, 0) AS remote_read FROM anyim_chat_session t1 " +
            " LEFT JOIN anyim_chat_session t2 ON t2.account = t1.remote_id AND t2.remote_id = t1.account " +
            " WHERE t1.closed = false AND t1.account = #{account} AND t1.session_type = 2 " +
            " ORDER BY t1.session_id ASC ")
    @Results({
            @Result(property = "joinTime", column = "join_time", typeHandler = StringListTypeHandler.class),
            @Result(property = "leaveTime", column = "leave_time", typeHandler = StringListTypeHandler.class)
    })
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
    @Results({
            @Result(property = "joinTime", column = "join_time", typeHandler = StringListTypeHandler.class),
            @Result(property = "leaveTime", column = "leave_time", typeHandler = StringListTypeHandler.class)
    })
    Session selectSession(String account, String sessionId);

    /**
     * 批量插入数据，如果联合主键已存在则更新
     * @param sessionList
     * @return
     */
    @Insert("<script>" +
            " INSERT IGNORE INTO anyim_chat_session (account, session_id, remote_id, session_type)" +
            " values" +
            " <foreach item='item' index='index' collection='sessionList' separator=','> " +
            " (#{item.account}, #{item.sessionId}, #{item.remoteId}, #{item.sessionType})" +
            " </foreach>" +
            "</script>")
    int batchInsertOrUpdate(List<Map<String, Object>> sessionList);

    @Update("<script>" +
            " update anyim_chat_session set join_time = CASE " +
            " <foreach item='item' index='index' collection='sessionList' separator=' '> " +
            " WHEN account = #{item.account} and session_id = #{item.sessionId} then JSON_ARRAY_APPEND(IFNULL(join_time, '[]'), '$', now())" +
            " </foreach> " +
            " ELSE join_time END" +
            "</script>")
    int batchUpdateForJoin(List<Map<String, Object>> sessionList);

    @Update("<script>" +
            " update anyim_chat_session set leave_time = CASE " +
            " <foreach item='item' index='index' collection='sessionList' separator=' '> " +
            " WHEN account = #{item.account} and session_id = #{item.sessionId} then JSON_ARRAY_APPEND(IFNULL(leave_time, '[]'), '$', now())" +
            " </foreach> " +
            " ELSE leave_time END" +
            "</script>")
    int batchUpdateForLeave(List<Map<String, Object>> sessionList);
}
