> **A message from the idiot that wrote this program:**
> 
> Status has not been abandoned, but is now incompatible with a growing range of
> devices due to a variety of problems detailed in
> [issue #87](https://jfenn.me/redirects/?t=github&d=Status/issues/87).
> Unfortunately, there is nothing that I or any other developer can do to
> prevent this. If the Play Store says that your device is incompatible, then
> there is nothing that I can do to help you other than point you to one of the
> links below about why it will not function.
> 
> https://www.androidpolice.com/2017/04/10/android-o-feature-spotlight-apps-can-no-longer-draw-top-system-ui/
> https://www.xda-developers.com/android-o-is-breaking-apps-that-overlay-on-top-of-the-status-bar/
> https://issuetracker.google.com/issues/36574245
> 
> Status will continue to receive support for devices below Android Oreo for as
> long as it remains in the interest of the community.

## About

Status is a status bar replacement that draws an overlay on top of the
system-generated status bar. This means that the actual status bar is only
hidden under the replacement; touch gestures are not overridden, and the
standard notification panel is still shown. It needs a lot of permissions in
order to obtain the information needed to display in the status bar. These are
listed below.

[![Build Status](https://travis-ci.com/fennifith/Status.svg)](https://travis-ci.com/fennifith/Status)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4f0694753964424b82ccb3544d24df2a)](https://www.codacy.com/app/fennifith/Status?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=fennifith/Status&amp;utm_campaign=Badge_Grade)
[![Discord](https://img.shields.io/discord/514625116706177035.svg?logo=discord&colorB=7289da)](https://discord.gg/ugwZR7V)
[![Liberapay](https://img.shields.io/badge/liberapay-donate-yellow.svg?logo=liberapay)](https://liberapay.com/fennifith/donate)

Special thanks to the contributors that have helped to design the app, fix
ssues, and translate it to different languages:

- [Anas Khan](https://twitter.com/MAKTHG): designed the app icon & helped with UI
- [Vukašin Anđelković](https://dribbble.com/zavukodlak): made a couple status bar icons
- [Ghost Ninja](https://technologx.com/): also made some status bar icons
- [Eugenio Martinez Seguin](https://github.com/Ryo567): Spanish translations
- [Kim Inseop](https://github.com/opnay): Korean translations
- Majida Whale: Chinese translations
- [Marwan ALsidi](https://github.com/Alsidi-Group): Arabic translations

Also, credit to some of Status's other icon sources:

- [Google Design](https://material.io/tools/icons/)
- [materialdesignicons.com](https://materialdesignicons.com/)
- [Icons8](https://icons8.com/icons)

## Installation

The app is available from both Google Play and F-Droid. However, because of
Google Play's policies regarding certain app permissions and battery
optimization, some features may not be included in the Google Play version of
the app.

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
    alt="Get it on Google Play"
    height="80">](https://play.google.com/store/apps/details?id=com.james.status)
[<img src="https://f-droid.org/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="80">](https://f-droid.org/en/packages/com.james.status/)

Alternatively, you can download the latest APK from
[the GitHub releases](../../releases/).

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
- External Storage: backup/restore all settings from a file.

## Contributing

### Issues

Okay, there aren't really any guidelines over issue formatting provided that
you don't create issues that already exist and test the app thoroughly before
creating an issue (ex: try clearing the app data).

### Pull Requests

Contributions are accepted. See [CONTRIBUTING.md](./.github/CONTRIBUTING.md)
for more information.

### Icons

There used to be a list of all the possible icon formats here to use as a
reference, but I stopped updating it and it's pretty useless now. If you want
to make status bar icons to add to this app, either [contact me](mailto:contact@jfenn.me)
or look in the repository for the existing icon files.

## License

Unless otherwise stated, all code in this repository is published under the
Apache 2.0 License as stated below.

Some notable exceptions are:
- Files in [app/src/main/res-google](./app/src/main/res-google) are published under a different license; see the `LICENSE` file included in that directory.
- Files in [app/src/main/res-icons8](./app/src/main/res-icons8) are published under a different license; see the `LICENSE` file included in that directory.
- Files in [app/src/main/res-materialdesignicons](./app/src/main/res-materialdesignicons) are published under a different license; see the `LICENSE` file included in that directory.
- Files contained in the subdirectories of [app/src/main/assets](./app/src/main/assets) are published under their own licenses; see the `LICENSE` file included in their respective directories.

```
Copyright 2019 James Fenn

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
