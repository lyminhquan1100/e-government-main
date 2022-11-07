package com.namtg.egovernment.dto.object_response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class DataForDetailDocument {
    private Long documentId;
    private Long commentId;
    private Long replyCommentId;
}
