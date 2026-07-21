// Copies the production Angular build into the Spring Boot static resources so
// the whole app ships as a single JAR. Run via `npm run build:spring`.
import { rmSync, mkdirSync, cpSync, existsSync, readdirSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, resolve } from 'node:path';

const here = dirname(fileURLToPath(import.meta.url));
const src = resolve(here, '../dist/frontend/browser');
const dest = resolve(here, '../../backend/src/main/resources/static');

if (!existsSync(src)) {
  console.error(`\n✗ Build output not found at:\n  ${src}\n  Run "ng build" first.\n`);
  process.exit(1);
}

rmSync(dest, { recursive: true, force: true });
mkdirSync(dest, { recursive: true });
cpSync(src, dest, { recursive: true });

const files = readdirSync(dest);
console.log(`\n✓ Copied Angular build into Spring static resources:\n  ${dest}`);
console.log(`  ${files.length} entries (${files.filter((f) => f.endsWith('.js')).length} JS chunks).`);
console.log('  Now run "mvnw package" in ../backend to bundle it into the JAR.\n');
