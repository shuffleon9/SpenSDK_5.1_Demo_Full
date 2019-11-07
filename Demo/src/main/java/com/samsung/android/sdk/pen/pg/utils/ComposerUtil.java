package com.samsung.android.sdk.pen.pg.utils;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.lang.reflect.Field;

public class ComposerUtil {
    private static Integer SEM_INT = null;

    static {
        try {
            Field field = android.os.Build.VERSION.class.getField("SEM_INT");
            SEM_INT = (Integer)field.getInt(android.os.Build.VERSION.class);
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
    }

    public final static boolean isSemDevice() {
        return SEM_INT == null ? false : true;
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }
}
