package carpet.worldedit;

import java.util.*;

/**
 * Converts between JNBT and Minecraft NBT classes.
 */
final class NBTConverter {

	private NBTConverter() {
	}

	public static net.minecraft.nbt.NbtElement toNative(com.sk89q.jnbt.Tag tag) {
		if (tag instanceof com.sk89q.jnbt.IntArrayTag) {
			return toNative((com.sk89q.jnbt.IntArrayTag) tag);

		} else if (tag instanceof com.sk89q.jnbt.ListTag) {
			return toNative((com.sk89q.jnbt.ListTag) tag);

		} else if (tag instanceof com.sk89q.jnbt.LongTag) {
			return toNative((com.sk89q.jnbt.LongTag) tag);

		} else if (tag instanceof com.sk89q.jnbt.StringTag) {
			return toNative((com.sk89q.jnbt.StringTag) tag);

		} else if (tag instanceof com.sk89q.jnbt.IntTag) {
			return toNative((com.sk89q.jnbt.IntTag) tag);

		} else if (tag instanceof com.sk89q.jnbt.ByteTag) {
			return toNative((com.sk89q.jnbt.ByteTag) tag);

		} else if (tag instanceof com.sk89q.jnbt.ByteArrayTag) {
			return toNative((com.sk89q.jnbt.ByteArrayTag) tag);

		} else if (tag instanceof com.sk89q.jnbt.CompoundTag) {
			return toNative((com.sk89q.jnbt.CompoundTag) tag);

		} else if (tag instanceof com.sk89q.jnbt.FloatTag) {
			return toNative((com.sk89q.jnbt.FloatTag) tag);

		} else if (tag instanceof com.sk89q.jnbt.ShortTag) {
			return toNative((com.sk89q.jnbt.ShortTag) tag);

		} else if (tag instanceof com.sk89q.jnbt.DoubleTag) {
			return toNative((com.sk89q.jnbt.DoubleTag) tag);
		} else {
			throw new IllegalArgumentException("Can't convert tag of type " + tag.getClass().getCanonicalName());
		}
	}

	public static net.minecraft.nbt.NbtIntArray toNative(com.sk89q.jnbt.IntArrayTag tag) {
		int[] value = tag.getValue();
		return new net.minecraft.nbt.NbtIntArray(Arrays.copyOf(value, value.length));
	}

	public static net.minecraft.nbt.NbtList toNative(com.sk89q.jnbt.ListTag tag) {
		net.minecraft.nbt.NbtList list = new net.minecraft.nbt.NbtList();
		for (com.sk89q.jnbt.Tag child : tag.getValue()) {
			if (child instanceof com.sk89q.jnbt.EndTag) {
				continue;
			}
			list.add(toNative(child));
		}
		return list;
	}

	public static net.minecraft.nbt.NbtLong toNative(com.sk89q.jnbt.LongTag tag) {
		return new net.minecraft.nbt.NbtLong(tag.getValue());
	}

	public static net.minecraft.nbt.NbtString toNative(com.sk89q.jnbt.StringTag tag) {
		return new net.minecraft.nbt.NbtString(tag.getValue());
	}

	public static net.minecraft.nbt.NbtInt toNative(com.sk89q.jnbt.IntTag tag) {
		return new net.minecraft.nbt.NbtInt(tag.getValue());
	}

	public static net.minecraft.nbt.NbtByte toNative(com.sk89q.jnbt.ByteTag tag) {
		return new net.minecraft.nbt.NbtByte(tag.getValue());
	}

	public static net.minecraft.nbt.NbtByteArray toNative(com.sk89q.jnbt.ByteArrayTag tag) {
		byte[] value = tag.getValue();
		return new net.minecraft.nbt.NbtByteArray(Arrays.copyOf(value, value.length));
	}

	public static net.minecraft.nbt.NbtCompound toNative(com.sk89q.jnbt.CompoundTag tag) {
		net.minecraft.nbt.NbtCompound compound = new net.minecraft.nbt.NbtCompound();
		for (Map.Entry<String, com.sk89q.jnbt.Tag> child : tag.getValue().entrySet()) {
			compound.put(child.getKey(), toNative(child.getValue()));
		}
		return compound;
	}

	public static net.minecraft.nbt.NbtFloat toNative(com.sk89q.jnbt.FloatTag tag) {
		return new net.minecraft.nbt.NbtFloat(tag.getValue());
	}

	public static net.minecraft.nbt.NbtShort toNative(com.sk89q.jnbt.ShortTag tag) {
		return new net.minecraft.nbt.NbtShort(tag.getValue());
	}

	public static net.minecraft.nbt.NbtDouble toNative(com.sk89q.jnbt.DoubleTag tag) {
		return new net.minecraft.nbt.NbtDouble(tag.getValue());
	}

