package com.namtg.egovernment.dto.unit;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UnitDto {
    private Long id;
    private int level;
    private Long unitLevelAboveId;
    private String name;
}
