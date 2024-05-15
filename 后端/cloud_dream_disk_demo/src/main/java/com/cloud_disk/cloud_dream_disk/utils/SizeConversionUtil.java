package com.cloud_disk.cloud_dream_disk.utils;

import io.lettuce.core.StrAlgoArgs;

import java.text.DecimalFormat;

public class SizeConversionUtil {
    public static String ByteToKb(Double Byte) {
        return (Byte / 1024) + " KB";
    }

    public static String ByteToMb(Double Byte) {
        return (Byte / 1024 / 1024) + "MB";
    }

    public static String ByteToGb(Double Byte) {
        return (Byte / 1024 / 1024 / 1024) + "GB";
    }

    public static String ByteToTb(Double Byte) {
        return (Byte / 1024 / 1024 / 1024 / 1024) + "TB";
    }

    public static String ByteAutomaticConversion(Double Byte) {
        String Size = null;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        for (int i = 4; i > 0; i--) {
            if ((Byte / Math.pow(1024, i)) >= 1) {
                switch (i) {
                    case 1:
                        Size = decimalFormat.format(Byte / Math.pow(1024, i)) + " KB";
                        break;
                    case 2:
                        Size = decimalFormat.format(Byte / Math.pow(1024, i)) + " MB";
                        break;
                    case 3:
                        Size = decimalFormat.format(Byte / Math.pow(1024, i)) + " GB";
                        break;
                    case 4:
                        Size = decimalFormat.format(Byte / Math.pow(1024, i)) + " TB";
                        break;
                }
                break;
            }
            if (Byte / Math.pow(1024, i) > 0.00) {
                Size = Byte + " B";
            } else {
                Size = decimalFormat.format(Byte / Math.pow(1024, i)) + " B";
            }
        }
        return Size;
    }

    public static String KbToMb(Double Kb) {
        return (Kb / 1024) + " MB";
    }

    public static String KbToGb(Double Kb) {
        return (Kb / 1024 / 1024) + " GB";
    }

    public static String KbToTb(Double Kb) {
        return (Kb / 1024 / 1024 / 1024) + " TB";
    }

    public static String MbToGb(Double Mb) {
        return (Mb / 1024) + " GB";
    }

    public static String MbToTb(Double Mb) {
        return (Mb / 1024 / 1024) + " TB";
    }

    public static String GbToTb(Double Gb) {
        return (Gb / 1024) + " TB";
    }
}
