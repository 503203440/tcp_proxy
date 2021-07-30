package io.yx.proxy;

import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;

public class ByteFormatUtil {

    public static String formatSize(long size) {
        //获取到的size为：1705230
        int GB = 1024 * 1024 * 1024;//定义GB的计算常量
        int MB = 1024 * 1024;//定义MB的计算常量
        int KB = 1024;//定义KB的计算常量
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String resultSize = "";
        if (size / GB >= 1) {
            //如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) GB) + "GB   ";
        } else if (size / MB >= 1) {
            //如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) MB) + "MB   ";
        } else if (size / KB >= 1) {
            //如果当前Byte的值大于等于1KB
            resultSize = df.format(size / (float) KB) + "KB   ";
        } else {
            resultSize = size + "B   ";
        }
        return resultSize;
    }


    public static String memoryUsageToString(MemoryUsage memoryUsage) {
        String init = formatSize(memoryUsage.getInit());
        String committed = formatSize(memoryUsage.getCommitted());
        String max = formatSize(memoryUsage.getMax());
        String used = formatSize(memoryUsage.getUsed());
        return String.format("init:%s  committed:%s  max:%s  used:%s", init, committed, max, used);
    }


}