	public static com.sk89q.jnbt.Tag fromNative(net.minecraft.nbt.NbtElement other) {
		if (other instanceof net.minecraft.nbt.NbtIntArray) {
			return fromNative((net.minecraft.nbt.NbtIntArray) other);

		} else if (other instanceof net.minecraft.nbt.NbtList) {
			return fromNative((net.minecraft.nbt.NbtList) other);

		} else if (other instanceof net.minecraft.nbt.NbtEnd) {
			return fromNative((net.minecraft.nbt.NbtEnd) other);

		} else if (other instanceof net.minecraft.nbt.NbtLong) {
			return fromNative((net.minecraft.nbt.NbtLong) other);

		} else if (other instanceof net.minecraft.nbt.NbtString) {
			return fromNative((net.minecraft.nbt.NbtString) other);

		} else if (other instanceof net.minecraft.nbt.NbtInt) {
			return fromNative((net.minecraft.nbt.NbtInt) other);

		} else if (other instanceof net.minecraft.nbt.NbtByte) {
			return fromNative((net.minecraft.nbt.NbtByte) other);

		} else if (other instanceof net.minecraft.nbt.NbtByteArray) {
			return fromNative((net.minecraft.nbt.NbtByteArray) other);

		} else if (other instanceof net.minecraft.nbt.NbtCompound) {
			return fromNative((net.minecraft.nbt.NbtCompound) other);

		} else if (other instanceof net.minecraft.nbt.NbtFloat) {
			return fromNative((net.minecraft.nbt.NbtFloat) other);

		} else if (other instanceof net.minecraft.nbt.NbtShort) {
			return fromNative((net.minecraft.nbt.NbtShort) other);

		} else if (other instanceof net.minecraft.nbt.NbtDouble) {
			return fromNative((net.minecraft.nbt.NbtDouble) other);
		} else {
			throw new IllegalArgumentException("Can't convert other of type " + other.getClass().getCanonicalName());
		}
	}

	public static com.sk89q.jnbt.IntArrayTag fromNative(net.minecraft.nbt.NbtIntArray other) {
		int[] value = other.getIntArray();
		return new com.sk89q.jnbt.IntArrayTag(Arrays.copyOf(value, value.length));
	}

	public static com.sk89q.jnbt.ListTag fromNative(net.minecraft.nbt.NbtList other) {
		other = other.copy();
		List<com.sk89q.jnbt.Tag> list = new ArrayList<>();
		Class<? extends com.sk89q.jnbt.Tag> listClass = com.sk89q.jnbt.StringTag.class;
		int tags = other.size();
		for (int i = 0; i < tags; i++) {
			com.sk89q.jnbt.Tag child = fromNative(other.remove(0));
			list.add(child);
			listClass = child.getClass();
		}
		return new com.sk89q.jnbt.ListTag(listClass, list);
	}

	public static com.sk89q.jnbt.EndTag fromNative(net.minecraft.nbt.NbtEnd other) {
		return new com.sk89q.jnbt.EndTag();
	}

	public static com.sk89q.jnbt.LongTag fromNative(net.minecraft.nbt.NbtLong other) {
		return new com.sk89q.jnbt.LongTag(other.getLong());
	}

	public static com.sk89q.jnbt.StringTag fromNative(net.minecraft.nbt.NbtString other) {
		return new com.sk89q.jnbt.StringTag(other.asString());
	}

	public static com.sk89q.jnbt.IntTag fromNative(net.minecraft.nbt.NbtInt other) {
		return new com.sk89q.jnbt.IntTag(other.getInt());
	}

	public static com.sk89q.jnbt.ByteTag fromNative(net.minecraft.nbt.NbtByte other) {
		return new com.sk89q.jnbt.ByteTag(other.getByte());
	}

	public static com.sk89q.jnbt.ByteArrayTag fromNative(net.minecraft.nbt.NbtByteArray other) {
		byte[] value = other.getByteArray();
		return new com.sk89q.jnbt.ByteArrayTag(Arrays.copyOf(value, value.length));
	}

	public static com.sk89q.jnbt.CompoundTag fromNative(net.minecraft.nbt.NbtCompound other) {
		Collection<String> tags = other.getKeys();
		Map<String, com.sk89q.jnbt.Tag> map = new HashMap<String, com.sk89q.jnbt.Tag>();
		for (String tagName : tags) {
			map.put(tagName, fromNative(other.get(tagName)));
		}
		return new com.sk89q.jnbt.CompoundTag(map);
	}

	public static com.sk89q.jnbt.FloatTag fromNative(net.minecraft.nbt.NbtFloat other) {
		return new com.sk89q.jnbt.FloatTag(other.getFloat());
	}

	public static com.sk89q.jnbt.ShortTag fromNative(net.minecraft.nbt.NbtShort other) {
		return new com.sk89q.jnbt.ShortTag(other.getShort());
	}

	public static com.sk89q.jnbt.DoubleTag fromNative(net.minecraft.nbt.NbtDouble other) {
		return new com.sk89q.jnbt.DoubleTag(other.getDouble());
	}

}
