package com.namtg.egovernment.dto.document;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestCreatePostDto {
    private Long ministryId;
    private Long agenciesId;
    private String title;
    private String content;
    private String target;
    private String closingDeadline;
    private boolean isCanCreatePost;
}
