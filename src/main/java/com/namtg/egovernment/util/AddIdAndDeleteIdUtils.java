package com.namtg.egovernment.util;

import java.util.List;
import java.util.stream.Collectors;

public class AddIdAndDeleteIdUtils {
    public static List<Long> getListIdAdd(List<Long> listIdBefore, List<Long> listIdAfter) {
        return listIdAfter.stream()
                .filter(id -> !listIdBefore.contains(id))
                .collect(Collectors.toList());
    }

    public static List<Long> getListIdDelete(List<Long> listIdBefore, List<Long> listIdAfter) {
        return listIdBefore.stream()
                .filter(id -> !listIdAfter.contains(id))
                .collect(Collectors.toList());
    }
}
