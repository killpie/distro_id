package com.killpie.distro_id.code;

import cn.hutool.core.net.Ipv4Util;
import com.killpie.distro_id.enums.MachineConfigEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Auther: killpie
 * @Date: 2023/8/17 22:51
 * @Description: 唯一id生成器
 *64位ID (1(符号位)+41(毫秒)+8(业务编码)+5(机器ID)+9(重复累加))
 */
@Slf4j
public class IdCreater {

    //初始时间 2023-08-17 00:00:00
    private final static long idEpoch = 1692201600000L;
    //机器标识位数
    private final static long workerIdBits = 5L;
    //业务编码8位方便业务拓展
    private final static long businessIdBits = 8L;
    //机器ID最大值 左移5位后 相当于是1111100000, 然后取反就成了0000011111, 即31
    private final static long MAX_WORKER_ID = ~(-1L << workerIdBits);
    //业务编码最大值
    private final static long MAX_BUSINESS_ID = ~(-1L << businessIdBits);
    //毫秒内自增位
    private final static long sequenceBits = 9L;
    //机器ID偏左移14位
    private final static long businessWorkerIdShift = sequenceBits+workerIdBits;
    //时间毫秒左移22位
    private final static long timestampLeftShift = sequenceBits+workerIdBits+businessIdBits;
    //自增序列掩码
    private final static long sequenceMask = ~(-1L << sequenceBits);
    //上次生成的时间戳
    private static volatile long lastTimestamp = -1L;
    //初始序列号
    private volatile long sequence = 0L;

    private static long CURRENT_MACHINE_ID;
    private static long BUSINESS_ID = 0;

    /**
     * 此服务强依赖机器id，如果id有问题，程序直接退出。
     */
    static {
        try{
            //获取本机ip
            InetAddress localHost = InetAddress.getLocalHost();
            String ipAddress = localHost.getHostAddress();
            int workerId = MachineConfigEnum.getCode(ipAddress);
            CURRENT_MACHINE_ID = workerId;
            log.info("机器ID={} IP={}",workerId,ipAddress);
        } catch (UnknownHostException e) {
            log.error("get server ip error.", e);
            System.exit(-1);
        }catch (Exception e){
            log.error("init error.", e);
            System.exit(-1);
        }
    }

    public IdCreater(){
    }



    public static long getMaxWorkId(){
        return MAX_WORKER_ID;
    }

    public long nextId(){
        return this.nextId(false,0);
    }

    public long nextId(long businessId){
        Assert.isTrue(businessId <= MAX_BUSINESS_ID && businessId >= 0,String.format("businessId必须大于等于零且小于等于%s",MAX_BUSINESS_ID));
        return this.nextId(true, businessId & MAX_BUSINESS_ID);
    }


    /**
     * 获取下一个ID
     * 64位ID (1(符号位)+41(毫秒)+8(业务编码)+5(机器ID)+9(重复累加))
     * @param isPadding 是否填充业务ID
     * @param businessId 业务ID
     * @return
     */
    private synchronized long nextId(boolean isPadding, long businessId){
        long timestamp = timeGen();
        if (isPadding){
            BUSINESS_ID = businessId;
        }

        if (timestamp < lastTimestamp){
            log.error("Clock moved backwards.  Refusing to generate id for " + (timestamp - lastTimestamp) + " milliseconds");
            throw new RuntimeException("Clock moved backwards");
        }

        if (lastTimestamp == timestamp){
            //在当前毫秒内, 且没溢出则加一
            sequence = ++sequence & sequenceMask;
            if (sequence == 0){
                //当前毫秒计数已满,等带下一秒
                timestamp = tilNextMills(lastTimestamp);
            }
        }else {
            //新的毫秒从0开始
            sequence = 0;
        }

        lastTimestamp = timestamp;

        //ID偏移组合生成最终ID,并返回ID
        return ((timestamp-idEpoch) << timestampLeftShift)
                | (businessId << businessWorkerIdShift)
                | (CURRENT_MACHINE_ID << sequenceBits)
                | sequence;

    }


    /**
     * 直到下一个毫秒数
     * @return
     */
    private long tilNextMills(final long lastTimestamp){
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp){
            timestamp = this.timeGen();
        }
        return timestamp;
    }

    private long timeGen(){
        return System.nanoTime();
    }

}
