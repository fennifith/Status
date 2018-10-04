## [setBackgroundColor](../app/src/main/java/com/james/status/data/icon/IconData.java#L294)

**Type:** `public` `void`

Determines the color of the icon based on various settings, 
some of which are icon-specific. 



|Parameter Name|Description|
|-----|-----|
|color|the color to be drawn behind the icon  |

## [draw](../app/src/main/java/com/james/status/data/icon/IconData.java#L389)

**Type:** `public` `void`

Draws the icon on a canvas. 



|Parameter Name|Description|
|-----|-----|
|canvas|the canvas to draw on|
|x|the x position (LTR px) to start drawing the icon at|
|width|the available width for the icon to be drawn within  |

## [getWidth](../app/src/main/java/com/james/status/data/icon/IconData.java#L431)

**Type:** `public` `int`

Returns the estimated width (px) of the icon, or -1 
if the icon needs to know the available space 
first. 



|Parameter Name|Description|
|-----|-----|
|height|the height (px) to scale the icon to|
|available|the available width for the icon, or -1 if not yet calculated|

**Returned Value:** the estimated width (px) of the icon  


