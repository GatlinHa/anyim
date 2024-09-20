package com.hibob.anyim.user.rpc;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hibob.anyim.common.rpc.service.UserRpcService;
import com.hibob.anyim.common.config.JwtProperties;
import com.hibob.anyim.common.utils.BeanUtil;
import com.hibob.anyim.user.dto.vo.UserVO;
import com.hibob.anyim.user.entity.Login;
import com.hibob.anyim.user.entity.User;
import com.hibob.anyim.user.mapper.LoginMapper;
import com.hibob.anyim.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@DubboService
@RequiredArgsConstructor
public class UserRpcServiceImpl implements UserRpcService {

    private final JwtProperties jwtProperties;
    private final LoginMapper loginMapper;
    private final UserMapper userMapper;

    @Override
    public List<String> queryOnline(String account) {
        log.info("UserRpcServiceImpl::queryOnline start......");
        LambdaQueryWrapper<Login> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper
                .select(Login::getUniqueId)
                .eq(Login::getAccount, account)
                .isNotNull(Login::getLoginTime)
                .isNull(Login::getLogoutTime)
                .and(wrapper -> wrapper.isNull(Login::getRefreshTime)
                                .or()
                                .lt(Login::getRefreshTime, LocalDateTime.now().minusSeconds(jwtProperties.getAccessTokenExpire())))
                .groupBy(Login::getUniqueId);
        List<String> list = new ArrayList<>();
        loginMapper.selectList(queryWrapper).forEach(x -> {
            list.add(x.getUniqueId());
        });
        return list;
    }

    @Override
    public Map<String, Object> queryUserInfo(String account) {
        log.info("UserRpcServiceImpl::queryUserInfo start......");
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getAccount, account);
        List<User> users = userMapper.selectList(queryWrapper);
        if (users.size() > 0) {
            try {
                UserVO vo = BeanUtil.copyProperties(users.get(0), UserVO.class);
                return BeanUtil.objectToMap(vo);
            } catch (IllegalAccessException e) {
                log.error("UserRpcServiceImpl::queryUserInfo type conversion error......exception: {}", e.getMessage());
            }
        }
        return null;
    }


    @Override
    public Map<String, Map<String, Object>> queryUserInfoBatch(List<String> accountList) {
        log.info("UserRpcServiceImpl::queryUserInfoBatch start......");
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(User::getAccount, accountList.toArray());
        List<User> users = userMapper.selectList(queryWrapper);
        Map<String, Map<String, Object>> result = new HashMap();
        try {
            for (User user : users) {
                // 把User对象转成返回对象
                UserVO vo = BeanUtil.copyProperties(user, UserVO.class);
                result.put(user.getAccount(), BeanUtil.objectToMap(vo));
            }
        } catch (IllegalAccessException e) {
            log.error("UserRpcServiceImpl::queryUserInfoBatch type conversion error......exception: {}", e.getMessage());
        }
        return result;
    }

}
