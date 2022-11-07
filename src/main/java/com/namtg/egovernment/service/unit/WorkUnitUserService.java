package com.namtg.egovernment.service.unit;

import com.namtg.egovernment.dto.user.WorkUnitIdAndAction;
import com.namtg.egovernment.entity.unit.WorkUnitUserEntity;
import com.namtg.egovernment.repository.unit.WorkUnitUserRepository;
import com.namtg.egovernment.util.CollectionUtils;
import com.namtg.egovernment.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class WorkUnitUserService {
    @Autowired
    private WorkUnitUserRepository workUnitUserRepository;

    @Autowired
    private WorkUnitService workUnitService;

    @Transactional
    public void save(Long userId, List<WorkUnitIdAndAction> listWorkUnitIdAndAction) {
        if (CollectionUtils.collectionIsNullOrEmpty(listWorkUnitIdAndAction)) {
            return;
        }
        Set<Long> setWorkUnitIdAfter = listWorkUnitIdAndAction
                .stream()
                .map(WorkUnitIdAndAction::getWorkUnitId)
                .collect(Collectors.toSet());
        Set<Long> listWorkUnitIdInDb = new HashSet<>(getListWorkUnitId(userId));

        // delete
        List<Long> listWorkUnitIdDelete = listWorkUnitIdInDb
                .stream()
                .filter(wuId -> !setWorkUnitIdAfter.contains(wuId))
                .collect(Collectors.toList());
        if (!listWorkUnitIdDelete.isEmpty()) {
            deleteByListWorkUnitId(listWorkUnitIdDelete);
        }

        Map<Long, Boolean> mapWorkUnitIdWithIsCanReceiveDocument = listWorkUnitIdAndAction
                .stream()
                .collect(Collectors.toMap(
                        WorkUnitIdAndAction::getWorkUnitId,
                        WorkUnitIdAndAction::isCanReceiveDocument));
        Map<Long, Boolean> mapWorkUnitIdWithIsCanManageUser = listWorkUnitIdAndAction
                .stream()
                .collect(Collectors.toMap(
                        WorkUnitIdAndAction::getWorkUnitId,
                        WorkUnitIdAndAction::isCanManageUser));

        // add
        List<Long> listWorkUnitIdAdd = setWorkUnitIdAfter
                .stream()
                .filter(wuId -> !listWorkUnitIdInDb.contains(wuId))
                .collect(Collectors.toList());
        List<WorkUnitUserEntity> listWorkUnitUserAdd = listWorkUnitIdAdd
                .stream()
                .map(wuId -> new WorkUnitUserEntity(wuId, userId,
                        mapWorkUnitIdWithIsCanReceiveDocument.get(wuId),
                        mapWorkUnitIdWithIsCanManageUser.get(wuId)))
                .collect(Collectors.toList());

        // update
        List<Long> listWorkUnitIdUpdate = setWorkUnitIdAfter
                .stream()
                .filter(listWorkUnitIdInDb::contains)
                .collect(Collectors.toList());
        listWorkUnitIdUpdate.add(0L);
        List<WorkUnitUserEntity> listWorkUnitUserUpdate = workUnitUserRepository
                .findByUserIdAndWorkUnitIdIn(userId, listWorkUnitIdUpdate);
        listWorkUnitUserUpdate.forEach(item -> {
            item.setCanReceiveDocument(mapWorkUnitIdWithIsCanReceiveDocument.get(item.getWorkUnitId()));
            item.setCanManageUser(mapWorkUnitIdWithIsCanManageUser.get(item.getWorkUnitId()));
        });

        workUnitUserRepository.saveAll(Stream
                .concat(listWorkUnitUserAdd.stream(), listWorkUnitUserUpdate.stream())
                .collect(Collectors.toList()));
    }

    @Transactional
    public void deleteByListWorkUnitId(List<Long> listWorkUnitIdDelete) {
        workUnitUserRepository.deleteByListWorkUnitId(listWorkUnitIdDelete);
    }

    public List<Long> getListWorkUnitId(Long userId) {
        return workUnitUserRepository.getListWorkUnitId(userId);
    }

    public Map<Long, List<Long>> getMapListUserIdReceiveDocumentByWorkUnitId(List<Long> listWorkUnitIdReceiveDocument) {
        if (listWorkUnitIdReceiveDocument.isEmpty()) {
            return Collections.emptyMap();
        }
        List<WorkUnitUserEntity> listWorkUnitUser = workUnitUserRepository
                .getByListWorkUnitId(listWorkUnitIdReceiveDocument);
        return listWorkUnitUser
                .stream()
                .collect(Collectors.groupingBy(WorkUnitUserEntity::getWorkUnitId,
                        Collectors.mapping(WorkUnitUserEntity::getUserId, Collectors.toList())));
    }

    public List<Long> getListUserIdCanReceiveDocument(List<Long> listWorkUnitId) {
        if (listWorkUnitId.isEmpty()) {
            return Collections.emptyList();
        }
        return workUnitUserRepository.getListUserIdCanReceiveDocumentByListWorkUnitId(listWorkUnitId);
    }

    public List<Long> getListUserIdByListWorkUnitId(List<Long> listWorkUnitId) {
        if (listWorkUnitId.isEmpty()) {
            return Collections.emptyList();
        }
        return workUnitUserRepository.getListUserIdByListWorkUnitId(listWorkUnitId);
    }

    public List<WorkUnitUserEntity> getListWorkUnit(Long userId) {
        List<WorkUnitUserEntity> listResult = workUnitUserRepository.getByUserId(userId);
        if (listResult.isEmpty()) {
            return listResult;
        }
        List<Long> listWorkUnitId = listResult.stream()
                .map(WorkUnitUserEntity::getWorkUnitId)
                .collect(Collectors.toList());
        Map<Long, String> mapNameWorkUnitByWorkUnitId = workUnitService.getMapNameWorkUnitByWorkUnitId(listWorkUnitId);
        listResult.forEach(workUnitUser ->
                workUnitUser.setNameWorkUnit(mapNameWorkUnitByWorkUnitId.getOrDefault(workUnitUser.getWorkUnitId(), Constants.TEXT_EMPTY))
        );
        return listResult;
    }

    public Map<Long, List<Long>> getMapListWorkUnitIdByUserId(List<Long> listUserId) {
        if (listUserId.isEmpty()) {
            return Collections.emptyMap();
        }
        List<WorkUnitUserEntity> listWorkUnitUser = workUnitUserRepository.findByListUserId(listUserId);
        if (listWorkUnitUser.isEmpty()) {
            return Collections.emptyMap();
        }
        return listWorkUnitUser
                .stream()
                .collect(Collectors.groupingBy(WorkUnitUserEntity::getUserId,
                        Collectors.mapping(WorkUnitUserEntity::getWorkUnitId, Collectors.toList())));
    }
}
