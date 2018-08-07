package com.ragentek.factorypaper;

import com.ragentek.factorypaper.utils.CommonUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class CommonUtilTest {

    @Test
    public void isFloatEquals() {
    }

    @Test
    public void isDigit() {
    }

    @Test
    public void isNumber() {
        boolean result = CommonUtil.isNumber("1.");
        assertEquals(false, result);
        boolean result2 = CommonUtil.isNumber("1");
        assertEquals(true, result2);
        boolean result3 = CommonUtil.isNumber("11");
        assertEquals(true, result3);
        boolean result4 = CommonUtil.isNumber("1.1");
        assertEquals(true, result4);
        boolean result5 = CommonUtil.isNumber("1..1");
        assertEquals(false, result5);
    }
    @Test
    public void isDate() {
        boolean result = CommonUtil.isDate("1");
        assertEquals(true, result);
        boolean result1 = CommonUtil.isDate("11");
        assertEquals(true, result1);
        boolean result2 = CommonUtil.isDate("1月7日");
        assertEquals(true, result2);
        boolean result3 = CommonUtil.isDate("11月");
        assertEquals(true, result3);
        boolean result4 = CommonUtil.isDate("11月7日");
        assertEquals(true, result4);
        boolean result5 = CommonUtil.isDate("201891212年11月7日");
        assertEquals(true, result5);
    }
    @Test
    public void isChoiceAnswer() {
    }
}