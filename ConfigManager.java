//ai code的配置系统，加的话记得把路径引用改了，同时添加json依赖“https://repo1.maven.org/maven2/org/json/json/20240303/json-20240303.jar”，Manager不知道放哪里了，随便放的......
package com.BlueArchive.Sunaookami.Shiroko.Yukari.config;

import com.BlueArchive.Sunaookami.Shiroko.Yukari.Yukari;
import com.BlueArchive.Sunaookami.Shiroko.Yukari.modules.Module;
import com.BlueArchive.Sunaookami.Shiroko.Yukari.values.Value;
import com.BlueArchive.Sunaookami.Shiroko.Yukari.values.ValueManager;
import lombok.Getter;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    @Getter
    private final File configFolder;

    public ConfigManager() {
        this.configFolder = new File(Yukari.CLIENT_NAME + File.separator + "configs");
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }
    }

    // 修改 ConfigManager.java 的保存方法
    public boolean saveConfig(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        File configFile = new File(configFolder, name + ".cfg");

        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
            }

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(Files.newOutputStream(configFile.toPath()), StandardCharsets.UTF_8))) {

                JSONObject configJson = new JSONObject();

                // 保存所有模块
                for (Module module : Yukari.getInstance().getModuleManager().getModules()) {
                    JSONObject moduleJson = new JSONObject();
                    moduleJson.put("state", module.isEnabled());
                    moduleJson.put("key", module.getKey());

                    // 保存参数值
                    JSONObject valuesJson = new JSONObject();
                    ValueManager valueManager = Yukari.getInstance().getValueManager();
                    List<Value> moduleValues = valueManager.getValuesByHasValue(module);

                    for (Value value : moduleValues) {
                        if (value.isVisible()) {
                            Object valueObj = extractValue(value);
                            if (valueObj != null) {
                                valuesJson.put(value.getName(), valueObj);
                            }
                        }
                    }

                    moduleJson.put("values", valuesJson);
                    configJson.put(module.getName(), moduleJson);
                }

                // 写入 CFG 文件（JSON 格式）
                writer.write(configJson.toString(2));
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 修改加载方法
    public boolean loadConfig(String name) {
        File configFile = new File(configFolder, name + ".cfg");
        if (!configFile.exists()) {
            return false;
        }

        try {
            String content = new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);
            JSONObject configJson = new JSONObject(content);

            for (String moduleName : configJson.keySet()) {
                JSONObject moduleJson = configJson.getJSONObject(moduleName);

                // 加载模块状态和按键
                Module module = Yukari.getInstance().getModuleManager().getModule(moduleName);
                if (module != null) {
                    module.setKey(moduleJson.getInt("key"));
                    boolean enabled = moduleJson.getBoolean("state");
                    if (enabled != module.isEnabled()) {
                        module.setEnabled(enabled);
                    }

                    // 加载参数值
                    if (moduleJson.has("values")) {
                        JSONObject valuesJson = moduleJson.getJSONObject("values");
                        ValueManager valueManager = Yukari.getInstance().getValueManager();

                        for (String valueName : valuesJson.keySet()) {
                            try {
                                Value value = valueManager.getValue(module, valueName);
                                Object valueData = valuesJson.get(valueName);
                                setValue(value, valueData.toString());
                            } catch (Exception e) {
                                System.err.println("Failed to load value: " + moduleName + "." + valueName);
                            }
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void saveWidgets(BufferedWriter writer) throws IOException {
        File widgetFile = new File(Yukari.CLIENT_NAME + File.separator + "widgets.cfg");
        if (widgetFile.exists()) {
            writer.write("# Widgets - Copy from widgets.cfg\n");
            List<String> lines = Files.readAllLines(widgetFile.toPath(), StandardCharsets.UTF_8);
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    writer.write("WIDGET:" + line + "\n");
                }
            }
        }
    }

    private void loadWidgets(String line) {
        try {
            if (line.startsWith("WIDGET:")) {
                String widgetData = line.substring(7);
                File widgetFile = new File(Yukari.CLIENT_NAME + File.separator + "widgets.cfg");
                List<String> existingLines = new ArrayList<>();
                if (widgetFile.exists()) {
                    existingLines = Files.readAllLines(widgetFile.toPath(), StandardCharsets.UTF_8);
                }
                String[] parts = widgetData.split(":");
                if (parts.length >= 3) {
                    String widgetName = parts[0];
                    boolean found = false;
                    for (int i = 0; i < existingLines.size(); i++) {
                        if (existingLines.get(i).startsWith(widgetName + ":")) {
                            existingLines.set(i, widgetData);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        existingLines.add(widgetData);
                    }
                    try (BufferedWriter widgetWriter = new BufferedWriter(
                            new OutputStreamWriter(Files.newOutputStream(widgetFile.toPath()), StandardCharsets.UTF_8))) {
                        for (String existingLine : existingLines) {
                            widgetWriter.write(existingLine + "\n");
                        }
                    }

                    System.out.println("Loaded widget: " + widgetData);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load widget: " + line);
        }
    }


    private void loadModule(String moduleName, String keyStr, String enabledStr) {
        try {
            int keybind = Integer.parseInt(keyStr);
            boolean enabled = Boolean.parseBoolean(enabledStr);

            Module module = Yukari.getInstance().getModuleManager().getModule(moduleName);
            if (module != null) {
                module.setKey(keybind);
                if (enabled != module.isEnabled()) {
                    module.setEnabled(enabled);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load module: " + moduleName);
        }
    }

    private boolean loadValue(String moduleName, String valueName, String valueData) {
        try {
            Module module = Yukari.getInstance().getModuleManager().getModule(moduleName);
            if (module == null) {
                System.err.println("Module not found: " + moduleName);
                return false;
            }

            ValueManager valueManager = Yukari.getInstance().getValueManager();
            Value value = valueManager.getValue(module, valueName);

            if (value == null) {
                System.err.println("Value not found: " + moduleName + "." + valueName);
                return false;
            }

            return setValue(value, valueData);
        } catch (Exception e) {
            System.err.println("Failed to load value: " + moduleName + "." + valueName + " - " + e.getMessage());
            return false;
        }
    }

    private Object extractValue(Value value) {
        try {
            System.out.println("=== Debug extractValue for: " + value.getName() + " ===");
            System.out.println("Class: " + value.getClass().getName());

            Field[] fields = value.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object fieldValue = field.get(value);
                System.out.println("Field: " + field.getName() + " | Type: " + field.getType().getSimpleName() + " | Value: " + fieldValue);
                if (java.lang.reflect.Modifier.isFinal(field.getModifiers()) ||
                        field.getName().equals("values")) {
                    continue;
                }
                if (fieldValue != null && !field.getType().getName().contains("Value")) {
                    return fieldValue;
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("Error extracting value: " + e.getMessage());
            return null;
        }
    }

    private boolean setValue(Value value, String valueData) {
        try {
            System.out.println("=== Debug setValue for: " + value.getName() + " ===");
            System.out.println("Setting value: " + valueData);

            Field[] fields = value.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (java.lang.reflect.Modifier.isFinal(field.getModifiers()) ||
                        field.getName().equals("values")) {
                    continue;
                }

                field.setAccessible(true);
                Object currentValue = field.get(value);

                if (currentValue != null && !field.getType().getName().contains("Value")) {
                    Object convertedValue = convertValue(valueData, currentValue);
                    field.set(value, convertedValue);
                    System.out.println("Successfully set field '" + field.getName() + "' to: " + convertedValue);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error setting value: " + e.getMessage());
            return false;
        }
    }

    private Object convertValue(String valueData, Object currentValue) {
        System.out.println("Converting: " + valueData + " to match: " + currentValue + " (" + currentValue.getClass().getSimpleName() + ")");

        if (currentValue instanceof Boolean) {
            return Boolean.parseBoolean(valueData);
        } else if (currentValue instanceof Float) {
            return Float.parseFloat(valueData);
        } else if (currentValue instanceof Double) {
            return Double.parseDouble(valueData);
        } else if (currentValue instanceof Integer) {
            return Integer.parseInt(valueData);
        } else if (currentValue instanceof String) {
            return valueData;
        } else {
            try {
                if (valueData.equalsIgnoreCase("true") || valueData.equalsIgnoreCase("false")) {
                    return Boolean.parseBoolean(valueData);
                } else if (valueData.contains(".")) {
                    return Float.parseFloat(valueData);
                } else {
                    return Integer.parseInt(valueData);
                }
            } catch (NumberFormatException e) {
                return valueData;
            }
        }
    }
    public boolean deleteConfig(String name) {
        File configFile = new File(configFolder, name + ".cfg");
        if (configFile.exists()) {
            return configFile.delete();
        }
        return false;
    }

    public String[] getConfigList() {
        File[] files = configFolder.listFiles((dir, name) -> name.endsWith(".cfg"));
        if (files == null) return new String[0];

        List<String> configs = new ArrayList<>();
        for (File file : files) {
            String fileName = file.getName();
            configs.add(fileName.substring(0, fileName.length() - 4));
        }

        return configs.toArray(new String[0]);
    }

}
