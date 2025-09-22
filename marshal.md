# Marshal Specification

3rd Edition

> “In most countries, the rank of [Marshal](https://en.wikipedia.org/wiki/Field_marshal) is the highest [Army](https://en.wikipedia.org/wiki/Army) rank.”

# 1. Introduction

**Marshal** is a high-level API designed to abstract the specific syntax used by [Brigadier](https://github.com/Mojang/brigadier) into a consumer-facing DSL.

## 1.1. Formalities

> The key words "**MUST**", "**MUST NOT**", "**REQUIRED**", "**SHALL**", "**SHALL
> NOT**", "**SHOULD**", "**SHOULD NOT**", "**RECOMMENDED**", "**NOT RECOMMENDED**",
> "**MAY**", and "**OPTIONAL**" in this document are to be interpreted as
> described in [BCP 14](https://datatracker.ietf.org/doc/html/bcp14) [[RFC2119](https://datatracker.ietf.org/doc/html/rfc2119)] [[RFC8174](https://datatracker.ietf.org/doc/html/rfc8174)] when, and only when, they
> appear in all capitals, as shown here.

## 1.2. Formatting Guide

The first occurence of some domain-specific terminology appears written in _Capitalised Italic_.

# 2. DSL

Any string that conforms to the Marshal DSL is referred to as a _Command Path_. A command path represents a sequence of steps one must take starting from the **root** node of the **command tree**, to reach the the specific **leaf** at which the command lies. Such a single "step" is referred to as a _Segment_.

Two segments are separated from each other with a **forward slash** (`/`, `u002F`). A command path **MAY** start with a single leading slash, and end in a single trailing one. A command path **MUST** start with a [Literal Segment](#211-literal-segments).

```ebnf
CommandPath = [ "/" ] , [ PrefixSegment , "/" ] , LiteralSegment , { "|" , LiteralSegment } , { "/" , Segment } , [ "/" ]
```