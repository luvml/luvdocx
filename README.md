# LuvDOCX - DOCX DSL for Java

A fluent DSL (Domain Specific Language) wrapper around `docx4j` that makes DOCX document generation simpler, more readable, and more maintainable.

## What is LuvDOCX?

LuvDOCX is **not** a general-purpose markup framework like luvml. Instead, it's a **practical API wrapper** around docx4j that:

- Maps OOXML elements to simple function calls
- Accepts varargs for natural nesting
- Provides composable fragment containers for building dynamic content
- Makes document structures obvious from the code

## Why Not Frag_I?

LuvDOCX does **not** implement `Frag_I` from luvml, and here's why:

### The Fundamental Difference

**luvml** operates at the **abstraction level**:
- Builds a `Frag_I` tree (intermediate representation)
- Renders that tree to HTML/XML markup
- Pattern matching via sealed types enables flexible rendering

**LuvDOCX** operates at the **direct object level**:
- Directly constructs docx4j OOXML objects (`P`, `R`, `Text`, etc.)
- No intermediate representation - the objects ARE the final output
- These are passed directly to the document and saved

### Why This Matters

If LuvDOCX tried to implement `Frag_I`:
- Every DSL call would create a fragment wrapper
- Then pattern matching would unpack it to docx4j objects
- You'd have an extra layer of abstraction with **zero benefit**
- The code would be more complex for no additional functionality

### What LuvDOCX Actually Is

```
User Code
    ↓
LuvDOCX DSL Functions (fluent API)
    ↓
docx4j OOXML Objects (P, R, Text, etc.)
    ↓
DOCX File
```

The DSL makes creating docx4j objects **transparent and readable**. That's all it needs to do.

---

## Design Philosophy

**Make Java code look like the structure of the document you're building.**

### Before LuvDOCX (Verbose)

```java
P p = factory.createP();
PPr pPr = factory.createPPr();
PPrBase.Spacing spacing = factory.createPPrBaseSpacing();
spacing.setAfter(BigInteger.valueOf(150));
pPr.setSpacing(spacing);
p.setPPr(pPr);

R r = factory.createR();
RPr rPr = factory.createRPr();
rPr.setB(factory.createBooleanDefaultTrue());
Color color = factory.createColor();
color.setVal("1E40AF");
rPr.setColor(color);
r.setRPr(rPr);

Text t = factory.createText();
t.setValue("Hello World");
t.setSpace("preserve");
r.getContent().add(t);
p.getContent().add(r);
```

**36 lines of boilerplate**

### After LuvDOCX (Clean)

```java
w_p(
    w_pPr(w_spacing(after(150))),
    w_r(
        w_rPr(w_b(), w_color("1E40AF")),
        w_t("Hello World")
    )
)
```

**6 lines of intent-focused code** (83% reduction)

---

## Core Concepts

### 1. DSL Functions Map to OOXML Elements

| DSL Function | OOXML Element | Returns |
|---|---|---|
| `w_p(...)` | `<w:p>` | `P` |
| `w_r(...)` | `<w:r>` | `R` |
| `w_t(...)` | `<w:t>` | `Text` |
| `w_pPr(...)` | `<w:pPr>` | `PPr` |
| `w_rPr(...)` | `<w:rPr>` | `RPr` |
| `w_spacing(...)` | `<w:spacing>` | `PPrBase.Spacing` |

### 2. Varargs for Natural Nesting

```java
w_p(
    properties,      // Optional: formatting/styling
    content1,        // Children: runs, text, etc.
    content2,
    content3
)
```

Properties come first, content follows.

### 3. WFrags for Composition

Use `wfrags()` to group multiple elements:

```java
var section = wfrags(
    heading("Title"),
    paragraph("First point"),
    paragraph("Second point")
);

addTo(document, section);  // Automatically unpacks
```

### 4. Attributes via A Record

OOXML-specific attributes are passed as `A` records:

```java
w_spacing(
    before(100),  // A("before", "100")
    after(150)    // A("after", "150")
)
```

---

## Usage Examples

### Simple Paragraph

```java
w_p(w_r(w_t("Hello World")))
```

### Styled Text

```java
w_p(
    w_r(
        w_rPr(w_b(), w_color("0000FF"), w_sz(24)),
        w_t("Important")
    )
)
```

### Paragraph with Spacing and Background

```java
w_p(
    w_pPr(
        w_spacing(after(150)),
        w_shd(fill("F3F4F6")),
        w_ind(left(100))
    ),
    w_r(w_t("Indented paragraph"))
)
```

### Dynamic Content with Conditionals

```java
var frags = wfrags(
    heading("Report", "Heading2")
);

if (hasData) {
    frags.add(w_p(w_r(w_t("Data available"))));
} else {
    frags.add(w_p(w_r(w_t("No data"))));
}

addTo(document, frags);
```

### Adding to Document

```java
WordprocessingMLPackage doc = WordprocessingMLPackage.createPackage();

addTo(doc,
    w_p(w_r(w_t("Title"))),
    w_p(w_r(w_t("Content")))
);

doc.save(new File("output.docx"));
```

---

## API Design

### WFrags - Composable Fragments

`WFrags` is a simple container for multiple OOXML objects:

```java
public class WFrags implements Iterable<Object> {
    public WFrags add(Object... objects) { ... }
    public List<Object> items() { ... }
    public Iterator<Object> iterator() { ... }
    public boolean isEmpty() { ... }
}
```

