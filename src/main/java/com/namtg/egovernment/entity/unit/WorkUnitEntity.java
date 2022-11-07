package com.namtg.egovernment.entity.unit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "work_unit")
@NoArgsConstructor
public class WorkUnitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int level;
    private Long workUnitLevelAboveId;
    private Long unitId;
    private String name;
    @Transient
    private List<WorkUnitEntity> listChild;
    @Transient
    private String unitName;

    public WorkUnitEntity(Long id, int level, Long workUnitLevelAboveId, Long unitId, String name) {
        this.id = id;
        this.level = level;
        this.workUnitLevelAboveId = workUnitLevelAboveId;
        this.unitId = unitId;
        this.name = name;
    }
}
