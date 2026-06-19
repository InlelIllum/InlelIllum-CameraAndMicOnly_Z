package ru.liner.sensorprivacy;

import android.hardware.ISensorPrivacyManager;
import android.os.RemoteException;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Тонкая обёртка над скрытым ISensorPrivacyManager.
 *
 * Скрывает детали гранулярного API и предоставляет
 * простые методы для работы ТОЛЬКО с микрофоном и камерой.
 *
 * Главные методы:
 *  - {@link #setPrivacyForMicAndCamera(boolean)} — включает/выключает
 *    приватность сразу для микрофона и камеры (атомарно с точки зрения UI).
 *  - {@link #isPrivacyEnabledForMicAndCamera()} — true, если ОБА датчика
 *    сейчас находятся в режиме приватности.
 *  - {@link #syncPrivacyForMicAndCamera(boolean)} — принудительно
 *    приводит состояние системы к ожидаемому (используется при запуске).
 */
public final class SensorPrivacyController {

    @Nullable
    private final ISensorPrivacyManager manager;

    public SensorPrivacyController(@Nullable ISensorPrivacyManager manager) {
        this.manager = manager;
    }

    public boolean isAvailable() {
        return manager != null;
    }

    /**
     * Устанавливает состояние приватности для микрофона и камеры одновременно.
     * Возвращает true, если оба вызова прошли без ошибок.
     */
    public boolean setPrivacyForMicAndCamera(boolean enable) {
        if (manager == null) return false;
        int userId = UserHandle.myUserId();
        int source = SensorPrivacyConstants.DEFAULT_SOURCE;
        boolean ok = true;
        try {
            manager.setToggleSensorPrivacy(
                    userId, source,
                    SensorPrivacyConstants.SENSOR_MICROPHONE,
                    enable);
        } catch (RemoteException | SecurityException e) {
            e.printStackTrace();
            ok = false;
        }
        try {
            manager.setToggleSensorPrivacy(
                    userId, source,
                    SensorPrivacyConstants.SENSOR_CAMERA,
                    enable);
        } catch (RemoteException | SecurityException e) {
            e.printStackTrace();
            ok = false;
        }
        return ok;
    }

    /**
     * Возвращает true, только если ОБА датчика (микрофон и камера)
     * находятся в режиме приватности.
     */
    public boolean isPrivacyEnabledForMicAndCamera() {
        if (manager == null) return false;
        try {
            boolean mic = manager.isToggleSensorPrivacyEnabled(
                    SensorPrivacyConstants.TOGGLE_TYPE_SOFTWARE,
                    SensorPrivacyConstants.SENSOR_MICROPHONE);
            boolean cam = manager.isToggleSensorPrivacyEnabled(
                    SensorPrivacyConstants.TOGGLE_TYPE_SOFTWARE,
                    SensorPrivacyConstants.SENSOR_CAMERA);
            return mic && cam;
        } catch (RemoteException | SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Возвращает true, если ХОТЯ БЫ ОДИН из датчиков
     * (микрофон или камера) находится в режиме приватности.
     * Полезно для отображения partial-состояния в UI.
     */
    public boolean isPrivacyEnabledForAny() {
        if (manager == null) return false;
        try {
            boolean mic = manager.isToggleSensorPrivacyEnabled(
                    SensorPrivacyConstants.TOGGLE_TYPE_SOFTWARE,
                    SensorPrivacyConstants.SENSOR_MICROPHONE);
            boolean cam = manager.isToggleSensorPrivacyEnabled(
                    SensorPrivacyConstants.TOGGLE_TYPE_SOFTWARE,
                    SensorPrivacyConstants.SENSOR_CAMERA);
            return mic || cam;
        } catch (RemoteException | SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }
}
