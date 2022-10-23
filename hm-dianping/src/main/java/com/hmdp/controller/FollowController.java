package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.service.IFollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 用户关注取关
 */
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;

    // 关注
    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id")Long followUserId,@PathVariable("isFollow")Boolean isFollow){
        return followService.follow(followUserId,isFollow);
    }

    // 判断有无关注
    @GetMapping("/or/not/{id}")
    public Result follow(@PathVariable("id")Long followUserId){
        return followService.isFollow(followUserId);
    }

    // 共同关注
    @GetMapping("/common/{id}")
    public Result followCommon(@PathVariable("id")Long id){
        return followService.followCommon(id);
    }
}
