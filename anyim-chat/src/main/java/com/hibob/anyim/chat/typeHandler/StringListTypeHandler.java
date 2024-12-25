package com.hibob.anyim.chat.typeHandler;

import com.alibaba.fastjson.TypeReference;
import com.hibob.anyim.chat.typeHandler.base.ListTypeHandler;

import java.util.List;

public class StringListTypeHandler extends ListTypeHandler<String> {
    @Override
    protected TypeReference<List<String>> specificType() {
        return new TypeReference<List<String>>() {};
    }
}
