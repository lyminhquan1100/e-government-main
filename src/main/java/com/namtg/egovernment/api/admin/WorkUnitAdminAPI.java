package com.namtg.egovernment.api.admin;

import com.namtg.egovernment.config.security.CustomUserDetail;
import com.namtg.egovernment.dto.response.ServerResponseDto;
import com.namtg.egovernment.dto.unit.WorkUnitDto;
import com.namtg.egovernment.service.unit.WorkUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/work_unit/")
public class WorkUnitAdminAPI {
    @Autowired
    private WorkUnitService workUnitService;

    @GetMapping("/getWorkUnitInfo")
    public ResponseEntity<ServerResponseDto> getWorkUnitInfo(@AuthenticationPrincipal CustomUserDetail currentUser,
                                                             @RequestParam String type) {
        return ResponseEntity.ok(ServerResponseDto.successWithData(
                workUnitService.getWorkUnitInfo(currentUser != null ? currentUser.getId() : null, type)));
    }

    @PostMapping("/saveWorkUnit")
    public ResponseEntity<ServerResponseDto> saveWorkUnit(@RequestBody List<WorkUnitDto> listWorkUnit) {
        return ResponseEntity.ok(workUnitService.saveWorkUnit(listWorkUnit));
    }

    @GetMapping("/getListWorkUnit")
    public ResponseEntity<ServerResponseDto> getListWorkUnit(@AuthenticationPrincipal CustomUserDetail customUserDetail) {
        if (customUserDetail == null) {
            return ResponseEntity.ok(ServerResponseDto.ERROR);
        }
        return ResponseEntity.ok(workUnitService.getListWorkUnitByUserId(customUserDetail.getId()));
    }
}
