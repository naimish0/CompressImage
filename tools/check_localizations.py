#!/usr/bin/env python3
"""Validate production locale resources against the default English catalog."""

from __future__ import annotations

import collections
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
LOCALES = {
    "hi": "values-hi",
    "gu": "values-gu",
    "mr": "values-mr",
    "bn": "values-bn",
    "pa": "values-pa",
    "ta": "values-ta",
    "te": "values-te",
    "kn": "values-kn",
    "ml": "values-ml",
    "as": "values-as",
    "or": "values-or",
    "ur": "values-ur",
    "ru": "values-ru",
    "es": "values-es",
    "fr": "values-fr",
    "de": "values-de",
    "pt": "values-pt",
    "pt-BR": "values-pt-rBR",
    "it": "values-it",
    "id": "values-id",
    "ar": "values-ar",
    "ja": "values-ja",
    "ko": "values-ko",
    "zh-Hans": "values-b+zh+Hans",
    "zh-Hant": "values-b+zh+Hant",
}
COMPATIBILITY_ALIASES = {
    # Android 14 and earlier rewrite the modern Indonesian code `id` to `in`.
    "values-in": "values-id",
}
POSITIONAL_FORMAT = re.compile(
    r"%(\d+)\$[-#+ 0,(<]*\d*(?:\.\d+)?([a-zA-Z])"
)
PROTECTED_TOKENS = ("Photo Compressor", "JPG", "JPEG", "PNG", "WEBP")
SENTENCE_END = re.compile(r"[.!?।۔。！？](?:\s|$)")
LOCALE_DIRECTORY = re.compile(
    r"^values-(?:[a-z]{2,3}|[a-z]{2,3}-r(?:[A-Z]{2}|[0-9]{3})|"
    r"b(?:\+[A-Za-z0-9]{1,8})+)$"
)


def text(element: ET.Element) -> str:
    return "".join(element.itertext()).strip()


def placeholders(value: str) -> collections.Counter[str]:
    return collections.Counter(
        f"{position}${conversion}"
        for position, conversion in POSITIONAL_FORMAT.findall(value.replace("%%", ""))
    )


def sentence_count(value: str) -> int:
    return len(SENTENCE_END.findall(value))


def read_catalog(
    path: Path,
) -> tuple[dict[str, str], dict[str, dict[str, str]], set[str], set[str]]:
    root = ET.parse(path).getroot()
    strings: dict[str, str] = {}
    plurals: dict[str, dict[str, str]] = {}
    nontranslatable: set[str] = set()
    names: list[str] = []
    for element in root:
        name = element.attrib.get("name")
        if not name:
            continue
        names.append(name)
        if element.tag == "string":
            strings[name] = text(element)
            if element.attrib.get("translatable") == "false":
                nontranslatable.add(name)
        elif element.tag == "plurals":
            plurals[name] = {
                item.attrib["quantity"]: text(item)
                for item in element.findall("item")
            }
    duplicates = {
        name for name, count in collections.Counter(names).items() if count > 1
    }
    return strings, plurals, nontranslatable, duplicates


def required_plural_quantities(locale: str) -> set[str]:
    if locale == "ar":
        return {"zero", "one", "two", "few", "many", "other"}
    if locale == "ru":
        return {"one", "few", "many", "other"}
    if locale in {"ja", "ko", "zh-Hans", "zh-Hant", "id"}:
        return {"other"}
    if locale in {"es", "fr", "pt", "pt-BR", "it"}:
        return {"one", "many", "other"}
    return {"one", "other"}


