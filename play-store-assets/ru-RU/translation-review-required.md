# Russian store-asset copy — translation review required

> **NOT PRODUCTION-READY — DO NOT PUBLISH OR UPLOAD.**
>
> This is an unreviewed working translation for layout preparation. A qualified
> Russian reviewer must approve meaning, tone, grammar, line breaks, cultural
> fit, and the rendered graphic before this status can change.

- App resource locale: `ru`
- Intended Google Play listing locale: `ru-RU`
- Terminology anchors: existing `values-ru/strings.xml`
- Marketing-review evidence: **none supplied**
- Reviewer / date / approval record: **pending**

The English source in `../qa/copy-deck.csv` remains authoritative for claims.
Numeric tokens must be populated only from the same real capture's evidence.
The current graphics localize the marketing overlay but retain English text
inside genuine app captures. The reviewer must explicitly approve that mixed-
language presentation or request fresh real captures with the app running in
Russian before any upload.

| Asset | Draft headline | Draft supporting or measurement copy |
| --- | --- | --- |
| Feature graphic | Фото меньше. Фон чистый. | Сжать • Удалить • Заменить |
| Phone 01 — compression | Файл меньше. Детали чёткие. | `{ORIGINAL_SIZE} → {PROCESSED_SIZE} • Экономия {PERCENT_SAVED}%` |
| Phone 02 — background removal | Легко удаляйте фон | — |
| Phone 03 — solid-color replacement | Замените фон сплошным цветом | Белый • Синий • Зелёный • Красный |
| Phone 04 — batch compression | Сжимайте несколько фото сразу | — |
| Phone 05 — resizing | Размер в процентах или пикселях | 25% • 50% • 75% • Свои ширина и высота • Сохранить соотношение сторон |
| Phone 06 — format conversion | Конвертируйте JPG, PNG и WEBP | — |
| Phone 07 — comparison | Посмотрите разницу | `Исходный размер: {ORIGINAL_SIZE} • Обработанный: {PROCESSED_SIZE} • Экономия {PERCENT_SAVED}% • {COMPRESSION_RATIO}:1` |
| Phone 08 — trust | Обработка изображений — на устройстве | Изображения не загружаются для обработки • Аккаунт не требуется • Приложение не добавляет водяной знак • Легко сохраняйте и делитесь |

## Mandatory reviewer checks

- Confirm that “Детали чёткие” communicates visible clarity without promising
  lossless or identical quality.
- Confirm “сплошным цветом” is a natural description of a solid RGB background,
  not a photo scene or generated background.
- Keep the background-removal headline equivalent to **“Remove Backgrounds
  Easily”**; do not introduce “one tap,” “instant,” or perfect-edge wording.
- Keep the trust statement scoped to image processing. The app uses networked
  advertising/consent services and must not be described as wholly offline.
- Approve “Аккаунт не требуется” and “Приложение не добавляет водяной знак” as
  new marketing translations; they do not have reviewed localized resources.
- Check capitalization, punctuation, headline register, wrapping, Cyrillic
  glyphs, and the final Noto Sans rendering at full and thumbnail sizes.

Localized alt-text drafts are in `alt-text.md` and have the same review gate.
