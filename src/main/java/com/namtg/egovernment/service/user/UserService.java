package com.namtg.egovernment.service.user;

import com.google.common.collect.Lists;
import com.namtg.egovernment.dto.EmailTemplate;
import com.namtg.egovernment.dto.response.ResponseCase;
import com.namtg.egovernment.dto.response.ServerResponseDto;
import com.namtg.egovernment.dto.user.IdAndName;
import com.namtg.egovernment.dto.user.UserDto;
import com.namtg.egovernment.entity.token.TokenEntity;
import com.namtg.egovernment.entity.unit.WorkUnitEntity;
import com.namtg.egovernment.entity.unit.WorkUnitUserEntity;
import com.namtg.egovernment.entity.user.RoleEntity;
import com.namtg.egovernment.entity.user.UserEntity;
import com.namtg.egovernment.enum_common.TypeRole;
import com.namtg.egovernment.repository.unit.WorkUnitRepository;
import com.namtg.egovernment.repository.unit.WorkUnitUserRepository;
import com.namtg.egovernment.repository.user.UserRepository;
import com.namtg.egovernment.service.AmazonService;
import com.namtg.egovernment.service.EmailService;
import com.namtg.egovernment.service.token.TokenService;
import com.namtg.egovernment.service.unit.WorkUnitService;
import com.namtg.egovernment.service.unit.WorkUnitUserService;
import com.namtg.egovernment.util.Constants;
import com.namtg.egovernment.util.PageableUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    @Value("${host}")
    public String host;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AmazonService amazonService;

    @Autowired
    private WorkUnitUserService workUnitUserService;

    @Autowired
    private WorkUnitService workUnitService;

    @Autowired
    private WorkUnitRepository workUnitRepository;

    @Autowired
    private WorkUnitUserRepository workUnitUserRepository;

    private static final String FORMAT_BIRTHDAY = "dd/MM/yyyy";

    public UserEntity findByEmailAndPassword(String email, String password) {
        UserEntity userEntity = userRepository.findByEmailToCheckLogin(email);
        if (userEntity == null) {
            return null;
        }
        boolean isPasswordTrue = passwordEncoder.matches(password, userEntity.getPassword());
        return isPasswordTrue ? userEntity : null;
    }

    public Page<UserEntity> getPage(String keywordNameUser, String keywordNameWorkUnit,
                                    Long currentUserId,
                                    Pageable pageable) {
        List<UserEntity> listUserResult;
        if (isLevelGovernment(currentUserId)) {
            listUserResult = userRepository.getList(keywordNameUser);
        } else {
            List<Long> listWorkUnitId = workUnitUserRepository.getListWorkUnitId(currentUserId);
            List<WorkUnitEntity> listAllWorkUnit = workUnitRepository.getAllWorkUnit();
            addWorkUnitIdChild(listWorkUnitId, listAllWorkUnit);
            listUserResult = userRepository.getList(keywordNameUser, listWorkUnitId);
        }
        if (listUserResult.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }

        // set listWorkUnitName
        List<Long> listUserId = listUserResult
                .stream()
                .map(UserEntity::getId)
                .collect(Collectors.toList());
        Map<Long, List<Long>> mapListWorkUnitIdByUserId = workUnitUserService.getMapListWorkUnitIdByUserId(listUserId);
        Map<Long, String> mapNameWorkUnitByWorkUnitId = workUnitService.getMapNameWorkUnitByWorkUnitId();

        listUserResult.forEach(userEntity -> {
            List<Long> listWorkUnitId = mapListWorkUnitIdByUserId.getOrDefault(userEntity.getId(), Collections.emptyList());
            List<String> listWorkUnitName = Lists.newArrayListWithExpectedSize(listWorkUnitId.size());
            listWorkUnitId.forEach(workUnitId ->
                    listWorkUnitName.add(mapNameWorkUnitByWorkUnitId.getOrDefault(workUnitId, Constants.TEXT_EMPTY)));
            userEntity.setListWorkUnitName(listWorkUnitName);
        });
        listUserResult = listUserResult
                .stream()
                .filter(userEntity -> userEntity.getListWorkUnitName()
                        .stream()
                        .anyMatch(workUnitName -> StringUtils.containsIgnoreCase(workUnitName, keywordNameWorkUnit)))
                .collect(Collectors.toList());

        final int idxStart = PageableUtils.getIdxStartPage(pageable);
        final int idxEnd = PageableUtils.getIdxEndPage(idxStart, pageable, listUserResult);

        return new PageImpl<>(listUserResult.subList(idxStart, idxEnd), pageable, listUserResult.size());
    }

    private void addWorkUnitIdChild(List<Long> listWorkUnitIdHighestLv, List<WorkUnitEntity> listAllWorkUnit) {
        int lengthListHighestLv = listWorkUnitIdHighestLv.size();
        for (int i = 0; i < lengthListHighestLv; i++) {
            Long workUnitId = listWorkUnitIdHighestLv.get(i);
            List<Long> listWorkUnitIdChild = listAllWorkUnit
                    .stream()
                    .filter(wu -> Objects.equals(wu.getWorkUnitLevelAboveId(), workUnitId))
                    .map(WorkUnitEntity::getId)
                    .collect(Collectors.toList());
            if (!listWorkUnitIdChild.isEmpty()) {
                addWorkUnitIdChild(listWorkUnitIdChild, listAllWorkUnit);
            }
            listWorkUnitIdHighestLv.addAll(listWorkUnitIdChild);
        }
    }

    @Transactional
    public ServerResponseDto createOrUpdate(UserDto userDto) {
        if (userDto == null) {
            return ServerResponseDto.ERROR;
        }
        ServerResponseDto objectUserEntity = convertEntityToDto(userDto);
        int code = objectUserEntity.getStatus().code;
        if (code == 1600) {
            return new ServerResponseDto(ResponseCase.EMAIL_EXIST);
        } else if (code == 4) {
            return new ServerResponseDto(ResponseCase.ERROR);
        }
        UserEntity userEntity = (UserEntity) objectUserEntity.getData();
        setRoleForUser(userEntity, userDto);
        Long userId = userRepository.save(userEntity).getId();
        workUnitUserService.save(userId, userDto.getListWorkUnitIdAndAction());

        return ServerResponseDto.SUCCESS;
    }

    private void setRoleForUser(UserEntity userEntity, UserDto userDto) {
        List<TypeRole> listRoleName = new ArrayList<>(Arrays.asList(
                TypeRole.OWNER_DOCUMENT, TypeRole.VIEW_DOCUMENT,
                TypeRole.UPDATE_DOCUMENT, TypeRole.DELETE_DOCUMENT,
                TypeRole.APPROVE_DOCUMENT, TypeRole.SEND_DOCUMENT));
        if (userDto.isManageUnit()) {
            listRoleName.add(TypeRole.MANAGE_UNIT);
        }
        if (userDto.isManageField()) {
            listRoleName.add(TypeRole.MANAGE_FIELD);
        }
        if (userDto.isManageReasonDeniedComment()) {
            listRoleName.add(TypeRole.MANAGE_REASON_DENIED_COMMENT);
        }
        if (userDto.isManageNews()) {
            listRoleName.add(TypeRole.MANAGE_NEWS);
        }
        Set<RoleEntity> setRoles = roleService.getSetRoleByListName(listRoleName);
        userEntity.setRoles(setRoles);
    }

    private ServerResponseDto convertEntityToDto(UserDto userDto) {
        Long userId = userDto.getId();
        boolean isUpdate = userId != null;

        boolean isEmailExist = false;
        if (!isUpdate) {
            isEmailExist = isEmailExist(userDto.getEmail());
        }
        if (isEmailExist) {
            log.info("Email: {} exist in db", userDto.getEmail());
            return new ServerResponseDto(ResponseCase.EMAIL_EXIST);
        }

        UserEntity userEntity;
        if (isUpdate) {
            userEntity = userRepository.findByIdAndIsDeletedFalse(userId);
        } else {
            userEntity = new UserEntity();
            userEntity.setCreatedTime(new Date());
            userEntity.setStatus(1);
            userEntity.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        MultipartFile avatar = userDto.getAvatar();
        if (avatar != null) {
            String nameImage = avatar.getOriginalFilename();
            userEntity.setNameAvatar(nameImage);
            String urlImage = amazonService.uploadFile(avatar);
            userEntity.setUrlAvatar(urlImage);
        } else {
            Boolean isHaveAvatar = userDto.getIsHaveAvatar();
            if (isHaveAvatar == null || !isHaveAvatar) {
                userEntity.setNameAvatar(null);
            }
        }
        userEntity.setUpdatedTime(new Date());
        userEntity.setName(userDto.getName());
        userEntity.setEmail(userDto.getEmail());
        userEntity.setPhone(userDto.getPhone());
        userEntity.setAddress(userDto.getAddress());
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_BIRTHDAY);
        try {
            userEntity.setBirthday(sdf.parse(userDto.getBirthday()));
        } catch (ParseException e) {
            log.info("Exception when parse birthday: {}", userDto.getBirthday());
            return new ServerResponseDto(ResponseCase.ERROR);
        }
        userEntity.setGender(userDto.getGender());
        return new ServerResponseDto(ResponseCase.SUCCESS, userEntity);
    }

    private boolean isEmailExist(String email) {
        return userRepository.countEmail(email) > 0;
    }

    public ServerResponseDto detail(Long id) {
        UserEntity userEntity = userRepository.findByIdAndIsDeletedFalse(id);
        if (userEntity == null) {
            log.info("userEntity is null with userId: {} when view detail", id);
            return new ServerResponseDto(ResponseCase.ERROR);
        }
        List<WorkUnitUserEntity> listWorkUnitUser = workUnitUserRepository.getByUserId(userEntity.getId());

        userEntity.setMapWorkUnitIdWithIsCanReceiveDocument(listWorkUnitUser.stream()
                .collect(Collectors.toMap(WorkUnitUserEntity::getWorkUnitId, WorkUnitUserEntity::isCanReceiveDocument)));
        userEntity.setMapWorkUnitIdWithIsCanManageUser(listWorkUnitUser.stream()
                .collect(Collectors.toMap(WorkUnitUserEntity::getWorkUnitId, WorkUnitUserEntity::isCanManageUser)));
        return new ServerResponseDto(ResponseCase.SUCCESS, userEntity);
    }

    public ServerResponseDto changeStatus(Long id) {
        UserEntity userEntity = userRepository.findByIdAndIsDeletedFalse(id);
        if (userEntity == null) {
            log.info("userEntity is null with userId: {} when change status", id);
            return new ServerResponseDto(ResponseCase.ERROR);
        }
        userEntity.setStatus(userEntity.getStatus() == 1 ? 0 : 1);
        userEntity.setUpdatedTime(new Date());
        userRepository.save(userEntity);
        return new ServerResponseDto(ResponseCase.SUCCESS);
    }

    public UserEntity getById(Long userId) {
        return userRepository.getById(userId);
    }

    public String getNameUserById(Long userId) {
        return userRepository.getNameUserByUserId(userId);
    }

    public ServerResponseDto changePassword(Long adminId, String currentPassword, String newPassword) {
        UserEntity userEntity = userRepository.findByIdAndIsDeletedFalse(adminId);
        if (userEntity == null) {
            log.info("userEntity is null with userId: {} when change password", adminId);
            return new ServerResponseDto(ResponseCase.ERROR);
        }

        if (!passwordEncoder.matches(currentPassword, userEntity.getPassword())) {
            log.info("Input password not match with old password when change password");
            return new ServerResponseDto(ResponseCase.WRONG_PASSWORD);
        }

        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(userEntity);
        return new ServerResponseDto(ResponseCase.SUCCESS);
    }

    public ServerResponseDto forgotPassword(String email) {
        UserEntity userEntity = userRepository.findByEmailAndIsDeletedFalse(email);
        if (userEntity == null) {
            log.info("Not found userEntity with email: {}", email);
            return new ServerResponseDto(ResponseCase.EMAIL_NOT_FOUND);
        }

        String tokenString = tokenService.generateToken();
        String confirmationUrl = host + "confirmForgotPassword?token=" + tokenString;
        String contentEmailConfirm = "Đổi lại mật khẩu mới tại đường dẫn: " + confirmationUrl;
        String subject = "Quên mật khẩu";
        EmailTemplate emailTemplate = new EmailTemplate(email, subject, contentEmailConfirm);
        emailService.sendMail(emailTemplate);

        TokenEntity tokenEntity = new TokenEntity(userEntity.getId(), tokenString, 4);
        tokenService.save(tokenEntity);
        return new ServerResponseDto(ResponseCase.SUCCESS);
    }

    public boolean confirmForgotPassword(String token) {
        TokenEntity tokenEntity = tokenService.validateToken(token, 4);
        return tokenEntity != null;
    }

    @Transactional
    public ServerResponseDto setPassword(String token, String password) {
        TokenEntity tokenEntity = tokenService.validateToken(token, 4);
        if (tokenEntity == null) {
            log.info("tokenEntity is null with token: {} when set password", token);
            return new ServerResponseDto(ResponseCase.ERROR);
        }
        UserEntity userEntity = userRepository.findByIdAndIsDeletedFalse(tokenEntity.getUserId());
        userEntity.setPassword(passwordEncoder.encode(password));
        userRepository.save(userEntity);

        tokenService.deleteToken(tokenEntity);

        return new ServerResponseDto(ResponseCase.SUCCESS);
    }

    public ServerResponseDto getMyProfile(Long currentUserId) {
        UserEntity userEntity = getById(currentUserId);
        setInfoUser(userEntity);
        return new ServerResponseDto(ResponseCase.SUCCESS, userEntity);
    }

    private void setInfoUser(UserEntity userEntity) {
        int genderInt = userEntity.getGender();
        if (genderInt == 1) {
            userEntity.setGenderStr("Nam");
        } else if (genderInt == 2) {
            userEntity.setGenderStr("Nữ");
        } else {
            userEntity.setGenderStr("Khác");
        }

        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_BIRTHDAY);
        if (userEntity.getBirthday() != null) {
            userEntity.setBirthdayStr(sdf.format(userEntity.getBirthday()));
        } else {
            userEntity.setBirthdayStr("");
        }
        userEntity.setListWorkUnit(workUnitUserService.getListWorkUnit(userEntity.getId()));
    }

    public ServerResponseDto updateMyProfile(UserDto userDto) {
        ServerResponseDto objectUserEntity = convertEntityToDto(userDto);
        int code = objectUserEntity.getStatus().code;
        if (code == 1600) {
            return new ServerResponseDto(ResponseCase.EMAIL_EXIST);
        } else if (code == 4) {
            return new ServerResponseDto(ResponseCase.ERROR);
        }
        UserEntity userEntity = (UserEntity) objectUserEntity.getData();
        userRepository.save(userEntity);
        return new ServerResponseDto(ResponseCase.SUCCESS);
    }

    public List<UserEntity> getListUserByListUserId(Collection<Long> listUserId) {
        if (listUserId.isEmpty()) {
            return Collections.emptyList();
        }
        return userRepository.findByIdInAndIsDeletedFalse(listUserId);
    }

    public Map<Long, String> getMapNameUserByUserId(Collection<Long> listUserId) {
        if (listUserId.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UserEntity> listUser = getListUserByListUserId(listUserId);
        return getMapNameUserByUserId(listUser);
    }

    public Map<Long, String> getMapNameUserByUserId(List<UserEntity> listUser) {
        if (listUser.isEmpty()) {
            return Collections.emptyMap();
        }
        return listUser
                .stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getName));
    }

    public Map<Long, String> getMapUrlAvatarUserByUserId(List<UserEntity> listUser) {
        if (listUser.isEmpty()) {
            return Collections.emptyMap();
        }
        return listUser
                .stream()
                .collect(HashMap::new, (m, v) -> m.put(v.getId(), v.getUrlAvatar()), HashMap::putAll);
    }

    public Map<Long, UserEntity> getMapUserByUserId(Collection<Long> listUserId) {
        if (listUserId.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UserEntity> listUser = userRepository.findByIdInAndIsDeletedFalse(listUserId);
        return listUser
                .stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
    }

    public List<UserEntity> getListUserCanTakePartInDocument(Long workUnitId) {
        List<Long> listWorkUnitParentId = workUnitService.getListWorkUnitParentId(workUnitId);
        return userRepository.getListUserByListWorkUnitId(listWorkUnitParentId);
    }

    public List<UserEntity> getAllMemberForViewAndUpdate() {
        return userRepository.findAllMemberActive();
    }

    public boolean isLevelGovernment(Long userId) {
        UserEntity userEntity = userRepository.findByIdAndIsDeletedFalse(userId);
        return userEntity != null && userEntity.isLevelGovernment();
    }

    public List<IdAndName> getObjectIdAndNameByListId(List<Long> listUserId) {
        if (listUserId.isEmpty()) {
            return Collections.emptyList();
        }
        return userRepository.getObjectIdAndNameByListId(listUserId);
    }

    public List<UserEntity> getListMemberSameWorkUnit(Long workUnitId) {
        List<Long> listWorkUnitChildId = workUnitService.getListWorkUnitChildId(workUnitId);
        return userRepository.getListUserByListWorkUnitId(listWorkUnitChildId);
    }
}
