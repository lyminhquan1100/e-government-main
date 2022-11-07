package com.namtg.egovernment.util;

import java.util.*;
import java.util.stream.Collectors;

public class CollectionUtils {
    public static List<Long> convertListIdStrToListLong(String listStrId) {
        if (listStrId.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays
                .stream(listStrId.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public static Set<Long> convertListIdStrToSetLong(String listStrId) {
        if (listStrId.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays
                .stream(listStrId.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    public static boolean collectionIsNullOrEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean notEmpty(Collection collection) {
        return collection != null && !collection.isEmpty();
    }
}
