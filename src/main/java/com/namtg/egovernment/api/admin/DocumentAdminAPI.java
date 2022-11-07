package com.namtg.egovernment.api.admin;

import com.namtg.egovernment.config.security.CustomUserDetail;
import com.namtg.egovernment.dto.document.DocumentDto;
import com.namtg.egovernment.dto.document.SaveApproveDocument;
import com.namtg.egovernment.dto.document.SendDocumentDto;
import com.namtg.egovernment.dto.response.ResponseCase;
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

import java.text.ParseException;

@RestController
@RequestMapping("/api/admin/document")
public class DocumentAdminAPI {
    @Autowired
    private DocumentService documentService;

    @GetMapping("/getPage")
    public Page<DocumentEntity> getPage(@RequestParam int size, @RequestParam int page,
                                        @RequestParam String sortDir, @RequestParam String sortField,
                                        @RequestParam String keyword,
                                        @AuthenticationPrincipal CustomUserDetail customUserDetail) {
        Pageable pageable = PageableUtils.from(page, size, sortDir, sortField);
        return documentService.getPage(keyword, customUserDetail.getId(), pageable);
    }

    @PostMapping("/save")
    public ResponseEntity<ServerResponseDto> save(@RequestBody DocumentDto postDto,
                                                  @AuthenticationPrincipal CustomUserDetail customUserDetail)
            throws ParseException {
        return ResponseEntity.ok(documentService.save(customUserDetail.getId(), postDto));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ServerResponseDto> detail(@PathVariable Long id,
                                                    @AuthenticationPrincipal CustomUserDetail customUserDetail) {
        return ResponseEntity.ok(documentService.detail(id, customUserDetail.getId()));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<ServerResponseDto> delete(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.delete(id));
    }

    @GetMapping("/getContent")
    public ResponseEntity<ServerResponseDto> getContent(@RequestParam Long id) {
        return ResponseEntity.ok(new ServerResponseDto(ResponseCase.SUCCESS, documentService.getContent(id)));
    }

    @GetMapping("/getDataForApproveDocument")
    public ResponseEntity<ServerResponseDto> getDataForApproveDocument(@RequestParam Long documentId) {
        return ResponseEntity.ok(documentService.getDataForApproveDocument(documentId));
    }

    @PostMapping("/saveApproveContent")
    public ResponseEntity<ServerResponseDto> saveApproveContent(@RequestBody SaveApproveDocument saveApproveDocument,
                                                                @AuthenticationPrincipal CustomUserDetail currentUser) {
        return ResponseEntity.ok(documentService.saveApproveContent(saveApproveDocument, currentUser.getId()));
    }

    @PostMapping("/sendDocument")
    public ResponseEntity<ServerResponseDto> sendDocument(@RequestBody SendDocumentDto sendDocumentDto,
                                                          @AuthenticationPrincipal CustomUserDetail currentUser) {
        return ResponseEntity.ok(documentService.sendDocument(sendDocumentDto, currentUser.getId()));
    }

    @PostMapping("/receive")
    public ResponseEntity<ServerResponseDto> receiveDocument(@RequestParam Long documentId,
                                                             @RequestParam Long workUnitIdReceive) {
        return ResponseEntity.ok(documentService.receiveDocument(documentId, workUnitIdReceive));
    }

    @PostMapping("/changeEnable/{documentId}")
    public ResponseEntity<ServerResponseDto> changeEnable(@PathVariable Long documentId) {
        return ResponseEntity.ok(documentService.changeEnable(documentId));
    }

    @PostMapping("/changePublic/{documentId}")
    public ResponseEntity<ServerResponseDto> publicEnable(@PathVariable Long documentId) {
        return ResponseEntity.ok(documentService.publicDocument(documentId));
    }
}
