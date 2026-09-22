package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.model.MetricSource
import com.example.ui.components.MetricReadout
import com.example.ui.components.TechCard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TechCyan
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun tech_card_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        TechCard(
          title = "CPU Processor",
          sourceTag = MetricSource.MEASURED,
          accentColor = TechCyan,
          modifier = Modifier.padding(16.dp)
        ) {
          MetricReadout(
            label = "Total Load",
            value = "42%",
            accentColor = TechCyan,
            sublabel = "8 Cores active"
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/tech_card.png")
  }
}
