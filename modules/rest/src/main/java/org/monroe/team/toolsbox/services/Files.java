package org.monroe.team.toolsbox.services;

import java.text.DecimalFormat;

public class Files {


    public static enum Units{

        Kilobyte(1024, "kB"),
        Byte(1, "B"),
        Megabyte(1024*1024, "mB");

        private final double byteInOneUnit;
        private final String ext;

        Units(long byteInOneUnit, String ext) {
            this.byteInOneUnit = byteInOneUnit;
            this.ext = ext;
        }

        double fromBytes(double size){
            return size / byteInOneUnit;
        }

        long toBytes(double size){
            return (long) (size * byteInOneUnit);
        }

        public String getExt() {
            return ext;
        }
    }

    public static long convertFromUnits(double size, Units unit){
         return unit.toBytes(size);
    }


    public static double convertToUnits(double bytes, Units unit){
        return unit.fromBytes(bytes);
    }

    public static String convertToUnitsAsString(long byteCount, Units unit) {
        double answer = convertToUnits(byteCount, unit);
        return new DecimalFormat("##.##").format(answer) + " " + unit.getExt();
    }

}
