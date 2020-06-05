package org.openzen.zenscript.scriptingexample.tests.actual_test.control_structures;

import org.junit.jupiter.api.*;
import org.openzen.zenscript.scriptingexample.tests.helpers.*;

public class LoopTest extends ZenCodeTest {
    
    @Test
    public void validateOutput() {
        addScript("for i in 0 .. 100 println(i);");
        executeEngine();
        
        logger.assertPrintOutputSize(100);
        for(int i = 0; i < 100; i++) {
            logger.assertPrintOutput(i, String.valueOf(i));
        }
    }
}
