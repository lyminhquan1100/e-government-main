package com.namtg.egovernment.api.user;

import com.namtg.egovernment.config.security.CustomUserDetail;
import com.namtg.egovernment.dto.document.ConclusionDto;
import com.namtg.egovernment.dto.response.ServerResponseDto;
import com.namtg.egovernment.entity.document.DocumentEntity;
import com.namtg.egovernment.service.document.DocumentService;
import com.namtg.egovernment.util.PageableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/document")
public class DocumentAPI {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/getPage")
    public Page<DocumentEntity> getPage(@RequestParam("size") int size,
                                        @RequestParam("page") int page,
                                        @RequestParam String keyword,
                                        @RequestParam String orderBy,
                                        @RequestParam Long workUnitId,
                                        @AuthenticationPrincipal CustomUserDetail customUserDetail) {
        Pageable pageable = PageableUtils.from(page, size);
        return documentService.getPageForUser(customUserDetail != null ? customUserDetail.getId() : null,
                workUnitId, keyword, orderBy, pageable);
    }

    @GetMapping("/detail/{documentId}")
    public ResponseEntity<ServerResponseDto> detail(@PathVariable Long documentId,
                                                    @AuthenticationPrincipal CustomUserDetail customUserDetail) {
        Long currentUserId = customUserDetail != null ? customUserDetail.getId() : null;
        return ResponseEntity.ok(documentService.detailForUser(documentId, currentUserId));
    }

    @PostMapping("/plusView/{postId}")
    public ResponseEntity<ServerResponseDto> plusView(@PathVariable Long postId) {
        return ResponseEntity.ok(documentService.plusView(postId));
    }

    @PostMapping("/saveConclusion")
    public ResponseEntity<ServerResponseDto> saveConclusion(@RequestBody ConclusionDto conclusionDto) {
        return ResponseEntity.ok(documentService.saveConclusion(conclusionDto));
    }

}
