const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..");
const commonEntryPath = "common/src/main/java/alku/spd/Spd.java";
const forgeClientPath = "forge/src/main/java/alku/spd/forge/client/SpdForgeClient.java";
const fabricClientPath = "fabric/src/main/java/alku/spd/fabric/client/SpdFabricClient.java";

function read(relativePath) {
  return fs.readFileSync(path.join(root, relativePath), "utf8");
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

const commonEntry = read(commonEntryPath);
const forgeClient = read(forgeClientPath);
const fabricClient = read(fabricClientPath);
const registrations = [
  "AbyssalHeartForgeNetworking.register();",
  "MoltenChromeNozzleNetworking.register();",
  "SpdBigEyesNetworking.register();",
  "SpdWeatherNetworking.register();",
];

for (const registration of registrations) {
  assert(
    !commonEntry.includes(registration),
    `${commonEntryPath} must not register the client-only receiver ${registration}`,
  );
  assert(
    forgeClient.includes(registration),
    `${forgeClientPath} must register the client-only receiver ${registration}`,
  );
  assert(
    fabricClient.includes(registration),
    `${fabricClientPath} must register the client-only receiver ${registration}`,
  );
}

console.log("SPD S2C receivers are isolated to client entrypoints.");