**When to use:**
- Composing **multiple elements** that stay together
- Building **dynamic/conditional** content
- **Reusable component** groups

**Returns from:**
- `wfrags()` constructor
- Methods that build multi-element sections

### Single Element Methods

Return OOXML objects directly when method creates a single element:

```java
private P createTitle(String text) {
    return w_p(w_r(w_t(text)));  // Direct P return
}

private WFrags createSection(String title, List<String> items) {
    var frags = wfrags(heading(title));  // Multi-element, return WFrags
    for (var item : items) {
        frags.add(w_p(w_r(w_t(item))));
    }
    return frags;
}
```

### addTo() - Adding to Document

```java
public static void addTo(WordprocessingMLPackage doc, Object... elements) {
    var content = doc.getMainDocumentPart().getContent();
    for (var el : elements) {
        switch (el) {
            case WFrags wf -> content.addAll(wf.items());  // Unpack WFrags
            default -> content.add(el);                     // Add directly
        }
    }
}
```

**Usage:**
```java
addTo(doc, paragraph1, multiElementSection, paragraph2);
```

---

## Creating Reusable Components

Like React/Vue components, create reusable DOCX patterns:

```java
public static P styledHeading(String text, String colorHex) {
    return w_p(
        w_pPr(w_spacing(after(150))),
        w_r(
            w_rPr(w_b(), w_color(colorHex), w_sz(28)),
            w_t(text)
        )
    );
}

public static WFrags statsBlock(String label, String value) {
    return wfrags(
        w_p(w_r(w_rPr(w_b()), w_t(label + ": "))),
        w_p(
            w_pPr(w_ind(left(200))),
            w_r(w_t(value))
        )
    );
}

// Usage
addTo(doc,
    styledHeading("Report Title", "1E40AF"),
    statsBlock("Views", "1,234,567"),
    statsBlock("Comments", "89")
);
```

---

## Design Decisions

### Why `w_` Prefix?

- Makes it explicit: these are **OOXML (Word ML)** elements
- Maps to the `w:` namespace in OOXML
- Avoids confusion with generic HTML-like names
- Keeps code searchable and clear

### Why Varargs Instead of Builder Pattern?

- Mirrors the **hierarchical structure** of OOXML
- More **concise and readable** than chained calls
- **Natural functional composition** - nesting is obvious
- Fewer intermediate variables

### Why WFrags Instead of List?

- **Semantic clarity**: "These are Word fragments being composed"
- **Auto-unpacking** in `addTo()` - transparent composition
- **Extensible API**: Can add methods later without breaking changes
- **Type safety**: Can't accidentally pass wrong types

---

## Key Benefits

| Benefit | Impact |
|---------|--------|
| **Readability** | Code structure mirrors document structure |
| **Terseness** | 80%+ reduction in boilerplate vs. verbose docx4j |
| **Maintainability** | Changes to styling propagate through components |
| **Composability** | Build complex documents from simple functions |
| **Type Safety** | Compile-time validation of structure (mostly) |
| **Reusability** | Create component libraries for common patterns |

---

## Getting Started

### Basic Setup

```java
import luvdocx.*;
import static luvdocx.E.*;
import static luvdocx.A.*;

public class DocumentGenerator {
    public static void main(String[] args) throws Exception {
        var doc = WordprocessingMLPackage.createPackage();

        addTo(doc,
            w_p(w_r(w_rPr(w_b()), w_t("My Document"))),
            w_p(w_r(w_t("This is the first paragraph.")))
        );

        doc.save(new File("output.docx"));
    }
}
```

### Dependencies

```xml
<dependency>
    <groupId>io.github.xyz-jphil</groupId>
    <artifactId>xyz-jphil-luvdocx</artifactId>
    <version>1.0</version>
</dependency>

<dependency>
    <groupId>org.docx4j</groupId>
    <artifactId>docx4j-JAXB-ReferenceImpl</artifactId>
    <version>11.4.9</version>
</dependency>
```

---

## What LuvDOCX Is NOT

- ❌ NOT a markup abstraction layer (like luvml)
- ❌ NOT a rendering framework
- ❌ NOT implementing Frag_I pattern (it doesn't need to)
- ❌ NOT meant to be HTML-compatible

## What LuvDOCX IS

- ✅ A fluent DSL for docx4j
- ✅ A transparency layer making OOXML obvious
- ✅ A boilerplate reduction tool
- ✅ A component composition framework
- ✅ Practical and focused on DOCX generation

---

## Architecture

```
E.java (DSL Functions)
├── w_p(), w_r(), w_t(), etc.         [Core OOXML mapping]
├── w_pPr(), w_rPr(), etc.            [Property builders]
├── w_spacing(), w_shd(), etc.        [Specific properties]
└── wfrags(), addTo()                 [Composition]

A.java (Attributes)
└── Factory methods: before(), after(), fill(), etc.

WFrags.java (Fragment Container)
└── Simple list wrapper for composable building
```

---

## Compatibility

- **Java:** 21+
- **docx4j:** 11.4.9+
- **luvx-base:** 2.0 (for `Attr_I` interface only)

---

## Philosophy Summary

LuvDOCX proves that **you don't need a complex abstraction layer** to make code more readable. By transparently mapping OOXML structure to function calls and providing simple composition tools, document generation becomes intuitive and maintainable.

The code mirrors what you're building. Nothing more, nothing less.
