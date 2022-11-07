package com.namtg.egovernment.api.admin;

import com.namtg.egovernment.config.security.CustomUserDetail;
import com.namtg.egovernment.dto.document.ContentDocumentDto;
import com.namtg.egovernment.dto.response.ServerResponseDto;
import com.namtg.egovernment.service.old_version_document.ContentDocumentUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/content_document_update")
public class ContentDocumentUpdateAdminAPI {
    @Autowired
    private ContentDocumentUpdateService contentDocumentUpdateService;

    @PostMapping("/updateContent")
    public ResponseEntity<ServerResponseDto> updateContent(@AuthenticationPrincipal CustomUserDetail currentUserLogin,
                                                           @RequestBody ContentDocumentDto contentDocumentDto) {
        return ResponseEntity.ok(contentDocumentUpdateService.updateContent(contentDocumentDto, currentUserLogin.getId()));
    }

    @GetMapping("/getContentUpdated")
    public ResponseEntity<ServerResponseDto> getContentUpdated(@AuthenticationPrincipal CustomUserDetail currentUserLogin,
                                                               @RequestParam Long documentId) {
        return ResponseEntity.ok(ServerResponseDto
                .successWithData(contentDocumentUpdateService.getContentUpdated(documentId, currentUserLogin.getId())));
    }
}
