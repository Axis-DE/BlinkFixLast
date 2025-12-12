package moe.ichinomiya.naven.ui.clickgui;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.impl.EventShader;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.utils.Colors;
import moe.ichinomiya.naven.utils.RenderUtils;
import moe.ichinomiya.naven.utils.SmoothAnimationTimer;
import moe.ichinomiya.naven.utils.StencilUtils;
import moe.ichinomiya.naven.utils.font.FontManager;
import moe.ichinomiya.naven.utils.font.GlyphPageFontRenderer;
import moe.ichinomiya.naven.values.Value;
import moe.ichinomiya.naven.values.ValueType;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import moe.ichinomiya.naven.values.impl.FloatValue;
import moe.ichinomiya.naven.values.impl.ModeValue;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ClientClickGUI extends GuiScreen {
    public static float windowX = 100, windowY = 100, windowWidth = 400, windowHeight = 250;

    Category selectedCategory = null;
    Module selectedModule = null;

    int[] dragMousePosition = null;

    SmoothAnimationTimer widthAnimation = new SmoothAnimationTimer(100);
    SmoothAnimationTimer heightAnimation = new SmoothAnimationTimer(140);
    SmoothAnimationTimer titleAnimation = new SmoothAnimationTimer(100);
    SmoothAnimationTimer titleHoverAnimation = new SmoothAnimationTimer(0);
    SmoothAnimationTimer categoryMotionY = new SmoothAnimationTimer(0);
    SmoothAnimationTimer moduleValuesMotionY = new SmoothAnimationTimer(0);

    HashMap<Category, SmoothAnimationTimer> categoryXAnimation = new HashMap<Category, SmoothAnimationTimer>() {{
        for (Category value : Category.values()) {
            put(value, new SmoothAnimationTimer(0));
        }
    }};

    HashMap<Category, SmoothAnimationTimer> categoryYAnimation = new HashMap<Category, SmoothAnimationTimer>() {{
        for (Category value : Category.values()) {
            put(value, new SmoothAnimationTimer(0));
        }
    }};

    HashMap<Category, List<Module>> modules = new HashMap<Category, List<Module>>() {{
        for (Category value : Category.values()) {
            put(value, BlinkFix.getInstance().getModuleManager().getModulesByCategory(value));
        }
    }};

    HashMap<Module, SmoothAnimationTimer> modulesAnimation = new HashMap<Module, SmoothAnimationTimer>() {{
        for (Module value : BlinkFix.getInstance().getModuleManager().getModules()) {
            put(value, new SmoothAnimationTimer(0));
        }
    }};

    HashMap<Module, SmoothAnimationTimer> modulesToggleAnimation = new HashMap<Module, SmoothAnimationTimer>() {{
        for (Module value : BlinkFix.getInstance().getModuleManager().getModules()) {
            put(value, new SmoothAnimationTimer(0));
        }
    }};

    HashMap<Value, SmoothAnimationTimer> valuesAnimation = new HashMap<Value, SmoothAnimationTimer>() {{
        for (Value value : BlinkFix.getInstance().getValueManager().getValues()) {
            put(value, new SmoothAnimationTimer(0));
        }
    }};


    String titleDisplayName = "";
    float finalModuleHeight, finalValueHeight;
    boolean clickReturnModules = false, clickReturnCategories = false, clickOpenModuleSettings = false, clickToggleModule = false, clickOpenCategoryModules = false, clickResizeWindow = false, clickDragWindow = false;

    Category hoveringCategory = null;
    Module hoveringModule = null;
    Module bindingModule = null;

    SmoothAnimationTimer moduleSwapAnimation = new SmoothAnimationTimer(0);
    SmoothAnimationTimer bindingAnimation = new SmoothAnimationTimer(0);
    List<Module> categoryModules;
    List<Value> renderValues;

    BooleanValue hoveringBooleanValue;
    FloatValue hoveringFloatValue;
    FloatValue draggingFloatValue;
    ModeValue hoveringModeValue;
    int targetModeValueIndex;
    String bindingModuleName;

    @Override
    public void onGuiClosed() {
        BlinkFix.getInstance().getFileManager().save();
        BlinkFix.getInstance().getEventManager().unregister(this);
        super.onGuiClosed();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (bindingModule != null && (mouseButton == 3 || mouseButton == 4)) {
            bindingModule.setKey(-mouseButton);
            bindingModule = null;
            return;
        }

        if (mouseButton == 0 && bindingModule == null) {
            if (clickReturnCategories) {
                selectedCategory = null;
                selectedModule = null;
                clickReturnCategories = false;
            }

            if (clickReturnModules) {
                selectedModule = null;
                clickReturnModules = false;
                renderValues = null;
            }

            if (clickToggleModule && hoveringModule != null) {
                hoveringModule.toggle();
                clickToggleModule = false;
            }

            if (clickOpenModuleSettings && hoveringModule != null) {
                selectedModule = hoveringModule;
                renderValues = BlinkFix.getInstance().getValueManager().getValuesByHasValue(hoveringModule);
                clickOpenModuleSettings = false;

                moduleValuesMotionY.target = moduleValuesMotionY.value = 0;
            }

            if (clickOpenCategoryModules && hoveringCategory != null) {
                selectedCategory = hoveringCategory;
                categoryMotionY.value = categoryMotionY.target = 0;
                moduleSwapAnimation.value = 5;
                moduleSwapAnimation.target = 255;
                clickOpenCategoryModules = false;
            }

            if (clickResizeWindow) {
                dragMousePosition = new int[]{mouseX, mouseY};
                clickResizeWindow = false;
            }

            if (clickDragWindow) {
                dragMousePosition = new int[]{mouseX, mouseY};
                clickDragWindow = false;
            }

            if (hoveringBooleanValue != null) {
                hoveringBooleanValue.setCurrentValue(!hoveringBooleanValue.getCurrentValue());
            }

            if (hoveringFloatValue != null) {
                draggingFloatValue = hoveringFloatValue;
            }

            if (hoveringModeValue != null) {
                hoveringModeValue.setCurrentValue(targetModeValueIndex);
                SmoothAnimationTimer animation = valuesAnimation.get(hoveringModeValue);
                animation.value = 0;
                animation.target = 255;
            }
        } else if (mouseButton == 1 && bindingModule == null){
            if (hoveringModule != null) {
                selectedModule = hoveringModule;
                renderValues = BlinkFix.getInstance().getValueManager().getValuesByHasValue(hoveringModule);
//                clickOpenModuleSettings = false;

                moduleValuesMotionY.target = moduleValuesMotionY.value = 0;
            }
        }
        else if (mouseButton == 2) {
            if (hoveringModule != null) {
                bindingModule = hoveringModule;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (draggingFloatValue != null) {
            draggingFloatValue = null;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (bindingModule != null) {
            if (keyCode == 1) {
                bindingModule.setKey(0);
            } else {
                bindingModule.setKey(keyCode);
            }
            bindingModule = null;
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }


    @Override
    public void initGui() {
        BlinkFix.getInstance().getEventManager().register(this);
        valuesAnimation.forEach((value, animation) -> {
            if (value.getValueType() == ValueType.MODE) {
                animation.value = 0;
                animation.target = 255;
            }
        });
    }

    @EventTarget
    public void onRenderShader(EventShader e) {
        RenderUtils.drawRoundedRect(windowX, windowY, windowX + widthAnimation.value, windowY + heightAnimation.value, 6, 0x80000000);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        hoveringModule = null;
        clickReturnModules = clickReturnCategories = clickOpenCategoryModules = false;

        FontManager fontManager = BlinkFix.getInstance().getFontManager();

        GlyphPageFontRenderer font20 = fontManager.opensans20;
        GlyphPageFontRenderer font18 = fontManager.siyuan18;
        GlyphPageFontRenderer font30 = fontManager.opensans30;

        if (selectedCategory == null) {
            widthAnimation.target = font30.getStringWidth(/*BlinkFix.CLIENT_DISPLAY_NAME*/"BLINK_FIX.TODAY") + 10;;
            heightAnimation.target = 140;
        } else {
            widthAnimation.target = windowWidth;
            heightAnimation.target = windowHeight;
        }

        widthAnimation.update(true);
        heightAnimation.update(true);

        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(windowX, windowY, windowX + widthAnimation.value, windowY + heightAnimation.value, 6, 0x80000000);
        StencilUtils.erase(true);

        RenderUtils.drawRoundedRect(windowX, windowY, windowX + widthAnimation.value, windowY + heightAnimation.value, 6, Colors.getColor(0, 0, 0, 80));

        for (Category value : Category.values()) {
            SmoothAnimationTimer xAnimation = categoryXAnimation.get(value);
            SmoothAnimationTimer yAnimation = categoryYAnimation.get(value);

            if (selectedCategory == null) {
                yAnimation.target = 255;
            } else {
                yAnimation.target = 4;
            }

            xAnimation.update(true);
            yAnimation.update(true);

            float height = (value.ordinal() * 25) * (yAnimation.value / 255f);

            if (yAnimation.target >= 4) {
                font20.drawStringWithShadow(value.getDisplayName(), windowX + 8 + xAnimation.value, windowY + 40 + height, Colors.getColor(255, 255, 255, (int) yAnimation.value));
            }

            boolean hovering = RenderUtils.isHovering(mouseX, mouseY, windowX, windowY + 40 + height, windowX + 100, windowY + 40 + height + 20);

            if (hovering) {
                xAnimation.target = 5;
            } else {
                xAnimation.target = 0;
            }

            if (yAnimation.value >= 250 && hovering) {
                hoveringCategory = value;
                clickOpenCategoryModules = true;
            }
        }

        titleAnimation.update(true);
        titleHoverAnimation.update(true);

        if (titleAnimation.value > 5) {
            font20.drawStringWithShadow(titleDisplayName, windowX + 6 + titleHoverAnimation.value, windowY + 5, Colors.getColor(255, 255, 255, (int) titleAnimation.value));
        }

        font30.drawCenteredString(/*BlinkFix.CLIENT_DISPLAY_NAME*/"BlinkFix", windowX + 47 + 8, windowY + 5, Colors.getColor(255, 255, 255, (int) (259 - titleAnimation.value)));

        if (selectedCategory != null && selectedModule == null) {
            titleAnimation.target = 255;
            titleDisplayName = "< " + selectedCategory.getDisplayName();

            if (RenderUtils.isHovering(mouseX, mouseY, windowX + 8, windowY + 5, windowX + 5 + font18.getStringWidth(titleDisplayName), windowY + 5 + font18.getFontHeight()) && dragMousePosition == null) {
                titleHoverAnimation.target = -2;
                clickReturnCategories = true;
            } else {
                titleHoverAnimation.target = 0;
            }

            List<Module> categoryModules = modules.get(selectedCategory);

            int dWheel = Mouse.getDWheel();
            if (bindingModule == null) {
                categoryMotionY.target += dWheel / 2f;
            }

            if (categoryMotionY.target < -finalModuleHeight) {
                categoryMotionY.target = -finalModuleHeight;
            }

            if (categoryMotionY.target > 0) {
                categoryMotionY.target = 0;
            }

            categoryMotionY.update(true);

            if (categoryModules != null) {
                clickResizeWindow = RenderUtils.isHovering(mouseX, mouseY, windowX + windowWidth - 10, windowY + windowHeight - 10, windowX + windowWidth, windowY + windowHeight) && dragMousePosition == null;

                if (dragMousePosition != null && Mouse.isButtonDown(0)) {
                    windowWidth += mouseX - dragMousePosition[0];
                    windowHeight += mouseY - dragMousePosition[1];

                    if (windowWidth < 250) {
                        windowWidth = 250;
                    }

                    if (windowHeight < 150) {
                        windowHeight = 150;
                    }

                    dragMousePosition = new int[]{mouseX, mouseY};
                } else {
                    dragMousePosition = null;
                }
            }
        } else if (selectedModule != null) {
            titleAnimation.target = 255;
            titleDisplayName = "< " + selectedModule.getDescription();

            if (RenderUtils.isHovering(mouseX, mouseY, windowX + 8, windowY + 5, windowX + 5 + font18.getStringWidth(titleDisplayName), windowY + 5 + font18.getFontHeight()) && dragMousePosition == null) {
                titleHoverAnimation.target = -2;
                clickReturnModules = true;
            } else {
                titleHoverAnimation.target = 0;
            }

        } else {
            titleAnimation.target = 4;
            clickDragWindow = RenderUtils.isHovering(mouseX, mouseY, windowX, windowY, windowX + 100, windowY + 40) && dragMousePosition == null;

            if (dragMousePosition != null && Mouse.isButtonDown(0)) {
                windowX += mouseX - dragMousePosition[0];
                windowY += mouseY - dragMousePosition[1];
                dragMousePosition = new int[]{mouseX, mouseY};
            } else {
                dragMousePosition = null;
            }
        }

        // Render category modules
        float renderModuleWidth = 0, renderModuleHeight = 0;

        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(windowX, windowY + 20, windowX + widthAnimation.value, windowY + heightAnimation.value, 6, 0x80000000);
        StencilUtils.erase(true);

        List<Module> inList = modules.get(selectedCategory);

        if (inList != null && selectedModule == null) {
            categoryModules = inList;
            moduleSwapAnimation.target = 255;
        } else {
            moduleSwapAnimation.target = 5;
        }
        moduleSwapAnimation.update(true);

        if (inList == null && moduleSwapAnimation.value < 8) {
            categoryModules = null;
        }

        if (categoryModules != null) {
            for (Module categoryModule : categoryModules) {
                int stringWidth = font20.getStringWidth(categoryModule.getName());

                if (renderModuleWidth + stringWidth + 50 > windowWidth) {
                    renderModuleWidth = 0;
                    renderModuleHeight += 35;
                }

                boolean isHovering = RenderUtils.isHoveringBound(mouseX, mouseY, windowX + 12 + renderModuleWidth, windowY + 25 + renderModuleHeight + categoryMotionY.value, stringWidth + 28, 25) && moduleSwapAnimation.value > 250 && bindingModule == null;
                boolean isHoveringSetting = RenderUtils.isHoveringBound(mouseX, mouseY, windowX + 12 + renderModuleWidth + stringWidth + 28 - 18, windowY + 25 + renderModuleHeight + categoryMotionY.value + 5, 15, 15) && moduleSwapAnimation.value > 250 && bindingModule == null;

                SmoothAnimationTimer moduleToggleAnimation = modulesToggleAnimation.get(categoryModule);

                if (categoryModule.isEnabled()) {
                    moduleToggleAnimation.target = moduleSwapAnimation.value;
                } else {
                    moduleToggleAnimation.target = 6;
                }

                moduleToggleAnimation.update(true);

                int alpha = (int) moduleSwapAnimation.value;
                int enabledColor = Colors.getColor(54, 98, 236, (int) moduleToggleAnimation.value);
                int disabledColor = Colors.getColor(25, 25, 25, alpha);

                RenderUtils.drawBoundRoundedRect(windowX + 12 + renderModuleWidth, windowY + 25 + renderModuleHeight + categoryMotionY.value, stringWidth + 28, 25, 3, disabledColor);
                RenderUtils.drawBoundRoundedRect(windowX + 12 + renderModuleWidth, windowY + 25 + renderModuleHeight + categoryMotionY.value, stringWidth + 28, 25, 3, enabledColor);

                SmoothAnimationTimer moduleAnimation = modulesAnimation.get(categoryModule);

                if (isHovering && !isHoveringSetting) {
                    moduleAnimation.target = 150;
                    hoveringModule = categoryModule;
                    clickToggleModule = true;
                    clickOpenModuleSettings = false;
                } else {
                    moduleAnimation.target = 5;
                }

                moduleAnimation.update(true);

                if (isHoveringSetting) {
                    hoveringModule = categoryModule;
                    clickOpenModuleSettings = true;
                    clickToggleModule = false;
                }

                int hoveringColor = Colors.getColor(255, 255, 255, (int) moduleAnimation.value / 3);
                RenderUtils.drawBoundRoundedRect(windowX + 12 + renderModuleWidth, windowY + 25 + renderModuleHeight + categoryMotionY.value, stringWidth + 28, 25, 3, hoveringColor);

                font20.drawString(categoryModule.getName(), windowX + 17 + renderModuleWidth, windowY + 30 + renderModuleHeight + categoryMotionY.value, Colors.getColor(255, 255, 255, alpha));
                font20.drawString(">", windowX + 24 + renderModuleWidth + stringWidth, windowY + 30 + renderModuleHeight + categoryMotionY.value, Colors.getColor(255, 255, 255, alpha));

                renderModuleWidth += stringWidth + 40;
            }
            finalModuleHeight = renderModuleHeight + 60 - windowHeight;
        }


        if (renderValues != null) {
            int dWheel = Mouse.getDWheel();
            float motion = moduleValuesMotionY.value;
            if (bindingModule == null) {
                moduleValuesMotionY.target += dWheel / 2f;
            }

            if (moduleValuesMotionY.target < -finalValueHeight) {
                moduleValuesMotionY.target = -finalValueHeight;
            }

            if (moduleValuesMotionY.target > 0) {
                moduleValuesMotionY.target = 0;
            }

            moduleValuesMotionY.update(true);

            int x = 0;
            float valueHeight = 0;

            // draw boolean values
            hoveringBooleanValue = null;
            for (Value value : renderValues) {
                if (!value.isVisible()) {
                    continue;
                }

                if (value.getValueType() == ValueType.BOOLEAN) {
                    BooleanValue booleanValue = value.getBooleanValue();
                    SmoothAnimationTimer animation = valuesAnimation.get(booleanValue);

                    if (booleanValue.getCurrentValue()) {
                        animation.target = 255;
                    } else {
                        animation.target = 0;
                    }

                    animation.update(true);

                    int currentLength = font18.getStringWidth(value.getName()) + 20;

                    if (x + currentLength + 20 > windowWidth) {
                        x = 0;
                        valueHeight += 20;
                    }

                    if (RenderUtils.isHoveringBound(mouseX, mouseY, windowX + 10 + x, windowY + valueHeight + motion + 25, currentLength, 13)) {
                        hoveringBooleanValue = booleanValue;
                    }

                    // draw checkbox
                    int enabledColor = Colors.getColor(54, 98, 236, (int) animation.value);
                    RenderUtils.drawRoundedRect(windowX + 10f + x, windowY + valueHeight + motion + 26, windowX + 22 + x, windowY + valueHeight + motion + 25 + 13f, 4, Colors.getColor(0, 0, 0, 150));

                    RenderUtils.circle(windowX + 16 + x, windowY + valueHeight + motion + 31.5f, 3.5f, enabledColor);
                    font18.drawStringWithShadow(value.getName(), windowX + 23 + x, windowY + valueHeight + motion + 25, Colors.getColor(255, 255, 255, 255));
                    x += currentLength;
                }
            }

            valueHeight += 18;
            hoveringFloatValue = null;
            for (Value value : renderValues) {
                if (!value.isVisible()) {
                    continue;
                }

                if (value.getValueType() == ValueType.FLOAT) {
                    FloatValue floatValue = value.getFloatValue();
                    SmoothAnimationTimer animation = valuesAnimation.get(floatValue);

                    if (RenderUtils.isHoveringBound(mouseX, mouseY, windowX + 10, windowY + valueHeight + motion + 39, windowWidth - 20, 15)) {
                        hoveringFloatValue = floatValue;
                    }

                    font18.drawStringWithShadow(value.getName(), windowX + 10, windowY + valueHeight + motion + 25, Colors.getColor(255, 255, 255, 255));
                    String currentValue = Math.round(floatValue.getCurrentValue() * 100) / 100f + " / " + floatValue.getMaxValue();
                    font18.drawStringWithShadow(currentValue, windowX + windowWidth - font18.getStringWidth(currentValue) - 10, windowY + valueHeight + motion + 25, Colors.getColor(255, 255, 255, 255));

                    // draw bar
                    float stage = (floatValue.getCurrentValue() - floatValue.getMinValue()) / (floatValue.getMaxValue() - floatValue.getMinValue());
                    int enabledColor = Colors.getColor(54, 98, 236, 255);
                    RenderUtils.drawRoundedRect(windowX + 10, windowY + valueHeight + motion + 42, windowX + windowWidth - 10, windowY + valueHeight + motion + 25 + 26, 4, Colors.getColor(0, 0, 0, 150));
                    animation.target = (windowWidth - 20) * stage;

                    animation.update(true);
                    RenderUtils.drawRoundedRect(windowX + 10, windowY + valueHeight + motion + 42, windowX + 10 + animation.value, windowY + valueHeight + motion + 25 + 26, 4, enabledColor);

                    // Draw Center Point
                    RenderUtils.circle(windowX + 10 + animation.value, windowY + valueHeight + motion + 25 + 22, 5.5f, Colors.getColor(255, 255, 255, 255));

                    valueHeight += 30;
                }
            }

            hoveringModeValue = null;
            for (Value value : renderValues) {
                if (!value.isVisible()) {
                    continue;
                }

                if (value.getValueType() == ValueType.MODE) {
                    ModeValue modeValue = value.getModeValue();
                    SmoothAnimationTimer animation = valuesAnimation.get(modeValue);
                    animation.update(true);

                    font18.drawStringWithShadow(value.getName(), windowX + 10, windowY + valueHeight + motion + 25, Colors.getColor(255, 255, 255, 255));

                    x = 0;
                    valueHeight += 15;

                    for (int modeIndex = 0; modeIndex < modeValue.getValues().length; modeIndex++) {
                        String mode = modeValue.getValues()[modeIndex];
                        int currentLength = font18.getStringWidth(mode) + 20;

                        if (x + currentLength + 20 > windowWidth) {
                            x = 0;
                            valueHeight += 20;
                        }

                        if (RenderUtils.isHoveringBound(mouseX, mouseY, windowX + 10 + x, windowY + valueHeight + motion + 25, currentLength, 13)) {
                            hoveringModeValue = modeValue;
                            targetModeValueIndex = modeIndex;
                        }

                        // draw checkbox
                        int enabledColor = Colors.getColor(54, 98, 236, modeValue.isCurrentMode(mode) ? (int) animation.value : 10);
                        RenderUtils.circle(windowX + 15.5f + x, windowY + valueHeight + motion + 25 + 6.5f, 5f, Colors.getColor(0, 0, 0, 150));
                        RenderUtils.circle(windowX + 15.5f + x, windowY + valueHeight + motion + 25 + 6.5f, 3.5f, enabledColor);


                        font18.drawStringWithShadow(mode, windowX + 23 + x, windowY + valueHeight + motion + 25, Colors.getColor(255, 255, 255, 255));
                        x += currentLength;
                    }
                    valueHeight += 20;
                }
            }
            finalValueHeight = valueHeight - windowHeight + 25;
        }

        StencilUtils.dispose();

        // Handle Float Value Dragging
        if (draggingFloatValue != null) {
            float stage = (mouseX - windowX - 10) / (windowWidth - 20);
            float value = draggingFloatValue.getMinValue() + (draggingFloatValue.getMaxValue() - draggingFloatValue.getMinValue()) * stage;

            // clamp
            if (value < draggingFloatValue.getMinValue()) {
                value = draggingFloatValue.getMinValue();
            }

            if (value > draggingFloatValue.getMaxValue()) {
                value = draggingFloatValue.getMaxValue();
            }

            value = Math.round(value / draggingFloatValue.getStep()) * draggingFloatValue.getStep();

            draggingFloatValue.setCurrentValue(value);
        }

        if (bindingModule != null) {
            bindingAnimation.target = 250;
            bindingModuleName = bindingModule.getName();
        } else {
            bindingAnimation.target = 5;
        }

        bindingAnimation.update(true);

        if (bindingAnimation.value > 6) {
            RenderUtils.drawRoundedRect(windowX, windowY, windowX + widthAnimation.value, windowY + heightAnimation.value, 6, Colors.getColor(0, 0, 0, (int) bindingAnimation.value / 2));
            font30.drawCenteredStringWithShadow("Press a key to bind " + bindingModuleName, windowX + widthAnimation.value / 2, windowY + (heightAnimation.value - font30.getFontHeight()) / 2, Colors.getColor(255, 255, 255, (int) bindingAnimation.value));
            font18.drawCenteredStringWithShadow("(Press ESC to cancel key bind)", windowX + widthAnimation.value / 2, windowY + (heightAnimation.value - font30.getFontHeight()) / 2 + 25, Colors.getColor(255, 255, 255, (int) bindingAnimation.value));
        }

        StencilUtils.dispose();
    }
}
