package com.namtg.egovernment.api.admin;

import com.namtg.egovernment.dto.response.ServerResponseDto;
import com.namtg.egovernment.service.old_version_document.OldVersionDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/old_version_document")
public class OldVersionDocumentAdminAPI {

    @Autowired
    private OldVersionDocumentService oldVersionDocumentService;

    @GetMapping("/getListOldVersion")
    public ResponseEntity<ServerResponseDto> getListOldVersion(@RequestParam Long documentId) {
        return ResponseEntity.ok(ServerResponseDto.successWithData(oldVersionDocumentService.getListOldVersion(documentId)));
    }

    @GetMapping("/getContentOldVersion")
    public ResponseEntity<ServerResponseDto> getContentOldVersion(@RequestParam Long oldVersionId) {
        return ResponseEntity.ok(oldVersionDocumentService.getContentOldVersion(oldVersionId));
    }
}
