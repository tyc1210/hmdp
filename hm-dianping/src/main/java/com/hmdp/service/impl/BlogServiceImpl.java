package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.common.exception.BaseException;
import com.hmdp.dto.Result;
import com.hmdp.dto.ScrollResult;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {
    @Resource
    private IUserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IFollowService followService;

    @Override
    public Blog queryById(Long id) {
        Blog blog = getById(id);
        if(null == blog){
            throw new BaseException("数据不存在");
        }
        User user = userService.getById(blog.getUserId());
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
        String key = "blog:liked:" + id;
        blog.setIsLike((stringRedisTemplate.opsForZSet().score(key,user.getId()) != null));
        return blog;
    }

    @Override
    public void like(Long id) {
        Long userId = UserHolder.getUser().getId();
        // 判断是否点过赞
        String key = "blog:liked:" + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if(score != null){
            // 已经点过赞 此次为取消点赞
            boolean isSuccess = update().setSql("like = liked - 1").eq("id", id).update();
            if(isSuccess){
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }else {
            // 未点赞 此次为点赞
            boolean isSuccess = update().setSql("like = liked + 1").eq("id", id).update();
            if(isSuccess){
                stringRedisTemplate.opsForZSet().add(key,userId.toString(),System.currentTimeMillis());
            }
        }
    }

    @Override
    public List<Blog> getHot(Integer current) {
        // 根据用户查询
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog ->{
            Long userId = blog.getUserId();
            User user = userService.getById(userId);
            blog.setName(user.getNickName());
            blog.setIcon(user.getIcon());
            String key = "blog:liked:" + blog.getId();
            blog.setIsLike((stringRedisTemplate.opsForZSet().score(key,user.getId()) != null));
        });
        return records;
    }

    @Override
    public List<UserDTO> getLikedBlogUsers(Long id) {
        String key = "blog:liked:" + id;
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if(null == top5 || top5.isEmpty()){
            return null;
        }
        List<Long> userIds = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        // 注意查出的数据要按照 top5 顺序
        String idStr = StrUtil.join(",",userIds);
        List<UserDTO> dtos = userService.query()
                .in("id",userIds).last("order by field(id,"+ idStr +")").list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return dtos;
    }

    @Override
    public Result saveBlog(Blog blog) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // 保存探店博文
        boolean save = save(blog);
        // 找到关注的粉丝
        if(save){
            List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
            for (Follow follow : follows) {
                Long userId = follow.getUserId();
                // 推送消息
                String key = "feed:"+userId;
                stringRedisTemplate.opsForZSet().add(key,blog.getId().toString(),System.currentTimeMillis());
            }
        }
        // 返回id
        return Result.ok(blog.getId());
    }

    @Override
    public Result getFollowsBlog(Long max, Integer offset) {
        Long userId = UserHolder.getUser().getId();
        // 查询我关注的所有博主发的笔记
        String key = "feed:"+userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 10);
        if(null == typedTuples || typedTuples.isEmpty()){
            Result.ok();
        }
        // 解析数据
        List<Long> ids = new ArrayList<>(typedTuples.size());
        Long minTime = 0L;
        // 记录本次列表 score 相同的个数 + 1
        int os = 1;
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            ids.add(Long.valueOf(typedTuple.getValue()));
            long time = typedTuple.getScore().longValue();
            if(time == minTime){
                os++;
            }else {
                minTime = time;
                os = 1;
            }
        }
        String idStr = StrUtil.join(",",ids);
        // 从数据库查询
        List<Blog> blogs = query().in("id", ids).last("order by filed(id," + idStr + ")").list();
        return Result.ok(new ScrollResult(blogs,minTime,os));
    }
}
