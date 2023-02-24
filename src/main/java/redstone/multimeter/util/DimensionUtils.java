package redstone.multimeter.util;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

public class DimensionUtils {
	
	private static final Map<Identifier, DimensionType> ID_TO_TYPE;
	private static final Map<DimensionType, Identifier> TYPE_TO_ID;
	
	private static void register(Identifier id, DimensionType type) {
		ID_TO_TYPE.put(id, type);
		TYPE_TO_ID.put(type, id);
	}
	
	private static void register(String name, DimensionType type) {
		register(new Identifier(name), type);
	}
	
	public static DimensionType getType(Identifier id) {
		return ID_TO_TYPE.get(id);
	}
	
	public static Identifier getId(DimensionType type) {
		return TYPE_TO_ID.get(type);
	}
	
	static {
		
		ID_TO_TYPE = new HashMap<>();
		TYPE_TO_ID = new HashMap<>();
		
		for (DimensionType type : DimensionType.values()) {
			register(type.getName(), type);
		}
	}
}
