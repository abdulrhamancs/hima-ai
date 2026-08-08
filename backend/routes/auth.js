
const express = require("express");
const router = express.Router();
const supabase = require("../config/supabase");

router.post("/signup", async (req, res) => {
  try {
    const { email, password, full_name, role } = req.body;

    if (!email || !password || !full_name || !role) {
      return res.status(400).json({ error: "email, password, full_name, and role are required" });
    }

    if (!["field_agent", "authority"].includes(role)) {
      return res.status(400).json({ error: "role must be either 'field_agent' or 'authority'" });
    }

    const passwordRegex = /^(?=.*[A-Z])(?=.*[0-9]).{8,}$/;
    if (!passwordRegex.test(password)) {
      return res.status(400).json({
        error: "Password does not meet requirements",
        requirements: [
          "At least 8 characters long",
          "At least one uppercase English letter (A-Z)",
          "At least one number (0-9)",
        ],
      });
    }

    const { data: authData, error: authError } = await supabase.auth.signUp({
   
      email,
      password,
    });

    if (authError) {
      return res.status(400).json({ error: authError.message });
    }

    const { error: profileError } = await supabase.from("profiles").insert({
      id: authData.user.id,
      full_name,
      role,
    });

    if (profileError) {
      return res.status(400).json({ error: profileError.message });
    }

    res.json({
      status: "success",
      message: "User registered successfully",
      user: {
        id: authData.user.id,
        email: authData.user.email,
        full_name,
        role,
      },
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Something went wrong during signup" });
  }
});
router.post("/login", async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ error: "email and password are required" });
    }

    const { data: authData, error: authError } = await supabase.auth.signInWithPassword({
      email,
      password,
    });

    if (authError) {
      return res.status(401).json({ error: "Invalid email or password" });
    }

    const { data: profileData, error: profileError } = await supabase
      .from("profiles")
      .select("full_name, role")
      .eq("id", authData.user.id)
      .single();

    if (profileError) {
      return res.status(400).json({ error: profileError.message });
    }

    res.json({
      status: "success",
      message: "Login successful",
      session: {
        access_token: authData.session.access_token,
        refresh_token: authData.session.refresh_token,
      },
      user: {
        id: authData.user.id,
        email: authData.user.email,
        full_name: profileData.full_name,
        role: profileData.role,
      },
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Something went wrong during login" });
  }
});
router.post("/logout", async (req, res) => {
  try {
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Access token is required" });
    }

    const token = authHeader.split(" ")[1];

    const { error } = await supabase.auth.admin.signOut(token);

    if (error) {
      return res.status(400).json({ error: error.message });
    }

    res.json({
      status: "success",
      message: "Logged out successfully",
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Something went wrong during logout" });
  }
});
module.exports = router;