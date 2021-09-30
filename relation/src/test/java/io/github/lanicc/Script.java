package io.github.lanicc;

import java.util.*;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public class Script {

    public static void main(String[] args) {
        Map<String, List<String>> relationKeysMap = new HashMap<>();
        relationKeysMap.put("t1", Arrays.asList("a", "b"));
        relationKeysMap.put("t2", Arrays.asList("c", "d"));
        relationKeysMap.put("t3", Arrays.asList("e", "f"));
        String mainRemoveKeyScript = "ctx._source.removeKey('%s');";
        String relateOneRemoveKeyScript = "if (ctx._source.%s != null) {%s}";
        String relateOneInnerRemoveKeyScript = "ctx._source.%s.removeKey('%s');";
        String relateNestedRemoveKeyScript = "if (ctx._source.%s != null) ctx._source.%s.foreach(item -> {%s});";
        String relateNestedRemoveInnerKeyScript = "item.removeKey('%s');";

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : relationKeysMap.entrySet()) {

            String name = entry.getKey();
            List<String> removeKeys = entry.getValue();

            if (Objects.equals(name, "t1")) {
                for (String removeKey : removeKeys) {
                    sb.append(String.format(mainRemoveKeyScript, removeKey));
                }
            } else if (Objects.equals(name, "t2")) {
                StringBuilder sbb = new StringBuilder();
                for (String removeKey : removeKeys) {
                    sbb.append(String.format(relateOneInnerRemoveKeyScript, name, removeKey));
                }
                sb.append(String.format(relateOneRemoveKeyScript, name, sbb));
            } else if (Objects.equals(name, "t3")) {
                StringBuilder sbb = new StringBuilder();
                for (String removeKey : removeKeys) {
                    sbb.append(String.format(relateNestedRemoveInnerKeyScript, removeKey));
                }
                sb.append(String.format(relateNestedRemoveKeyScript, name, name, sbb));
            }
            System.out.println(sb);

        }
    }
}
