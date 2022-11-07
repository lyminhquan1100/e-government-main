package com.namtg.egovernment.service.unit;

import com.namtg.egovernment.dto.response.ServerResponseDto;
import com.namtg.egovernment.dto.unit.WorkUnitDto;
import com.namtg.egovernment.dto.unit.WrapUnitLv1;
import com.namtg.egovernment.entity.unit.UnitEntity;
import com.namtg.egovernment.entity.unit.WorkUnitEntity;
import com.namtg.egovernment.repository.unit.WorkUnitRepository;
import com.namtg.egovernment.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkUnitService {
    @Autowired
    private UnitService unitService;

    @Autowired
    private WorkUnitRepository workUnitRepository;

    @Autowired
    private WorkUnitUserService workUnitUserService;

    @Autowired
    private UserService userService;

    public Object[] getWorkUnitInfo(Long currentUserId, String type) {
        Object[] objectsDefault = new Object[]{Collections.emptyList(), 1};
        List<UnitEntity> listUnitLvHighest;

        List<WorkUnitEntity> listAllWorkUnit = workUnitRepository.findAll();
        List<WorkUnitEntity> listWorkUnitLvHighest;
        if ("all".equals(type) || userService.isLevelGovernment(currentUserId)) {
            listWorkUnitLvHighest = listAllWorkUnit
                    .stream()
                    .filter(w -> w.getLevel() == 1)
                    .collect(Collectors.toList());
            listUnitLvHighest = unitService.getListUnitIdLv1();
        } else {
            listWorkUnitLvHighest = workUnitRepository.getListWorkUnitCanCreateUser(currentUserId);
            listUnitLvHighest = unitService.getListUnitByIdIn(listWorkUnitLvHighest
                    .stream()
                    .map(WorkUnitEntity::getUnitId)
                    .collect(Collectors.toList()));
        }
        if (listUnitLvHighest.isEmpty()) {
            return objectsDefault;
        }
        setUnitName(listAllWorkUnit);

        listWorkUnitLvHighest.forEach(workUnit -> setListChild(workUnit, listAllWorkUnit));
        Map<Long, List<WorkUnitEntity>> mapListWorkUnitLvHighestByUnitLvHighestId = listWorkUnitLvHighest
                .stream()
                .collect(Collectors.groupingBy(WorkUnitEntity::getUnitId));
        List<WrapUnitLv1> listResult = listUnitLvHighest
                .stream()
                .map(unitLv1 -> new WrapUnitLv1(
                        unitLv1.getId(),
                        unitLv1.getName(),
                        mapListWorkUnitLvHighestByUnitLvHighestId.getOrDefault(unitLv1.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        Long maxWorkUnitId = listAllWorkUnit
                .stream()
                .map(WorkUnitEntity::getId)
                .max(Comparator.comparing(Long::valueOf))
                .orElse(0L);
        return new Object[]{listResult, maxWorkUnitId};
    }

    private void setUnitName(List<WorkUnitEntity> listAllWorkUnit) {
        Map<Long, String> mapUnitNameByUnitId = unitService.getMapUnitNameByUnitId(listAllWorkUnit
                .stream()
                .map(WorkUnitEntity::getUnitId)
                .distinct()
                .collect(Collectors.toList()));
        listAllWorkUnit.forEach(workUnit ->
                workUnit.setUnitName(mapUnitNameByUnitId.getOrDefault(workUnit.getUnitId(), "")));
    }

    private void setListChild(WorkUnitEntity wuParent, List<WorkUnitEntity> listAllWorkUnit) {
        wuParent.setListChild(listAllWorkUnit
                .stream()
                .filter(wuChild -> wuParent.getLevel() + 1 == wuChild.getLevel() &&
                        wuParent.getId().equals(wuChild.getWorkUnitLevelAboveId()))
                .collect(Collectors.toList()));
        if (wuParent.getListChild().isEmpty()) {
            return;
        }
        wuParent.getListChild().forEach(wuChild -> setListChild(wuChild, listAllWorkUnit));
    }

    @Transactional
    public ServerResponseDto saveWorkUnit(List<WorkUnitDto> listWorkUnit) {
        List<Long> listAllWorkUnitId = workUnitRepository.getAllWorkUnitId();
        Set<Long> listWorkUnitAfter = listWorkUnit.stream().map(WorkUnitDto::getId).collect(Collectors.toSet());

        List<Long> listWorkUnitIdDelete = listAllWorkUnitId
                .stream()
                .filter(wuId -> !listWorkUnitAfter.contains(wuId))
                .collect(Collectors.toList());
        if (!listWorkUnitIdDelete.isEmpty()) {
            deleteWorkUnitByListId(listWorkUnitIdDelete);
        }
        List<WorkUnitEntity> listWorkUnitEntity = listWorkUnit
                .stream()
                .map(this::convertDtoToEntity)
                .collect(Collectors.toList());
        workUnitRepository.saveAll(listWorkUnitEntity);
        return ServerResponseDto.SUCCESS;
    }

    @Transactional
    public void deleteWorkUnitByListId(List<Long> listWorkUnitIdDelete) {
        workUnitRepository.deleteByListId(listWorkUnitIdDelete);
    }

    private WorkUnitEntity convertDtoToEntity(WorkUnitDto dto) {
        return new WorkUnitEntity(
                dto.getId(),
                dto.getLevel(),
                dto.getWorkUnitLevelAboveId(),
                dto.getUnitId(),
                dto.getName());
    }

    public ServerResponseDto getListWorkUnitByUserId(Long userId) {
        List<WorkUnitEntity> listAllWorkUnit = workUnitRepository.findAll();

        List<Long> listWorkUnitParentId = workUnitUserService.getListWorkUnitId(userId);
        List<WorkUnitEntity> listWorkUnitParent = workUnitRepository.findByIdIn(listWorkUnitParentId);
        List<WorkUnitEntity> listResult = new ArrayList<>(listWorkUnitParent);

        collectWorkUnitChild(listResult, listWorkUnitParent, listAllWorkUnit);
        return ServerResponseDto.successWithData(listResult);
    }

    private void collectWorkUnitChild(List<WorkUnitEntity> listResult,
                                      List<WorkUnitEntity> listWorkUnitParent,
                                      List<WorkUnitEntity> listAllWorkUnit) {
        listWorkUnitParent.forEach(wuParent -> {
            List<WorkUnitEntity> listChild = listAllWorkUnit
                    .stream()
                    .filter(wuChild -> wuParent.getLevel() + 1 == wuChild.getLevel() &&
                            wuParent.getId().equals(wuChild.getWorkUnitLevelAboveId()))
                    .collect(Collectors.toList());
            listResult.addAll(listChild);
            if (!listChild.isEmpty()) {
                collectWorkUnitChild(listResult, listChild, listAllWorkUnit);
            }
        });
    }

    public List<Long> getListWorkUnitParentId(Long workUnitId) {
        WorkUnitEntity workUnit = workUnitRepository.getById(workUnitId);
        List<WorkUnitEntity> listAllWorkUnit = workUnitRepository.findAll();
        List<Long> listResult = new ArrayList<>(List.of(workUnitId));

        collectWorkUnitIdParent(listResult, workUnit, listAllWorkUnit);
        return listResult;
    }

    private void collectWorkUnitIdParent(List<Long> listResult,
                                         WorkUnitEntity workUnit,
                                         List<WorkUnitEntity> listAllWorkUnit) {
        WorkUnitEntity workUnitParent = listAllWorkUnit
                .stream()
                .filter(wu -> Objects.equals(workUnit.getWorkUnitLevelAboveId(), wu.getId()))
                .findFirst()
                .orElse(null);
        if (workUnitParent == null) {
            return;
        }
        listResult.add(workUnitParent.getId());
        collectWorkUnitIdParent(listResult, workUnitParent, listAllWorkUnit);
    }

    public Map<Long, String> getMapNameWorkUnitByWorkUnitId(List<Long> listWorkUnitId) {
        if (listWorkUnitId.isEmpty()) {
            return Collections.emptyMap();
        }
        return workUnitRepository.findByIdIn(listWorkUnitId)
                .stream()
                .collect(Collectors.toMap(WorkUnitEntity::getId, WorkUnitEntity::getName));
    }

    public Map<Long, String> getMapNameWorkUnitByWorkUnitId() {
        List<WorkUnitEntity> listAllWorkUnit = workUnitRepository.getAllWorkUnit();
        return listAllWorkUnit
                .stream()
                .collect(Collectors.toMap(WorkUnitEntity::getId, WorkUnitEntity::getName));
    }

    public String getNameById(Long workUnitId) {
        return workUnitRepository.getNameById(workUnitId);
    }

    public List<Long> getListWorkUnitChildId(Long workUnitId) {
        WorkUnitEntity workUnitEntity = workUnitRepository.getById(workUnitId);
        List<WorkUnitEntity> listAllWorkUnit = workUnitRepository.findAll();
        List<WorkUnitEntity> listResultEntity = new ArrayList<>(List.of(workUnitEntity));

        collectWorkUnitChild(listResultEntity, List.of(workUnitEntity), listAllWorkUnit);
        return listResultEntity
                .stream()
                .map(WorkUnitEntity::getId)
                .collect(Collectors.toList());
    }
}
