package com.hmdp.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 达人探店 发布博客相关
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService;

    // 发布
    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id){
        return Result.ok(blogService.queryById(id));
    }

    /**
     * 查询点赞前五的用户
     */
    @PutMapping("/like/{id}")
    public Result getLikedBlogUsers(@PathVariable("id") Long id) {
        return Result.ok(blogService.getLikedBlogUsers(id));
    }

    // 查询我的发布
    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    // 查询热榜
    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return Result.ok(blogService.getHot(current));
    }

    // 查询我的关注发布内容
    @GetMapping("of/follow")
    public Result getFollowsBlog(@RequestParam("lastId")Long max, @RequestParam(value = "offset",defaultValue = "0")Integer offset){
        return blogService.getFollowsBlog(max,offset);
    }
}
