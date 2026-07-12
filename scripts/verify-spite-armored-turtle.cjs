const fs = require('fs');
const path = require('path');

const REPO_ROOT = path.resolve(__dirname, '..');

const RESOURCE_FILES = {
  geo: 'common/src/main/resources/assets/spd/geo/spite_armored_turtle.geo.json',
  animation: 'common/src/main/resources/assets/spd/animations/spite_armored_turtle.animation.json',
  texture: 'common/src/main/resources/assets/spd/textures/entity/spite_armored_turtle.png',
};

const RUNTIME_RESOURCE_ENTRIES = [
  'assets/spd/geo/spite_armored_turtle.geo.json',
  'assets/spd/animations/spite_armored_turtle.animation.json',
  'assets/spd/textures/entity/spite_armored_turtle.png',
];

const SOURCE_MARKERS = [
  {
    relativePath: 'common/src/main/java/alku/spd/registry/SpdEntities.java',
    markers: ['SPITE_ARMORED_TURTLE'],
  },
  {
    relativePath: 'common/src/main/java/alku/spd/registry/SpdItems.java',
    markers: ['HEAVY_SPITE_SCUTE', 'RESIDUAL_MALICE', 'SPITE_ARMORED_TURTLE_SPAWN_EGG'],
  },
  {
    relativePath: 'common/src/main/java/alku/spd/registry/SpdBlocks.java',
    markers: ['SPITE_NODULE', 'RUST_SAND'],
  },
  {
    relativePath: 'common/src/main/java/alku/spd/entity/SpiteArmoredTurtleEntity.java',
    markers: ['class SpiteArmoredTurtleEntity'],
  },
  {
    relativePath: 'common/src/main/java/alku/spd/block/SpiteNoduleBlock.java',
    markers: ['class SpiteNoduleBlock'],
  },
  {
    relativePath: 'common/src/main/java/alku/spd/block/entity/SpiteNoduleBlockEntity.java',
    markers: ['class SpiteNoduleBlockEntity'],
  },
  {
    relativePath: 'common/src/main/java/alku/spd/client/model/SpiteArmoredTurtleModel.java',
    markers: ['class SpiteArmoredTurtleModel'],
  },
  {
    relativePath: 'common/src/main/java/alku/spd/client/renderer/SpiteArmoredTurtleRenderer.java',
    markers: ['class SpiteArmoredTurtleRenderer'],
  },
];

function parseArgs(argv) {
  let resourcesOnly = false;
  let jarPath = null;

  for (let index = 0; index < argv.length; index += 1) {
    const argument = argv[index];

    if (argument === '--resources-only') {
      resourcesOnly = true;
      continue;
    }

    if (argument === '--jar') {
      const value = argv[index + 1];
      if (!value || value.startsWith('--')) {
        throw new Error('--jar requires a ZIP/JAR path');
      }
      jarPath = path.resolve(process.cwd(), value);
      index += 1;
      continue;
    }

    throw new Error(`Unknown argument: ${argument}`);
  }

  return { resourcesOnly, jarPath };
}

function absoluteRepoPath(relativePath) {
  return path.resolve(REPO_ROOT, ...relativePath.split('/'));
}

function readRequiredFile(relativePath, failures) {
  const filePath = absoluteRepoPath(relativePath);
  try {
    return fs.readFileSync(filePath);
  } catch (error) {
    const detail = error && error.code ? `${error.code}: ${error.message}` : error.message;
    failures.push(`Cannot read ${relativePath}: ${detail}`);
    return null;
  }
}

function parseJson(relativePath, contents, failures) {
  try {
    return JSON.parse(contents.toString('utf8'));
  } catch (error) {
    failures.push(`Invalid JSON in ${relativePath}: ${error.message}`);
    return undefined;
  }
}

function findEndOfCentralDirectory(buffer) {
  if (buffer.length < 22) {
    throw new Error('file is too small to contain a ZIP end-of-central-directory record');
  }

  const minimumOffset = Math.max(0, buffer.length - 0xffff - 22);
  for (let offset = buffer.length - 22; offset >= minimumOffset; offset -= 1) {
    if (buffer.readUInt32LE(offset) === 0x06054b50) {
      return offset;
    }
  }

  throw new Error('ZIP end-of-central-directory record was not found');
}

