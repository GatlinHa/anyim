# netty
## 大致思路
- netty定位是提供长连接通道
- netty对接agw，在agw处认证wss，同时还可以做负载均衡，省去了TRS的逻辑
- 消息模式采用推拉式，长连接在线场时推下去，不在线时下次登录自动拉去
- 不管消息能不能推下去，都要发给MQ给Chat去把消息入库和入Redis
- 单聊表和群聊表类似，都是通过fromid+toid/groupid来区分，msgid在fromid+toid/groupid下是单调递增的，可以不连续，如果给组合主键上面加一个单调递增的ID列？
- 产生消息时，同步写DB和Redis（先写DB，再写Redis，可以试一下能不能放在一个事务方法里面），Redis缓存7天，DB永久保存
- 客户端拉去消息时，传递上次更新时间，上次更新的最后一个msgid，如果上次更新时间在7天内，直接从Redis拉取，否则从DB拉取（不同时拉去Redis和数据库）
- 账号退出时，websocket通道要关闭，可以通过user发送mq事件来监听到
- 每次连接成功后，要返回一个timelife的信息给客户端，告诉客户端这个连接要持续多久，客户端要在结束之前更新timelife

## 缓存

| 缓存     | Netty在活全局路由表                                          | Netty在活本地路由表                           | Netty在线客户端                                               |
| -------- | ------------------------------------------------------------ | --------------------------------------------- |----------------------------------------------------------|
| 存储容器 | Redis                                                        | Map                                           | Redis                                                    |
| key      | RedisKey.NETTY_GLOBAL_ROUTE + uniqueId                       | RedisKey.NETTY_GLOBAL_ROUTE + uniqueId        | RedisKey.NETTY_ONLINE_CLIENT + account                   |
| Value    | instance: ip+端口                                            | channel                                       | Set集合，存放uniqueId                                         |
| TTL      | 1800s                                                        |                                               | 整体3600s                                                  |
| 新增     | Netty通道建连收到Hello消息时                                 | Netty通道建连收到Hello消息时                  | Netty通道建连收到Hello消息时<br />如果缓存未命中，需要从调用User的RPC接口重新加载缓存进来 |
| 删除     | channel通道关闭时                                            | channel通道关闭时                             | channel通道关闭时，释放Set的一个                                    |
| 查询     | Netty通道建连时认证，检查是不是登录过了，如果是就不允许重复登录<br />消息路由时，查询对端所在的instance | 消息路由时，根据uniqueId查询对端所在的channel | 消息扩散时，需要查询这个账号下所有在线客户端的连接的uniqueId                       |
| 修改     |                                                              |                                               |                                                          |
