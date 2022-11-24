var PropertiesReader = require('properties-reader');
module.exports.readVersion = function (contents) {
    console.log(contents);
    var properties = PropertiesReader();
    properties.read(contents);
    console.log(properties);

    return properties.get("VERSION_NAME");
  }
  
  module.exports.writeVersion = function (contents, version) {
    const properties = PropertiesReader();
    properties.read(contents);
    let versionCode = properties.get("VERSION_CODE");
    properties.set("VERSION_NAME", version);
    properties.set("VERSION_CODE", (versionCode + 1));
    let output = "";
    properties.each((key, value) => {
       output += `\n${key}=${value}`;
      });
    return output;
  }