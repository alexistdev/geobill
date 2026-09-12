package com.alexistdev.geolicense.starter.service;

import com.alexistdev.geolicense.starter.properties.GeoLicenseProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LicenseStateStoreTest {

    private LicenseStateStore store(Path stateDir) {
        GeoLicenseProperties properties = new GeoLicenseProperties();
        properties.setStateDir(stateDir.toString());
        properties.setLicenseKey("GEOLIC-TEST-KEY");
        properties.setProductSku("BILL");
        return new LicenseStateStore(properties);
    }

    @Test
    void save_thenLoad_returnsCachedActivation(@TempDir Path stateDir) {
        store(stateDir).save("machine-1", "token-1");

        LicenseStateStore.CachedActivation cached = store(stateDir).load();

        assertEquals("machine-1", cached.machineId());
        assertEquals("token-1", cached.token());
    }

    @Test
    void load_returnsNullWhenNothingStored(@TempDir Path stateDir) {
        assertNull(store(stateDir).load());
    }

    @Test
    void clear_removesCachedActivation(@TempDir Path stateDir) {
        LicenseStateStore store = store(stateDir);
        store.save("machine-1", "token-1");

        store.clear();

        assertNull(store.load());
    }
}
