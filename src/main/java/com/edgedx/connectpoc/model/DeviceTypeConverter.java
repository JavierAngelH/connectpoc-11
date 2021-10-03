package com.edgedx.connectpoc.model;

import com.edgedx.connectpoc.model.DeviceType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class DeviceTypeConverter  implements AttributeConverter<DeviceType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(DeviceType deviceType) {
        if (deviceType == null) {
            return null;
        }
        return deviceType.getCode();
    }

    @Override
    public DeviceType convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }

        return Stream.of(DeviceType.values()).filter(c -> c.getCode().equals(code)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
