const express = require("express");
const router = express.Router();
const supabase = require("../config/supabase");
const authMiddleware = require("../middleware/authMiddleware");

router.get("/", authMiddleware, async (req, res) => {
  try {
    const userId = req.user.id;

    const { data: reports, error } = await supabase
      .from("reports")
      .select("*")
      .eq("user_id", userId)
      .order("created_at", { ascending: false });

    if (error) {
      return res.status(400).json({ error: error.message });
    }

    res.json({
      status: "success",
      count: reports.length,
      reports,
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Failed to fetch reports" });
  }
});

router.post("/answer", authMiddleware, async (req, res) => {
  try {
    const { report_id, answer } = req.body;

    if (!report_id || !answer) {
      return res.status(400).json({ error: "report_id and answer are required" });
    }

    const { data, error } = await supabase
      .from("reports")
      .update({
        investigation_answer: answer,
        status: "completed"
      })
      .eq("id", report_id)
      .select()
      .single();

    if (error) {
      return res.status(400).json({ error: error.message });
    }

    res.json({
      status: "success",
      message: "Investigation answer recorded successfully",
      report: data,
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Failed to submit investigation answer" });
  }
});

module.exports = router;