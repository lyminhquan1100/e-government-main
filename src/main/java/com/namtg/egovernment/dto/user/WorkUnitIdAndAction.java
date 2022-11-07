package com.namtg.egovernment.dto.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WorkUnitIdAndAction {
    private Long workUnitId;
    private boolean isCanReceiveDocument;
    private boolean isCanManageUser;
}
