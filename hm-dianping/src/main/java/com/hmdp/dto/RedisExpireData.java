package com.hmdp.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisExpireData {
    private LocalDateTime expireTime;
    private Object data;
}
