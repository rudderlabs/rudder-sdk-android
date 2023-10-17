var PropertiesReader = require('properties-reader');
const fs = require('fs');
const package = process.argv[2];
const json = fs.readFileSync(`${package}/package.json`)
const packageJson = JSON.parse(json)
const version = packageJson.version;
const contents = fs.readFileSync(`${package}/gradle.properties`, 'utf8');
const properties = PropertiesReader();
properties.read(contents);
let oldVersion = properties.get("VERSION_NAME");
if (oldVersion === version) {
  return;
}
let versionCode = properties.get("VERSION_CODE");
properties.set("VERSION_NAME", version);
properties.set("VERSION_CODE", (versionCode + 1));
let output = "";
properties.each((key, value) => {
   output += `\n${key}=${value}`;
  });
fs.writeFileSync(`${package}/gradle.properties`, output);
