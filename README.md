# SensorsOff (только микрофон + камера) — модифицированная версия

> Форк [LinerSRT/SensorsOff](https://github.com/LinerSRT/SensorsOff) v1.3
> с одним важным изменением: блокируются **только микрофон и камера**,
> остальные датчики (акселерометр, гироскоп, магнитометр, датчик тряски,
> поворота, шагомер, освещённости, приближения) остаются работать.

## Что было изменено

### Проблема
Оригинальное приложение вызывает скрытый метод Android
`ISensorPrivacyManager.setSensorPrivacy(boolean)` — это **глобальный**
переключатель приватности, который отключает вообще ВСЕ датчики устройства.
Поэтому у вас заодно отрубается тряска, поворот экрана, шагомер и т.д.

### Решение
Начиная с Android 12 (API 31) у Android есть гранулярный метод:

```java
setToggleSensorPrivacy(int userId, int source, int sensor, boolean enable)
```

где параметр `sensor` может быть `MICROPHONE = 1` или `CAMERA = 2`.
Мы делаем два вызова подряд — для микрофона и для камеры — вместо одного
глобального. Все остальные датчики система больше не трогает.

### Конкретные правки в коде

| Файл | Что изменилось |
|------|----------------|
| `SensorPrivacyConstants.java` *(новый)* | Константы скрытого API: `SENSOR_MICROPHONE=1`, `SENSOR_CAMERA=2`, `TOGGLE_TYPE_SOFTWARE=0`, `SOURCE_SHELL=4` |
| `SensorPrivacyController.java` *(новый)* | Обёртка над `ISensorPrivacyManager` с двумя удобными методами: `setPrivacyForMicAndCamera(bool)` и `isPrivacyEnabledForMicAndCamera()` |
| `SensorsOffTileService.java` | Вызов `setSensorPrivacy(...)` заменён на `privacyController.setPrivacyForMicAndCamera(...)`. Состояние тайла теперь читается из системы через `isToggleSensorPrivacyEnabled` |
| `BlockingService.java` | Аналогичная замена для функции блокировки по списку приложений |
| `app/build.gradle` | Версия bumped до `1.4-mic-cam-only`, добавлена поддержка подписи через GitHub Secrets с fallback на debug-ключ |
| `res/values/strings.xml`, `res/values-ru/strings.xml` | Текст тайла изменён на «Mic + Cam off» / «Мик+Кам откл», чтобы было понятно, что именно блокируется |
| `.github/workflows/build.yml` *(новый)* | CI workflow для сборки APK прямо на GitHub |

Полный diff смотрите в git-истории (если зальёте в репозиторий).

---

## Как собрать APK через GitHub Actions (пошагово для новичка)

Вам **не нужно** устанавливать Android Studio, JDK или Android SDK на свой
компьютер. Всё соберётся на серверах GitHub бесплатно.

### Шаг 1. Создайте свой репозиторий на GitHub
1. Зайдите на [github.com](https://github.com) (зарегистрируйтесь, если ещё нет).
2. Нажмите **`+`** в правом верхнем углу → **New repository**.
3. Имя: например `sensorsoff-mic-cam`. Тип: **Public** или **Private** (любой).
4. **НЕ** ставьте галочки «Add README» / «.gitignore» / «license» — пустой репозиторий.
5. Нажмите **Create repository**.

### Шаг 2. Залейте код в репозиторий
Скачайте архив `SensorsOff-mic-cam-only-src.zip` (из этого же delivery),
распакуйте его. У вас получится папка с проектом. В этой папке:

**На компьютере с установленным git** (через терминал / CMD / PowerShell):
```bash
cd путь/к/распакованной/папке
git init
git add .
git commit -m "SensorsOff: only mic + camera"
git branch -M main
git remote add origin https://github.com/ВАШ_ЛОГИН/sensorsoff-mic-cam.git
git push -u origin main
```

**Если git не установлен** — просто откройте репозиторий на GitHub,
нажмите **Add file → Upload files** и перетащите все файлы из распакованной
папки (сохраняя структуру каталогов). Это не очень удобно, но работает.

### Шаг 3. Запустите сборку
1. В репозитории откройте вкладку **Actions**.
2. Слева выберите **Build APK**.
3. Справа нажмите **Run workflow → Run workflow** (зелёная кнопка).
4. Подождите 5–10 минут._workflow.
5. Когда сборка закончится, откройте её — там будет раздел **Artifacts**.
6. Скачайте `SensorsOff-release-mic-cam-only` — это zip, внутри лежит
   `app-release.apk`. Это и есть готовое приложение.

### Шаг 4. Установите APK на телефон
1. Перекиньте `app-release.apk` на телефон (через USB, Telegram «Saved Messages», диск — как угодно).
2. На телефоне: **Настройки → Приложения → Доступ к special apps → Установка неизвестных приложений** → разрешите для браузера/файлового менеджера.
3. Откройте APK-файл на телефоне → подтвердите установку.
4. Если телефон ругается «Этот пакет был подписан другим ключом» — сначала удалите старый SensorsOff, потом ставьте новый.

### Шаг 5. Настройте Shizuku и тайл
1. Установите **[Shizuku](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api)** из Play Market.
2. Запустите Shizuku, следуйте инструкции по активации (через ADB или Root).
3. Откройте SensorsOff → выдайте разрешение в Shizuku.
4. Опустите шторку уведомлений → нажмите **«Изменить»** (карандаш) →
   перетащите тайл **SensorsOff** в активную зону.
5. Готово. Нажатие на тайл теперь блокирует **только** микрофон и камеру.

---

## (Опционально) Подпись APK своим ключом

По умолчанию GitHub Actions подпишет APK debug-ключом — этого достаточно
для личного использования. Если хотите «правильную» подпись:

1. На компьютере с Java сгенерируйте keystore:
   ```bash
   keytool -genkey -v -keystore release.keystore \
     -alias sensors -keyalg RSA -keysize 2048 -validity 10000
   ```
   Запомните пароли (keystore и ключа).

2. Закодируйте файл в base64:
   - **Linux/macOS:** `base64 release.keystore > release.keystore.b64`
   - **Windows PowerShell:**
     ```powershell
     [Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore")) | Out-File -Encoding ascii release.keystore.b64
     ```

3. На GitHub: **Settings → Secrets and variables → Actions → New repository secret**.
   Добавьте 4 секрета:
   | Имя секрета | Значение |
   |-------------|----------|
   | `KEYSTORE_BASE64` | Содержимое файла `release.keystore.b64` (одной строкой) |
   | `KEYSTORE_PASSWORD` | Пароль от keystore |
   | `KEY_ALIAS` | `sensors` (или тот алиас, что указали) |
   | `KEY_PASSWORD` | Пароль от ключа |

4. Запустите workflow заново — APK будет подписан вашим ключом.

---

## Проверка: как убедиться, что тряска работает

1. Включите SensorsOff (тап по тайлу — тайл загорится).
2. Откройте любое приложение, где есть «потрясти телефон» — например,
   Telegram (настройки → данные → потрясти для отправки отзыва) или
   тест датчиков типа **Sensor Box Free**.
3. Потрясите телефон — функция должна сработать.
4. Проверьте, что камера и микрофон реально заблокированы:
   откройте камеру — будет чёрный экран; запишите голосовое сообщение
   в Telegram — будет тишина.

Если тряска всё ещё не работает — убедитесь, что у вас установлена именно
модифицированная версия (проверьте версию в приложении: должно быть
**1.4-mic-cam-only**).

---

## Совместимость
- Android **12+** (API 31+). На более старых версиях гранулярный API
  недоступен — приложение на таких устройствах работать не будет
  (это ограничение самого Android, не приложения).
- Требуется установленный и активированный **Shizuku**.

## Благодарности
Оригинальный автор: **Line'R** ([github.com/LinerSRT](https://github.com/LinerSRT)).
Без его работы ничего бы не было. Эта модификация распространяется на тех же
условиях — Apache-2.0, с обязательным указанием автора и ссылки на
[оригинальный репозиторий](https://github.com/LinerSRT/SensorsOff).

## Лицензия
Apache-2.0. Используя этот код, вы обязаны указать автора и ссылку на
оригинальный репозиторий.
