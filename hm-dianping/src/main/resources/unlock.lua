 -- 获取redis中的value,对比value值是否一致
 if(ARGV[1] == redis.call('get',KEYS[1])) then
  -- 释放锁
  return redis.call('delete',key)
 end
 return 0