function readZipEntries(filePath) {
  const buffer = fs.readFileSync(filePath);
  const endOffset = findEndOfCentralDirectory(buffer);
  const entries = buffer.readUInt16LE(endOffset + 10);
  const centralDirectorySize = buffer.readUInt32LE(endOffset + 12);
  const centralDirectoryOffset = buffer.readUInt32LE(endOffset + 16);

  if (
    entries === 0xffff ||
    centralDirectorySize === 0xffffffff ||
    centralDirectoryOffset === 0xffffffff
  ) {
    throw new Error('ZIP64 archives are not supported by this verifier');
  }

  const centralDirectoryEnd = centralDirectoryOffset + centralDirectorySize;
  if (centralDirectoryEnd > buffer.length) {
    throw new Error('ZIP central directory extends beyond the end of the file');
  }

  const names = new Set();
  let offset = centralDirectoryOffset;
  for (let index = 0; index < entries; index += 1) {
    if (offset + 46 > centralDirectoryEnd || buffer.readUInt32LE(offset) !== 0x02014b50) {
      throw new Error(`invalid ZIP central-directory entry at byte offset ${offset}`);
    }

    const nameLength = buffer.readUInt16LE(offset + 28);
    const extraLength = buffer.readUInt16LE(offset + 30);
    const commentLength = buffer.readUInt16LE(offset + 32);
    const entryEnd = offset + 46 + nameLength + extraLength + commentLength;
    if (entryEnd > centralDirectoryEnd) {
      throw new Error(`ZIP entry at byte offset ${offset} exceeds the central directory`);
    }

    const name = buffer
      .subarray(offset + 46, offset + 46 + nameLength)
      .toString('utf8')
      .replace(/\\/g, '/');
    names.add(name);
    offset = entryEnd;
  }

  return names;
}

function checkSourceMarkers(failures) {
  for (const { relativePath, markers } of SOURCE_MARKERS) {
    const contents = readRequiredFile(relativePath, failures);
    if (!contents) {
      continue;
    }

    const source = contents.toString('utf8');
    for (const marker of markers) {
      if (!source.includes(marker)) {
        failures.push(`Missing source marker ${marker} in ${relativePath}`);
      }
    }
  }
}

function checkJar(jarPath, failures) {
  let entries;
  try {
    entries = readZipEntries(jarPath);
  } catch (error) {
    const detail = error && error.code ? `${error.code}: ${error.message}` : error.message;
    failures.push(`Cannot inspect JAR ${jarPath}: ${detail}`);
    return;
  }

  for (const entry of RUNTIME_RESOURCE_ENTRIES) {
    if (!entries.has(entry)) {
      failures.push(`Missing JAR runtime resource ${entry} in ${jarPath}`);
    }
  }
}

function main() {
  const { resourcesOnly, jarPath } = parseArgs(process.argv.slice(2));
  const failures = [];

  const geoContents = readRequiredFile(RESOURCE_FILES.geo, failures);
  const animationContents = readRequiredFile(RESOURCE_FILES.animation, failures);
  readRequiredFile(RESOURCE_FILES.texture, failures);

  if (geoContents) {
    const geo = parseJson(RESOURCE_FILES.geo, geoContents, failures);
    if (geo !== undefined && (!geo || !Array.isArray(geo['minecraft:geometry']))) {
      failures.push(`Expected ${RESOURCE_FILES.geo} to contain a minecraft:geometry array`);
    }
  }

  if (animationContents) {
    const animation = parseJson(RESOURCE_FILES.animation, animationContents, failures);
    const animationMap = animation && animation.animations;
    if (!animationMap || typeof animationMap !== 'object' || Array.isArray(animationMap)) {
      failures.push(`Expected ${RESOURCE_FILES.animation} to contain an animations object`);
    } else {
      const terminalNames = new Set(
        Object.keys(animationMap).map((key) => {
          const parts = key.split('.');
          return parts[parts.length - 1];
        }),
      );
      for (const requiredClip of ['idle', 'walk', 'attack']) {
        if (!terminalNames.has(requiredClip)) {
          failures.push(
            `Missing animation clip with terminal name ${requiredClip} in ${RESOURCE_FILES.animation}`,
          );
        }
      }
    }
  }

  if (!resourcesOnly) {
    checkSourceMarkers(failures);
  }

  if (jarPath) {
    checkJar(jarPath, failures);
  }

  if (failures.length > 0) {
    console.error(`FAIL: ${failures.length} verification error(s)`);
    for (const failure of failures) {
      console.error(`- ${failure}`);
    }
    process.exitCode = 1;
    return;
  }

  const mode = resourcesOnly ? 'resources-only' : 'resources and source markers';
  const jarSummary = jarPath ? `; JAR entries checked in ${jarPath}` : '';
  console.log(`PASS: verified ${mode}${jarSummary}`);
}

try {
  main();
} catch (error) {
  console.error(`FAIL: ${error.message}`);
  process.exitCode = 1;
}
