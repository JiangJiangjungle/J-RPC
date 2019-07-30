package com.jsj.rpc.common.config;

import com.jsj.rpc.common.codec.CodeC;
import com.jsj.rpc.common.codec.DefaultCodeC;

/**
 * @author jiangshenjie
 */
public class Configuration {

    /**
     * 编解码器
     */
    protected CodeC codeC = DefaultCodeC.getInstance();

    public CodeC getCodeC() {
        return codeC;
    }

    public void setCodeC(CodeC codeC) {
        this.codeC = codeC;
    }
}
