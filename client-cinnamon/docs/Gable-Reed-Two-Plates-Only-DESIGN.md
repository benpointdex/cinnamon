---
version: "alpha"
name: "Gable & Reed — Two Plates Only"
description: "Gable Reed Feature Section is designed for highlighting product capabilities and value points. Key features include reusable structure, responsive behavior, and production-ready presentation. It is suitable for component libraries and responsive product interfaces."
colors:
  primary: "#0D8D9C"
  secondary: "#E0447D"
  tertiary: "#D9D2BF"
  neutral: "#171512"
  background: "#171512"
  surface: "#0D8D9C"
  text-primary: "#6F6A5B"
  text-secondary: "#171512"
  border: "#171512"
  accent: "#0D8D9C"
typography:
  display-lg:
    fontFamily: "Inter"
    fontSize: "152px"
    fontWeight: 600
    lineHeight: "152px"
    letterSpacing: "-0.05em"
    textTransform: "uppercase"
  body-md:
    fontFamily: "Inter"
    fontSize: "16px"
    fontWeight: 400
    lineHeight: "24px"
  label-md:
    fontFamily: "Inter"
    fontSize: "12px"
    fontWeight: 600
    lineHeight: "16px"
    letterSpacing: "2.4px"
    textTransform: "uppercase"
rounded:
  md: "0px"
spacing:
  base: "8px"
  sm: "1px"
  md: "8px"
  lg: "14px"
  xl: "16px"
  gap: "6px"
  card-padding: "24px"
  section-padding: "24px"
components:
  button-primary:
    backgroundColor: "{colors.neutral}"
    textColor: "#F1ECE0"
    typography: "{typography.label-md}"
    rounded: "{rounded.md}"
    padding: "16px"
  button-link:
    textColor: "{colors.neutral}"
    typography: "{typography.body-md}"
    rounded: "{rounded.md}"
    padding: "1px"
---

## Overview

- **Composition cues:**
  - Layout: Grid
  - Content Width: Full Bleed
  - Framing: Framed
  - Grid: Strong

## Colors

The color system uses light mode with #0D8D9C as the main accent and #171512 as the neutral foundation.

