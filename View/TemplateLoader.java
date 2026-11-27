package com.byahero.view;

import java.util.Map;

/**
 * ABSTRACTION: "something that can give us an HTML template".
 * Controllers depend on this interface, not on files, so the templates
 * could come from anywhere (a folder, memory in a test, a database...).
 *
 * ISP/DIP note: this used to be a class with a STATIC folder setting that
 * Main had to configure before anything worked. Now it is a normal object
 * that is handed to the classes that need it.
 */
public interface TemplateLoader {

    /** Returns the raw text of a template, like "board.html". */
    String load(String fileName);

    /**
     * Loads a template and fills in every {{placeholder}} from the map.
     * The text is scanned only once, so a value that contains "{{something}}"
     * is never filled in a second time. Unknown placeholders are left as they are.
     */
    default String render(String fileName, Map<String, String> values) {
        String template = load(fileName);
        StringBuilder out = new StringBuilder();

        int position = 0;
        while (position < template.length()) {
            int open = template.indexOf("{{", position);
            int close = -1;
            if (open != -1) {
                close = template.indexOf("}}", open + 2);
            }
            if (open == -1 || close == -1) {
                out.append(template, position, template.length());
                break;
            }

            out.append(template, position, open);
            String key = template.substring(open + 2, close);
            if (values.containsKey(key)) {
                out.append(values.get(key));
            } else {
                out.append(template, open, close + 2); // keep unknown placeholder visible
            }
            position = close + 2;
        }
        return out.toString();
    }
}
