# Skill: Three.js & 3D Web Development

Guidelines for building high-performance 3D experiences using Three.js and React Three Fiber (R3F).

## TL;DR - Quick Reference

### Critical Rules
1. **Use React Three Fiber (R3F)**: The standard way to integrate Three.js with React/Next.js.
2. **Asset Optimization**: Always use compressed `.glb` files (Draco/Meshopt). Avoid raw `.obj` or `.fbx`.
3. **Performance (Instancing)**: Use `InstancedMesh` for rendering hundreds of identical objects (e.g., grass, stars).
4. **Lifecycle Management**: Never create geometries or materials inside the render loop (`useFrame`).
5. **Drei Library**: Use `@react-three/drei` for production-ready helpers (loaders, controls, shapes).

---

## 1. React Three Fiber Basics

### Proper Component Structure
// Bad: Creating heavy objects in the render loop
function Box() {
  useFrame(() => {
    const geometry = new THREE.BoxGeometry(); // Memory leak!
  });
  return <mesh geometry={geometry} />;
}

// Good: Geometries and materials defined outside or via hooks
function Box() {
  const meshRef = useRef();
  useFrame((state, delta) => {
    meshRef.current.rotation.x += delta;
  });
  return (
    <mesh ref={meshRef}>
      <boxGeometry args={[1, 1, 1]} />
      <meshStandardMaterial color="orange" />
    </mesh>
  );
}

---

## 2. Asset & Model Management

### Loading Models
Always use `useGLTF` from `@react-three/drei` for automatic caching and multi-instance support.
// Good: Loading a compressed GLB model
import { useGLTF } from '@react-three/drei'

function Model() {
  const { scene } = useGLTF('/models/hero.glb', true) // true usage Draco
  return <primitive object={scene} />
}

### Optimization Checklist
- **Draco Compression**: Reduces file size by ~70-90%.
- **Texture Compression**: Use `.webp` or Basis Universal for textures.
- **Polycount**: Keep models under 50k triangles for mobile performance.

---

## 3. High-Performance Patterns

### Instancing
Use for many copies of the same geometry to reduce draw calls.
// Good: Rendering 1000 boxes in 1 draw call
<InstancedMesh args={[null, null, 1000]}>
  <boxGeometry />
  <meshStandardMaterial />
</InstancedMesh>

### Geometry & Material Reuse
Define shared materials/geometries at the top level to save GPU memory.
// Good
const sharedMaterial = new THREE.MeshStandardMaterial({ color: 'red' });
function Particles() {
  return items.map(id => <mesh key={id} material={sharedMaterial} ... />);
}

---

## 4. Post-Processing & Effects
Use `@react-three/postprocessing` sparingly as it can be GPU-intensive.
// Good: Adding subtle effects
<EffectComposer>
  <Bloom luminanceThreshold={1} intensity={1.2} />
  <Vignette eskil={false} offset={0.1} darkness={1.1} />
</EffectComposer>

---

## Related Skills
- **Performance Optimization**: `skills/nextjs/performance.md`
- **Interactivity & Animation**: `skills/nextjs/interactivity.md`
