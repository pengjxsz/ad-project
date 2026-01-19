@file:OptIn(org.jetbrains.compose.resources.InternalResourceApi::class)

package sigad.composeapp.generated.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.StringResource

private object CommonMainString0 {
  public val back: StringResource by 
      lazy { init_back() }

  public val label_artist: StringResource by 
      lazy { init_label_artist() }

  public val label_credits: StringResource by 
      lazy { init_label_credits() }

  public val label_date: StringResource by 
      lazy { init_label_date() }

  public val label_department: StringResource by 
      lazy { init_label_department() }

  public val label_dimensions: StringResource by 
      lazy { init_label_dimensions() }

  public val label_medium: StringResource by 
      lazy { init_label_medium() }

  public val label_repository: StringResource by 
      lazy { init_label_repository() }

  public val label_title: StringResource by 
      lazy { init_label_title() }

  public val no_data_available: StringResource by 
      lazy { init_no_data_available() }
}

@InternalResourceApi
internal fun _collectCommonMainString0Resources(map: MutableMap<String, StringResource>) {
  map.put("back", CommonMainString0.back)
  map.put("label_artist", CommonMainString0.label_artist)
  map.put("label_credits", CommonMainString0.label_credits)
  map.put("label_date", CommonMainString0.label_date)
  map.put("label_department", CommonMainString0.label_department)
  map.put("label_dimensions", CommonMainString0.label_dimensions)
  map.put("label_medium", CommonMainString0.label_medium)
  map.put("label_repository", CommonMainString0.label_repository)
  map.put("label_title", CommonMainString0.label_title)
  map.put("no_data_available", CommonMainString0.no_data_available)
}

internal val Res.string.back: StringResource
  get() = CommonMainString0.back

private fun init_back(): StringResource = org.jetbrains.compose.resources.StringResource(
  "string:back", "back",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(), "composeResources/sigad.composeapp.generated.resources/values/strings.commonMain.cvr", 10, 20),
    )
)

internal val Res.string.label_artist: StringResource
  get() = CommonMainString0.label_artist

private fun init_label_artist(): StringResource = org.jetbrains.compose.resources.StringResource(
  "string:label_artist", "label_artist",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(), "composeResources/sigad.composeapp.generated.resources/values/strings.commonMain.cvr", 31, 28),
    )
)

internal val Res.string.label_credits: StringResource
  get() = CommonMainString0.label_credits

private fun init_label_credits(): StringResource = org.jetbrains.compose.resources.StringResource(
  "string:label_credits", "label_credits",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(), "composeResources/sigad.composeapp.generated.resources/values/strings.commonMain.cvr", 60, 33),
    )
)

internal val Res.string.label_date: StringResource
  get() = CommonMainString0.label_date

private fun init_label_date(): StringResource = org.jetbrains.compose.resources.StringResource(
  "string:label_date", "label_date",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(), "composeResources/sigad.composeapp.generated.resources/values/strings.commonMain.cvr", 94, 26),
    )
)

internal val Res.string.label_department: StringResource
  get() = CommonMainString0.label_department

private fun init_label_department(): StringResource = org.jetbrains.compose.resources.StringResource(
  "string:label_department", "label_department",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(), "composeResources/sigad.composeapp.generated.resources/values/strings.commonMain.cvr", 121, 40),
    )
)

internal val Res.string.label_dimensions: StringResource
  get() = CommonMainString0.label_dimensions

private fun init_label_dimensions(): StringResource = org.jetbrains.compose.resources.StringResource(
  "string:label_dimensions", "label_dimensions",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(), "composeResources/sigad.composeapp.generated.resources/values/strings.commonMain.cvr", 162, 40),
    )
)

internal val Res.string.label_medium: StringResource
  get() = CommonMainString0.label_medium

private fun init_label_medium(): StringResource = org.jetbrains.compose.resources.StringResource(
  "string:label_medium", "label_medium",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(), "composeResources/sigad.composeapp.generated.resources/values/strings.commonMain.cvr", 203, 28),
    )
)

internal val Res.string.label_repository: StringResource
  get() = CommonMainString0.label_repository

private fun init_label_repository(): StringResource = org.jetbrains.compose.resources.StringResource(
  "string:label_repository", "label_repository",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(), "composeResources/sigad.composeapp.generated.resources/values/strings.commonMain.cvr", 232, 40),
    )
)

internal val Res.string.label_title: StringResource
  get() = CommonMainString0.label_title

private fun init_label_title(): StringResource = org.jetbrains.compose.resources.StringResource(
  "string:label_title", "label_title",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(), "composeResources/sigad.composeapp.generated.resources/values/strings.commonMain.cvr", 273, 27),
    )
)

internal val Res.string.no_data_available: StringResource
  get() = CommonMainString0.no_data_available

private fun init_no_data_available(): StringResource = org.jetbrains.compose.resources.StringResource(
  "string:no_data_available", "no_data_available",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(), "composeResources/sigad.composeapp.generated.resources/values/strings.commonMain.cvr", 301, 49),
    )
)
