package com.chedaojunan.report.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class DateUtilsTest {

    DateUtils dateUtils = new DateUtils();
    long localTime = 0L;
    long utcTime = 0L;

    @Before
    public void init() throws IOException {
        localTime = 1521446239000L;
        utcTime = 1521417439000L;
    }

    @Test
    public void testGetYMD() throws Exception {
        Assert.assertEquals("20180320", dateUtils.getYMD());
    }

    @Test
    public void testGetHM() throws Exception {
        Assert.assertEquals("17_52", dateUtils.getHM());
    }

    @Test
    public void testGetUTCTimeFromLocal() throws Exception {
        Assert.assertEquals(1521417439L, dateUtils.getUTCTimeFromLocal(localTime));
    }

    @Test
    public void testGetLocalTimeFromUTC() throws Exception {
        Assert.assertEquals(1521446239000L, dateUtils.getLocalTimeFromUTC(utcTime));
    }

}