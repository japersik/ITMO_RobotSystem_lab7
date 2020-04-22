package com.itmo.r3135.System.Tools;

import com.itmo.r3135.System.Command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Класс обработки комадны execute_script
 */
public class ScriptReader {
    public ScriptReader() {
    }

    public static ArrayList<Command> read(String script) {
        ArrayList<Command> executeCommands = new ArrayList<>();
        try {
            File scriptFile = new File(script);
            if (!scriptFile.exists() || !scriptFile.isFile()) {
                System.out.println(("Файл по указанному пути (" + scriptFile.getAbsolutePath() + ") не существует."));
                return executeCommands;
            }
            if (!scriptFile.canRead()) {
                System.out.println("Файл защищён от чтения.");
                return executeCommands;
            }
            if (scriptFile.length() == 0) {
                System.out.println("Скрипт не содержит команд.");
                return executeCommands;
            }
            System.out.println("Начинается анализ скрипта. Это может занять некоторое время");
            VirtualStack virtualStack = new VirtualStack();
            StringCommandManager stringCommandManager = new StringCommandManager();
            ArrayList<String> executeStringCommands = virtualStack.stackGenerate(script);
            System.out.println("Анализ содердимого команд:");
            for (String executeStringCommand : executeStringCommands) {
                Command executeCommand = stringCommandManager.getCommandFromString(executeStringCommand);
                if (executeCommand != null)
                    executeCommands.add(executeCommand);
            }
            System.out.println("Анализ завершён.");
            if (!executeStringCommands.isEmpty()) {
                return executeCommands;
            } else {
                System.out.println("Невозможно прочитать скрипт или скрипт пуст.");
                return null;
            }
        } catch (IOException e) {
            System.out.println("Ошибка работы с файлами.");
            return executeCommands;
        }
    }
}
