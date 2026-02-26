package com.lifeos.mapper;

import com.lifeos.dto.LifeRecordDTO;
import com.lifeos.entity.LifeRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface LifeRecordConverter {

    LifeRecordConverter INSTANCE = Mappers.getMapper(LifeRecordConverter.class);

    @Mapping(source = "tags", target = "tags", qualifiedByName = "jsonToList")
    @Mapping(source = "amount", target = "amount", qualifiedByName = "bigDecimalToDouble")
    LifeRecordDTO toDTO(LifeRecord entity);

    @Mapping(source = "tags", target = "tags", qualifiedByName = "listToJson")
    @Mapping(source = "amount", target = "amount", qualifiedByName = "doubleToBigDecimal")
    LifeRecord toEntity(LifeRecordDTO dto);

    List<LifeRecordDTO> toDTOList(List<LifeRecord> entities);

    List<LifeRecord> toEntityList(List<LifeRecordDTO> dtos);

    @Named("jsonToList")
    default List<String> jsonToList(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            json = json.replace("[", "").replace("]", "").replace("\"", "");
            if (json.isEmpty()) {
                return Collections.emptyList();
            }
            return Arrays.asList(json.split(","));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Named("listToJson")
    default String listToJson(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < tags.size(); i++) {
            sb.append("\"").append(tags.get(i)).append("\"");
            if (i < tags.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Named("bigDecimalToDouble")
    default Double bigDecimalToDouble(BigDecimal amount) {
        return amount != null ? amount.doubleValue() : null;
    }

    @Named("doubleToBigDecimal")
    default BigDecimal doubleToBigDecimal(Double amount) {
        return amount != null ? BigDecimal.valueOf(amount) : null;
    }
}
