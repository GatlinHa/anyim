package com.hibob.anyim.user.rpc;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hibob.anyim.common.rpc.UserRpcService;
import com.hibob.anyim.common.config.JwtProperties;
import com.hibob.anyim.user.entity.Login;
import com.hibob.anyim.user.mapper.LoginMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@DubboService
@RequiredArgsConstructor
public class UserRpcServiceImpl implements UserRpcService {

    private final JwtProperties jwtProperties;
    private final LoginMapper loginMapper;

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


}
