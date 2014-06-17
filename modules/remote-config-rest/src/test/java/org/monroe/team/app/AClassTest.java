package org.monroe.team.app;

import junit.framework.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * User: MisterJBee
 * Date: 1/7/14 Time: 3:54 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class AClassTest {

    @Test
    public void dateTest(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm [dd-MM-yyyy]");
        Calendar now = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        System.out.println(dateFormat.format(now.getTime()));
        now.add(Calendar.SECOND, 30 * 60);
        System.out.println(dateFormat.format(now.getTime()));
    }

}
