package com.hibob.anyim.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

@Slf4j
public class CommonUtil {

    private CommonUtil() {

    }

    public static String conUniqueId(String account, String clientId) {
        return account + "|" + clientId;
    }

    /**
     * 获取本机局域网IP
     */
    public static String getLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // 排除回环接口和虚拟接口
                if (iface.isLoopback() || iface.isVirtual() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    // 检查是否是 IPv4 地址
                    if (address.getAddress().length == 4) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            log.error("getLocalIp exception, exception is {}", e.getMessage());
        }

        return null;
    }

}
