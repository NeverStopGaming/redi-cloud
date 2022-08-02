package net.suqatri.redicloud.commons.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.FileWriter;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class FileEditor {

    private final List<String> lines;
    private final String splitter;
    private final HashMap<String, String> keyValues;

    public FileEditor(Type type) {
        ;
        this.splitter = type.splitter;
        this.lines = new ArrayList<>();
        this.keyValues = new HashMap<>();
    }

    public void read(File file) throws IOException {
        try (FileReader fileReader = new FileReader(file); BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            fileReader.close();
            bufferedReader.close();
            this.loadMap();
        }
    }

    public void read(List<String> list) throws IOException {
        this.lines.addAll(list);
        this.loadMap();
    }

    public void save(File file) throws IOException {
        FileWriter writer = new FileWriter(file);
        for (String s : newLine()) {
            writer.write(s + "\n");
        }
        writer.close();
    }

    private void loadMap() {
        for (String line : this.lines) {
            if (line.contains(" - ")) continue;
            if (line.contains("- ")) continue;
            if (line.endsWith(":")) continue;
            if (line.startsWith("#")) continue;
            String key = "";
            if (!line.contains(this.splitter) || line.split(this.splitter).length < 2) {
                if (line.startsWith(" ")) {
                    int count = getAmountOfStartSpacesInLine(key);
                    key = key.substring(count);
                }
                key = line.replace(this.splitter, "");
                for (String s : this.splitter.split("")) {
                    key = key.replace(s, "");
                }
                this.keyValues.put(key, "");
                continue;
            }
            try {
                String[] keyValue = line.split(this.splitter);
                if (keyValue[0].startsWith(" ")) {
                    int count = getAmountOfStartSpacesInLine(keyValue[0]);
                    key = keyValue[0].substring(count);
                } else {
                    key = keyValue[0];
                }
                this.keyValues.put(key, keyValue[1]);
            } catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException("Invalid line: " + line);
            }
        }
    }

    public void setValue(String key, String value) {
        if (!this.keyValues.containsKey(key)) throw new IllegalArgumentException("Key " + key + " not found");
        this.keyValues.put(key, value);
    }

    public String getValue(String key) {
        return this.keyValues.get(key);
    }

    public List<String> newLine() {
        List<String> list = new ArrayList<>(this.lines);
        this.keyValues.forEach((key, value) -> {
            int lineIndex = getLineIndexByKey(key);
            String newLine = constructNewLine(key, value, lineIndex);
            list.set(lineIndex, newLine);
        });
        return list;
    }

    public String constructNewLine(String key, String value, int lineIndex) {
        String lineWithoutSpaces = key + this.splitter + value;
        int amountOfStartSpaces = getAmountOfStartSpacesInLine(this.lines.get(lineIndex));
        String spacesString = getStringWithSpaces(amountOfStartSpaces);
        return spacesString + lineWithoutSpaces;
    }

    public String getStringWithSpaces(int amount) {
        String spacesString = "";
        for (int i = 0; i < amount; i++) {
            spacesString += " ";
        }
        return spacesString;
    }

    public String removeFirstSpaces(String line) {
        int amountOfSpaces = getAmountOfStartSpacesInLine(line);
        return line.substring(amountOfSpaces);
    }

    public int getAmountOfStartSpacesInLine(String line) {
        int amountOfSpaces = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ') {
                amountOfSpaces++;
                continue;
            }
            break;
        }
        return amountOfSpaces;
    }

    public int getLineIndexByKey(String key) {
        String match = key + this.splitter;
        for (int i = 0; i < this.lines.size(); i++) {
            if (this.lines.get(i).contains(match)) {
                return i;
            }
        }
        return -1;
    }

    @AllArgsConstructor
    @Getter
    public static enum Type {
        YML(": "),
        TOML(" = "),
        PROPERTIES("=");
        private String splitter;
    }

}
