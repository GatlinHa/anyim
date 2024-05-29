# any-user
## 简介

## 缓存

| 缓存     | 在活AccessToken                                              | 在活RefreshToken                              | 请求记录                                                |
| -------- | ------------------------------------------------------------ | --------------------------------------------- | ------------------------------------------------------- |
| 存储容器 | Redis                                                        | Redis                                         | Redis                                                   |
| key      | RedisKey.USER_ACTIVE_TOKEN + uniqueId                        | RedisKey.USER_ACTIVE_TOKEN_REFRESH + uniqueId | RedisKey.USER_REQ_RECORD + uniqueId + SPLIT_V + traceId |
| Value    | token, secret, expire                                        | token, secret, expire                         | ""                                                      |
| TTL      | 1800s                                                        | 3600s                                         | 300s                                                    |
| 新增     | login                                                        | login                                         | User的认证拦截器中认证成功的请求                        |
| 删除     | logout，modifyPwd                                            | logout，modifyPwd                             |                                                         |
| 查询     | Netty通道建连时认证，要看redis的token是否存在，连接参数的token与redis的token是否一致<br />User的认证拦截器中认证token的有效性，一致性 | User的认证拦截器中认证token的有效性，一致性   | User的认证拦截器中认证时，看是不是重放攻击              |
| 修改     | refreshToken                                                 |                                               |                                                         |