- **Primary (#0D8D9C):** Main accent and emphasis color.
- **Secondary (#E0447D):** Supporting accent for secondary emphasis.
- **Tertiary (#D9D2BF):** Reserved accent for supporting contrast moments.
- **Neutral (#171512):** Neutral foundation for backgrounds, surfaces, and supporting chrome.

- **Usage:** Background: #171512; Surface: #0D8D9C; Text Primary: #6F6A5B; Text Secondary: #171512; Border: #171512; Accent: #0D8D9C

## Typography

Typography relies on Inter across display, body, and utility text.

- **Display (`display-lg`):** Inter, 152px, weight 600, line-height 152px, letter-spacing -0.05em, uppercase.
- **Body (`body-md`):** Inter, 16px, weight 400, line-height 24px.
- **Labels (`label-md`):** Inter, 12px, weight 600, line-height 16px, letter-spacing 2.4px, uppercase.

## Layout

Layout follows a grid composition with reusable spacing tokens. Preserve the grid, full bleed structural frame before changing ornament or component styling. Use 8px as the base rhythm and let larger gaps step up from that cadence instead of introducing unrelated spacing values.

Treat the page as a grid / full bleed composition, and keep that framing stable when adding or remixing sections.

- **Layout type:** Grid
- **Content width:** Full Bleed
- **Base unit:** 8px
- **Scale:** 1px, 8px, 14px, 16px, 24px, 28px, 32px
- **Section padding:** 24px
- **Card padding:** 24px
- **Gaps:** 6px, 12px, 14px, 20px

## Elevation & Depth

Depth is communicated through outlined, border contrast, and reusable shadow or blur treatments. Keep those recipes consistent across hero panels, cards, and controls so the page reads as one material system.

Surfaces should read as outlined first, with borders, shadows, and blur only reinforcing that material choice.

- **Surface style:** Outlined
- **Borders:** 0.8px #171512; 0.8px #D9D2BF

### Techniques
- **Gradient border shell:** Use a thin gradient border shell around the main card. Wrap the surface in an outer shell with 1px padding and a 0px radius. Drive the shell with linear-gradient(135deg, rgba(23, 21, 18, 0.9), rgba(13, 141, 156, 0.55) 48%, rgba(224, 68, 125, 0.7)) so the edge reads like premium depth instead of a flat stroke. Keep the actual stroke understated so the gradient shell remains the hero edge treatment. Inset the real content surface inside the wrapper with a slightly smaller radius so the gradient only appears as a hairline frame.

## Shapes

Shapes rely on a tight radius system anchored by 2px and scaled across cards, buttons, and supporting surfaces. Icon geometry should stay compatible with that soft-to-controlled silhouette.

Use the radius family intentionally: larger surfaces can open up, but controls and badges should stay within the same rounded DNA instead of inventing sharper or pill-only exceptions.

- **Corner radii:** 2px, 9999px
- **Icon treatment:** Linear
- **Icon sets:** Solar

## Components

Anchor interactions to the detected button styles.

### Buttons
- **Primary:** background #171512, text #F1ECE0, radius 0px, padding 16px, border 0px solid rgb(229, 231, 235).
- **Links:** text #171512, radius 0px, padding 1px, border 0px solid rgb(229, 231, 235).

### Iconography
- **Treatment:** Linear.
- **Sets:** Solar.

## Do's and Don'ts

Use these constraints to keep future generations aligned with the current system instead of drifting into adjacent styles.

### Do
- Do use the primary palette as the main accent for emphasis and action states.
- Do keep spacing aligned to the detected 8px rhythm.
- Do reuse the Outlined surface treatment consistently across cards and controls.
- Do keep corner radii within the detected 2px, 9999px family.

### Don't
- Don't introduce extra accent colors outside the core palette roles unless the page needs a new semantic state.
- Don't exceed the detected minimal motion intensity without a deliberate reason.

## Motion

Motion stays restrained and interface-led across text, layout, and scroll transitions. Timing clusters around 500ms. Easing favors ease and cubic-bezier(0.16. Scroll choreography uses GSAP ScrollTrigger for section reveals and pacing.

**Motion Level:** minimal

**Durations:** 500ms

**Easings:** ease, cubic-bezier(0.16, 1, 0.3, 1)

**Scroll Patterns:** gsap-scrolltrigger

## WebGL

Reconstruct the graphics as a full-bleed background field using webgl, renderer, dpr clamp, custom shaders. The effect should read as technical, meditative, and atmospheric: fine line lattice with soft amber and sparse spacing. Build it from line trails + sparse anchors so the effect reads clearly. Animate it as slow breathing pulse. Interaction can react to the pointer, but only as a subtle drift. Preserve reduced motion + dom fallback.

**Id:** webgl

**Label:** WebGL

**Stack:** ThreeJS, WebGL

**Insights:**
  - **Scene:**
    - **Value:** Full-bleed background field
  - **Effect:**
    - **Value:** Fine line lattice
  - **Primitives:**
    - **Value:** Line trails + sparse anchors
  - **Motion:**
    - **Value:** Slow breathing pulse
  - **Interaction:**
    - **Value:** Pointer-reactive drift
  - **Render:**
    - **Value:** WebGL, Renderer, DPR clamp, custom shaders

**Techniques:** Perspective grid, Line lattice, Breathing pulse, Pointer parallax, Shader gradients

**Code Evidence:**
  - **HTML reference:**
    - **Language:** html
    - **Snippet:**
      ```html
      <section id="hero" class="relative flex min-h-screen flex-col overflow-hidden p-4 sm:p-5 md:p-6" style="min-height:100svh">

        <canvas id="three" class="absolute inset-0 h-full w-full"></canvas>
        <div class="pointer-events-none absolute inset-0" style="background:radial-gradient(58% 50% at 46% 48%,rgba(241,236,224,.88) 0%,rgba(241,236,224,.44) 60%,transparent 92%)"></div>
      ```
  - **JS reference:**
    - **Language:** js
    - **Snippet:**
      ```
      import * as THREE from 'https://unpkg.com/three@0.169.0/build/three.module.js';

      const canvas = document.getElementById('three');
      const renderer = new THREE.WebGLRenderer({ canvas, antialias:false });
      renderer.setPixelRatio(Math.min(devicePixelRatio, 2));
      ```
  - **Renderer setup:**
    - **Language:** js
    - **Snippet:**
      ```
      import * as THREE from 'https://unpkg.com/three@0.169.0/build/three.module.js';

      const canvas = document.getElementById('three');
      const renderer = new THREE.WebGLRenderer({ canvas, antialias:false });
      renderer.setPixelRatio(Math.min(devicePixelRatio, 2));

      const camera = new THREE.OrthographicCamera(-1, 1, 1, -1, 0, 1);
      const scene = new THREE.Scene();
      ```
  - **Draw call:**
    - **Language:** js
    - **Snippet:**
      ```
      vertexShader:`varying vec2 vUv; void main(){ vUv=uv; gl_Position=vec4(position.xy,0.0,1.0); }`,
        fragmentShader:`
          precision highp float;
          varying vec2 vUv;
          uniform float uTime, uAspect;
          uniform vec2 uMouse, uRes;
      …
      ```

## ThreeJS

Reconstruct the Three.js layer as a full-bleed background field with layered spatial depth that feels technical. Use dpr clamp renderer settings, orthographic projection, plane geometry, shadermaterial materials, and ambient + key + rim lighting. Motion should read as timeline-led reveals, with reduced motion + non-3d fallback.

**Id:** threejs

**Label:** ThreeJS

**Stack:** ThreeJS, WebGL

**Insights:**
  - **Scene:**
    - **Value:** Full-bleed background field with layered spatial depth
  - **Render:**
    - **Value:** DPR clamp
  - **Camera:**
    - **Value:** Orthographic projection
  - **Lighting:**
    - **Value:** ambient + key + rim
  - **Materials:**
    - **Value:** ShaderMaterial
  - **Geometry:**
    - **Value:** plane
  - **Motion:**
    - **Value:** Timeline-led reveals

**Techniques:** Shader materials, Timeline beats, DPR clamp, Reduced motion + non-3D fallback

**Code Evidence:**
  - **HTML reference:**
    - **Language:** html
    - **Snippet:**
      ```html
      <section id="hero" class="relative flex min-h-screen flex-col overflow-hidden p-4 sm:p-5 md:p-6" style="min-height:100svh">

        <canvas id="three" class="absolute inset-0 h-full w-full"></canvas>
        <div class="pointer-events-none absolute inset-0" style="background:radial-gradient(58% 50% at 46% 48%,rgba(241,236,224,.88) 0%,rgba(241,236,224,.44) 60%,transparent 92%)"></div>
      ```
  - **JS reference:**
    - **Language:** js
    - **Snippet:**
      ```
      import * as THREE from 'https://unpkg.com/three@0.169.0/build/three.module.js';

      const canvas = document.getElementById('three');
      const renderer = new THREE.WebGLRenderer({ canvas, antialias:false });
      renderer.setPixelRatio(Math.min(devicePixelRatio, 2));
      ```
  - **Renderer setup:**
    - **Language:** js
    - **Snippet:**
      ```
      import * as THREE from 'https://unpkg.com/three@0.169.0/build/three.module.js';

      const canvas = document.getElementById('three');
      const renderer = new THREE.WebGLRenderer({ canvas, antialias:false });
      renderer.setPixelRatio(Math.min(devicePixelRatio, 2));

      const camera = new THREE.OrthographicCamera(-1, 1, 1, -1, 0, 1);
      const scene = new THREE.Scene();
      ```
  - **Draw call:**
    - **Language:** js
    - **Snippet:**
      ```
      vertexShader:`varying vec2 vUv; void main(){ vUv=uv; gl_Position=vec4(position.xy,0.0,1.0); }`,
        fragmentShader:`
          precision highp float;
          varying vec2 vUv;
          uniform float uTime, uAspect;
          uniform vec2 uMouse, uRes;
      …
      ```
