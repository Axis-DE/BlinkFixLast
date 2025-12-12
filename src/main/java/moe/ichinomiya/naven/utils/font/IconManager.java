package moe.ichinomiya.naven.utils.font;

import moe.ichinomiya.naven.modules.Category;

import java.util.HashMap;
import java.util.Map;

public class IconManager {
    private static final Map<Category, String> categoryIcons = new HashMap<>();

    static {
        // 使用你分配的 Unicode 字符
        categoryIcons.put(Category.COMBAT, "A");      // U+0041 - 攻击图标
        categoryIcons.put(Category.MOVEMENT, "B");    // U+0042 - 移动图标
        categoryIcons.put(Category.MISC, "C");        // U+0043 - 其他图标
//        categoryIcons.put(Category.PLAYER, "D");      // U+0044 - 玩家图标
//        categoryIcons.put(Category.WORLD, "E");       // U+0045 - 世界图标
        categoryIcons.put(Category.RENDER, "F");      // U+0046 - 视觉图标
    }

    public static String getIconForCategory(Category category) {
        return categoryIcons.getOrDefault(category, "?"); // 默认问号图标
    }

    /**
     * 测试方法：打印所有图标映射
     */
    public static void printAllIcons() {
        System.out.println("=== Naven Icons Mapping ===");
        for (Category category : Category.values()) {
            String icon = getIconForCategory(category);
            System.out.println(category.getDisplayName() + ": '" + icon + "' (U+" +
                    Integer.toHexString(icon.charAt(0)).toUpperCase() + ")");
        }
    }

    /**
     * 获取图标描述（用于调试）
     */
    public static String getIconDescription(Category category) {
        switch (category) {
            case COMBAT: return "攻击图标 (A)";
            case MOVEMENT: return "移动图标 (B)";
            case MISC: return "其他图标 (C)";
//            case PLAYER: return "玩家图标 (D)";
//            case WORLD: return "世界图标 (E)";
            case RENDER: return "视觉图标 (F)";
            default: return "未知图标";
        }
    }
}