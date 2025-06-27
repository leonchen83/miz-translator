package org.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Baoyi Chen
 */
public class Configure {
    
    private Properties properties;
    
    private Configure() {
        this.properties = new Properties();
        try {
            String path = System.getProperty("conf");
            if (path != null && path.trim().length() != 0) {
                try (InputStream in = new FileInputStream(path)) {
                    Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                    properties.load(reader);
                }
            } else {
                ClassLoader loader = Configure.class.getClassLoader();
                try (InputStream in = loader.getResourceAsStream("trans.conf")) {
                    Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                    properties.load(reader);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private Configure(Properties properties) {
        this();
        if (properties != null)
            this.properties.putAll(properties);
    }
    
    public Properties properties() {
        return this.properties;
    }

    private String hint = null;
    private String baseURL = null;
    private String apiKey = null;
    private String model = null;
    private String translator = null;
    private boolean original = false;
    private double temperature = 0d;
    private int maxTokens = 4096;
    private int minimumLength = 12;
    private String[] filters = new String[0];
    private String[] keyFilters = new String[0];
    
    private Map<String, String> fixed = new HashMap<>();
    
    public String getHint() {
        return hint;
    }
    
    public void setHint(String hint) {
        this.hint = hint;
    }
    
    public String getTranslator() {
        return translator;
    }
    
    public void setTranslator(String translator) {
        this.translator = translator;
    }
    
    public boolean getOriginal() {
        return original;
    }
    
    public void setOriginal(boolean original) {
        this.original = original;
    }
    
    public String getBaseURL() {
        return baseURL;
    }
    
    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public int getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public int getMinimumLength() {
        return minimumLength;
    }
    
    public void setMinimumLength(int minimumLength) {
        this.minimumLength = minimumLength;
    }
    
    public String[] getFilters() {
        return filters;
    }
    
    public void setFilters(String[] filters) {
        this.filters = filters;
    }
    
    public String[] getKeyFilters() {
        return keyFilters;
    }
    
    public void setKeyFilters(String[] keyFilters) {
        this.keyFilters = keyFilters;
    }
    
    public Map<String, String> getFixed() {
        return fixed;
    }
    
    public void setFixed(Map<String, String> fixed) {
        this.fixed = fixed;
    }
    
    public static Configure bind() {
        return bind(null);
    }
    
    public static Configure bind(Properties properties) {
        Configure conf = new Configure(properties);
        conf.hint = getString(conf, "hint", null, false);
        conf.translator = getString(conf, "translator", null, false);
        conf.baseURL = getString(conf, "baseURL", null, false);
        conf.apiKey = getString(conf, "apiKey", null, false);
        conf.model = getString(conf, "model", null, false);
        conf.temperature = getDouble(conf, "temperature", 0.3d, true);
        conf.maxTokens = getInt(conf, "maxTokens", 4096, true);
        conf.minimumLength = getInt(conf, "minimumLength", 12, true);
        conf.filters = getStrings(conf, "filters");
        conf.keyFilters = getStrings(conf, "keyFilters");
        conf.fixed = getMap(conf, "source", "target"); 
        return conf;
    }

    public static boolean getBool(String value, boolean defaultValue) {
        if (value == null)
            return defaultValue;
        if (value.equals("false") || value.equals("no"))
            return false;
        if (value.equals("true") || value.equals("yes"))
            return true;
        return defaultValue;
    }

    public static int getInt(String value, int defaultValue) {
        if (value == null)
            return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double getDouble(String value, double defaultValue) {
        if (value == null)
            return defaultValue;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public static String getString(Configure conf, String key) {
        return getString(conf, key, null, false);
    }
    
    public static Integer getInt(Configure conf, String key) {
        return getInt(conf, key, null, false);
    }
    
    public static Boolean getBool(Configure conf, String key) {
        return getBool(conf, key, null, false);
    }

    public static List<String> getList(Configure conf, String key) {
        return getList(conf, key, null, false);
    }
    
    public static String getString(Configure conf, String key, String value, boolean optional) {
        String v = System.getProperty(key);
        if (Strings.isEmpty(v) && Strings.isEmpty(v = conf.properties.getProperty(key)))
            v = value;
        if (v == null && !optional) {
            throw new IllegalArgumentException("not found the config[key=" + key + "]");
        }
        return v;
    }
    
    public static String[] getStrings(Configure conf, String prefix) {
        List<String> list = new ArrayList<>();
        conf.properties.forEach((k, v) -> {
            if (k instanceof String key && key.startsWith(prefix)) {
                String value = getString(conf, key, null, true);
                if (value != null) list.add(value);
            }
        });
        return list.toArray(new String[0]);
    }
    
    public static Map<String, String> getMap(Configure conf, String prefix1, String prefix2) {
        final Map<String, String> map = new HashMap<>();
        conf.properties.forEach((k, v) -> {
            if (k instanceof String key && key.startsWith(prefix1)) {
                String value1 = getString(conf, key, null, true);
                if (value1 != null) {
                    key = key.replace(prefix1, prefix2);
                    String value2 = getString(conf, key, null, true);
                    if (value2 != null) {
                        map.put(value1, value2);
                    }
                }
            }
        });
        return map;
    }
    
    public static Integer getInt(Configure conf, String key, Integer value, boolean optional) {
        String v = getString(conf, key, value == null ? null : value.toString(), optional);
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("not found the config[key=" + key + "]");
        }
    }
    
    public static Double getDouble(Configure conf, String key, Double value, boolean optional) {
        String v = getString(conf, key, value == null ? null : value.toString(), optional);
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("not found the config[key=" + key + "]");
        }
    }
    
    public static Boolean getBool(Configure conf, String key, Boolean value, boolean optional) {
        String v = getString(conf, key, value == null ? null : value.toString(), optional);
        if (v == null)
            return value;
        if (v.equals("yes") || v.equals("true"))
            return Boolean.TRUE;
        if (v.equals("no") || v.equals("false"))
            return Boolean.FALSE;
        throw new IllegalArgumentException("not found the config[key=" + key + "]");
    }

    public static List<String> getList(Configure conf, String key, String value, boolean optional) {
        String v = getString(conf, key, value == null ? null : value, optional);
        if (v == null) 
            return null;
        return Arrays.stream(v.split(",")).map(e -> e.trim()).collect(Collectors.toList());
    }
    
    @Override
    public String toString() {
        return "Configure{" +
                "hint='" + hint + '\'' +
                ", baseURL='" + baseURL + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", model='" + model + '\'' +
                ", translator='" + translator + '\'' +
                ", original=" + original +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                ", minimumLength=" + minimumLength +
                ", filters=" + Arrays.toString(filters) +
                ", keyFilters=" + Arrays.toString(keyFilters) +
                ", fixed=" + fixed +
                '}';
    }
}
