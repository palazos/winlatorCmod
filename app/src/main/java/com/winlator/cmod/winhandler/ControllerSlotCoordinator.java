package com.winlator.cmod.winhandler;

import android.view.InputDevice;

import com.winlator.cmod.inputcontrols.ExternalController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keeps stable slot reservations by controller fingerprint so reconnects can
 * restore the same slot.
 */
class ControllerSlotCoordinator {
    private final Map<String, Integer> fingerprintToSlot = new HashMap<>();

    public List<InputDevice> getConnectedControllersSorted() {
        List<InputDevice> controllers = new ArrayList<>();
        for (int deviceId : InputDevice.getDeviceIds()) {
            InputDevice device = InputDevice.getDevice(deviceId);
            if (device != null && ExternalController.isGameController(device)) {
                controllers.add(device);
            }
        }

        controllers.sort((a, b) -> {
            String left = getDeviceFingerprint(a);
            String right = getDeviceFingerprint(b);
            if (left == null) left = "";
            if (right == null) right = "";
            return left.compareToIgnoreCase(right);
        });
        return controllers;
    }

    public int reservePreferredSlot(int deviceId, int maxSlots, Set<Integer> usedSlots, Map<Integer, Integer> deviceToSlot) {
        String fingerprint = getDeviceFingerprint(deviceId);
        if (fingerprint == null) {
            return -1;
        }

        Integer reservedSlot = fingerprintToSlot.get(fingerprint);
        if (reservedSlot != null && reservedSlot >= 0 && reservedSlot < maxSlots && !usedSlots.contains(reservedSlot)) {
            usedSlots.add(reservedSlot);
            deviceToSlot.put(deviceId, reservedSlot);
            return reservedSlot;
        }

        for (int slot = 0; slot < maxSlots; slot++) {
            if (!usedSlots.contains(slot)) {
                usedSlots.add(slot);
                deviceToSlot.put(deviceId, slot);
                fingerprintToSlot.put(fingerprint, slot);
                return slot;
            }
        }
        return -1;
    }

    public void rememberAssignedSlot(int deviceId, int slot) {
        String fingerprint = getDeviceFingerprint(deviceId);
        if (fingerprint != null) {
            fingerprintToSlot.put(fingerprint, slot);
        }
    }

    public void clear() {
        fingerprintToSlot.clear();
    }

    private String getDeviceFingerprint(int deviceId) {
        if (deviceId < 0) {
            return null;
        }
        InputDevice device = InputDevice.getDevice(deviceId);
        return getDeviceFingerprint(device);
    }

    private String getDeviceFingerprint(InputDevice device) {
        if (device == null) {
            return null;
        }
        String descriptor = device.getDescriptor() != null ? device.getDescriptor() : "";
        String name = device.getName() != null ? device.getName() : "";
        return device.getVendorId() + ":" + device.getProductId() + ":" + descriptor + ":" + name;
    }
}
