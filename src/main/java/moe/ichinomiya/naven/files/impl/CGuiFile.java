package moe.ichinomiya.naven.files.impl;

import moe.ichinomiya.naven.files.ClientFile;
import moe.ichinomiya.naven.ui.clickgui.ClientClickGUI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class CGuiFile extends ClientFile {
    public CGuiFile() {
        super("clickgui.cfg");
    }

    @Override
    public void read(BufferedReader reader) throws IOException {
        try {
            ClientClickGUI.windowX = Integer.parseInt(reader.readLine());
            ClientClickGUI.windowY = Integer.parseInt(reader.readLine());
            ClientClickGUI.windowWidth = Integer.parseInt(reader.readLine());
            ClientClickGUI.windowHeight = Integer.parseInt(reader.readLine());
        } catch (Exception ignored) {}
    }

    @Override
    public void save(BufferedWriter writer) throws IOException {
        writer.write((int) ClientClickGUI.windowX + "\n");
        writer.write((int) ClientClickGUI.windowY + "\n");
        writer.write((int) ClientClickGUI.windowWidth + "\n");
        writer.write((int) ClientClickGUI.windowHeight + "\n");
    }
}