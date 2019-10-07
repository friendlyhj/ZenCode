package org.openzen.zencode.java;

public interface IZSLogger {
    
    void info(String message);
    
    void debug(String message);
    
    void warning(String message);
    
    void error(String message);
    
    void throwingErr(String message, Throwable throwable);
    
    void throwingWarn(String message, Throwable throwable);
}
