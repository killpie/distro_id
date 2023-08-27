package com.killpie.distro_id.factory;

import com.killpie.distro_id.code.IdCreater;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Auther: killpie
 * @Date: 2023/8/20 21:55
 * @Description: 分布式ID工厂类
 */
public class IdFactory {
    private static volatile IdCreater IDC = null;

    private final static Lock LOCK = new ReentrantLock();

    public static IdCreater getInstance(){
        if (IDC != null){
            return IDC;
        }

        try {
            LOCK.lock();
            if (IDC != null){
                return IDC;
            }
            IDC = new IdCreater();
        }finally {
            LOCK.unlock();
        }

        return IDC;
    }
}
