
const express = require("express");
const router = express.Router();
const authMiddleware = require("../middleware/authMiddleware");

// west,south,east,north — covers Saudi Arabia's territory
const SAUDI_ARABIA_BBOX = "34.5,16.0,55.7,32.5";
const SENSOR = "VIIRS_NOAA20_NRT";
const CACHE_TTL_MS = 30 * 60 * 1000; // 30 minutes, well under FIRMS' rate limit

const cache = new Map(); // key -> { data, cachedAt }

function parseFirmsCsv(csvText) {
  const lines = csvText.trim().split("\n");
  if (lines.length <= 1) return [];

  const headers = lines[0].split(",").map((h) => h.trim());
  return lines.slice(1).map((line) => {
    const values = line.split(",");
    const row = {};
    headers.forEach((header, i) => {
      row[header] = values[i] !== undefined ? values[i].trim() : null;
    });

    return {
      latitude: parseFloat(row.latitude),
      longitude: parseFloat(row.longitude),
      // VIIRS reports bright_ti4, MODIS reports brightness — normalize to one field
      brightness: parseFloat(row.bright_ti4 ?? row.brightness),
      confidence: row.confidence,
      acq_date: row.acq_date,
      acq_time: row.acq_time,
      satellite: row.satellite,
    };
  });
}

// GET /fires — satellite fire detections (NOT confirmed fires) over Saudi Arabia, cached
router.get("/", authMiddleware, async (req, res) => {
  try {
    if (!process.env.NASA_FIRMS_MAP_KEY) {
      return res.status(500).json({ error: "NASA_FIRMS_MAP_KEY is missing in .env file" });
    }

    const bbox = req.query.bbox || SAUDI_ARABIA_BBOX;

    let days = parseInt(req.query.days, 10);
    if (!Number.isFinite(days) || days < 1) days = 1;
    days = Math.min(days, 5); // FIRMS Area API supports a 1-5 day range

    const cacheKey = `${bbox}|${SENSOR}|${days}`;
    const cached = cache.get(cacheKey);

    if (cached && Date.now() - cached.cachedAt < CACHE_TTL_MS) {
      return res.json({
        status: "success",
        source: "NASA FIRMS - Satellite Fire Detection (not a confirmed fire)",
        cached: true,
        cached_at: new Date(cached.cachedAt).toISOString(),
        count: cached.data.length,
        detections: cached.data,
      });
    }

    const url = `https://firms.modaps.eosdis.nasa.gov/api/area/csv/${process.env.NASA_FIRMS_MAP_KEY}/${SENSOR}/${bbox}/${days}`;
    const response = await fetch(url);

    if (!response.ok) {
      return res.status(502).json({ error: "NASA FIRMS request failed" });
    }

    const csvText = await response.text();

    // FIRMS returns a plaintext error message (not CSV) on invalid MAP_KEY/params
    if (/invalid/i.test(csvText.slice(0, 200))) {
      return res.status(502).json({ error: "NASA FIRMS rejected the request", detail: csvText.slice(0, 200) });
    }

    const detections = parseFirmsCsv(csvText);
    cache.set(cacheKey, { data: detections, cachedAt: Date.now() });

    res.json({
      status: "success",
      source: "NASA FIRMS - Satellite Fire Detection (not a confirmed fire)",
      cached: false,
      cached_at: new Date().toISOString(),
      count: detections.length,
      detections,
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Something went wrong while fetching satellite fire detections" });
  }
});

module.exports = router;
