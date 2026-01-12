# AGENTS.md - Mips Simulator Development Guide

## Project Overview
See [README.md](README.md) for more details.

## Style
- Follow existing codebase conventions and style.

## Dev tips
- Run `./gradlew spotlessApply` to format files after making a change.
- Reference [Mips for Programmers](https://s3-eu-west-1.amazonaws.com/downloads-mips/documents/MD00086-2B-MIPS32BIS-AFP-6.06.pdf) instruction format.
- Use comments sparingly and reserve them for obtuse codes only.

## Testing instructions
- Write unit tests for any non-trivial change you make.
- Unit tests should test for correctness not structure.
- Add or update tests for the code you change, even if nobody asked.

## PR instructions
- Always run `./gradlew spotlessApply build` before committing.
- Use PR title that summarizes the change.