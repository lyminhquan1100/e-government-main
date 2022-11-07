package com.namtg.egovernment.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServerResponseDto {

    private ResponseStatus status;
    private Object data;

    public static ServerResponseDto SUCCESS = new ServerResponseDto(ResponseCase.SUCCESS);
    public static ServerResponseDto ERROR = new ServerResponseDto(ResponseCase.ERROR);

    public static ServerResponseDto successWithData(Object data) {
        return new ServerResponseDto(ResponseCase.SUCCESS, data);
    }

    public ServerResponseDto(ResponseStatus responseStatus) {
        this.status = responseStatus;
    }

    public ServerResponseDto(ResponseStatus responseStatus, Object data) {
        this.status = responseStatus;
        this.data = data;
    }

}
