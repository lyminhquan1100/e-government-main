package com.namtg.egovernment.dto.unit;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WorkUnitDto {
    private Long id;
    private int level;
    private Long workUnitLevelAboveId;
    private Long unitId;
    private String name;
}
