# Hindi store-asset copy — translation review required

> **NOT PRODUCTION-READY — DO NOT PUBLISH OR UPLOAD.**
>
> This is an unreviewed working translation for layout preparation. A qualified
> Hindi reviewer must approve meaning, tone, grammar, line breaks, glyph shaping,
> cultural fit, and the rendered graphic before this status can change.

- App resource locale: `hi`
- Intended Google Play listing locale: `hi-IN`
- Terminology anchors: existing `values-hi/strings.xml`
- Marketing-review evidence: **none supplied**
- Reviewer / date / approval record: **pending**

The English source in `../qa/copy-deck.csv` remains authoritative for claims.
Numeric tokens must be populated only from the same real capture's evidence.
The current graphics localize the marketing overlay but retain English text
inside genuine app captures. The reviewer must explicitly approve that mixed-
language presentation or request fresh real captures with the app running in
Hindi before any upload.

| Asset | Draft headline | Draft supporting or measurement copy |
| --- | --- | --- |
| Feature graphic | छोटी फ़ोटो। साफ़ बैकग्राउंड। | कम्प्रेस • हटाएँ • बदलें |
| Phone 01 — compression | छोटी फ़ोटो। डिटेल साफ़। | `{ORIGINAL_SIZE} → {PROCESSED_SIZE} • {PERCENT_SAVED}% बचत` |
| Phone 02 — background removal | बैकग्राउंड आसानी से हटाएँ | — |
| Phone 03 — solid-color replacement | किसी भी ठोस रंग से बदलें | सफ़ेद • नीला • हरा • लाल |
| Phone 04 — batch compression | कई फ़ोटो एक साथ कम्प्रेस करें | — |
| Phone 05 — resizing | प्रतिशत या पिक्सेल से आकार बदलें | 25% • 50% • 75% • कस्टम चौड़ाई और ऊँचाई • आस्पेक्ट रेशियो बनाए रखें |
| Phone 06 — format conversion | JPG, PNG और WEBP में बदलें | — |
| Phone 07 — comparison | फ़र्क देखें | `मूल {ORIGINAL_SIZE} • प्रोसेस किया गया {PROCESSED_SIZE} • {PERCENT_SAVED}% बचत • अनुपात {COMPRESSION_RATIO}:1` |
| Phone 08 — trust | इमेज प्रोसेसिंग डिवाइस पर होती है | प्रोसेसिंग के लिए छवियाँ अपलोड नहीं होतीं • अकाउंट की ज़रूरत नहीं • ऐप वॉटरमार्क नहीं जोड़ता • आसानी से सहेजें और साझा करें |

## Mandatory reviewer checks

- Confirm that “डिटेल साफ़” communicates visible clarity without promising
  lossless or identical quality.
- Confirm “किसी भी ठोस रंग” means a solid RGB background, not a photo scene or
  generated background.
- Keep the background-removal headline equivalent to **“Remove Backgrounds
  Easily”**; do not introduce “one tap,” “instant,” or perfect-edge wording.
- Keep the trust statement scoped to image processing. The app uses networked
  advertising/consent services and must not be described as wholly offline.
- Approve “अकाउंट की ज़रूरत नहीं” and “ऐप वॉटरमार्क नहीं जोड़ता” as new
  marketing translations; they do not have reviewed localized resource strings.
- Inspect Devanagari conjuncts, matras, punctuation, wrapping, and the final
  Noto Sans Devanagari rendering at full and thumbnail sizes.

Localized alt-text drafts are in `alt-text.md` and have the same review gate.
