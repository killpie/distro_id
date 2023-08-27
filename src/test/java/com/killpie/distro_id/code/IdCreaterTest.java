package com.killpie.distro_id.code;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.killpie.distro_id.factory.IdFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @Auther: killpie
 * @Date: 2023/8/20 22:14
 * @Description: IdCreater测试类
 */

@Slf4j
public class IdCreaterTest {

    @Test
    public void test(){
        IdCreater idCreater = IdFactory.getInstance();
        System.out.println(idCreater.nextId());
    }

    /**
     * 验证大量线程下的ID生成
     */
    @Test
    public void test2() throws InterruptedException {
        Set<Long> sets = new ConcurrentHashSet<>();
        IdCreater idCreater = IdFactory.getInstance();
        int count = 500;
        //计算耗时
        long start = System.currentTimeMillis();
        //1000个线程并发同时开始
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            new Thread(()->{
                try {
                    for (int j = 0; j < 100000; j++) {
                        long id = idCreater.nextId();
                        if (sets.contains(id)){
                            System.out.println("重复id:"+id);
                        }else {
                            sets.add(id);
                        }
                    }
                }finally {
                    countDownLatch.countDown();
                }

            }).start();
        }
        log.info("等待线程执行完毕");
        countDownLatch.await();
        log.info("生成id数量:{}",sets.size());
        log.info("耗时:{}ms",System.currentTimeMillis()-start);


    }
}
