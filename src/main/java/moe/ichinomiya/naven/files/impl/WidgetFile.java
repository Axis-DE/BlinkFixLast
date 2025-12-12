package moe.ichinomiya.naven.files.impl;

import moe.ichinomiya.naven.BlinkFix;
import moe.ichinomiya.naven.files.ClientFile;
import moe.ichinomiya.naven.modules.impl.render.Widget;
import moe.ichinomiya.naven.ui.widgets.DraggableWidget;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class WidgetFile extends ClientFile {
    public WidgetFile() {
        super("widgets.cfg");
    }

    @Override
    public void read(BufferedReader reader) throws IOException {
        Widget widget = (Widget) BlinkFix.getInstance().getModuleManager().getModule(Widget.class);

        String line;
        while ((line = reader.readLine()) != null) {
            String[] split = line.split(":", 3);

            if (split.length != 3) {
                continue;
            }

            String name = split[0];
            int x = Integer.parseInt(split[1]);
            int y = Integer.parseInt(split[2]);

            for (DraggableWidget draggableWidget : widget.widgets) {
                if (draggableWidget.getName().equals(name)) {
                    draggableWidget.getX().target = draggableWidget.getX().value = x;
                    draggableWidget.getY().target = draggableWidget.getY().value = y;
                }
            }
        }
    }

    @Override
    public void save(BufferedWriter writer) throws IOException {
        Widget widget = (Widget) BlinkFix.getInstance().getModuleManager().getModule(Widget.class);

        for (DraggableWidget draggableWidget : widget.widgets) {
            writer.write(draggableWidget.getName() + ":" + (int) draggableWidget.getX().target + ":" + (int) draggableWidget.getY().target + "\n");
        }
    }
}
