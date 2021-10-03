package com.edgedx.connectpoc.model;

import com.edgedx.connectpoc.model.CartridgeType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class CartridgeTypeConverter implements AttributeConverter<CartridgeType, String> {

	@Override
	public String convertToDatabaseColumn(CartridgeType code) {
		if (code == null) {
			return null;
		}
		return code.getDescription();
	}

	@Override
	public CartridgeType convertToEntityAttribute(String description) {
		if ((description == null) || (description.isEmpty())) {
			return null;
		}

		return Stream.of(CartridgeType.values()).filter(c -> c.getDescription().equals(description)).findFirst()
				.orElseThrow(IllegalArgumentException::new);
	}

}
