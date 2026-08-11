
const express = require("express");
const router = express.Router();
const authMiddleware = require("../middleware/authMiddleware");
const { filterSaudiDetections, parseFirmsCsv } = require("../domain/fireDetection");


const SAUDI_ARABIA_BBOX = "34.5,16.0,55.7,32.5";
const SENSOR = "VIIRS_NOAA20_NRT";
const CACHE_TTL_MS = 30 * 60 * 1000; 

const cache = new Map(); 


router.get("/", authMiddleware, async (req, res) => {
  try {
    if (!process.env.NASA_FIRMS_MAP_KEY) {
      
      
     
      console.error("NASA_FIRMS_MAP_KEY is missing in .env file");
      return res.status(500).json({ error: "Satellite fire detection is not configured on the server" });
    }

  
    const bbox = SAUDI_ARABIA_BBOX;


    let days = parseInt(req.query.days, 10);
    if (!Number.isFinite(days) || days < 1) days = 1;
    days = Math.min(days, 2);

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

    
    if (/invalid/i.test(csvText.slice(0, 200))) {
      console.error("NASA FIRMS rejected the request:", csvText.slice(0, 200));
      return res.status(502).json({ error: "NASA FIRMS rejected the request" });
    }

    
    const detections = filterSaudiDetections(parseFirmsCsv(csvText));
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
