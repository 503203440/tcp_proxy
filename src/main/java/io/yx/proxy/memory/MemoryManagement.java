package io.yx.proxy.memory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * @author YX
 * @date 2021/8/2
 * 内存管理
 */
public class MemoryManagement {

    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    public static void memoryInfo() {


        final MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();

        final MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

        System.out.println("堆内存状态：" + MemoryFormatUtil.formatMemoryUsageInfo(heapMemoryUsage));

        System.out.println("非堆内存状态：" + MemoryFormatUtil.formatMemoryUsageInfo(nonHeapMemoryUsage));

        System.out.println("系统总共占用内存：" + MemoryFormatUtil.format(heapMemoryUsage.getUsed() + nonHeapMemoryUsage.getUsed()));

    }

    /**
     * GC
     */
    public static void gc() {
        memoryMXBean.gc();
    }

}
