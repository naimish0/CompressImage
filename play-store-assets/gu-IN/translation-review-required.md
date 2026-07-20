# Gujarati store-asset copy — translation review required

> **NOT PRODUCTION-READY — DO NOT PUBLISH OR UPLOAD.**
>
> This is an unreviewed working translation for layout preparation. A qualified
> Gujarati reviewer must approve meaning, tone, grammar, line breaks, glyph
> shaping, cultural fit, and the rendered graphic before this status can change.

- App resource locale: `gu`
- Internal asset folder: `gu-IN`
- Intended Google Play listing locale: **`gu`** (not `gu-IN`)
- Terminology anchors: existing `values-gu/strings.xml`
- Marketing-review evidence: **none supplied**
- Reviewer / date / approval record: **pending**

The English source in `../qa/copy-deck.csv` remains authoritative for claims.
Numeric tokens must be populated only from the same real capture's evidence.
The current graphics localize the marketing overlay but retain English text
inside genuine app captures. The reviewer must explicitly approve that mixed-
language presentation or request fresh real captures with the app running in
Gujarati before any upload.

| Asset | Draft headline | Draft supporting or measurement copy |
| --- | --- | --- |
| Feature graphic | નાના ફોટા. સ્વચ્છ બૅકગ્રાઉન્ડ. | કમ્પ્રેસ • દૂર કરો • બદલો |
| Phone 01 — compression | નાના ફોટા. સ્પષ્ટ વિગતો. | `{ORIGINAL_SIZE} → {PROCESSED_SIZE} • {PERCENT_SAVED}% બચત` |
| Phone 02 — background removal | બૅકગ્રાઉન્ડ સરળતાથી દૂર કરો | — |
| Phone 03 — solid-color replacement | કોઈપણ ઘન રંગથી બદલો | સફેદ • વાદળી • લીલો • લાલ |
| Phone 04 — batch compression | એક સાથે વધુ ફોટા કમ્પ્રેસ કરો | — |
| Phone 05 — resizing | ટકાવારી અથવા પિક્સેલથી કદ બદલો | 25% • 50% • 75% • કસ્ટમ પહોળાઈ અને ઊંચાઈ • આસ્પેક્ટ રેશિયો જાળવો |
| Phone 06 — format conversion | JPG, PNG અને WEBPમાં કન્વર્ટ કરો | — |
| Phone 07 — comparison | ફરક જુઓ | `મૂળ {ORIGINAL_SIZE} • પ્રોસેસ કરેલું {PROCESSED_SIZE} • {PERCENT_SAVED}% બચત • ગુણોત્તર {COMPRESSION_RATIO}:1` |
| Phone 08 — trust | ઇમેજ પ્રોસેસિંગ ડિવાઇસ પર થાય છે | પ્રોસેસિંગ માટે છબીઓ અપલોડ થતી નથી • એકાઉન્ટની જરૂર નથી • ઍપ વૉટરમાર્ક ઉમેરતી નથી • સરળતાથી સાચવો અને શેર કરો |

## Mandatory reviewer checks

- Confirm that “સ્પષ્ટ વિગતો” communicates visible clarity without promising
  lossless or identical quality.
- Confirm “કોઈપણ ઘન રંગ” means a solid RGB background, not a photo scene or
  generated background.
- Keep the background-removal headline equivalent to **“Remove Backgrounds
  Easily”**; do not introduce “one tap,” “instant,” or perfect-edge wording.
- Keep the trust statement scoped to image processing. The app uses networked
  advertising/consent services and must not be described as wholly offline.
- Approve “એકાઉન્ટની જરૂર નથી” and “ઍપ વૉટરમાર્ક ઉમેરતી નથી” as new marketing
  translations; they do not have reviewed localized resource strings.
- Inspect Gujarati shaping, vowel signs, punctuation, wrapping, and the final
  Noto Sans Gujarati rendering at full and thumbnail sizes.

Localized alt-text drafts are in `alt-text.md` and have the same review gate.