def main() -> int:
    default_strings, default_plurals, nontranslatable, default_duplicates = read_catalog(
        RES / "values" / "strings.xml"
    )
    required_strings = set(default_strings) - nontranslatable
    failures: list[str] = []
    warnings: list[str] = []

    if default_duplicates:
        failures.append(
            "default: duplicate resource names: " + ", ".join(sorted(default_duplicates))
        )

    expected_directories = set(LOCALES.values()) | set(COMPATIBILITY_ALIASES)
    discovered_directories = {
        path.parent.name
        for path in RES.glob("values-*/strings.xml")
        if LOCALE_DIRECTORY.fullmatch(path.parent.name)
    }
    unexpected_directories = sorted(discovered_directories - expected_directories)
    if unexpected_directories:
        failures.append(
            "unexpected locale catalogs are not validated: "
            + ", ".join(unexpected_directories)
        )

    for locale, directory in LOCALES.items():
        path = RES / directory / "strings.xml"
        if not path.is_file():
            failures.append(f"{locale}: missing {path.relative_to(ROOT)}")
            continue
        try:
            translated, translated_plurals, _, duplicates = read_catalog(path)
        except ET.ParseError as error:
            failures.append(f"{locale}: invalid XML: {error}")
            continue

        if duplicates:
            failures.append(
                f"{locale}: duplicate resource names: {', '.join(sorted(duplicates))}"
            )

        missing = sorted(required_strings - set(translated))
        if missing:
            failures.append(f"{locale}: missing {len(missing)} strings: {', '.join(missing)}")

        empty = sorted(key for key in required_strings if not translated.get(key, "").strip())
        if empty:
            failures.append(f"{locale}: empty translations: {', '.join(empty)}")

        for key in sorted(required_strings & set(translated)):
            expected = placeholders(default_strings[key])
            actual = placeholders(translated[key])
            if actual != expected:
                failures.append(
                    f"{locale}:{key}: placeholder mismatch; expected {expected}, found {actual}"
                )
            source_words = POSITIONAL_FORMAT.sub("", default_strings[key])
            if (
                len(re.findall(r"[A-Za-z]", source_words)) >= 12
                and translated[key] == default_strings[key]
            ):
                failures.append(f"{locale}:{key}: long translation is identical to English")
            for token in PROTECTED_TOKENS:
                if token in default_strings[key] and token not in translated[key]:
                    failures.append(f"{locale}:{key}: protected token {token!r} was changed")
            expected_sentences = sentence_count(default_strings[key])
            if (
                key.startswith(("settings_", "policy_"))
                and expected_sentences >= 2
                and sentence_count(translated[key]) < expected_sentences
            ):
                failures.append(
                    f"{locale}:{key}: translated prose has fewer sentences "
                    f"({sentence_count(translated[key])}/{expected_sentences}); check for omitted meaning"
                )

        for plural_name, default_items in default_plurals.items():
            actual_items = translated_plurals.get(plural_name)
            if actual_items is None:
                failures.append(f"{locale}: missing plurals/{plural_name}")
                continue
            missing_quantities = required_plural_quantities(locale) - set(actual_items)
            if missing_quantities:
                failures.append(
                    f"{locale}:{plural_name}: missing quantities "
                    f"{', '.join(sorted(missing_quantities))}"
                )
            expected_placeholders = placeholders(default_items["other"])
            for quantity, value in actual_items.items():
                if placeholders(value) != expected_placeholders:
                    failures.append(
                        f"{locale}:{plural_name}/{quantity}: placeholder mismatch"
                    )

    for alias_directory, canonical_directory in COMPATIBILITY_ALIASES.items():
        alias_path = RES / alias_directory / "strings.xml"
        canonical_path = RES / canonical_directory / "strings.xml"
        if not alias_path.is_file():
            failures.append(
                f"compatibility alias: missing {alias_path.relative_to(ROOT)}"
            )
            continue
        try:
            alias_catalog = read_catalog(alias_path)
            canonical_catalog = read_catalog(canonical_path)
        except ET.ParseError as error:
            failures.append(f"compatibility alias {alias_directory}: invalid XML: {error}")
            continue
        if alias_catalog != canonical_catalog or alias_path.read_bytes() != canonical_path.read_bytes():
            failures.append(
                f"compatibility alias {alias_directory} must be byte-identical to {canonical_directory}"
            )

    for warning in warnings:
        print(f"WARNING: {warning}")
    if failures:
        for failure in failures:
            print(f"ERROR: {failure}", file=sys.stderr)
        print(f"Localization validation failed with {len(failures)} error(s).", file=sys.stderr)
        return 1

    print(
        f"Localization validation passed for {len(LOCALES)} locales, "
        f"{len(required_strings)} translated strings, and {len(default_plurals)} plural resource(s)."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
