package com.alexistdev.geobill.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Properties;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Menjaga kedua bundle tetap sinkron. Key yang hanya ada di satu bahasa akan melempar
 * NoSuchMessageException saat runtime, dan baru ketahuan oleh pengguna.
 */
class MessagesBundleTest {

    private Properties load(String name) throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("messages/" + name)) {
            assertTrue(in != null, "Bundle tidak ditemukan di classpath: " + name);
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        }
    }

    @Test
    @DisplayName("Bundle Inggris dan Indonesia punya key yang sama persis")
    void bundles_haveTheSameKeys() throws Exception {
        Properties english = load("messages_en.properties");
        Properties indonesian = load("messages_id.properties");

        assertEquals(new TreeSet<>(english.stringPropertyNames()),
                new TreeSet<>(indonesian.stringPropertyNames()));
    }

    @Test
    @DisplayName("Tidak ada pesan yang kosong")
    void bundles_haveNoBlankValues() throws Exception {
        for (String bundle : new String[]{"messages_en.properties", "messages_id.properties"}) {
            Properties properties = load(bundle);
            for (String key : properties.stringPropertyNames()) {
                assertFalse(properties.getProperty(key).isBlank(), "Pesan kosong pada " + bundle + ": " + key);
            }
        }
    }
}
