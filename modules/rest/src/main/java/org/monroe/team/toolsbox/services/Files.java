package org.monroe.team.toolsbox.services;

public class Files {

    public static enum Units{

        Kilobyte(1024),
        Byte(1),
        Megabyte(1024*1024);

        private final double byteInOneUnit;

        Units(long byteInOneUnit) {
            this.byteInOneUnit = byteInOneUnit;
        }

        double fromBytes(double size){
            return size / byteInOneUnit;
        }

        long toBytes(double size){
            return (long) (size * byteInOneUnit);
        }
    }

    public static long convertFromUnits(double size, Units unit){
         return unit.toBytes(size);
    }


    public static double convertToUnits(double bytes, Units unit){
        return unit.fromBytes(bytes);
    }
}
