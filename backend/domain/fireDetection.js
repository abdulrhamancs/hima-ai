const fs = require("node:fs");
const path = require("node:path");

const boundaryPath = path.join(__dirname, "..", "data", "saudi-arabia-boundary.geojson");
const saudiBoundary = JSON.parse(fs.readFileSync(boundaryPath, "utf8"));

const saudiPolygons = saudiBoundary.features.flatMap((feature) => {
  if (feature.geometry?.type === "Polygon") return [feature.geometry.coordinates];
  if (feature.geometry?.type === "MultiPolygon") return feature.geometry.coordinates;
  return [];
});

function parseCsvLine(line) {
  const values = [];
  let current = "";
  let quoted = false;

  for (let index = 0; index < line.length; index += 1) {
    const character = line[index];
    if (character === '"') {
      if (quoted && line[index + 1] === '"') {
        current += '"';
        index += 1;
      } else {
        quoted = !quoted;
      }
    } else if (character === "," && !quoted) {
      values.push(current);
      current = "";
    } else {
      current += character;
    }
  }

  values.push(current);
  return values;
}

function finiteNumber(value) {
  const number = Number.parseFloat(value);
  return Number.isFinite(number) ? number : null;
}

function parseFirmsCsv(csvText) {
  if (typeof csvText !== "string" || !csvText.trim()) return [];

  const lines = csvText.trim().split(/\r?\n/).filter(Boolean);
  if (lines.length <= 1) return [];

  const headers = parseCsvLine(lines[0]).map((header) => header.trim());
  if (!headers.includes("latitude") || !headers.includes("longitude")) {
    throw new Error("NASA FIRMS returned an unexpected response schema");
  }

  return lines.slice(1).map((line) => {
    const values = parseCsvLine(line);
    const row = {};
    headers.forEach((header, index) => {
      row[header] = values[index] !== undefined ? values[index].trim() : null;
    });

    return {
      latitude: finiteNumber(row.latitude),
      longitude: finiteNumber(row.longitude),
      // VIIRS reports bright_ti4, MODIS reports brightness — normalize to one field.
      brightness: finiteNumber(row.bright_ti4 ?? row.brightness),
      confidence: row.confidence || null,
      acq_date: row.acq_date || null,
      acq_time: row.acq_time || null,
      satellite: row.satellite || null,
    };
  });
}

function pointOnSegment(longitude, latitude, start, end) {
  const [startLongitude, startLatitude] = start;
  const [endLongitude, endLatitude] = end;
  const cross = (latitude - startLatitude) * (endLongitude - startLongitude) -
    (longitude - startLongitude) * (endLatitude - startLatitude);
  if (Math.abs(cross) > 1e-10) return false;

  return longitude >= Math.min(startLongitude, endLongitude) &&
    longitude <= Math.max(startLongitude, endLongitude) &&
    latitude >= Math.min(startLatitude, endLatitude) &&
    latitude <= Math.max(startLatitude, endLatitude);
}

function pointInRing(longitude, latitude, ring) {
  let inside = false;
  for (let index = 0, previous = ring.length - 1; index < ring.length; previous = index, index += 1) {
    const currentPoint = ring[index];
    const previousPoint = ring[previous];
    if (pointOnSegment(longitude, latitude, previousPoint, currentPoint)) return true;

    const [currentLongitude, currentLatitude] = currentPoint;
    const [previousLongitude, previousLatitude] = previousPoint;
    const crossesLatitude = (currentLatitude > latitude) !== (previousLatitude > latitude);
    const crossingLongitude = (previousLongitude - currentLongitude) *
      (latitude - currentLatitude) / (previousLatitude - currentLatitude) + currentLongitude;
    if (crossesLatitude && longitude < crossingLongitude) inside = !inside;
  }
  return inside;
}

function pointInPolygon(longitude, latitude, rings) {
  if (!rings.length || !pointInRing(longitude, latitude, rings[0])) return false;
  return rings.slice(1).every((hole) => !pointInRing(longitude, latitude, hole));
}

function isInsideSaudiArabia(detection) {
  const { latitude, longitude } = detection;
  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) return false;
  return saudiPolygons.some((polygon) => pointInPolygon(longitude, latitude, polygon));
}

function filterSaudiDetections(detections) {
  return detections.filter(isInsideSaudiArabia);
}

module.exports = {
  filterSaudiDetections,
  isInsideSaudiArabia,
  parseFirmsCsv,
};
