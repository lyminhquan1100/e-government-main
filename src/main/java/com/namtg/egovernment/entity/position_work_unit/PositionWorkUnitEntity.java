package com.namtg.egovernment.entity.position_work_unit;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "position_work_unit")
public class PositionWorkUnitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long workUnitId;
    private String namePosition;
}
