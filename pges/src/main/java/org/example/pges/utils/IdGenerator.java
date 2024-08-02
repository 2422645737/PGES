package org.example.pges.utils;

import com.github.yitter.contract.IdGeneratorOptions;
import com.github.yitter.idgen.YitIdHelper;

public class IdGenerator {
    static {
        IdGeneratorOptions options = new IdGeneratorOptions((short) 0);
        YitIdHelper.setIdGenerator(options);
    }
    /**
     * 生成唯一主键
     * @return
     */
    public static Long generateId() {
        return  YitIdHelper.nextId();
    }
}