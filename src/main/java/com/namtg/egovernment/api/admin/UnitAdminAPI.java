package com.namtg.egovernment.api.admin;

import com.namtg.egovernment.dto.response.ServerResponseDto;
import com.namtg.egovernment.dto.unit.UnitDto;
import com.namtg.egovernment.service.unit.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/unit/")
public class UnitAdminAPI {
    @Autowired
    private UnitService unitService;

    @GetMapping("/getUnitInfo")
    public ResponseEntity<ServerResponseDto> getUnitInfo() {
        return ResponseEntity.ok(ServerResponseDto.successWithData(unitService.getUnitInfo()));
    }

    @PostMapping("/saveUnit")
    public ResponseEntity<ServerResponseDto> saveUnit(@RequestBody List<UnitDto> listUnit) {
        return ResponseEntity.ok(unitService.saveUnit(listUnit));
    }

    @GetMapping("/getListUnitChild")
    public ResponseEntity<ServerResponseDto> getListUnitChild(@RequestParam Long unitParentId) {
        return ResponseEntity.ok(ServerResponseDto.successWithData(unitService.getListUnitChild(unitParentId)));
    }
}
