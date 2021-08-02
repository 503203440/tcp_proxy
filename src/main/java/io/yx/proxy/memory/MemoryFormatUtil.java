package io.yx.proxy.memory;

import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;

/**
 * @author YX
 * @date 2021/8/2
 * 内存格式化显示
 */
public class MemoryFormatUtil {

    public static String format(long size) {
        StringBuilder bytes = new StringBuilder();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        } else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        } else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        } else {
            if (size <= 0) {
                bytes.append("0B");
            } else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }


    public static String formatMemoryUsageInfo(MemoryUsage memoryUsage) {

        String init = format(memoryUsage.getInit());
        String used = format(memoryUsage.getUsed());
        String committed = format(memoryUsage.getCommitted());
        String max = format(memoryUsage.getMax());

        return String.format("\n\tinit:%s\n\tused:%s\n\tcommitted:%s\n\tmax:%s", init, used, committed, max);

    }


}
