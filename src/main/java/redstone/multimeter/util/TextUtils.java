package redstone.multimeter.util;

import java.util.List;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextUtils {
	
	public static void addFancyText(List<Text> lines, String title, Object info) {
		addFancyText(lines, title, info.toString());
	}
	
	public static void addFancyText(List<Text> lines, String title, String info) {
		lines.add(formatFancyText(title, info));
	}
	
	public static Text formatFancyText(String title, Object info) {
		return new LiteralText("").
			append(new LiteralText(title + ": ").setStyle(new Style().setFormatting(Formatting.GOLD))).
			append(new LiteralText(info.toString()));
	}
}
