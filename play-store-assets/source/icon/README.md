# Icon source status

Audit date: **2026-07-20**

## Blocking source discrepancy

The brief identifies `ic_splitframe_logo.png` as the required brand source.
That filename is absent from the repository, and there is no textual reference
that maps it to another file. Therefore a new 512 px Store icon cannot honestly
be certified as derived from the named source.

Do one of the following before treating
`../../common/icon/app-icon-512.png` as production-ready:

1. supply the authoritative `ic_splitframe_logo.png`; or
2. provide written confirmation that the existing
   `app/src/main/res/drawable-nodpi/ic_image_compressor_logo.png` is the same
   authoritative artwork under a different name.

Until then, any generated 512 px candidate is **conditional and must not be
uploaded as a source-verified replacement**.

## Installed-brand references

The current installed identity is a text-free blue/cyan/purple mark made from
overlapping photo cards and inward compression arrows. The following repository
files are the auditable references:

| Repository file | Role | Technical record | SHA-256 |
| --- | --- | --- | --- |
| `app/src/main/res/drawable-nodpi/ic_image_compressor_logo.png` | Android 12+ splash icon | PNG, 1024×1024, 8-bit RGB; no alpha; 841,368 bytes | `cd7d65fbdbf0ba78217e97ae7a6528161337a961be76defed47d3844ed43a067` |
| `app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png` | Highest-density adaptive launcher foreground | PNG, 432×432, 8-bit RGBA | `c1407ad27fc547d18e44aae486553bbcc1fb14efa49c4b4ef5bf91b13dcb1122` |
| `app/src/main/res/mipmap-xxxhdpi/ic_launcher_monochrome.png` | Highest-density themed launcher layer | PNG, 432×432, 8-bit RGBA | `704409e5cc49a793fbcd60d6cc55bc9c60e92cdda66ea8fb2c6274a79c462d30` |
| `play-store/assets/app-icon-512.png` | Legacy publishing candidate, not an installed resource | PNG, 512×512, 8-bit RGBA; 395,611 bytes | `88d4979675a279c702dc22e2d2309116cd6204c9e5402236c13f81a8187101c8` |

Lower-density launcher variants and the adaptive icon XMLs also exist, but
they are not higher-quality reconstruction sources.

## Permitted interim use

The installed-brand files may be copied here as visual references so the asset
set remains consistent with the shipping app. They must not be relabeled as
`ic_splitframe_logo.png`, and no unrelated vector reconstruction should be
invented to conceal the missing source.

If the owner confirms the 1024 px splash raster as authoritative, a deterministic
downsample may be produced with explicit sRGB tagging. The resulting Play icon
must then be checked independently for:

- exactly 512×512 pixels;
- 32-bit PNG and no more than 1024 KB;
- no text, badge, ranking or promotional claim;
- no extra external drop shadow;
- no newly added pre-rounded outer mask; and
- faithful visual continuity with the installed launcher icon.

Record the confirmed source filename, source hash, approval date and exact
generation command in this directory before changing the status from
conditional to production-ready.
