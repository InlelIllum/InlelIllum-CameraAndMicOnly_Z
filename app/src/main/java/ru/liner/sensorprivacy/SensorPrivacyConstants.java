package ru.liner.sensorprivacy;

/**
 * Константы скрытого Android API SensorPrivacyManager.
 *
 * Оригинальный SensorsOff использует устаревший метод
 * {@code setSensorPrivacy(boolean)}, который выключает
 * ВСЕ датчики устройства (микрофон, камера, акселерометр,
 * гироскоп, магнитометр, датчик приближения и т.д.).
 *
 * Начиная с Android 12 (API 31) доступен гранулярный метод
 * {@code setToggleSensorPrivacy(userId, source, sensor, enable)},
 * который позволяет выключать датчики по отдельности.
 * Мы используем его, чтобы заблокировать ТОЛЬКО микрофон и камеру,
 * оставив остальные датчики (тряска, поворот, шагомер) рабочими.
 *
 * Значения констант взяты из AOSP:
 *  frameworks/base/core/java/android/hardware/SensorPrivacyManager.java
 */
public final class SensorPrivacyConstants {

    private SensorPrivacyConstants() { }

    /** Тип переключателя: программный (темплет QS, настройки). */
    public static final int TOGGLE_TYPE_SOFTWARE = 0;

    /** Тип переключателя: аппаратный (физический kill-switch). */
    public static final int TOGGLE_TYPE_HARDWARE = 1;

    /** Датчик: микрофон. */
    public static final int SENSOR_MICROPHONE = 1;

    /** Датчик: камера. */
    public static final int SENSOR_CAMERA = 2;

    // --- Источник изменения (для аналитики в системе) ---

    public static final int SOURCE_UNKNOWN     = 0;
    public static final int SOURCE_SETTINGS    = 1;
    public static final int SOURCE_QS_TILE     = 2;
    public static final int SOURCE_OTHER       = 3;
    public static final int SOURCE_SHELL       = 4;
    public static final int SOURCE_SAFETY_HUB  = 5;

    /**
     * Источник, который мы указываем при вызове
     * setToggleSensorPrivacy(...). Через Shizuku мы работаем
     * с правами shell-пользователя, поэтому SOURCE_SHELL
     * наиболее точно отражает происходящее. На работоспособность
     * значения source это не влияет — это метка для логов системы.
     */
    public static final int DEFAULT_SOURCE = SOURCE_SHELL;
}
