package com.namtg.egovernment.dto.unit;

import com.namtg.egovernment.entity.unit.WorkUnitEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WrapUnitLv1 {
    private Long unitLv1Id;
    private String unitLv1Name;
    private List<WorkUnitEntity> listWorkUnit;
}
