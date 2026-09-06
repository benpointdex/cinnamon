"use client";

import React, { useEffect, useRef } from "react";
import * as THREE from "three";

export default function ShaderHeroBackground() {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const prefersReducedMotion = window.matchMedia(
      "(prefers-reduced-motion: reduce)"
    ).matches;

    let animationFrameId: number;

    const renderer = new THREE.WebGLRenderer({
      canvas,
      antialias: false,
      alpha: true,
      powerPreference: "high-performance",
    });
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

    const camera = new THREE.OrthographicCamera(-1, 1, 1, -1, 0, 1);
    const scene = new THREE.Scene();

    const mouse = new THREE.Vector2(0.5, 0.5);
    const targetMouse = new THREE.Vector2(0.5, 0.5);

    const vertexShader = `
      varying vec2 vUv;
      void main() {
        vUv = uv;
        gl_Position = vec4(position.xy, 0.0, 1.0);
      }
    `;

    const fragmentShader = `
      precision highp float;
      varying vec2 vUv;
      uniform float uTime;
      uniform vec2 uResolution;
      uniform vec2 uMouse;

      const vec3 cPaper = vec3(0.945, 0.925, 0.878);
      const vec3 cPink  = vec3(0.878, 0.267, 0.490);
      const vec3 cAqua  = vec3(0.051, 0.553, 0.612);

      mat2 rotate2d(float angle) {
        return mat2(cos(angle), -sin(angle),
                    sin(angle),  cos(angle));
      }

      float hash21(vec2 p) {
        p = fract(p * vec2(234.34, 435.345));
        p += dot(p, p + 34.23);
        return fract(p.x * p.y);
      }

      void main() {
        vec2 aspectUv = vUv;
        aspectUv.x *= uResolution.x / uResolution.y;

        vec2 mouseOffset = (uMouse - 0.5) * 0.06;
        float t = uTime * 0.2;

        vec2 p = aspectUv + mouseOffset;
        float diag = p.x * 0.7 - p.y * 0.4;

        // Broader, more prominent wave field
        float wave = sin(diag * 3.5 - t * 0.6) * 0.5 + 0.5;
        wave += cos((p.x + p.y * 0.7) * 5.0 + t * 0.4) * 0.3;
        wave += sin(p.y * 3.0 - t * 0.25) * 0.18;
        wave = clamp(wave, 0.0, 1.0);

        // Wider coverage — pink starts from ~20% of screen width
        float textSafeMask = smoothstep(0.15, 0.55, vUv.x);
        float fieldIntensity = wave * textSafeMask;

        // Stronger right-side density — fills right half more aggressively
        float cornerDensity = smoothstep(0.1, 0.8, vUv.x * (1.2 - vUv.y * 0.35));
        fieldIntensity *= cornerDensity;

        // Boost intensity on far right
        float rightBoost = smoothstep(0.5, 1.0, vUv.x) * 0.35;
        fieldIntensity = min(fieldIntensity + rightBoost, 1.0);

        // Halftone dot grid (Riso Screen Angle)
        float gridFreq = 140.0;
        vec2 rotCoord = rotate2d(0.3926) * (aspectUv * gridFreq);
        vec2 gridCell = fract(rotCoord) - 0.5;
        float distToCenter = length(gridCell);

        float dotThreshold = fieldIntensity * 0.55;
        float dotAlpha = smoothstep(
          dotThreshold + 0.05, dotThreshold - 0.05, distToCenter
        );

        // Aqua plate with misregistration
        vec2 aquaOffset = vec2(0.018, -0.014);
        vec2 rotCoordAqua = rotate2d(0.3926)
          * ((aspectUv + aquaOffset) * (gridFreq * 0.94));
        vec2 gridCellAqua = fract(rotCoordAqua) - 0.5;
        float dotThresholdAqua = fieldIntensity * 0.32 * (1.0 - wave * 0.5);
        float dotAlphaAqua = smoothstep(
          dotThresholdAqua + 0.05,
          dotThresholdAqua - 0.05,
          length(gridCellAqua)
        );

        vec3 finalColor = cPaper;

        // Layer Aqua plate first
        finalColor = mix(finalColor, cAqua, dotAlphaAqua * 0.5 * textSafeMask);

        // Overprint dominant Fluorescent Pink plate
        finalColor = mix(finalColor, cPink, dotAlpha * 0.92 * textSafeMask);

        // Paper grain
        float grain = (hash21(gl_FragCoord.xy) - 0.5) * 0.03;
        finalColor += grain;

        gl_FragColor = vec4(finalColor, 1.0);
      }
    `;

    const material = new THREE.ShaderMaterial({
      vertexShader,
      fragmentShader,
      uniforms: {
        uTime: { value: 0.0 },
        uResolution: {
          value: new THREE.Vector2(window.innerWidth, window.innerHeight),
        },
        uMouse: { value: new THREE.Vector2(0.5, 0.5) },
      },
      depthWrite: false,
      depthTest: false,
    });

    const geometry = new THREE.PlaneGeometry(2, 2);
    const mesh = new THREE.Mesh(geometry, material);
    scene.add(mesh);

    const handleResize = () => {
      const width = canvas.clientWidth || window.innerWidth;
      const height = canvas.clientHeight || window.innerHeight;
      renderer.setSize(width, height, false);
      material.uniforms.uResolution.value.set(width, height);
    };

    const handlePointerMove = (e: PointerEvent) => {
      const rect = canvas.getBoundingClientRect();
      targetMouse.x = (e.clientX - rect.left) / rect.width;
      targetMouse.y = 1.0 - (e.clientY - rect.top) / rect.height;
    };

    window.addEventListener("resize", handleResize);
    window.addEventListener("pointermove", handlePointerMove);
    handleResize();

    const clock = new THREE.Clock();

    const animate = () => {
      animationFrameId = requestAnimationFrame(animate);
      mouse.lerp(targetMouse, 0.05);
      material.uniforms.uTime.value = prefersReducedMotion
        ? 0
        : clock.getElapsedTime();
      material.uniforms.uMouse.value.copy(mouse);
      renderer.render(scene, camera);
    };

    animate();

    return () => {
      cancelAnimationFrame(animationFrameId);
      window.removeEventListener("resize", handleResize);
      window.removeEventListener("pointermove", handlePointerMove);
      geometry.dispose();
      material.dispose();
      renderer.dispose();
    };
  }, []);

  return (
    <>
      <canvas
        ref={canvasRef}
        className="absolute inset-0 h-full w-full pointer-events-none z-0"
        style={{ minHeight: "100%" }}
      />
      {/* Softer radial overlay — less fade so pink stays visible on the right */}
      <div
        className="pointer-events-none absolute inset-0 z-[1]"
        style={{
          background:
            "radial-gradient(50% 45% at 30% 50%, rgba(241,236,224,0.85) 0%, rgba(241,236,224,0.3) 65%, transparent 100%)",
        }}
      />
    </>
  );
}