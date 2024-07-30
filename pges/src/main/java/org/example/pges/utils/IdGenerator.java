package org.example.pges.utils;

import com.github.yitter.contract.IdGeneratorOptions;
import com.github.yitter.idgen.YitIdHelper;

public class IdGenerator {
    /**
     * 生成唯一主键
     * @return
     */
    public static Long generateId() {
        IdGeneratorOptions options = new IdGeneratorOptions((short) 0);
        YitIdHelper.setIdGenerator(options);
        return  YitIdHelper.nextId();
    }
}
