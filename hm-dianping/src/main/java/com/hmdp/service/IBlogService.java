package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IBlogService extends IService<Blog> {

    Blog queryById(Long id);

    void like(Long id);

    List<Blog> getHot(Integer current);

    List<UserDTO> getLikedBlogUsers(Long id);

    Result saveBlog(Blog blog);

    Result getFollowsBlog(Long max, Integer offset);
}
