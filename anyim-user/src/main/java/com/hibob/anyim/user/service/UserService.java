package com.hibob.anyim.user.service;

import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.utils.ResultUtils;
import com.hibob.anyim.user.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    public IMHttpResponse querySelf(QuerySelfDTO dto) {
        log.info("UserService--querySelf");
        return ResultUtils.success();
    }

    public IMHttpResponse modifySelf(ModifySelfDTO dto) {
        log.info("UserService--modifySelf");
        return ResultUtils.success();
    }

    public IMHttpResponse query(QueryDTO dto) {
        log.info("UserService--query");
        return ResultUtils.success();
    }

    public IMHttpResponse findByNick(FindByNickDTO dto) {
        log.info("UserService--findByNick");
        return ResultUtils.success();
    }

    public IMHttpResponse findById(FindByIdDTO dto) {
        log.info("UserService--findById");
        return ResultUtils.success();
    }
}
