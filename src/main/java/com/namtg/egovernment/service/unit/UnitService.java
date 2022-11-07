package com.namtg.egovernment.service.unit;

import com.namtg.egovernment.dto.response.ServerResponseDto;
import com.namtg.egovernment.dto.unit.UnitDto;
import com.namtg.egovernment.entity.unit.UnitEntity;
import com.namtg.egovernment.repository.unit.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UnitService {

    @Autowired
    private UnitRepository unitRepository;

    public Object[] getUnitInfo() {
        List<UnitEntity> listAllUnit = unitRepository.findAll();
        Long maxId = listAllUnit
                .stream()
                .map(UnitEntity::getId)
                .max(Comparator.comparing(Long::valueOf))
                .orElse(1L);
        List<UnitEntity> listResult = listAllUnit
                .stream()
                .filter(u -> u.getLevel() == 1)
                .collect(Collectors.toList());

        listResult.forEach(uParent -> setListChild(uParent, listAllUnit));
        return new Object[]{listResult, maxId};
    }

    private void setListChild(UnitEntity uParent, List<UnitEntity> listAllUnit) {
        uParent.setListChild(listAllUnit
                .stream()
                .filter(uChild -> uParent.getLevel() + 1 == uChild.getLevel() &&
                        uParent.getId().equals(uChild.getUnitLevelAboveId()))
                .collect(Collectors.toList()));
        if (uParent.getListChild().isEmpty()) {
            return;
        }
        uParent.getListChild().forEach(uChild -> setListChild(uChild, listAllUnit));
    }

    @Transactional
    public ServerResponseDto saveUnit(List<UnitDto> listUnit) {
        List<Long> listAllUnitId = unitRepository.getListAllId();
        Set<Long> setIdAfter = listUnit.stream().map(UnitDto::getId).collect(Collectors.toSet());
        List<Long> listIdDeleted = listAllUnitId
                .stream()
                .filter(id -> !setIdAfter.contains(id))
                .collect(Collectors.toList());
        if (!listIdDeleted.isEmpty()) {
            deleteListUnitById(listIdDeleted);
        }
        List<UnitEntity> listUnitEntity = listUnit
                .stream()
                .map(this::convertDtoToEntity)
                .collect(Collectors.toList());
        unitRepository.saveAll(listUnitEntity);
        return ServerResponseDto.SUCCESS;
    }

    private UnitEntity convertDtoToEntity(UnitDto unitDto) {
        return new UnitEntity(unitDto.getId(), unitDto.getLevel(), unitDto.getUnitLevelAboveId(), unitDto.getName());
    }

    @Transactional
    public void deleteListUnitById(List<Long> listIdDeleted) {
        unitRepository.deleteListUnitById(listIdDeleted);
    }

    public List<UnitEntity> getListUnitIdLv1() {
        return unitRepository.getListUnitIdLv1();
    }

    public List<UnitEntity> getListUnitChild(Long unitParentId) {
        return unitRepository.getListUnitChild(unitParentId);
    }

    public Map<Long, String> getMapUnitNameByUnitId(List<Long> listUnitId) {
        List<UnitEntity> listUnit = unitRepository.findByIdIn(listUnitId);
        if (listUnit.isEmpty()) {
            return Collections.emptyMap();
        }
        return listUnit
                .stream()
                .collect(Collectors.toMap(UnitEntity::getId, UnitEntity::getName));
    }

    public List<UnitEntity> getListUnitByIdIn(List<Long> listUnitId) {
        if (listUnitId.isEmpty()) {
            return Collections.emptyList();
        }
        return unitRepository.findByIdIn(listUnitId);
    }
}
