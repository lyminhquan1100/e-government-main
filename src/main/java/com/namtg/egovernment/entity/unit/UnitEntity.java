package com.namtg.egovernment.entity.unit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "unit")
public class UnitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int level;
    private Long unitLevelAboveId;
    private String name;
    @Transient
    private List<UnitEntity> listChild;

    public UnitEntity(Long id, int level, Long unitLevelAboveId, String name) {
        this.id = id;
        this.level = level;
        this.unitLevelAboveId = unitLevelAboveId;
        this.name = name;
    }
}
