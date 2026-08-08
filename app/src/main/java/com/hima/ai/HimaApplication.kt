package com.hima.ai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application entry point. [HiltAndroidApp] bootstraps dependency injection. */
@HiltAndroidApp
class HimaApplication : Application()
