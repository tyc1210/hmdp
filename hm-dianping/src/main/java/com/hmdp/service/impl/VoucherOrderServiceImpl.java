package com.hmdp.service.impl;

import com.hmdp.common.exception.BaseException;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private IVoucherOrderService voucherOrderService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Override
    public Result seckillVoucher(Long voucherId) {
        // 1.查询优惠券
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        // 2.判断秒杀是否开始
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            // 尚未开始
            throw new BaseException("秒杀尚未开始");
        }
        // 3.判断秒杀是否已经结束
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            // 已经结束
            throw new BaseException("秒杀已经结束");
        }
        // 4.判断库存是否充足
        if (voucher.getStock() < 1) {
            // 库存不足
            throw  new BaseException("库存不足");
        }
        Long userId = UserHolder.getUser().getId();
        // 锁用户id是为了降低锁的范围
        // 注意不能在createOrder方法内加锁。若在方法内加锁会出现锁释放但事务还未提交造成线程不安全
        // 分布式项目下应该使用分布式锁
        synchronized (String.valueOf(userId).intern()){
            // 当前对象的代理对象调用，防止事务失效
            return voucherOrderService.createOrder(voucherId,userId);
        }
    }

    // 保证原子
    @Transactional
    public Result createOrder(Long voucherId,Long userId){
            // 限制一人最多一单
            Integer count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
            if(count > 0){
                throw new BaseException("用户已经购买过");
            }
            //5，扣减库存
            boolean success = seckillVoucherService.update()
                    .setSql("stock= stock -1")
                    .eq("voucher_id", voucherId)
                    .gt("stock",0).update();
            if (!success) {
                //扣减库存
                throw  new BaseException("库存不足");
            }

            //6.创建订单
            VoucherOrder voucherOrder = new VoucherOrder();
            // 6.1.订单id
            long orderId = redisIdWorker.nextId("order");
            voucherOrder.setId(orderId);
            // 6.2.用户id
            voucherOrder.setUserId(userId);
            // 6.3.代金券id
            voucherOrder.setVoucherId(voucherId);
            save(voucherOrder);
            return Result.ok(orderId);
        }
}
