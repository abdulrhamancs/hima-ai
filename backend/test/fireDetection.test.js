const test = require("node:test");
const assert = require("node:assert/strict");
const {
  filterSaudiDetections,
  isInsideSaudiArabia,
  parseFirmsCsv,
} = require("../domain/fireDetection");

test("Saudi boundary includes Saudi cities and excludes neighboring detections", () => {
  assert.equal(isInsideSaudiArabia({ latitude: 24.7136, longitude: 46.6753 }), true);
  assert.equal(isInsideSaudiArabia({ latitude: 26.4207, longitude: 50.0888 }), true);
  assert.equal(isInsideSaudiArabia({ latitude: 32.24539, longitude: 35.34391 }), false);
  assert.equal(isInsideSaudiArabia({ latitude: 29.3759, longitude: 47.9774 }), false);
});

test("FIRMS CSV parsing normalizes values and handles quoted fields", () => {
  const csv = [
    "latitude,longitude,bright_ti4,confidence,acq_date,acq_time,satellite,note",
    '24.7136,46.6753,331.2,h,2026-08-10,0033,N20,"value, with comma"',
    "not-a-number,46.0,missing,l,2026-08-10,1400,N20,plain",
  ].join("\n");

  const detections = parseFirmsCsv(csv);
  assert.equal(detections.length, 2);
  assert.equal(detections[0].brightness, 331.2);
  assert.equal(detections[1].latitude, null);
  assert.equal(detections[1].brightness, null);
});

test("Saudi filtering removes rectangular-bbox false positives and invalid rows", () => {
  const detections = [
    { latitude: 24.7136, longitude: 46.6753 },
    { latitude: 32.24539, longitude: 35.34391 },
    { latitude: null, longitude: 46.0 },
  ];

  assert.deepEqual(filterSaudiDetections(detections), [detections[0]]);
});

test("unexpected FIRMS response schemas fail instead of becoming a false empty result", () => {
  assert.throws(
    () => parseFirmsCsv("NASA service temporarily unavailable\ntry again later"),
    /unexpected response schema/,
  );
});
