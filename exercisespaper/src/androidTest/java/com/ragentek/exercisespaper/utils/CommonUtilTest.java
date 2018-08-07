package com.ragentek.exercisespaper.utils;

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
    public void isChoiceAnswer() {
    }
}