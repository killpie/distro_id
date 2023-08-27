package com.killpie.distro_id.enums;

import com.killpie.distro_id.code.IdCreater;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @Auther: killpie
 * @Date: 2023/8/17 22:51
 * @Description: 机器ID枚举
 */
@Slf4j
public enum MachineConfigEnum {
    IP_1(1, "127.0.0.1"),
    IP_2(2, "10.0.0.2"),
    IP_3(3, "10.0.0.3"),
    IP_4(4, "10.0.0.4"),
    ;
    private final Logger logger = LoggerFactory.getLogger(MachineConfigEnum.class);

    private final int code;
    private final String ip;

    MachineConfigEnum(int code, String ip) {
        this.code = code;
        this.ip = ip;
    }

    /**
     * 根据IP获取机器ID
     * @return
     */
    public static int getCode(String ip){
        check();
        int code = Arrays.stream(values())
                .filter(v->v.ip.equals(ip))
                .findFirst()
                .orElseThrow().getCode();
        if (code >= IdCreater.getMaxWorkId() || code < 0){
            log.error("the id({}) of this server is invalid, id must bigger than 0 and smaller than 32.",code);
            System.exit(-1);
        }
        return code;
    }

    public int getCode() {
        return code;
    }

    public String getIp() {
        return ip;
    }
    /**
     * 检查枚举的code和ip是否重复
     * @return
     */
    public static boolean check() {
        Set<Integer> codeSet = new HashSet<>();
        Set<String> ipSet = new HashSet<>();

        for (MachineConfigEnum value : values()) {
            if (!codeSet.add(value.getCode()) || !ipSet.add(value.getIp())) {
                log.error("MachineConfigEnum code or ip is duplicate, code:{}, ip:{}", value.getCode(), value.getIp());
                System.exit(-1);
            }
        }
        return true;
    }
    public static void main(String[] args) {
        System.out.println(MachineConfigEnum.IP_1.getCode());
        System.out.println(MachineConfigEnum.IP_1.getIp());
        System.out.println(MachineConfigEnum.check());
    }
}
