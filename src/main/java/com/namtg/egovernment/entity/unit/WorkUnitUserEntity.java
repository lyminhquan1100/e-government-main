package com.namtg.egovernment.entity.unit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "work_unit_user")
public class WorkUnitUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long workUnitId;
    private Long userId;
    private boolean isCanReceiveDocument;
    private boolean isCanManageUser;

    @Transient
    private String nameWorkUnit;

    public WorkUnitUserEntity(Long workUnitId, Long userId, Boolean isCanReceiveDocument, Boolean isCanManageUser) {
        this.workUnitId = workUnitId;
        this.userId = userId;
        this.isCanReceiveDocument = isCanReceiveDocument;
        this.isCanManageUser = isCanManageUser;
    }
}
