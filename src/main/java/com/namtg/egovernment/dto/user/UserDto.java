package com.namtg.egovernment.dto.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Setter
@Getter
public class UserDto {
    private Long id;
    private MultipartFile avatar;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String password;
    private String birthday;
    private int gender;
    private Boolean isHaveAvatar;
    private List<WorkUnitIdAndAction> listWorkUnitIdAndAction;
    private boolean isManageUnit;
    private boolean isManageField;
    private boolean isManageReasonDeniedComment;
    private boolean isManageNews;

}
