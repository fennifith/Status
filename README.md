# Status

Status is a status bar replacement that draws an overlay on top of the system-generated status bar. This means that the actual status bar is only hidden under the replacement; touch gestures are not overridden, and the standard notification panel is still shown. It needs a lot of permissions in order to obtain the information needed to display in the status bar. These are listed below.

## Permissions
- Accessibility Service: used to attempt to get the status bar color of the current app if the 'status bar coloring' preference is enabled.
- Notification Access: used to get the icons of the current notifications.
- System Alert Window, System Overlay Window: used to draw the status bar above all other apps.
- Battery Stats: used to find if the battery is charging and what percent it is at.
- Network State, Phone State: gets the type and connection of the phone network, and if airplane mode is enabled.
- Wifi State: finds if wifi is enabled and how good the connection is.
- Bluetooth: find if bluetooth is enabled & connected or not.
- Location Services: finds if GPS is enabled.
- Alarm: find if an alarm is set.

## Contributing
### Issues
Okay, there aren't really any guidelines over issue formatting provided that you don't create issues that already exist, test the app throughly before creating an issue (ex: try clearing the app data), and don't create issues like "It's pointless, just use root". You're not helping.

### Pull Requests
I usually don't have any organization over how I handle issues and what I commit at any given time. If I'm interrupted in the middle of a session, I might commit a half-finished class that causes an error before the project even compiles. To prevent good work going to waste or having to be copied and pasted a lot to prevent merge conflicts, please contact me before you start working on any changes. This way we can decide who will work on the project when, and exactly what changes they will be making.

### Icons
Icon styles can be added to the app quickly and easily provided they meet the specifications below.

#### Battery
- 15 icons total, as follows
- alert icon
- 20%
- 30%
- 50%
- 60%
- 80%
- 90%
- full
- charging 20%
- charging 30%
- charging 50%
- charging 60%
- charging 80%
- charging 90%
- charging full

#### Network
- 5 icons, signal strength 0-4

#### Wifi
- 5 icons, signal strength 0-4

#### Bluetooth
- 2 icons
- on (not paired)
- on (paired)

#### GPS
- 2 icons
- on (not locked)
- on (locked)

#### Airplane Mode
- one icon only

#### NFC
- one icon only

#### Ringer Volume
- 3 icons
- mute
- vibrate
- on

#### Headphones
- 2 icons
- normal headphones
- headset (with mic)

## License

```
Copyright 2016 James Fenn

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
