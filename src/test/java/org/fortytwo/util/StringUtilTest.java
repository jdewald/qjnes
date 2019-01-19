package org.fortytwo.util;

import org.fortytwo.common.util.StringUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilTest {

    @Test
    public void testLeftPadShortText(){
        assertEquals("     kot", StringUtil.leftPad("kot", 8));
    }

    @Test
    public void testLeftPadLongText(){
        assertEquals("Commodore 64", StringUtil.leftPad("Commodore 64", 8));
    }
}
