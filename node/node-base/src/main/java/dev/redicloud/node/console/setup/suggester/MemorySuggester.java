package dev.redicloud.node.console.setup.suggester;

import dev.redicloud.node.console.setup.Setup;
import dev.redicloud.node.console.setup.SetupEntry;
import dev.redicloud.node.console.setup.SetupSuggester;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MemorySuggester implements SetupSuggester {

    @Override
    public List<String> suggest(Setup<?> setup, SetupEntry entry) {
        List<Integer> list = Arrays.asList(512, 1024, 2048, 4096, 8192, 16384, 32768, 65536);
        Collections.reverse(list);
        return list.parallelStream().map(String::valueOf).collect(Collectors.toList());
    }
}
