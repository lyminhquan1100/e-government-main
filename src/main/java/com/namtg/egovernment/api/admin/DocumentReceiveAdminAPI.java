package com.namtg.egovernment.api.admin;

import com.namtg.egovernment.dto.response.ServerResponseDto;
import com.namtg.egovernment.service.document.DocumentReceiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/document-receive")
public class DocumentReceiveAdminAPI {

    @Autowired
    private DocumentReceiveService documentReceiveService;

    @GetMapping("/get-list-info-work-unit-sent")
    public ResponseEntity<ServerResponseDto> getListInfoWorkUnitSent(@RequestParam Long documentId) {
        return ResponseEntity.ok(documentReceiveService.getListInfoWorkUnitSent(documentId));
    }
}
